package twilightforest.gradle;

import net.fabricmc.accesswidener.AccessWidener;
import net.fabricmc.accesswidener.AccessWidenerClassVisitor;
import net.fabricmc.accesswidener.AccessWidenerReader;
import net.fabricmc.tinyremapper.NonClassCopyMode;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Turns the obfuscated MITE R196 client jar into the "named" jar mods compile against:
 * remaps official -> named with the mappings.tiny bundled in FishModLoader, then applies
 * FishModLoader's and this mod's access wideners (FML applies the same wideners at runtime).
 */
public abstract class RemapMiteJarTask extends DefaultTask {
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getObfJar();

    /** FishModLoader jar; mappings.tiny and fishmodloader.accesswidener are read from it. */
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getFmlJar();

    /** Additional (mod) access wideners in the "named" namespace. */
    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    public abstract ConfigurableFileCollection getAccessWideners();

    @OutputFile
    public abstract RegularFileProperty getNamedJar();

    @TaskAction
    public void run() throws IOException {
        Path obf = getObfJar().get().getAsFile().toPath();
        Path out = getNamedJar().get().getAsFile().toPath();
        Path remapped = getTemporaryDir().toPath().resolve("named-no-aw.jar");
        Files.deleteIfExists(remapped);
        Files.createDirectories(out.getParent());

        try (ZipFile fml = new ZipFile(getFmlJar().get().getAsFile())) {
            ZipEntry mappings = fml.getEntry("mappings.tiny");
            if (mappings == null) {
                throw new IllegalStateException("mappings.tiny not found in " + getFmlJar().get().getAsFile());
            }
            // The mapping provider reads lazily, so hand it an in-memory reader that never gets closed early.
            String tiny;
            try (InputStream in = fml.getInputStream(mappings)) {
                tiny = new String(readAll(in), StandardCharsets.UTF_8);
            }
            TinyRemapper remapper = TinyRemapper.newRemapper()
                    .withMappings(TinyUtils.createTinyMappingProvider(new BufferedReader(new StringReader(tiny)), "official", "named"))
                    .ignoreConflicts(true)
                    .checkPackageAccess(true)
                    .build();
            try (OutputConsumerPath consumer = new OutputConsumerPath.Builder(remapped).build()) {
                consumer.addNonClassFiles(obf, NonClassCopyMode.UNCHANGED, remapper);
                remapper.readInputs(obf);
                remapper.apply(consumer);
            } finally {
                remapper.finish();
            }

            AccessWidener widener = new AccessWidener();
            AccessWidenerReader awReader = new AccessWidenerReader(widener);
            ZipEntry fmlAw = fml.getEntry("fishmodloader.accesswidener");
            if (fmlAw != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(fml.getInputStream(fmlAw), StandardCharsets.UTF_8))) {
                    awReader.read(reader, "named");
                }
            }
            for (File file : getAccessWideners().getFiles()) {
                if (!file.isFile()) continue;
                try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                    awReader.read(reader, "named");
                }
            }
            applyAccessWidener(remapped, out, widener);
        }
    }

    private void applyAccessWidener(Path in, Path out, AccessWidener widener) throws IOException {
        int widened = 0;
        try (ZipInputStream zin = new ZipInputStream(Files.newInputStream(in));
             ZipOutputStream zout = new ZipOutputStream(Files.newOutputStream(out))) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                byte[] data = readAll(zin);
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    String className = name.substring(0, name.length() - ".class".length()).replace('/', '.');
                    if (widener.getTargets().contains(className)) {
                        ClassReader reader = new ClassReader(data);
                        ClassWriter writer = new ClassWriter(0);
                        ClassVisitor visitor = AccessWidenerClassVisitor.createClassVisitor(Opcodes.ASM9, writer, widener);
                        reader.accept(visitor, 0);
                        data = writer.toByteArray();
                        widened++;
                    }
                }
                zout.putNextEntry(new ZipEntry(name));
                zout.write(data);
                zout.closeEntry();
            }
        }
        getLogger().lifecycle("Remapped MITE jar to named, access-widened {} classes -> {}", widened, out);
    }

    private static byte[] readAll(InputStream in) throws IOException {
        return in.readAllBytes();
    }
}
