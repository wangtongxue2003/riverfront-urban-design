package examples;

import Typologies.*;
import igeo.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Standalone, deterministic demonstration using synthetic rectangular plots.
 * Uses the project's existing courtyard classes and the supplied door/window asset.
 * No original site model, result CSV, GUI or installed Rhino is needed to export.
 */
public final class SyntheticCourtyardDemo {
    private static final GeometryFactory GF = new GeometryFactory();

    private record Example(double width, double depth, float floors, double x, double y) { }

    public static void main(String[] args) throws IOException {
        if (args.length == 1 && args[0].equals("--help")) {
            System.out.println("SyntheticCourtyardDemo [width depth floors]\n"
                    + "No arguments: six courtyard examples. Dimensions are metres.\n"
                    + "width: 5..20; depth: 10..30; floors: 1, 1.5 or 2.\n"
                    + "1.5 is the original mixed one/two-storey mode, not a half floor.\n"
                    + "Properties: demo.components, demo.output (defaults to output/demo).");
            return;
        }
        List<Example> cases = new ArrayList<>();
        if (args.length == 0) {
            double[] widths = {7.5, 10.5, 14};
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 3; col++) {
                    cases.add(new Example(widths[col], row == 0 ? 18 : 23,
                            new float[]{1, 1.5f, 2}[col], col * 23, row * 35));
                }
            }
        } else if (args.length == 3) {
            double width = Double.parseDouble(args[0]);
            double depth = Double.parseDouble(args[1]);
            float floors = Float.parseFloat(args[2]);
            if (!Double.isFinite(width) || !Double.isFinite(depth)
                    || width < 5 || width > 20 || depth < 10 || depth > 30
                    || !(floors == 1 || floors == 1.5f || floors == 2)) {
                throw new IllegalArgumentException("Unsupported dimensions/mode. Run with --help.");
            }
            cases.add(new Example(width, depth, floors, 0, 0));
        } else {
            throw new IllegalArgumentException("Expected no arguments or: width depth floors");
        }

        Path componentPath = Path.of(System.getProperty("demo.components",
                "assets/components/door-and-window.3dm"));
        if (!Files.isRegularFile(componentPath)) {
            throw new IOException("Missing component asset: " + componentPath.toAbsolutePath()
                    + ". Run from the repository root, or set -Ddemo.components.");
        }
        Path output = Path.of(System.getProperty("demo.output", "output/demo"));
        Files.createDirectories(output);
        IG.init();
        IG.open(componentPath.toAbsolutePath().toString());
        IGeometry[][] templates = {IG.layer("door").geometries(), IG.layer("window").geometries()};
        if (templates[0].length == 0 || templates[1].length == 0) {
            throw new IllegalStateException("Component file needs non-empty door and window layers.");
        }
        int doorParts = templates[0].length;
        int windowParts = templates[1].length;
        IG.delAll();
        IG.unit(IUnit.Type.Meters);
        List<String> records = new ArrayList<>();
        for (int i = 0; i < cases.size(); i++) {
            Example c = cases.get(i);
            Coordinate[] corners = {
                    new Coordinate(c.x, c.y, 0), new Coordinate(c.x + c.width, c.y, 0),
                    new Coordinate(c.x + c.width, c.y + c.depth, 0),
                    new Coordinate(c.x, c.y + c.depth, 0), new Coordinate(c.x, c.y, 0)};
            Polygon polygon = GF.createPolygon(corners);
            Site site = new Site(polygon, GF.createLineString(new Coordinate[]{corners[0], corners[1]}),
                    0.5, templates);
            String layer = String.format(Locale.ROOT, "Demo_%02d_", i + 1);
            String type = drawCourtyard(site, c.floors, layer);
            new Door(site).drawDoor(layer);
            IVec[] outline = new IVec[corners.length];
            for (int j = 0; j < corners.length; j++) {
                outline[j] = new IVec(corners[j].x, corners[j].y, 0);
            }
            new ICurve(outline).layer(layer + "Plot");
            records.add(String.format(Locale.ROOT,
                    "    {\"solution_id\":\"demo-%02d\",\"type\":\"%s\",\"width_m\":%.2f,"
                            + "\"depth_m\":%.2f,\"floor_mode\":%.1f}",
                    i + 1, type, c.width, c.depth, c.floors));
        }
        int objects = IG.objectNum();
        int surfaces = IG.surfaceNum();
        if (objects == 0 || surfaces == 0) {
            throw new IllegalStateException("Generation produced no surface geometry.");
        }
        Path model = output.resolve("courtyard-demo.3dm");
        // This bundled iGeo build reliably round-trips the Rhino 4 format.
        if (!IG.saveRhino4(model.toAbsolutePath().toString()) || Files.size(model) < 1024) {
            throw new IOException("Rhino export failed: " + model);
        }
        String report = "{\n  \"input\":\"synthetic rectangles\",\n  \"units\":\"metres\",\n"
                + "  \"door_template_parts\":" + doorParts + ",\n"
                + "  \"window_template_parts\":" + windowParts + ",\n"
                + "  \"objects\":" + objects + ",\n  \"surfaces\":" + surfaces + ",\n"
                + "  \"examples\":[\n" + String.join(",\n", records) + "\n  ]\n}\n";
        Files.writeString(output.resolve("run-summary.json"), report, StandardCharsets.UTF_8);
        System.out.println("Generated " + cases.size() + " courtyard(s), " + objects
                + " objects and " + surfaces + " surfaces: " + model.toAbsolutePath());
    }

    /** Same width/depth thresholds as TypeB; no site-specific Voronoi is needed here. */
    private static String drawCourtyard(Site site, float floors, String layer) {
        double width = site.getSiteb();
        double depth = site.getSitea();
        if (width <= 9) {
            if (depth <= 20) { new B101(site, floors).drawB101(layer); return "B101"; }
            if (depth <= 24) { new B0101(site, floors).drawB0101(layer); return "B0101"; }
            new B01010(site, floors).drawB01010(layer); return "B01010";
        }
        if (width <= 12) {
            if (depth <= 20) { new B10XY1(site, floors).drawB10XY1(layer); return "B10XY1"; }
            new B010XY1(site, floors).drawB010XY1(layer); return "B010XY1";
        }
        if (depth <= 20) { new B10XYX1(site, floors).drawB10XYX1(layer); return "B10XYX1"; }
        new B10XY10(site, floors).drawB10XY10(layer); return "B10XY10";
    }
}
