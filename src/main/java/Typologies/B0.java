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

//院子围墙
public class B0 {
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


    public double getThickness() {return thickness;}

    GeometryFactory gf = new GeometryFactory();

    public B0(Site site) {
        this.polygon = site.getSite();
        this.coordinates = polygon.getCoordinates();
        this.sitea = site.getSitea();
        this.siteb = site.getSiteb();
        this.OA = site.getOA();
        this.OB = site.getOB();
        this.site = site;
        this.OAunit = OA.cp();
        this.OBunit = OB.cp();
        OAunit.unit();
        OBunit.unit();
    }

    public B0(Coordinate[] coordinates) {
        this.coordinates = coordinates;
        this.sitea = coordinates[0].distance(coordinates[3]);  //进深方向
        this.siteb = coordinates[0].distance(coordinates[1]);  //开间方向
        this.OA = new IVec(coordinates[3].x - coordinates[0].x, coordinates[3].y - coordinates[0].y,0);
        this.OB = new IVec(coordinates[1].x - coordinates[0].x, coordinates[1].y - coordinates[0].y,0);
        this.OAunit = OA.cp();
        this.OBunit = OB.cp();
        OAunit.unit();
        OBunit.unit();
    }

    public void drawB0(String layer){
        //围墙
        IVec[] wallpnts = new IVec[]{
                new IVec(coordinates[0].x, coordinates[0].y, 0),
                new IVec(coordinates[1].x, coordinates[1].y, 0),
                new IVec(coordinates[2].x, coordinates[2].y, 0),
                new IVec(coordinates[2].x - thickness*OBunit.x,
                        coordinates[2].y - thickness*OBunit.y,
                        0),
                new IVec(coordinates[1].x - thickness*OBunit.x + thickness*OAunit.x,
                        coordinates[1].y - thickness*OBunit.y + thickness*OAunit.y,
                        0),
                new IVec(coordinates[0].x + thickness*OAunit.x + thickness*OBunit.x,
                        coordinates[0].y + thickness*OAunit.y + thickness*OBunit.y,
                        0),
                new IVec(coordinates[3].x + thickness*OBunit.x,
                        coordinates[3].y + thickness*OBunit.y,
                        0),
                new IVec(coordinates[3].x, coordinates[3].y, 0),
                new IVec(coordinates[0].x, coordinates[0].y, 0)
        };
        ISurface walltop = new ISurface(wallpnts).mv(new IVec(0,0,3)).clr(0.9,0.9,0.9).layer(layer + "TypeB");
        IG.extrude(wallpnts, new IVec(0,0,3)).clr(0.9,0.9,0.9).layer(layer + "TypeB");

     /*   //绘制门
        basic.ProjectPaths.open("assets/components/door-and-window.3dm");
        IGeometry[] door = IG.layer("door").geometries();
        IGeometry[] window = IG.layer("window").geometries();

        IGeometry[] newdoor = new IGeometry[door.length];

        double angledoor = new IVec(1,0,0).angle(OB, new IVec(0,0,1));
        for (int i = 0; i < door.length; i++) {
            newdoor[i] = door[i].cp();
            newdoor[i].rot(angledoor);
            newdoor[i] = newdoor[i].mv(new IVec((coordinates[0].x+coordinates[1].x)/2, (coordinates[0].y+coordinates[1].y)/2, 0).sub(OBunit.cp().mul(0.45))).clr(0.83, 0.70,0.55).layer(layer +"TypeB");
        }

        IG.del(door);
        IG.del(window);*/
    }
}
