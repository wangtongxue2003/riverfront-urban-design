package regression;

import NSGAII.Plot;
import Spacesyntax.Graph;
import Spacesyntax.Spacesyntax_00;
import Typologies.B101;
import Typologies.RecTypeB;
import Typologies.Site;
import basic.Vec;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import java.lang.reflect.Method;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CoreRegressionTest {
    private final GeometryFactory gf = new GeometryFactory();

    private LineString line(double x1, double y1, double x2, double y2) {
        return gf.createLineString(new Coordinate[]{new Coordinate(x1, y1), new Coordinate(x2, y2)});
    }
    private Polygon rectangle(double x, double y, double width, double depth) {
        return gf.createPolygon(new Coordinate[]{new Coordinate(x, y), new Coordinate(x + width, y),
                new Coordinate(x + width, y + depth), new Coordinate(x, y + depth), new Coordinate(x, y)});
    }

    @Test void crossingLinesBecomeConnectedAtTheirIntersection() {
        var network = List.of(line(-5, 0, 5, 0), line(0, -5, 0, 5));
        var graph = Spacesyntax_00.buildGraphWithIntersections(network);
        assertEquals(10, Spacesyntax_00.findShortestPath(graph, new Coordinate(-5, 0), new Coordinate(0, 5)), 1e-8);
        assertEquals(10, new Graph(network).findShortestPath(new Coordinate(-5, 0), new Coordinate(0, 5)), 1e-8);
    }

    @Test void tJunctionAndDisconnectedQueriesAreHandled() {
        var graph = Spacesyntax_00.buildGraphWithIntersections(List.of(line(0, 0, 10, 0), line(8, 0, 8, 3)));
        assertEquals(11, Spacesyntax_00.findShortestPath(graph, new Coordinate(0, 0), new Coordinate(8, 3)), 1e-8);
        assertEquals(Double.MAX_VALUE, Spacesyntax_00.findShortestPath(graph, new Coordinate(0, 0), new Coordinate(99, 99)));
        assertTrue(Spacesyntax_00.getNodedLineStrings(List.of()).isEmpty());
    }

    @Test void insertingPointsDoesNotFoldBackAlongTheRoad() {
        var cut = Spacesyntax_00.cutLineStringsByPoints(List.of(line(0, 0, 10, 0)),
                new Coordinate[]{new Coordinate(8, 0), new Coordinate(2, 0)});
        assertEquals(10, cut.stream().mapToDouble(LineString::getLength).sum(), 1e-8);
        assertEquals(6, Spacesyntax_00.findShortestPath(cut, new Coordinate(2, 0), new Coordinate(8, 0)), 1e-8);
    }

    @Test void diagonalRidgeProducesAPerpendicularCut() throws Exception {
        Plot plot = new Plot(rectangle(-5, -5, 10, 10), List.of(line(-5, -6, 5, -6)));
        Method method = Plot.class.getDeclaredMethod("createPerpendicularLine", Coordinate.class, LineSegment.class);
        method.setAccessible(true);
        for (Coordinate end : List.of(new Coordinate(10, 10), new Coordinate(-10, 10), new Coordinate(10, -10))) {
            LineString cut = (LineString) method.invoke(plot, new Coordinate(0, 0), new LineSegment(new Coordinate(0, 0), end));
            Coordinate a = cut.getCoordinateN(0), b = cut.getCoordinateN(1);
            assertEquals(0, (b.x - a.x) * end.x + (b.y - a.y) * end.y, 1e-8);
        }
    }

    @Test void b101AreaMatchesTwoThirtyFivePercentBuildingFootprints() {
        Polygon p = rectangle(0, 0, 6, 18);
        Site site = new Site(p, line(0, 0, 6, 0), 0.5, null);
        assertEquals(6 * 18 * 0.7 * 1.5, new B101(site, 1.5f).getBuildingArea(), 1e-8);
    }

    @Test void rectangleAreasAccumulateAndDoNotResampleFloors() {
        Polygon first = rectangle(0, 0, 5, 8), second = rectangle(20, 0, 5, 8);
        long seed = 42;
        RecTypeB combined = new RecTypeB(new Polygon[]{first, second}, 0, null, seed);
        double expected = new RecTypeB(first, 0, null, seed).getBuildingArea()
                + new RecTypeB(second, 0, null, seed).getBuildingArea();
        assertTrue(expected > 0);
        assertEquals(expected, combined.getBuildingArea(), 1e-8);
        assertEquals(expected, combined.getBuildingArea(), 1e-8);
        RecTypeB subdivided = new RecTypeB(rectangle(0, 0, 16, 40), 0, null, seed);
        double total = subdivided.getBuildingArea();
        assertTrue(total > 0);
        assertEquals(total, subdivided.getBuildingArea(), 1e-8);
    }

    @Test void vectorSubtractionDoesNotMutateItsInput() {
        Vec original = new Vec(5f, 7f, 9f);
        Vec result = original.subNew(1f, 2f, 3f);
        assertEquals(4f, result.x); assertEquals(5f, result.y); assertEquals(6f, result.z);
        assertEquals(5f, original.x); assertEquals(7f, original.y); assertEquals(9f, original.z);
    }
    @Test void drawingSingleFloorTypesDoesNotChangeTheirArea() {
        igeo.IG.init();
        var templates = new igeo.IGeometry[][]{new igeo.IGeometry[0], new igeo.IGeometry[0]};
        Site site = new Site(rectangle(0, 0, 8, 12), line(0, 0, 8, 0), 0.5, templates);
        var one = new Typologies.B1(site, 1.5f);
        var courtyard = new Typologies.B10(site, 1.5f);
        var wing = new Typologies.B10XY(site, 1.5f);
        double areaOne = one.getBuildingArea(), areaCourt = courtyard.getBuildingArea(), areaWing = wing.getBuildingArea();
        one.drawB1("one"); courtyard.drawB10("court"); wing.drawB10XY("wing");
        assertEquals(8 * 12, areaOne, 1e-8);
        assertEquals(areaOne, one.getBuildingArea(), 1e-8);
        assertEquals(areaCourt, courtyard.getBuildingArea(), 1e-8);
        assertEquals(areaWing, wing.getBuildingArea(), 1e-8);
        assertTrue(igeo.IG.surfaceNum() > 0);
        for (igeo.ISurface surface : igeo.IG.surfaces()) assertTrue(surface.isValid());
        igeo.IG.delAll();
    }
}
