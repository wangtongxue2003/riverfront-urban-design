package Typologies;

import igeo.IG;
import igeo.ISurface;
import igeo.IVec;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import wblut.geom.WB_Polygon;

public class C1 {
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

    GeometryFactory gf = new GeometryFactory();

    public C1(Site site, float floor) {
        this.floor = floor;
        this.polygon = site.getSite();
        this.coordinates = polygon.getCoordinates();
        this.height = floor * 3.5;
        this.sitea = site.getSitea();
        this.siteb = site.getSiteb();
        this.OA = site.getOA();
        this.OB = site.getOB();
        this.site = site;
    }

    public C1(Coordinate[] coordinates, float floor) {
        this.floor = floor;
        this.coordinates = coordinates;
        this.height = floor * 3.5;
        this.sitea = coordinates[0].distance(coordinates[3]);  //进深方向
        this.siteb = coordinates[0].distance(coordinates[1]);  //开间方向
        this.OA = new IVec(coordinates[3].x - coordinates[0].x, coordinates[3].y - coordinates[0].y,0);
        this.OB = new IVec(coordinates[1].x - coordinates[0].x, coordinates[1].y - coordinates[0].y,0);
    }

    public void drawC1(String layer){
        //山墙面点集
        IVec[] gablewallpnts = new IVec[]{
                new IVec(coordinates[0].x, coordinates[0].y, coordinates[0].z),
                new IVec(coordinates[0].x, coordinates[0].y, height),
                new IVec((coordinates[0].x+coordinates[3].x)/2, (coordinates[0].y+coordinates[3].y)/2, height + 0.25*sitea),
                new IVec(coordinates[3].x, coordinates[3].y, height),
                new IVec(coordinates[3].x, coordinates[3].y, coordinates[3].z),
        };
        ISurface gablewall = new ISurface(gablewallpnts).clr(0,0,1.0).layer(layer +"TypeC");  //绘制山墙面
        ISurface gablewall2 = gablewall.cp(OB);
        IG.extrude(gablewallpnts, OB).clr(0,0,1.0).layer(layer + "TypeC");
    }

    public double getBuildingArea(){
        return sitea * siteb * floor;
    }
}
