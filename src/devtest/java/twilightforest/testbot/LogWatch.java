package twilightforest.testbot;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.regex.Pattern;

/**
 * Watches everything the game prints (System.out/err tee, JUL console handler writes to System.err, log4j appender) and attributes
 * suspicious lines to the step that is running: "Missing resource", missing texture, GL ERROR, exceptions, ERROR level lines.
 */
public final class LogWatch {
    private static final Pattern FAILING = Pattern.compile("Missing resource|GL ERROR|issing texture|Using missing|FileNotFoundException|(^|\\s|\\.)\\w*(Exception|Error)\\b.*");
    private static final Pattern SUSPICIOUS = Pattern.compile("\\bERROR\\b|\\bSEVERE\\b|Unable to|Failed to|Could not|Couldn't|Cannot |missingno|Exception");
    private static volatile Report.Section current;
    private static volatile boolean installed;
    private static final ThreadLocal<Boolean> BUSY = new ThreadLocal<>();
    private static final ThreadLocal<Noise> NOISE = ThreadLocal.withInitial(Noise::new);

    private LogWatch() {
    }

    static void setCurrent(Report.Section s) {
        current = s;
    }

    // ------------------------------------------------------------------ known vanilla-MITE noise

    /**
     * Log lines that are not caused by the mod and would bury real findings (each one is counted in the report as "ignored", never dropped silently):
     * <ul>
     * <li>MITE's ChunkProviderFlat calls Chunk.generateSkylightMap(true) twice per chunk (lines 92 and 104 of ChunkProviderFlat), so the flat arena
     * world of the bot prints "generateSkylightMapMITE: already called" / "generateHeightMapMITE: initial heightmap has already been generated" for
     * every chunk of dimension 0. The same messages for any other dimension (the Twilight Forest!) stay findings.</li>
     * <li>the bare {@code java.lang.Exception} header + stack that Debug.printStackTrace prints right after such a message: ignored only while the
     * stack holds no {@code twilightforest.} frame. A twilightforest frame turns the whole group into a real (failing) finding.</li>
     * <li>MapStorage.setUniqueDataId dev mode reminder "prefix of magicmap/mazemap used, did you want to propagate it to client?": TF ships its own map
     * packet (TFPacket / NetClientHandlerMapMixin), the MITE hint only knows its own "map" prefix.</li>
     * </ul>
     */
    private static String knownNoiseKind(String line) {
        if (line.contains("dimension 0 on server")) {
            if (line.contains("generateSkylightMapMITE: already called for Chunk")) {
                return "MITE ChunkProviderFlat: generateSkylightMapMITE already called (dimension 0, vanilla flat provider calls it twice)";
            }
            if (line.contains("generateHeightMapMITE: initial heightmap has already been generated")) {
                return "MITE ChunkProviderFlat: generateHeightMapMITE initial heightmap already generated (dimension 0, same cause)";
            }
        }
        if (line.contains("setUniqueDataId: prefix of \"magicmap\" used") || line.contains("setUniqueDataId: prefix of \"mazemap\" used")) {
            return "MITE dev mode hint setUniqueDataId prefix magicmap/mazemap (TF syncs its maps with its own packet)";
        }
        return null;
    }

    /** Per thread: Minecraft.setErrorMessage and the following Debug.printStackTrace run on one thread, so the association is kept per thread. */
    private static final class Noise {
        /** a known noise message was the last thing this thread logged: a bare java.lang.Exception right after it belongs to it */
        boolean armed;
        /** inside the stack of such a bare exception */
        boolean inStack;
        boolean tfFrameReported;
    }

    private static boolean isStackLine(String line) {
        return line.startsWith("\tat ") || line.startsWith("        at ") || line.startsWith("at ");
    }

