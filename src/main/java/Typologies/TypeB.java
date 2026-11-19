package Typologies;

import NSGAII.Plot;
import NSGAII.Plots;
import igeo.ICurve;
import igeo.IGeometry;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import wblut.geom.WB_Polygon;

import static Spacesyntax.dataTransfer.*;

public class TypeB {
    private String type;
    private Plots plots;
    private WB_Polygon wb_polygon;
    private int index;
    private float floor;
    private Polygon polygon;
    private Polygon sitePolygon;
    private final double buffer = -1;
    private IGeometry[][] geometries;


    public TypeB(Plots plots, int index, IGeometry[][] geometries) {
        this.plots = plots;
        this.wb_polygon = plots.getWbPolygon(index);
        this.index = index;
        this.floor = plots.getFloor(index);
        this.polygon = WB_PolygonToPolygon(wb_polygon);
        this.geometries = geometries;
    }

    public TypeB(Plots plots, int index) {
        this.plots = plots;
        this.wb_polygon = plots.getWbPolygon(index);
        this.index = index;
        this.floor = plots.getFloor(index);
        this.polygon = WB_PolygonToPolygon(wb_polygon);
        this.geometries = geometries;
    }

    public void drawTypeB(String layer) {

        GeometryFactory gf = new GeometryFactory();
        Geometry psiteB = plots.getPlotRectangle(index).buffer(buffer);

        Coordinate[] coords = psiteB.getCoordinates();
        if (coords.length > 3) {
            for (int i = 0; i < coords.length; i++) {
                coords[i] = new Coordinate(coords[i].x, coords[i].y,0);
            }
            sitePolygon = gf.createPolygon(coords);
            drawPolygonwithclr(sitePolygon,1,1,0,layer+"TypeB");

            Site siteB = new Site(sitePolygon, (new Plot(sitePolygon,plots.getRiver()).getopenEdge(plots.getRiver())),0.5, geometries);
            siteB = siteB.getSiteanother();
            siteB = siteB.getSiteanother(); //巷道一侧作为入口

            if (siteB.getSiteb() <= 4.5){}

            //没有厢房
            if (siteB.getSiteb() > 4.5 && siteB.getSiteb() <= 9){
                //一进院
                if (siteB.getSitea() <= 20 ){
                    new B101(siteB, floor).drawB101(layer+"TypeB");
                }
                //两进院
                if (siteB.getSitea() > 20 && siteB.getSitea() <= 24){
                    new B0101(siteB, floor).drawB0101(layer+"TypeB");
                }
                //三进院
                if (siteB.getSitea() > 24){
                    new B01010(siteB, floor).drawB01010(layer+"TypeB");
                }
            }

            //有一个厢房
            if (siteB.getSiteb() > 9 && siteB.getSiteb() <= 12){
                //一进院
                if (siteB.getSitea() <= 20 ){
                    new B10XY1(siteB, floor).drawB10XY1(layer+"TypeB");
                }
                //两进院
                if (siteB.getSitea() > 20){
                    new B010XY1(siteB, floor).drawB010XY1(layer+"TypeB");
                }
            }

            //有两个厢房
            if (siteB.getSiteb() > 12){
                //一进院
                if (siteB.getSitea() <= 20 ){
                    new B10XYX1(siteB,floor).drawB10XYX1(layer+"TypeB");
                }
                //两进院
                if (siteB.getSitea() > 20){
                    new B10XY10(siteB, floor).drawB10XY10(layer+"TypeB");
                }
            }

        }
    }

    public double getBuildingArea() {
        double BuildingArea = 0.0;


        GeometryFactory gf = new GeometryFactory();
        Geometry psiteB = plots.getPlotRectangle(index).buffer(buffer);

        Coordinate[] coords = psiteB.getCoordinates();
        if (coords.length > 3){
            for (int i = 0; i < coords.length; i++) {
                coords[i] = new Coordinate(coords[i].x, coords[i].y,0);
            }
            sitePolygon = gf.createPolygon(coords);

            Site siteB = new Site(sitePolygon, (new Plot(sitePolygon,plots.getRiver()).getopenEdge(plots.getRiver())),0.5, geometries);

            if (siteB.getSiteb() <= 4.5){}

            //没有厢房
            if (siteB.getSiteb() > 4.5 && siteB.getSiteb() <= 9){
                //一进院
                if (siteB.getSitea() <= 20 ){
                    BuildingArea = new B101(siteB, floor).getBuildingArea();
                }
                //两进院
                if (siteB.getSitea() > 20 && siteB.getSitea() <= 24){
                    BuildingArea = new B0101(siteB, floor).getBuildingArea();
                }
                //三进院
                if (siteB.getSitea() > 24){
                    BuildingArea = new B01010(siteB, floor).getBuildingArea();
                }
            }

            //有一个厢房
            if (siteB.getSiteb() > 9 && siteB.getSiteb() <= 12){
                //一进院
                if (siteB.getSitea() <= 20 ){
                    BuildingArea = new B10XY1(siteB, floor).getBuildingArea();
                }
                //两进院
                if (siteB.getSitea() > 20){
                    BuildingArea = new B010XY1(siteB, floor).getBuildingArea();
                }
            }

            //有两个厢房
            if (siteB.getSiteb() > 12){
                //一进院
                if (siteB.getSitea() <= 20 ){
                    BuildingArea = new B10XYX1(siteB,floor).getBuildingArea();
                }
                //两进院
                if (siteB.getSitea() > 20){
                    BuildingArea = new B10XY10(siteB, floor).getBuildingArea();
                }
            }
        }

        return BuildingArea;
    }
}
