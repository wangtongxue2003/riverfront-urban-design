package Typologies;

import igeo.*;
import org.locationtech.jts.geom.*;
import wblut.geom.WB_Polygon;

import java.util.Arrays;

import static org.uma.jmetal.problem.singleobjective.cec2005competitioncode.Benchmark.random;

//一进一间
public class B1 {
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
    private double roofthickness = 0.5;  //屋顶厚度
    private IVec OAunit;
    private IVec OBunit;
    IGeometry[][] geometries;


    GeometryFactory gf = new GeometryFactory();

    public B1(Site site, float floor) {
        this.floor = floor == 1.5f ? 1f : floor;
        this.polygon = site.getSite();
        this.coordinates = polygon.getCoordinates();
        this.height = this.floor * 3.5;
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

    public B1(Coordinate[] coordinates, float floor, IGeometry[][] geometries) {
        this.floor = floor == 1.5f ? 1f : floor;
        this.coordinates = coordinates;
        this.height = this.floor * 3.0;
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

    public void drawB1(String layer){
        //山墙面点集
        IVec[] gablewallpnts = new IVec[]{
                new IVec(coordinates[0].x, coordinates[0].y, coordinates[0].getZ()),
                new IVec(coordinates[0].x, coordinates[0].y, height),
                new IVec((coordinates[0].x+coordinates[3].x)/2, (coordinates[0].y+coordinates[3].y)/2, height + 0.25*sitea),
                new IVec(coordinates[3].x, coordinates[3].y, height),
                new IVec(coordinates[3].x, coordinates[3].y, coordinates[3].getZ()),
        };
        ISurface gablewall = new ISurface(gablewallpnts).clr(0.9,0.9,0.9).layer(layer +"TypeB");  //绘制山墙面
        ISurface gablewall2 = gablewall.cp(OB).clr(0.9,0.9,0.9).layer(layer +"TypeB");
        IG.extrude(gablewallpnts, OB).clr(0.9,0.9,0.9).layer(layer + "TypeB");

        //绘制屋顶
        IVec[] roofpnts = new IVec[]{
                new IVec(coordinates[0].x, coordinates[0].y, height + roofthickness),
                new IVec((coordinates[0].x+coordinates[3].x)/2, (coordinates[0].y+coordinates[3].y)/2, height + 0.25*sitea + roofthickness),
                new IVec(coordinates[3].x, coordinates[3].y, height + roofthickness),
        };
        IG.extrude(roofpnts, OB).clr(0.3,0.3,0.3).layer(layer +"TypeB");

        IVec[] roofpnts2 = new IVec[]{
                new IVec(coordinates[0].x, coordinates[0].y, height + roofthickness),
                new IVec((coordinates[0].x+coordinates[3].x)/2, (coordinates[0].y+coordinates[3].y)/2, height + 0.25*sitea + roofthickness),
                new IVec(coordinates[3].x, coordinates[3].y, height + roofthickness),
                new IVec(coordinates[2].x, coordinates[2].y, height + roofthickness),
                new IVec((coordinates[2].x+coordinates[1].x)/2, (coordinates[2].y+coordinates[1].y)/2, height + 0.25*sitea + roofthickness),
                new IVec(coordinates[1].x, coordinates[1].y, height + roofthickness),
                new IVec(coordinates[0].x, coordinates[0].y, height + roofthickness),
        };
        IG.extrude(roofpnts2, new IVec(0,0,-roofthickness)).clr(0.3,0.3,0.3).layer(layer +"TypeB");

        //绘制门窗

        IGeometry[] door = geometries[0];
        IGeometry[] window = geometries[1];
        IGeometry[] newdoor = new IGeometry[door.length];
        IGeometry[] newwindow = new IGeometry[window.length];

        double angledoor = new IVec(1,0,0).angle(OB, new IVec(0,0,1));
        double anglewindow = new IVec(1,0,0).angle(OA, new IVec(0,0,1));
     /*   for (int i = 0; i < door.length; i++) {
            newdoor[i] = door[i].cp();
            newdoor[i].rot(angledoor);
            newdoor[i] = newdoor[i].mv(new IVec((coordinates[0].x+coordinates[1].x)/2, (coordinates[0].y+coordinates[1].y)/2, 0).sub(OBunit.cp().mul(0.45))).clr(0.83, 0.70,0.55).layer(layer +"TypeB");
        }*/
        for (int i = 0; i < window.length; i++) {
            newwindow[i] = window[i].cp();
            newwindow[i].rot(anglewindow);
            newwindow[i] = newwindow[i].mv(new IVec((coordinates[1].x+coordinates[2].x)/2, (coordinates[1].y+coordinates[2].y)/2, height-0.5).sub(OAunit.cp().mul(0.335))).clr(0.83, 0.70,0.55).layer(layer +"TypeB");
            newwindow[i].cp().mv(OB.cp().mul(-1)).clr(0.83, 0.70,0.55).layer(layer +"TypeB");
        }
    }

    public double getBuildingArea(){
        return sitea * siteb * floor;
    }
}
