package Zshare.Skeleton;


import org.locationtech.jts.geom.*;
import org.twak.camp.*;
import org.twak.utils.collections.Loop;
import org.twak.utils.collections.LoopL;
import wblut.geom.WB_Coord;
import wblut.geom.WB_GeometryFactory;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.hemesh.HEC_FromPolygons;
import wblut.hemesh.HE_Mesh;
import wblut.hemesh.HE_Vertex;

import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * compute straight skeleton using campskeleton by twak
 * and extract some geometries, support 2D polygon with holes
 * polygon shell must be counterclockwise
 * polygon should be valid (first point coincides with last point)
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/30
 * @time 11:12
 */
public class ZSkeleton_share {
    public final GeometryFactory jtsgf = new GeometryFactory();
    public final WB_GeometryFactory wbgf = new WB_GeometryFactory();

    // input
    private Polygon polygon;
    private double capHeight = 0;
    private double offsetDist = 0;
    private double generalMachine = Math.PI / 3;

    private Skeleton skeleton;

    // output
    private List<LineString> allEdges;
    private List<LineString> topEdges;
    private List<LineString> sideEdges;
    private List<LineString> bottomEdges;

    private List<LineString> ridges;
    private List<LineString> extendedRidges;
    private List<Coordinate> ridgePoints;

    private List<Polygon> allFacePolys;
    private HE_Mesh skeletonMesh;

    /* ------------- constructors ------------- */

    public ZSkeleton_share() {

    }

    public ZSkeleton_share(Polygon polygon) {
        // input polygon needs to be face up
        this.polygon = (Polygon) polygon.norm().reverse();

        initSkeleton();
        // extract bottom, side, top edges and ridges, ridgePoint
        extractEdges2D();
    }

    public ZSkeleton_share(Polygon polygon, double offsetDist, boolean if3d) {
        // input polygon needs to be face up (norm)
        this.polygon = (Polygon) polygon.norm().reverse();

        setOffsetDist(offsetDist);
        initSkeleton();
        // extract bottom, side, top edges and ridges, ridgePoint
        if (if3d) {
            extractEdges3D();
        } else {
            extractEdges2D();
        }
    }

    public ZSkeleton_share(WB_Polygon polygon) {
        // input polygon needs to be face up (norm)
        WB_Polygon validate = polygonFaceUp(validateWB_Polygon(polygon));
        this.polygon = WB_PolygonToPolygon(validate);

        initSkeleton();
        // extract bottom, side, top edges and ridges, ridgePoint
        extractEdges2D();
    }

    public ZSkeleton_share(WB_Polygon polygon, double offsetDist, boolean if3d) {
        // input polygon needs to be face up (norm)
        WB_Polygon validate = polygonFaceUp(validateWB_Polygon(polygon));
        this.polygon = WB_PolygonToPolygon(validate);

        setOffsetDist(offsetDist);
        initSkeleton();

        // extract bottom, side, top edges and ridges, ridgePoint
        if (if3d) {
            extractEdges3D();
        } else {
            extractEdges2D();
        }
    }

    /* ------------- set & get ------------- */

    public void setOffsetDist(double offsetDist) {
        this.offsetDist = offsetDist;
    }

    public void setCapHeight(double capHeight) {
        this.capHeight = capHeight;
    }

    public void setGeneralMachine(double generalMachine) {
        this.generalMachine = generalMachine;
    }

    public Skeleton getSkeleton() {
        return skeleton;
    }

    public List<LineString> getAllEdges() {
        return this.allEdges;
    }

    public List<LineString> getTopEdges() {
        return this.topEdges;
    }

    public List<LineString> getSideEdges() {
        return this.sideEdges;
    }

    public List<LineString> getBottomEdges() {
        return this.bottomEdges;
    }

    public List<LineString> getRidges() {
        return this.ridges;
    }

    public List<LineString> getExtendedRidges() {
        return this.extendedRidges;
    }

    public List<Coordinate> getRidgePoints() {
        return this.ridgePoints;
    }

    public List<Polygon> getAllFacePolys() {
        return allFacePolys;
    }

