package Typologies;

import igeo.IG;
import igeo.IGeometry;
import igeo.ISurface;
import igeo.IVec;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import wblut.geom.WB_Polygon;

import static org.uma.jmetal.problem.singleobjective.cec2005competitioncode.Benchmark.random;

public class Door {
    private Polygon polygon;
    private Site site;
    private LineString entranceEdge;
    private Coordinate[] coordinates;
    private WB_Polygon wbpolygon;
    private double sitea;
    private double siteb;
    private IVec OA;
    private IVec OB;
    private double thickness = 0.25;  //围墙厚度
    private double height = 3.0;  //围墙高度
    private IVec OAunit;
    private IVec OBunit;
    double entrance;  //默认中间
    private IGeometry[][] geometries;


    public double getThickness() {return thickness;}

    GeometryFactory gf = new GeometryFactory();

    public Door(Site site) {
        this.polygon = site.getSite();
        this.coordinates = polygon.getCoordinates();
        this.sitea = site.getSitea();
        this.siteb = site.getSiteb();
        this.OA = site.getOA();
        this.OB = site.getOB();
        this.entrance = site.getEntrance();
        this.site = site;
        this.OAunit = OA.cp();
        this.OBunit = OB.cp();
        OAunit.unit();
        OBunit.unit();
        this.geometries = site.getGeometries();
    }

    public Door(Coordinate[] coordinates, IGeometry[][] geometries) {
        this.coordinates = coordinates;
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

    public void drawDoor(String layer){

        //绘制门
        IGeometry[] door = geometries[0];
        IGeometry[] window = geometries[1];
        IGeometry[] newdoor = new IGeometry[door.length];
        IGeometry[] newwindow = new IGeometry[window.length];

        double angledoor = new IVec(1,0,0).angle(OB, new IVec(0,0,1));
        for (int i = 0; i < door.length; i++) {
            newdoor[i] = door[i].cp();
            newdoor[i].rot(angledoor);
            newdoor[i] = newdoor[i].mv(new IVec(((1-entrance)*(coordinates[0].x)+entrance*(coordinates[1].x)), ((1-entrance)*(coordinates[0].y)+entrance*(coordinates[1].y)), 0).cp().sub(OBunit.cp().mul(0.45))).clr(0.83, 0.70,0.55).layer(layer +"Entrance");
        }

        // Templates are shared across buildings; the caller owns their lifecycle.
    }
}
