package Typologies;

import igeo.IGeometry;
import igeo.IVec;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import wblut.geom.WB_Polygon;

public class B10XY {
    private Polygon polygon;
    private Site site;
    private float floor;
    private LineString entranceEdge;
    private Coordinate[] coordinates;
    private WB_Polygon wbpolygon;
    private double height;
    private double sitea;
    private double siteb;
    private IVec OA;
    private IVec OB;
    private IVec OAunit;
    private IVec OBunit;
    IGeometry[][] geometries;

    GeometryFactory gf = new GeometryFactory();

    public B10XY(Site site, float floor) {
        this.floor = floor == 1.5f ? 1f : floor;
        this.polygon = site.getSite();
        this.coordinates = polygon.getCoordinates();
        this.height = this.floor * 3.0;
        this.sitea = site.getSitea();
        this.siteb = site.getSiteb();
        this.OA = site.getOA();
        this.OB = site.getOB();
        this.site = site;
        this.OAunit = OA.cp();
        this.OBunit = OB.cp();
        OAunit.unit();
        OBunit.unit();
        this.geometries = site.getGeometries();
    }

    public void drawB10XY(String layer) {
        Coordinate[] coordinatesB1 = new Coordinate[coordinates.length];
        coordinatesB1[0] = new Coordinate(coordinates[0].x + 0.4*OA.x, coordinates[0].y + 0.4*OA.y, 0);
        coordinatesB1[1] = new Coordinate(coordinates[1].x + 0.4*OA.x, coordinates[1].y + 0.4*OA.y, 0);
        coordinatesB1[2] = coordinates[2];
        coordinatesB1[3] = coordinates[3];
        new B1(coordinatesB1, floor, geometries).drawB1(layer);

        Coordinate[] coordinatesB10X = new Coordinate[coordinates.length];
        coordinatesB10X[0] = new Coordinate(coordinates[0].x + 0.85*OA.x + 0.6*OB.x, coordinates[0].y + 0.85*OA.y + 0.6*OB.y, 0);
        coordinatesB10X[1] = new Coordinate(coordinates[0].x + 0.6*OB.x, coordinates[0].y + 0.6*OB.y, 0);
        coordinatesB10X[2] = new Coordinate(coordinates[1].x - 0.1*OBunit.x, coordinates[1].y - 0.1*OBunit.y, 0);
        coordinatesB10X[3] = new Coordinate(coordinates[1].x + 0.85*OA.x - 0.1*OBunit.x, coordinates[1].y + 0.85*OA.y - 0.1*OBunit.y, 0);
        new B1(coordinatesB10X, floor, geometries).drawB1(layer);

        double thickness = new B0(coordinates).getThickness();
        Coordinate[] coordinatesB0 = new Coordinate[coordinates.length];
        coordinatesB0[0] = coordinates[0];
        coordinatesB0[1] = coordinatesB10X[1];
        coordinatesB0[2] = new Coordinate(coordinatesB10X[1].x + thickness*OAunit.x, coordinatesB10X[1].y + thickness*OAunit.y, 0);
        coordinatesB0[3] = coordinatesB1[0];
        new B0(coordinatesB0).drawB0(layer);
    }

    public double getBuildingArea(){
        return site.getSite().getArea() * (0.65 + 0.35*0.4) * floor;
    }
}
