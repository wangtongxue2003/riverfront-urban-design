package Typologies;

import igeo.IG;
import igeo.IGeometry;
import igeo.ISurface;
import igeo.IVec;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import wblut.geom.WB_Polygon;

public class B10XYX1 {
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

    public B10XYX1(Site site,float floor) {
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

    public B10XYX1(Coordinate[] coordinates, float floor, IGeometry[][] geometries){
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

    public void drawB10XYX1(String layer) {
        Coordinate[] coordinatesB101 = new Coordinate[coordinates.length];
        coordinatesB101[0] = new Coordinate(coordinates[0].x + 0.65*OA.x, coordinates[0].y + 0.65*OA.y, 0);
        coordinatesB101[1] = new Coordinate(coordinates[1].x + 0.65*OA.x, coordinates[1].y + 0.65*OA.y, 0);
        coordinatesB101[2] = coordinates[2];
        coordinatesB101[3] = coordinates[3];
        if (floor == 1.5){  new B1(coordinatesB101, 2, geometries ).drawB1(layer);}
        else new B1(coordinatesB101, floor, geometries).drawB1(layer);

        Coordinate[] coordinatesB1 = new Coordinate[coordinates.length];
        coordinatesB1[0] = coordinates[0];
        coordinatesB1[1] = coordinates[1];
        coordinatesB1[2] = new Coordinate(coordinates[1].x + 0.35*OA.x, coordinates[1].y + 0.35*OA.y, 0);
        coordinatesB1[3] = new Coordinate(coordinates[0].x + 0.35*OA.x, coordinates[0].y + 0.35*OA.y, 0);
        if (floor == 1.5){new B1(coordinatesB1, 1, geometries).drawB1(layer);}
        else new B1(coordinatesB1, floor, geometries).drawB1(layer);

        Coordinate[] coordinatesB10X = new Coordinate[coordinates.length];
        coordinatesB10X[0] = new Coordinate(coordinates[2].x -0.15*OA.x - 0.3*OB.x, coordinates[2].y -0.15*OA.y - 0.3*OB.y,  0);
        coordinatesB10X[1] = new Coordinate(coordinates[1].x +0.15*OA.x - 0.3*OB.x, coordinates[1].y +0.15*OA.y - 0.3*OB.y,  0);
        coordinatesB10X[2] = new Coordinate(coordinates[1].x + 0.15*OA.x - 0.1*OBunit.x, coordinates[1].y +0.15*OA.y - 0.1*OBunit.y,  0);
        coordinatesB10X[3] = new Coordinate(coordinates[1].x + 0.85*OA.x - 0.1*OBunit.x, coordinates[1].y + 0.85*OA.y - 0.1*OBunit.y, 0);
        if (floor == 1.5){ new B1(coordinatesB10X, 1, geometries).drawB1(layer);}
        else new B1(coordinatesB10X, floor, geometries).drawB1(layer);

        Coordinate[] coordinatesB10XYX = new Coordinate[coordinates.length];
        coordinatesB10XYX[0] = new Coordinate(coordinates[3].x -0.15*OA.x + 0.3*OB.x, coordinates[3].y -0.15*OA.y + 0.3*OB.y,  0);
        coordinatesB10XYX[1] = new Coordinate(coordinates[0].x +0.15*OA.x + 0.3*OB.x, coordinates[0].y +0.15*OA.y + 0.3*OB.y,  0);
        coordinatesB10XYX[2] = new Coordinate(coordinates[0].x +0.15*OA.x + 0.1*OBunit.x, coordinates[0].y +0.15*OA.y + 0.1*OBunit.y,  0);
        coordinatesB10XYX[3] = new Coordinate(coordinates[0].x + 0.85*OA.x + 0.1*OBunit.x, coordinates[0].y + 0.85*OA.y + 0.1*OBunit.y, 0);
        if(floor == 1.5){ new B1(coordinatesB10XYX, 1, geometries).drawB1(layer);}
        else new B1(coordinatesB10XYX, floor, geometries).drawB1(layer);

    }

   public double getBuildingArea(){
        return site.getSite().getArea() * (0.7 + 0.3* 0.7) * floor;
   }
}
