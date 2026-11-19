package basic;

import igeo.IG;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

/** Validates local-only inputs and creates output directories for the historical examples. */
public final class ProjectPaths {
    private ProjectPaths() {}

    public static void open(String name) {
        Path path = Path.of(name);
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Missing input: " + path.toAbsolutePath()
                    + ". Original site data is intentionally excluded. See README, or run examples.SyntheticCourtyardDemo.");
        }
        IG.open(path.toString());
    }

    public static String prepared(String name) {
        Path generated = Path.of("output", "prepared", name);
        return Files.isRegularFile(generated) ? generated.toString() : Path.of("private-data", name).toString();
    }

    public static String outputPath(String name) {
        Path path = Path.of(name);
        try {
            if (path.getParent() != null) Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create output directory: " + path, e);
        }
        return path.toString();
    }

    public static void save(String name) {
        String path = outputPath(name);
        // The bundled iGeo reliably round-trips Rhino 4; its default v5 export may lose geometry on import.
        boolean saved = name.toLowerCase(java.util.Locale.ROOT).endsWith(".3dm")
                ? IG.saveRhino4(path) : IG.save(path);
        if (!saved) throw new IllegalStateException("Geometry export failed: " + path);
    }
}
