package Typologies;

import igeo.ICurve;
import igeo.IGeometry;
import org.locationtech.jts.geom.*;
import wblut.geom.WB_Polygon;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import static Spacesyntax.dataTransfer.*;

/** Rectangle-based courtyard rules. Drawing and area evaluation share the same selection. */
public class RecTypeB {
    private final Polygon[] polygons;
    private final double buffer;
    private final IGeometry[][] geometries;
    private final long seed;

    public RecTypeB(ICurve[] curves, double buffer, IGeometry[][] geometries) {
        this.polygons = new Polygon[curves.length];
        for (int i = 0; i < curves.length; i++) polygons[i] = WB_PolygonToPolygon(ICurveToWB_Polygon(curves[i]));
        this.buffer = buffer;
        this.geometries = geometries;
        this.seed = Long.getLong("urban.seed", 42L);
    }

    public RecTypeB(Polygon polygon, double buffer, IGeometry[][] geometries) {
        this(polygon, buffer, geometries, Long.getLong("urban.seed", 42L));
    }

    public RecTypeB(Polygon polygon, double buffer, IGeometry[][] geometries, long seed) {
        this(new Polygon[]{polygon}, buffer, geometries, seed);
    }

    public RecTypeB(Polygon[] polygons, double buffer, IGeometry[][] geometries, long seed) {
        this.polygons = new Polygon[polygons.length];
        for (int i = 0; i < polygons.length; i++) this.polygons[i] = (Polygon) polygons[i].copy();
        this.buffer = buffer;
        this.geometries = geometries;
        this.seed = seed;
    }

    public RecTypeB(WB_Polygon polygon, double buffer, IGeometry[][] geometries) {
        this(WB_PolygonToPolygon(polygon), buffer, geometries);
    }

    public RecTypeB(ICurve curve, double buffer, IGeometry[][] geometries) {
        this(WB_PolygonToPolygon(ICurveToWB_Polygon(curve)), buffer, geometries);
    }

    public void drawRecTypeB(String layer) {
        for (Polygon polygon : polygons) visit(polygon, buffer, layer, null, true, 0);
    }

    public void drawRealRecTypeB(String layer, List<LineString> roads) {
        for (Polygon polygon : polygons) visit(polygon, buffer, layer, roads, true, 0);
    }

    public double getBuildingArea() {
        double area = 0;
        for (Polygon polygon : polygons) area += visit(polygon, buffer, "", null, false, 0);
        return area;
    }

    private double visit(Polygon polygon, double offset, String layer, List<LineString> roads,
                         boolean draw, int depth) {
        if (depth > 32) throw new IllegalArgumentException("Rectangle subdivision exceeded 32 levels");
        Geometry buffered = polygon.buffer(offset);
        if (buffered.isEmpty()) return 0;
        if (!(buffered instanceof Polygon p) || p.getNumInteriorRing() != 0 || p.getExteriorRing().getNumPoints() != 5) {
            throw new IllegalArgumentException("RecTypeB requires a rectangular polygon (and a non-rounding inset)");
        }
        Coordinate[] c = p.getCoordinates();
        for (int i = 0; i < c.length; i++) c[i] = new Coordinate(c[i].x, c[i].y, 0);
        GeometryFactory gf = new GeometryFactory();
        p = gf.createPolygon(c);
        int edge = c[1].distance(c[2]) < c[0].distance(c[1]) ? 1 : 0;
        Site site = new Site(p, gf.createLineString(new Coordinate[]{c[edge], c[edge + 1]}), 0.5, geometries);
        // A stable seed per rectangle means repeat draws and metric evaluation use identical floors.
        int choice = new Random(seed ^ p.norm().toText().hashCode()).nextInt(3);
        float floor = choice == 0 ? 1 : choice == 1 ? 2 : 1.5f;
        double a = site.getSitea(), b = site.getSiteb();
        if (a > 21 || (a > 15 && b > 15 && b <= 21)) {
            double area = 0;
            for (Polygon child : site.get2Site()) {
                if (child.getArea() >= p.getArea() - 1e-8) throw new IllegalStateException("Subdivision did not reduce area");
                area += visit(child, 0, layer, null, draw, depth + 1);
            }
            if (draw && roads != null && !roads.isEmpty()) new Door(site.getRealSite(roads)).drawDoor(layer);
            return area;
        }
        Choice selected = select(site, floor);
        if (selected == null) return 0;
        if (draw) {
            selected.draw.accept(layer);
            if (roads != null && !roads.isEmpty()) new Door(site.getRealSite(roads)).drawDoor(layer);
        }
        return selected.area.getAsDouble();
    }

    private Choice select(Site site, float floor) {
        double a = site.getSitea(), b = site.getSiteb();
        if (a <= 4.5) return null;
        if (a <= 9) {
            site = site.getSiteanother();
            if (site.getSitea() < 3) return null;
            if (site.getSitea() <= 6) return b1(site, floor);
            B10 t = new B10(site, floor); return new Choice(t::drawB10, t::getBuildingArea);
        }
        if (a <= 12) {
            if (b <= 6) return b1(site.getSiteanother(), floor);
            if (b <= 9) { B10XY t = new B10XY(site, floor); return new Choice(t::drawB10XY, t::getBuildingArea); }
            if (b <= 12) return b10xyx(site, floor);
            return null;
        }
        if (a <= 15) {
            site = site.getSiteanother();
            if (site.getSitea() <= 6) return b1(site, floor);
            if (site.getSitea() <= 15) return b10xyx(site, floor);
            return null;
        }
        if (a <= 21) {
            if (b <= 6) { B101 t = new B101(site, floor); return new Choice(t::drawB101, t::getBuildingArea); }
            if (b <= 12) { B10XY1 t = new B10XY1(site, floor); return new Choice(t::drawB10XY1, t::getBuildingArea); }
            if (b <= 15) { B10XYX1 t = new B10XYX1(site, floor); return new Choice(t::drawB10XYX1, t::getBuildingArea); }
        }
        return null;
    }

    private Choice b1(Site site, float floor) {
        B1 t = new B1(site, floor); return new Choice(t::drawB1, t::getBuildingArea);
    }

    private Choice b10xyx(Site site, float floor) {
        B10XYX t = new B10XYX(site, floor); return new Choice(t::drawB10XYX, t::getBuildingArea);
    }

    private record Choice(Consumer<String> draw, DoubleSupplier area) {}
}