    /** @return true when the line was consumed as known noise (or as part of its stack) and must not be looked at any further */
    private static boolean consumeKnownNoise(String line) {
        Noise n = NOISE.get();
        String kind = knownNoiseKind(line);
        if (kind != null) {
            n.armed = true;
            n.inStack = false;
            Report.Section s = current;
            if (s != null) {
                s.ignoredNoise(kind);
            }
            return true;
        }
        if (n.armed && (line.equals("java.lang.Exception") || line.equals("EXCEPTION java.lang.Exception"))) {
            n.inStack = true;
            n.tfFrameReported = false;
            return true;
        }
        if (n.inStack && isStackLine(line)) {
            if (!n.tfFrameReported && line.contains("twilightforest.")) {
                // not vanilla noise after all: a twilightforest frame is on the stack, report it with that frame
                n.tfFrameReported = true;
                record("java.lang.Exception raised under a known MITE message but with a twilightforest frame: " + line.trim());
            }
            return true;
        }
        // any other line ends the group
        n.inStack = false;
        if (!isStackLine(line)) {
            n.armed = false;
        }
        return false;
    }

    static boolean isFailing(String line) {
        return FAILING.matcher(line).find();
    }

    public static void install() {
        if (installed) {
            return;
        }
        installed = true;
        System.setOut(new PrintStream(new Tee(System.out), true));
        System.setErr(new PrintStream(new Tee(System.err), true));
        try {
            installLog4j();
        } catch (Throwable t) {
            System.err.println("[TFTestBot] log4j appender not installed: " + t);
        }
    }

    private static void installLog4j() {
        org.apache.logging.log4j.core.LoggerContext ctx = (org.apache.logging.log4j.core.LoggerContext) org.apache.logging.log4j.LogManager.getContext(false);
        org.apache.logging.log4j.core.config.Configuration cfg = ctx.getConfiguration();
        org.apache.logging.log4j.core.appender.AbstractAppender app = new org.apache.logging.log4j.core.appender.AbstractAppender(
            "TFTestBotWatch", null, null, true, org.apache.logging.log4j.core.config.Property.EMPTY_ARRAY) {
            @Override
            public void append(org.apache.logging.log4j.core.LogEvent event) {
                String msg = event.getMessage().getFormattedMessage();
                String lvl = event.getLevel().name();
                handle(lvl + " " + msg);
                Throwable t = event.getThrown();
                if (t != null) {
                    handle("EXCEPTION " + t);
                }
            }
        };
        app.start();
        cfg.getRootLogger().addAppender(app, null, null);
        ctx.updateLoggers();
    }

    static void handle(String line) {
        if (line == null || line.isEmpty() || line.contains("[TFTestBot]")) {
            return;
        }
        if (Boolean.TRUE.equals(BUSY.get())) {
            return;
        }
        BUSY.set(Boolean.TRUE);
        try {
            if (consumeKnownNoise(line)) {
                return;
            }
            if (isStackLine(line)) {
                return;
            }
            if (FAILING.matcher(line).find() || SUSPICIOUS.matcher(line).find()) {
                record(line);
            }
        } finally {
            BUSY.set(null);
        }
    }

    private static void record(String line) {
        String text = line.length() > 400 ? line.substring(0, 400) + "..." : line;
        Report.Section s = current;
        if (s != null) {
            s.logHit(text);
        } else {
            TestBot.noteStartupLog(text);
        }
    }

    private static final class Tee extends OutputStream {
        private final PrintStream target;
        private final ByteArrayOutputStream line = new ByteArrayOutputStream();

        Tee(PrintStream target) {
            this.target = target;
        }

        @Override
        public synchronized void write(int b) {
            target.write(b);
            if (b == '\n') {
                flushLine();
            } else if (b != '\r') {
                line.write(b);
            }
        }

        @Override
        public synchronized void write(byte[] b, int off, int len) {
            target.write(b, off, len);
            for (int i = off; i < off + len; i++) {
                if (b[i] == '\n') {
                    flushLine();
                } else if (b[i] != '\r') {
                    line.write(b[i]);
                }
            }
        }

        @Override
        public void flush() {
            target.flush();
        }

        private void flushLine() {
            String s = new String(line.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
            line.reset();
            handle(s);
        }
    }
}