    public List<WB_Polygon> getAllFacePolysWB() {
        List<WB_Polygon> result = new ArrayList<>();
        for (Polygon p : allFacePolys) {
            result.add(PolygonToWB_Polygon(p));
        }
        return result;
    }

    public HE_Mesh getSkeletonMesh() {
        return skeletonMesh;
    }

    /* ------------- initialize ------------- */

    /**
     * initialize straight skeleton
     *
     * @param
     * @return void
     */
    public void initSkeleton() {
        this.allEdges = new ArrayList<>();
        this.topEdges = new ArrayList<>();
        this.sideEdges = new ArrayList<>();
        this.bottomEdges = new ArrayList<>();

        this.ridgePoints = new ArrayList<>();
        this.ridges = new ArrayList<>();
        this.extendedRidges = new ArrayList<>();

        // set angles
        Machine speed = new Machine(generalMachine);

        if (this.polygon.getNumInteriorRing() > 0) {
            LoopL<Edge> loopL = new LoopL<>();

            // exterior
            LineString exterior = polygon.getExteriorRing();
            List<Corner> corners_E = new ArrayList<>();
            for (int i = 0; i < exterior.getNumPoints(); i++) {
                corners_E.add(new Corner(exterior.getCoordinateN(i).x, exterior.getCoordinateN(i).y));
            }
            Loop<Edge> loop_E = new Loop<>();
            for (int j = 0; j < corners_E.size() - 1; j++) {
                Edge edge = new Edge(corners_E.get(j), corners_E.get((j + 1) % (corners_E.size() - 1)));
                edge.machine = speed;
                loop_E.append(edge);
            }
            loopL.add(loop_E);

            // interior holes should be clockwise
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                LineString interior = polygon.getInteriorRingN(i);
                List<Corner> corners_I = new ArrayList<>();
                for (int j = 0; j < interior.getNumPoints(); j++) {
                    corners_I.add(new Corner(interior.getCoordinateN(j).x, interior.getCoordinateN(j).y));
                }
                Loop<Edge> loop_I = new Loop<>();
                for (int j = 0; j < corners_I.size() - 1; j++) {
                    Edge edge = new Edge(corners_I.get(j), corners_I.get((j + 1) % (corners_I.size() - 1)));
                    edge.machine = speed;
                    loop_I.append(edge);
                }
                loopL.add(loop_I);
            }

            // add cap or not
            if (this.offsetDist == 0) {
                this.skeleton = new Skeleton(loopL, true);
            } else {
                if (this.generalMachine == Math.PI / 4) {
                    this.skeleton = new Skeleton(loopL, offsetDist);
                } else {
                    this.skeleton = new Skeleton(loopL, offsetDist * Math.tan(generalMachine));
                }
            }
        } else {
            // add corners
            List<Corner> corners = new ArrayList<>();
            for (int i = 0; i < polygon.getCoordinates().length; i++) {
                corners.add(new Corner(polygon.getCoordinates()[i].x, polygon.getCoordinates()[i].y));
            }
            // create loop
            Loop<Edge> loop = new Loop<>();
            for (int j = 0; j < corners.size() - 1; j++) {
                Edge edge = new Edge(corners.get(j), corners.get((j + 1) % (corners.size() - 1)));
                edge.machine = speed;
                loop.append(edge);
            }
            // add cap or not
            if (this.offsetDist == 0) {
                this.skeleton = new Skeleton(loop.singleton(), true);
            } else {
                if (this.generalMachine == Math.PI / 4) {
                    this.skeleton = new Skeleton(loop.singleton(), offsetDist);
                } else {
                    this.skeleton = new Skeleton(loop.singleton(), offsetDist * Math.tan(generalMachine));
                }
            }
        }
        skeleton.skeleton();
    }

    /**
     * extract edges in 3D mode
     *
     * @param
     * @return void
     */
    private void extractEdges3D() {
        // extract all edges
        for (Output.SharedEdge se : skeleton.output.edges.map.values()) {
            allEdges.add(jtsgf.createLineString(
                    new Coordinate[]{
                            new Coordinate(se.start.getX(), se.start.getY(), se.start.getZ()), new Coordinate(se.end.getX(), se.end.getY(), se.end.getZ())
                    }
            ));
        }

        // convert skeleton faces to HE_Mesh, find ridges from mesh
        this.allFacePolys = new ArrayList<>();
        Set<Output.Face> nonRepeatFace = new HashSet<>();
        for (Output.Face face : skeleton.output.faces.values()) {
            if (!nonRepeatFace.contains(face) && face.points.size() == 1) {
                List<Coordinate> polyCoords = new ArrayList<>();
                for (Loop<Point3d> loop : face.points) {
                    for (Point3d p : loop) {
                        polyCoords.add(new Coordinate(p.getX(), p.getY(), p.getZ()));
                    }
                }
                polyCoords.add(polyCoords.get(0));
                allFacePolys.add(createPolygonFromList(polyCoords));
            }
            nonRepeatFace.add(face);
        }
        findRidgesFromMesh(allFacePolys);

        // find top, side, bottom edges
        Set<Output.SharedEdge> nonRepeatShared = new HashSet<>();
        for (Output.Face face : skeleton.output.faces.values()) {
            for (Loop<Output.SharedEdge> egdeLoop : face.edges) {
                for (Output.SharedEdge edge : egdeLoop) {
                    Coordinate start = new Coordinate(edge.start.getX(), edge.start.getY(), edge.start.getZ());
                    Coordinate end = new Coordinate(edge.end.getX(), edge.end.getY(), edge.start.getZ());
                    if (face.isSide(edge) && !nonRepeatShared.contains(edge)) {
                        sideEdges.add(jtsgf.createLineString(new Coordinate[]{start, end}));
                    } else if (face.isBottom(edge) && !nonRepeatShared.contains(edge)) {
                        bottomEdges.add(jtsgf.createLineString(new Coordinate[]{start, end}));
                    } else if (face.isTop(edge) && !nonRepeatShared.contains(edge)) {
                        topEdges.add(jtsgf.createLineString(new Coordinate[]{start, end}));
                    }
                    nonRepeatShared.add(edge);
                }
            }
        }
        topEdges.addAll(ridges);
    }

    /**
     * extract edges in 2D mode
     *
     * @param
     * @return void
     */
    private void extractEdges2D() {
        // extract all edges
        for (Output.SharedEdge se : skeleton.output.edges.map.values()) {
            allEdges.add(jtsgf.createLineString(
                    new Coordinate[]{
                            new Coordinate(se.start.getX(), se.start.getY()), new Coordinate(se.end.getX(), se.end.getY())
                    }
            ));
        }

        // convert skeleton faces to HE_Mesh, find ridges from mesh
        this.allFacePolys = new ArrayList<>();
        Set<Output.Face> nonRepeatFace = new HashSet<>();
        for (Output.Face face : skeleton.output.faces.values()) {
            if (!nonRepeatFace.contains(face) && face.points.size() == 1) {
                List<Coordinate> polyCoords = new ArrayList<>();
                for (Loop<Point3d> loop : face.points) {
                    for (Point3d p : loop) {
                        polyCoords.add(new Coordinate(p.getX(), p.getY()));
                    }
                }
                polyCoords.add(polyCoords.get(0));
                allFacePolys.add(createPolygonFromList(polyCoords));
            }
            nonRepeatFace.add(face);
        }
        findRidgesFromMesh(allFacePolys);

        // find top, side, bottom edges
        Set<Output.SharedEdge> nonRepeatShared = new HashSet<>();
        for (Output.Face face : skeleton.output.faces.values()) {
            for (Loop<Output.SharedEdge> egdeLoop : face.edges) {
                for (Output.SharedEdge edge : egdeLoop) {
                    Coordinate start = new Coordinate(edge.start.getX(), edge.start.getY());
                    Coordinate end = new Coordinate(edge.end.getX(), edge.end.getY());
                    if (face.isSide(edge) && !nonRepeatShared.contains(edge)) {
                        sideEdges.add(jtsgf.createLineString(new Coordinate[]{start, end}));
                    } else if (face.isBottom(edge) && !nonRepeatShared.contains(edge)) {
                        bottomEdges.add(jtsgf.createLineString(new Coordinate[]{start, end}));
                    } else if (face.isTop(edge) && !nonRepeatShared.contains(edge)) {
                        topEdges.add(jtsgf.createLineString(new Coordinate[]{start, end}));
                    }
                    nonRepeatShared.add(edge);
                }
            }
        }
        topEdges.addAll(ridges);
    }

    /**
     * extract ridges by converting faces to mesh
     *
     * @param faces all polygons to create mesh
     * @return void
     */
    private void findRidgesFromMesh(List<Polygon> faces) {
        List<WB_Polygon> facesTemp = new ArrayList<>();
        for (Polygon f : faces) {
            facesTemp.add(PolygonToWB_Polygon(f));
        }
        this.skeletonMesh = new HEC_FromPolygons(facesTemp).create();

        // find ridge in skeleton
        List<HE_Vertex> curr_vertices = new ArrayList<>();
        for (int i = 0; i < skeletonMesh.getNumberOfVertices(); i++) {
            HE_Vertex v = skeletonMesh.getVertexWithIndex(i);
            if (!v.isBoundary()) {
                curr_vertices.add(v);
                ridgePoints.add(new Coordinate(v.xd(), v.yd(), v.zd()));

                List<HE_Vertex> verticesFromRidgeEnd = new ArrayList<>();
                for (HE_Vertex vertex : v.getNeighborVertices()) {
                    if (!vertex.isBoundary() && !curr_vertices.contains(vertex)) {
                        ridges.add(jtsgf.createLineString(new Coordinate[]{
                                WB_CoordToCoordinate(v),
                                WB_CoordToCoordinate(vertex)
                        }));
                    }
                    if (vertex.isBoundary()) {
                        verticesFromRidgeEnd.add(vertex);
                    }
                }

                if (verticesFromRidgeEnd.size() == 2 && v.getNeighborVertices().size() == 3) {
                    HE_Vertex v0 = verticesFromRidgeEnd.get(0);
                    HE_Vertex v1 = verticesFromRidgeEnd.get(1);

                    Coordinate center = new Coordinate((v0.xd() + v1.xd()) * 0.5, (v0.yd() + v1.yd()) * 0.5, (v0.zd() + v1.zd()) * 0.5);
                    extendedRidges.add(jtsgf.createLineString(new Coordinate[]{WB_CoordToCoordinate(v), center}));
                }
            }
        }
    }

    /*-------- print & draw --------*/

    @Override
    public String toString() {
        return "ZSkeleton{" +
                "polygon=" + polygon +
                ", capHeight=" + capHeight +
                ", offsetDist=" + offsetDist +
                ", generalMachine=" + generalMachine +
                ", allEdges=" + allEdges.size() +
                ", topEdges=" + topEdges.size() +
                ", sideEdges=" + sideEdges.size() +
                ", bottomEdges=" + bottomEdges.size() +
                ", ridges=" + ridges.size() +
                ", extendedRidges=" + extendedRidges.size() +
                '}';
    }

    /**
     * create a Polygon from a Coordinate list
     *
     * @param list Coordinate list
     * @return org.locationtech.jts.geom.Polygon
     */
    private Polygon createPolygonFromList(final List<Coordinate> list) {
        Coordinate[] array = new Coordinate[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return jtsgf.createPolygon(array);
    }


    /**
     * make a polygon face up (normal vector is in the z direction) (holes supported)
     *
     * @param polygon input polygon
     * @return wblut.geom.WB_Polygon
     */
    private WB_Polygon polygonFaceUp(final WB_Polygon polygon) {
        if (polygon.getNormal().zd() < 0) {
            return reversePolygon(polygon);
        } else {
            return polygon;
        }
    }

    /**
     * reverse the order of a polygon (holes supported)
     *
     * @param original input polygon
     * @return wblut.geom.WB_Polygon
     */
    private WB_Polygon reversePolygon(final WB_Polygon original) {
        if (original.getNumberOfHoles() == 0) {
            WB_Point[] newPoints = new WB_Point[original.getNumberOfPoints()];
            for (int i = 0; i < newPoints.length; i++) {
                newPoints[i] = original.getPoint(newPoints.length - 1 - i);
            }
            return new WB_Polygon(newPoints);
        } else {
            WB_Point[] newExteriorPoints = new WB_Point[original.getNumberOfShellPoints()];
            for (int i = 0; i < original.getNumberOfShellPoints(); i++) {
                newExteriorPoints[i] = original.getPoint(original.getNumberOfShellPoints() - 1 - i);
            }

            final int[] npc = original.getNumberOfPointsPerContour();
            int index = npc[0];
            WB_Point[][] newInteriorPoints = new WB_Point[original.getNumberOfHoles()][];

            for (int i = 0; i < original.getNumberOfHoles(); i++) {
                WB_Point[] newHole = new WB_Point[npc[i + 1]];
                for (int j = 0; j < newHole.length; j++) {
                    newHole[j] = new WB_Point(original.getPoint(newHole.length - 1 - j + index));
                }
                newInteriorPoints[i] = newHole;
                index = index + npc[i + 1];
            }

            return new WB_Polygon(newExteriorPoints, newInteriorPoints);
        }
    }

    /**
     * WB_Polygon -> Polygon (holes supported)
     *
     * @param wbp input WB_Polygon
     * @return org.locationtech.jts.geom.Polygon
     */
    private Polygon WB_PolygonToPolygon(final WB_Polygon wbp) {
        if (wbp.getNumberOfHoles() == 0) {
            if (wbp.getPoint(0).equals(wbp.getPoint(wbp.getNumberOfPoints() - 1))) {
                Coordinate[] coords = new Coordinate[wbp.getNumberOfPoints()];
                for (int i = 0; i < wbp.getNumberOfPoints(); i++) {
                    coords[i] = new Coordinate(wbp.getPoint(i).xd(), wbp.getPoint(i).yd(), wbp.getPoint(i).zd());
                }
                return jtsgf.createPolygon(coords);
            } else {
                Coordinate[] coords = new Coordinate[wbp.getNumberOfPoints() + 1];
                for (int i = 0; i < wbp.getNumberOfPoints(); i++) {
                    coords[i] = new Coordinate(wbp.getPoint(i).xd(), wbp.getPoint(i).yd(), wbp.getPoint(i).zd());
                }
                coords[wbp.getNumberOfPoints()] = coords[0];
                return jtsgf.createPolygon(coords);
            }
        } else {
            // exterior
            List<Coordinate> exteriorCoords = new ArrayList<>();
            for (int i = 0; i < wbp.getNumberOfShellPoints(); i++) {
                exteriorCoords.add(new Coordinate(wbp.getPoint(i).xd(), wbp.getPoint(i).yd(), wbp.getPoint(i).zd()));
            }
            if (!exteriorCoords.get(0).equals3D(exteriorCoords.get(exteriorCoords.size() - 1))) {
                System.out.println("here");
                exteriorCoords.add(exteriorCoords.get(0));
            }
            LinearRing exteriorLinearRing = jtsgf.createLinearRing(exteriorCoords.toArray(new Coordinate[0]));

            // interior
            final int[] npc = wbp.getNumberOfPointsPerContour();
            int index = npc[0];
            LinearRing[] interiorLinearRings = new LinearRing[wbp.getNumberOfHoles()];
            for (int i = 0; i < wbp.getNumberOfHoles(); i++) {
                List<Coordinate> contour = new ArrayList<>();
                for (int j = 0; j < npc[i + 1]; j++) {
                    contour.add(new Coordinate(wbp.getPoint(index).xd(), wbp.getPoint(index).yd(), wbp.getPoint(index).zd()));
                    index++;
                }
                if (!contour.get(0).equals3D(contour.get(contour.size() - 1))) {
                    contour.add(contour.get(0));
                }
                interiorLinearRings[i] = jtsgf.createLinearRing(contour.toArray(new Coordinate[0]));
            }

            return jtsgf.createPolygon(exteriorLinearRing, interiorLinearRings);
        }
    }


    /**
     * Polygon -> WB_Polygon (holes supported)
     *
     * @param p input Polygon
     * @return wblut.geom.WB_Polygon
     */
    private WB_Polygon PolygonToWB_Polygon(final Polygon p) {
        if (p.getNumInteriorRing() == 0) {
            WB_Coord[] points = new WB_Point[p.getNumPoints()];
            for (int i = 0; i < p.getNumPoints(); i++) {
                points[i] = CoordinateToWB_Point(p.getCoordinates()[i]);
            }
            return new WB_Polygon(points).getSimplePolygon();
        } else {
            // exterior
            WB_Coord[] exteriorPoints = new WB_Point[p.getExteriorRing().getNumPoints()];
            for (int i = 0; i < p.getExteriorRing().getNumPoints(); i++) {
                exteriorPoints[i] = CoordinateToWB_Point(p.getCoordinates()[i]);
            }
            // interior
            int index = p.getExteriorRing().getNumPoints();
            WB_Coord[][] interiorHoles = new WB_Point[p.getNumInteriorRing()][];
            for (int i = 0; i < p.getNumInteriorRing(); i++) {
                LineString curr = p.getInteriorRingN(i);
                WB_Coord[] holePoints = new WB_Point[curr.getNumPoints()];
                for (int j = 0; j < curr.getNumPoints(); j++) {
                    WB_Point point = CoordinateToWB_Point(curr.getCoordinates()[j]);
                    holePoints[j] = point;
                }
                interiorHoles[i] = holePoints;
            }
            return new WB_Polygon(exteriorPoints, interiorHoles);
        }
    }

    /**
     * Coordinate -> WB_Point
     *
     * @param c input Coordinate
     * @return wblut.geom.WB_Point
     */
    private WB_Point CoordinateToWB_Point(final Coordinate c) {
        double z = c.z;
        if (Double.isNaN(z)) {
            return new WB_Point(c.x, c.y, 0);
        } else {
            return new WB_Point(c.x, c.y, c.z);
        }
    }

    /**
     * check the start point and the end point of a WB_Polygon
     * validate WB_Polygon (holes supported)
     *
     * @param polygon input WB_Polygon
     * @return wblut.geom.WB_Polygon
     */
    private WB_Polygon validateWB_Polygon(final WB_Polygon polygon) {
        if (polygon.getNumberOfHoles() == 0) {
            if (polygon.getPoint(0).equals(polygon.getPoint(polygon.getNumberOfPoints() - 1))) {
                return polygon;
            } else {
                List<WB_Coord> points = polygon.getPoints().toList();
                points.add(polygon.getPoint(0));
                return wbgf.createSimplePolygon(points);
            }
        } else {
            boolean flag = true;
            List<WB_Point> exterior = new ArrayList<>();
            for (int i = 0; i < polygon.getNumberOfShellPoints(); i++) {
                exterior.add(polygon.getPoint(i));
            }
            if (!exterior.get(0).equals(exterior.get(exterior.size() - 1))) {
                flag = false;
                exterior.add(exterior.get(0));
            }

            WB_Point[][] interior = new WB_Point[polygon.getNumberOfHoles()][];
            int[] npc = polygon.getNumberOfPointsPerContour();
            int index = npc[0];
            for (int i = 0; i < polygon.getNumberOfHoles(); i++) {
                List<WB_Point> contour = new ArrayList<>();
                for (int j = 0; j < npc[i + 1]; j++) {
                    contour.add(polygon.getPoint(index));
                    index = index + 1;
                }
                if (!contour.get(0).equals(contour.get(contour.size() - 1))) {
                    flag = false;
                    contour.add(contour.get(0));
                }
                interior[i] = contour.toArray(new WB_Point[0]);
            }
            if (flag) {
                return polygon;
            } else {
                return wbgf.createPolygonWithHoles(exterior.toArray(new WB_Point[0]), interior);
            }
        }
    }

    /**
     * WB_Coord -> Coordinate
     *
     * @param p input WB_Coord
     * @return org.locationtech.jts.geom.Coordinate
     */
    private Coordinate WB_CoordToCoordinate(final WB_Coord p) {
        return new Coordinate(p.xd(), p.yd(), p.zd());
    }
}
