package twilightforest.testbot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.glu.GLU;
import net.minecraft.Minecraft;
import net.minecraft.ScreenShotHelper;
import net.minecraft.server.MinecraftServer;
import org.lwjgl.opengl.GL11;

/**
 * Infrastructure of the dev-only client test bot. The script ({@link TestBotScript}) runs linearly on its own thread and hands work to the
 * game threads: client tick (runTick end), post render (a frame is fully drawn, screenshots + glGetError) and integrated server tick.
 */
public final class TestBot {
    public static final boolean ENABLED = Boolean.getBoolean("twilightforest.testbot");
    public static final String PREFIX = "[TFTestBot] ";

    private static volatile TestBot instance;
    private static final List<String> STARTUP_LOG = new ArrayList<>();

    final Minecraft mc;
    final Report report;
    final File dir;
    final File shots;

    private final Queue<Task<?>> clientTasks = new ConcurrentLinkedQueue<>();
    private final Queue<Task<?>> renderTasks = new ConcurrentLinkedQueue<>();
    private final Queue<Task<?>> serverTasks = new ConcurrentLinkedQueue<>();
    private volatile long frames;
    private volatile long ticks;
    private volatile long serverTicks;
    private volatile boolean aborted;
    private int consecutiveServerTimeouts;
    private Thread thread;

    private TestBot(Minecraft mc) {
        this.mc = mc;
        this.dir = new File(System.getProperty("twilightforest.testbot.dir", "docs/testbot"));
        this.shots = new File(dir, "screenshots");
        this.shots.mkdirs();
        this.report = new Report(dir);
    }

    public static void log(String msg) {
        System.out.println(PREFIX + msg);
    }

    static synchronized void noteStartupLog(String line) {
        if (STARTUP_LOG.size() < 200) {
            STARTUP_LOG.add(line);
        }
    }

    static synchronized List<String> startupLog() {
        return new ArrayList<>(STARTUP_LOG);
    }

    // ------------------------------------------------------------------ hooks (game threads)

    public static void onClientTick(Minecraft mc) {
        if (!ENABLED) {
            return;
        }
        TestBot bot = instance;
        if (bot == null) {
            synchronized (TestBot.class) {
                if (instance == null) {
                    instance = new TestBot(mc);
                    instance.start();
                }
                bot = instance;
            }
        }
        bot.ticks++;
        bot.keepUnfocusedFriendly();
        bot.drain(bot.clientTasks, "client tick");
        bot.glCheck("client tick");
    }

    public static void onPostRender(Minecraft mc) {
        TestBot bot = instance;
        if (bot == null) {
            return;
        }
        bot.frames++;
        bot.drain(bot.renderTasks, "post render");
        bot.glCheck("frame");
    }

    public static void onServerTick(MinecraftServer server) {
        TestBot bot = instance;
        if (bot == null) {
            return;
        }
        bot.serverTicks++;
        bot.drain(bot.serverTasks, "server tick");
    }

    // ------------------------------------------------------------------ server side entity spawn recorder (BotWorldMixin)

    /** "entityId:SimpleClassName" of every entity spawned on the server while a recording is open (projectiles live only a tick or two). */
    private static final List<String> SPAWNED = new CopyOnWriteArrayList<>();
    private static volatile boolean recordingSpawns;

    /** Called by BotWorldMixin at the head of World.spawnEntityInWorld on the server. */
    public static void onServerEntitySpawn(net.minecraft.Entity e) {
        if (recordingSpawns && e != null) {
            SPAWNED.add(e.entityId + ":" + e.getClass().getSimpleName());
        }
    }

    /** Starts a recording window (clears the previous one). */
    public void startSpawnRecording() {
        SPAWNED.clear();
        recordingSpawns = true;
    }

    /** Ends the window and returns the recorded entries "entityId:SimpleClassName". */
    public List<String> stopSpawnRecording() {
        recordingSpawns = false;
        return new ArrayList<>(SPAWNED);
    }

    // ------------------------------------------------------------------ progress heartbeat and hang diagnostics

