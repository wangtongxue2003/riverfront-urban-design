package examples;

import igeo.IG;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SyntheticCourtyardDemoTest {
    @TempDir Path temporaryOutput;

    @Test
    void exportsAndReopensCourtyardsWithRealComponentsWithoutPrivateData() throws Exception {
        String previous = System.getProperty("demo.output");
        try {
            System.setProperty("demo.output", temporaryOutput.toString());
            SyntheticCourtyardDemo.main(new String[0]);
            Path model = temporaryOutput.resolve("courtyard-demo.3dm");
            assertTrue(Files.size(model) > 1024, "Model must contain geometry");
            String report = Files.readString(temporaryOutput.resolve("run-summary.json"));
            assertTrue(report.contains("demo-06"));
            assertFalse(report.contains("\"door_template_parts\":0"));
            assertFalse(report.contains("\"window_template_parts\":0"));
            IG.clear();
            IG.open(model.toAbsolutePath().toString());
            assertTrue(IG.surfaceNum() > 0, "Export must reopen with surfaces");
            assertTrue(IG.layer("Demo_01_Entrance").geometries().length > 0,
                    "Copied door components must survive export/import");
            assertTrue(IG.layer("Demo_06_TypeB").geometries().length > 0,
                    "Last courtyard must survive export/import");
        } finally {
            IG.clear();
            if (previous == null) System.clearProperty("demo.output");
            else System.setProperty("demo.output", previous);
        }
    }

    @Test
    void rejectsInvalidInputBeforeGeneratingGeometry() {
        assertThrows(IllegalArgumentException.class,
                () -> SyntheticCourtyardDemo.main(new String[]{"NaN", "18", "1"}));
        assertThrows(IllegalArgumentException.class,
                () -> SyntheticCourtyardDemo.main(new String[]{"14", "-18", "1"}));
        assertThrows(IllegalArgumentException.class,
                () -> SyntheticCourtyardDemo.main(new String[]{"14", "18", "3"}));
    }
}
