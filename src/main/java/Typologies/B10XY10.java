package Typologies;

import igeo.IGeometry;
import igeo.IVec;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import wblut.geom.WB_Polygon;

public class B10XY10 {
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
    private IGeometry[][] geometries;

    public B10XY10(Site site, float floor) {
        this.floor = floor;
        this.polygon = site.getSite();
        this.coordinates = polygon.getCoordinates();
        this.height = floor * 3.0;
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

    public void drawB10XY10(String layer) {
        Coordinate[] coordinatesB0 = new Coordinate[coordinates.length];
        coordinatesB0[0] = coordinates[2];
        coordinatesB0[1] = coordinates[3];
        coordinatesB0[2] = new Coordinate(coordinates[3].getX() - 0.2*OA.x, coordinates[3].getY() - 0.2*OA.y, 0);
        coordinatesB0[3] = new Coordinate(coordinates[2].getX() - 0.2*OA.x, coordinates[2].getY() - 0.2*OA.y, 0);
        new B0(coordinatesB0).drawB0(layer);

        Coordinate[] coordinatesB10XYX1 = new Coordinate[coordinates.length];
        coordinatesB10XYX1[0] = coordinatesB0[3];
        coordinatesB10XYX1[1] = coordinatesB0[2];
        coordinatesB10XYX1[2] = coordinates[0];
        coordinatesB10XYX1[3] = coordinates[1];
        new B10XYX1(coordinatesB10XYX1,floor, geometries).drawB10XYX1(layer);
    }

    public double getBuildingArea(){
        return 0.8*site.getSite().getArea() * (0.7 + 0.3* 0.7) * floor;
    }
}
