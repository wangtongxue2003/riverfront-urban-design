package Typologies;

import igeo.IGeometry;
import igeo.IVec;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import wblut.geom.WB_Polygon;

public class B010XY1 {
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

    public B010XY1(Site site, float floor) {
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

    public void drawB010XY1(String layer) {
        Coordinate[] coordinatesB0 = new Coordinate[coordinates.length];
        coordinatesB0[0] = coordinates[0];
        coordinatesB0[1] = coordinates[1];
        coordinatesB0[2] = new Coordinate(coordinates[1].getX() + 0.2*OA.x, coordinates[1].getY() + 0.2*OA.y, 0);
        coordinatesB0[3] = new Coordinate(coordinates[0].getX() + 0.2*OA.x, coordinates[0].getY() + 0.2*OA.y, 0);
        new B0(coordinatesB0).drawB0(layer);

        Coordinate[] coordinatesB10XY1 = new Coordinate[coordinates.length];
        coordinatesB10XY1[0] = coordinatesB0[3];
        coordinatesB10XY1[1] = coordinatesB0[2];
        coordinatesB10XY1[2] = coordinates[2];
        coordinatesB10XY1[3] = coordinates[3];
        new B10XY1(coordinatesB10XY1,floor, geometries).drawB10XY1(layer);
    }

    public double getBuildingArea(){
        return 0.8*site.getSite().getArea() * (0.7 + 0.3*0.6) * floor;
    }
}