    private volatile long lastProgressMs = System.currentTimeMillis();
    private volatile String lastStatus = "starting";
    private long lastStatusLogMs;
    private boolean stallDumped;

    /** Marks forward progress (a step began, an item was handled, a screenshot was taken). Cheap, callable from any thread. */
    void progress(String what) {
        lastProgressMs = System.currentTimeMillis();
        lastStatus = what;
        stallDumped = false;
    }

    /**
     * Daemon watchdog: REPORT.md is rewritten at most every 20 s while the bot makes progress (its "Updated" line is the external hang signal), and when
     * nothing happened for 5 minutes the state of every thread is dumped once into the log so a hang can be diagnosed afterwards.
     */
    private void startWatchdog() {
        Thread w = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException e) {
                    return;
                }
                long idle = System.currentTimeMillis() - lastProgressMs;
                if (idle < 60_000) {
                    report.writeThrottled(20_000);
                } else if (idle >= 300_000 && !stallDumped) {
                    stallDumped = true;
                    StringBuilder sb = new StringBuilder();
                    sb.append(PREFIX).append("NO PROGRESS for ").append(idle / 1000).append(" s (last status: ").append(lastStatus).append(") - thread dump:\n");
                    for (java.util.Map.Entry<Thread, StackTraceElement[]> e : Thread.getAllStackTraces().entrySet()) {
                        Thread t = e.getKey();
                        if (t.getName().startsWith("Minecraft main thread") || t.getName().startsWith("Server thread") || t.getName().startsWith("TFTestBot-script")) {
                            sb.append("  \"").append(t.getName()).append("\" ").append(t.getState()).append("\n");
                            int n = 0;
                            for (StackTraceElement el : e.getValue()) {
                                sb.append("      at ").append(el).append("\n");
                                if (++n >= 25) {
                                    break;
                                }
                            }
                        }
                    }
                    System.out.println(sb);
                    report.setFatal("no progress for " + idle / 1000 + " s during '" + lastStatus + "' (thread dump in the client log)");
                    report.write();
                }
            }
        }, "TFTestBot-watchdog");
        w.setDaemon(true);
        w.start();
    }

    private boolean windowPlaced;
    private int mouseGrabWarnings;

    /**
     * The user keeps working on the machine: the game must keep running without window focus (pauseOnLostFocus off, re-asserted every tick
     * because the options screen / options.txt can switch it back), and the window (1280x720 by default, the user wants to watch the run) is
     * centred on the screen once. Nothing here requests focus, grabs the mouse or moves the cursor (see BotMouseHelperMixin / BotMinecraftMixin).
     * Safety net: if anything ever grabs the mouse anyway, it is released again within the same tick and logged loudly.
     */
    private void keepUnfocusedFriendly() {
        mc.gameSettings.pauseOnLostFocus = false;
        if (Mouse.isGrabbed()) {
            Mouse.setGrabbed(false);
            if (mouseGrabWarnings++ < 20) {
                log("WARNING: the mouse was grabbed (no-grab mixins not effective for this path) - released again");
            }
        }
        if (!windowPlaced && ticks > 20) {
            windowPlaced = true;
            try {
                DisplayMode desktop = Display.getDesktopDisplayMode();
                int dw = desktop.getWidth();
                int dh = desktop.getHeight();
                String override = System.getProperty("twilightforest.testbot.screen"); // "WxH" in window coordinates, set by the launcher when known
                if (override != null && override.matches("\\d+x\\d+")) {
                    dw = Integer.parseInt(override.substring(0, override.indexOf('x')));
                    dh = Integer.parseInt(override.substring(override.indexOf('x') + 1));
                }
                int x = Math.max(0, (dw - Display.getWidth()) / 2);
                int y = Math.max(40, (dh - Display.getHeight()) / 2 - 20);
                Display.setLocation(x, y);
                log("window " + Display.getWidth() + "x" + Display.getHeight() + " placed at " + x + "," + y + " (screen " + dw + "x" + dh + ", LWJGL desktop mode "
                    + desktop.getWidth() + "x" + desktop.getHeight() + ", display location now " + Display.getX() + "," + Display.getY() + ")");
            } catch (Throwable t) {
                log("could not position the window: " + t);
            }
        }
    }

    /** Non-blocking window title update (tells the user who is watching which step is running). Safe to call from the script thread. */
    public void status(String text) {
        progress(text);
        long now = System.currentTimeMillis();
        if (now - lastStatusLogMs > 20_000) {
            lastStatusLogMs = now;
            log("progress: " + text);
        }
        report.writeThrottled(20_000);
        String title = "TF TestBot - " + text;
        clientTasks.add(new Task<>(() -> {
            try {
                Display.setTitle(title);
            } catch (Throwable ignored) {
            }
            return null;
        }));
    }

    /** Pacing for the user watching the run: waits the given number of client ticks unless -Dtwilightforest.testbot.nopace=true. */
    public void pace(int tickCount) throws Exception {
        if (!Boolean.getBoolean("twilightforest.testbot.nopace") && tickCount > 0) {
            ticks(tickCount);
        }
    }

    private void drain(Queue<Task<?>> q, String where) {
        Task<?> t;
        while ((t = q.poll()) != null) {
            t.execute();
        }
    }

    private void glCheck(String where) {
        int err = GL11.glGetError();
        if (err != 0) {
            Report.Section s = report.current();
            String what = err + " (" + GLU.gluErrorString(err) + ")";
            if (s != null) {
                s.glError(where, what);
            }
            log("GL error after " + where + ": " + what + (s != null ? " in step " + s.id : ""));
        }
    }

    private void start() {
        thread = new Thread(() -> {
            try {
                new TestBotScript(this).run();
            } catch (Throwable t) {
                t.printStackTrace();
                report.setFatal("script thread died: " + t);
                report.write();
            } finally {
                finishAndExit();
            }
        }, "TFTestBot-script");
        thread.setDaemon(true);
        thread.start();
        startWatchdog();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Report.Section s = report.current();
            if (s != null) {
                report.setFatal("game exited while step '" + s.title + "' was running (crash or manual close)");
            }
            report.write();
        }));
    }

    void finishAndExit() {
        report.write();
        log("finished, writing report to " + new File(dir, "REPORT.md").getAbsolutePath() + " and exiting");
        try {
            clientCall(() -> {
                mc.shutdown();
                return null;
            }, 10_000);
        } catch (Throwable ignored) {
        }
        Thread killer = new Thread(() -> {
            try {
                Thread.sleep(20_000);
            } catch (InterruptedException ignored) {
            }
            Runtime.getRuntime().halt(0);
        }, "TFTestBot-killer");
        killer.setDaemon(true);
        killer.start();
    }

    // ------------------------------------------------------------------ API for the script thread

    private static final class Task<T> {
        final Callable<T> body;
        volatile boolean done;
        T result;
        Throwable error;

        Task(Callable<T> body) {
            this.body = body;
        }

        void execute() {
            try {
                result = body.call();
            } catch (Throwable t) {
                error = t;
            } finally {
                done = true;
            }
        }
    }

    private <T> T await(Queue<Task<?>> q, Callable<T> body, long timeoutMs, String what) throws Exception {
        if (aborted) {
            throw new IllegalStateException("bot aborted");
        }
        Task<T> task = new Task<>(body);
        q.add(task);
        long end = System.currentTimeMillis() + timeoutMs;
        while (!task.done) {
            if (System.currentTimeMillis() > end) {
                task.error = null;
                throw new java.util.concurrent.TimeoutException(what + " task not executed within " + timeoutMs + " ms");
            }
            Thread.sleep(2);
        }
        lastProgressMs = System.currentTimeMillis(); // every completed bot interaction is progress (hang detection only cares about total silence)
        if (task.error != null) {
            if (task.error instanceof Exception e) {
                throw e;
            }
            throw new RuntimeException(task.error);
        }
        return task.result;
    }

    /** Runs on the client thread at the end of runTick. */
    public <T> T clientCall(Callable<T> c, long timeoutMs) throws Exception {
        return await(clientTasks, c, timeoutMs, "client");
    }

    public <T> T client(Callable<T> c) throws Exception {
        return clientCall(c, 60_000);
    }

    public void client(Runnable r) throws Exception {
        clientCall(() -> {
            r.run();
            return null;
        }, 60_000);
    }

    /** Runs on the client thread right after a frame was fully rendered (GL framebuffer holds the finished frame). */
    public <T> T afterFrame(Callable<T> c) throws Exception {
        return await(renderTasks, c, 60_000, "post render");
    }

    /** Runs on the integrated server thread. Three consecutive timeouts abort the run (server thread dead). */
    public <T> T server(Callable<T> c) throws Exception {
        try {
            T r = await(serverTasks, c, 45_000, "server");
            consecutiveServerTimeouts = 0;
            return r;
        } catch (java.util.concurrent.TimeoutException e) {
            if (++consecutiveServerTimeouts >= 3) {
                aborted = true;
                report.setFatal("integrated server stopped responding (crashed?) - run aborted");
            }
            throw e;
        }
    }

    public void server(Runnable r) throws Exception {
        server(() -> {
            r.run();
            return null;
        });
    }

    public boolean isAborted() {
        return aborted;
    }

    public void abort(String why) {
        aborted = true;
        report.setFatal(why);
    }

    public void frames(int n) throws Exception {
        waitCounter(0, n);
    }

    public void ticks(int n) throws Exception {
        waitCounter(1, n);
    }

    public void serverTicks(int n) throws Exception {
        waitCounter(2, n);
    }

    private long counter(int which) {
        return which == 0 ? frames : which == 1 ? ticks : serverTicks;
    }

    private void waitCounter(int which, int n) throws Exception {
        long target = counter(which) + n;
        long end = System.currentTimeMillis() + 60_000L + n * 500L;
        while (counter(which) < target) {
            if (aborted) {
                throw new IllegalStateException("bot aborted");
            }
            if (System.currentTimeMillis() > end) {
                throw new java.util.concurrent.TimeoutException("waited too long for " + n + " " + (which == 0 ? "frames" : which == 1 ? "ticks" : "server ticks"));
            }
            Thread.sleep(2);
        }
    }

    /** Polls a client side condition (on the client thread) once per tick until true or the timeout (in ticks) runs out. */
    public boolean waitFor(Callable<Boolean> cond, int maxTicks) throws Exception {
        for (int i = 0; i < maxTicks; i++) {
            Boolean b = client(cond);
            if (b != null && b) {
                return true;
            }
            ticks(1);
        }
        return false;
    }

    /** Saves the finished frame as PNG (call from the script thread; waits for the next completed frame). */
    public String screenshot(Report.Section s, String relPath) {
        try {
            File out = new File(shots, relPath);
            out.getParentFile().mkdirs();
            String name = out.getName();
            File parent = out.getParentFile();
            String res = afterFrame(() -> {
                // ScreenShotHelper writes into <dir>/screenshots/<name>; give it the parent of a private folder
                File tmpBase = new File(shots, ".tmp");
                tmpBase.mkdirs();
                String r = ScreenShotHelper.func_74292_a(tmpBase, name, mc.displayWidth, mc.displayHeight);
                File written = new File(new File(tmpBase, "screenshots"), name);
                if (written.isFile()) {
                    if (out.exists()) {
                        out.delete();
                    }
                    written.renameTo(out);
                }
                return r;
            });
            if (!out.isFile()) {
                if (s != null) {
                    s.fail("screenshot " + relPath, String.valueOf(res));
                }
                return null;
            }
            if (s != null) {
                s.screenshot(relPath);
            }
            progress("screenshot " + relPath);
            // pacing: keep every captured step on screen for ~1 s (structures 3 s) so the user watching can follow
            if (!Boolean.getBoolean("twilightforest.testbot.nopace")) {
                ticks(relPath.startsWith("structures/") ? 60 : 20);
            }
            return out.getPath();
        } catch (Exception e) {
            if (s != null) {
                s.exception("screenshot " + relPath, e);
            }
            return null;
        }
    }
}
