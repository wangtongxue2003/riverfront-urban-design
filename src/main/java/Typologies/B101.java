package Typologies;

import igeo.IG;
import igeo.IGeometry;
import igeo.ISurface;
import igeo.IVec;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import wblut.geom.WB_Polygon;

public class B101 {

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

    public B101(Site site, float floor) {
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

    public B101(Coordinate[] coordinates, float floor, IGeometry[][] geometries) {
        this.coordinates = coordinates;
        this.floor = floor;
        this.height = floor * 3.0;
        this.sitea = coordinates[0].distance(coordinates[3]);  //进深方向
        this.siteb = coordinates[0].distance(coordinates[1]);  //开间方向
        this.OA = new IVec(coordinates[3].x - coordinates[0].x, coordinates[3].y - coordinates[0].y,0);
        this.OB = new IVec(coordinates[1].x - coordinates[0].x, coordinates[1].y - coordinates[0].y,0);
        this.OAunit = OA.cp();
        this.OBunit = OB.cp();
        OAunit.unit();
        OBunit.unit();
        this.geometries = geometries;
    }

    public void drawB101(String layer) {
        Coordinate[] coordinatesB101 = new Coordinate[coordinates.length];
        coordinatesB101[0] = new Coordinate(coordinates[0].x + 0.65*OA.x, coordinates[0].y + 0.65*OA.y, 0);
        coordinatesB101[1] = new Coordinate(coordinates[1].x + 0.65*OA.x, coordinates[1].y + 0.65*OA.y, 0);
        coordinatesB101[2] = coordinates[2];
        coordinatesB101[3] = coordinates[3];
        if (floor == 1.5) {new B1(coordinatesB101, 2, geometries).drawB1(layer);}
        else new B1(coordinatesB101, floor, geometries).drawB1(layer);

        Coordinate[] coordinatesB1 = new Coordinate[coordinates.length];
        coordinatesB1[0] = coordinates[0];
        coordinatesB1[1] = coordinates[1];
        coordinatesB1[2] = new Coordinate(coordinates[1].x + 0.35*OA.x, coordinates[1].y + 0.35*OA.y, 0);
        coordinatesB1[3] = new Coordinate(coordinates[0].x + 0.35*OA.x, coordinates[0].y + 0.35*OA.y, 0);
        if (floor == 1.5) {new B1(coordinatesB1, 1, geometries).drawB1(layer);}
        else  new B1(coordinatesB1, floor, geometries).drawB1(layer);

        //绘制院墙
        double thickness = new B0(coordinates).getThickness();
        IVec[] courtyardWall = new IVec[5];
        courtyardWall[0] = new IVec(coordinatesB101[0].x, coordinatesB101[0].y, 0);
        courtyardWall[1] = new IVec(coordinatesB1[3].x, coordinatesB1[3].y, 0);
        courtyardWall[2] = new IVec(coordinatesB1[3].x + thickness*OBunit.x, coordinatesB1[3].y + thickness*OBunit.y, 0);
        courtyardWall[3] = new IVec(coordinatesB101[0].x + thickness*OBunit.x, coordinatesB101[0].y + thickness*OBunit.y, 0);
        courtyardWall[4] = courtyardWall[0];
        ISurface walltop = new ISurface(courtyardWall).mv(new IVec(0,0,3)).clr(0.9,0.9,0.9).layer(layer + "TypeB");
        ISurface wall = IG.extrude(courtyardWall, new IVec(0,0,3)).clr(0.9,0.9,0.9).layer(layer + "TypeB");
        walltop.cp().mv(OB.cp().sub(OBunit.cp().mul(thickness)));
        wall.cp().mv(OB.cp().sub(OBunit.cp().mul(thickness)));


    }

    public double getBuildingArea(){
        return sitea * siteb * 0.7 * floor;
    }
}
