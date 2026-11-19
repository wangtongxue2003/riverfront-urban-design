package Typologies;

import NSGAII.Plot;
import NSGAII.Plots;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import wblut.geom.WB_Coord;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;

import static Spacesyntax.dataTransfer.*;

public class TypeA {
    private String type;
    private Plots plots;
    private WB_Polygon wb_polygon;
    private int index;
    private float floor;
    private Polygon polygon;
    private double BuildingArea=0;


    public TypeA(Plots plots, int index) {
        this.plots = plots;
        this.wb_polygon = plots.getWbPolygon(index);
        this.index = index;
        this.floor = plots.getFloor(index);
        this.polygon = WB_PolygonToPolygon(wb_polygon);
    }

    public void drawTypeA(String layer) {
        drawWB_Polygonwithclr(wb_polygon,0,1,0,layer+"TypeA");
        GeometryFactory gf = new GeometryFactory();

        Polygon siteA = plots.getPlotRectangle(index);
        Coordinate[] coords = siteA.getCoordinates();
   /*     drawPolygonwithclr(siteA,0,1,0,layer+"TypeA");*/

      /*  Polygon plane = (Polygon) polygon.buffer(-3);
        double height = floor*3.5;
        Coordinate[] planeCoordinates= plane.getCoordinates();
        for (int i = 0; i < planeCoordinates.length; i++) {
            planeCoordinates[i] = new Coordinate(planeCoordinates[i].x,planeCoordinates[i].y,0);
        }
        Coordinate[] roofCoordinates = new Coordinate[planeCoordinates.length];
        for (int i = 0; i < planeCoordinates.length; i++) {
            roofCoordinates[i] = new Coordinate(planeCoordinates[i].x,planeCoordinates[i].y,height);
        }
        WB_Coord[] roofcoords = new WB_Coord[roofCoordinates.length];
        for (int i = 0; i < roofCoordinates.length; i++) {
            roofcoords[i] = new WB_Point(roofCoordinates[i].x,roofCoordinates[i].y,height);
        }
        WB_Polygon roof = new WB_Polygon(roofcoords);
        drawWB_Polygonwithclr(roof,1,0,0,"TypeA");
        for (int i = 0; i < planeCoordinates.length-1; i++) {
            Coordinate[] facadeCoordinates = {planeCoordinates[i],planeCoordinates[i+1],roofCoordinates[i+1],roofCoordinates[i],planeCoordinates[i]};
            WB_Coord[] facadecoords = new WB_Coord[facadeCoordinates.length];
            for (int j = 0; j < facadeCoordinates.length; j++) {
                facadecoords[j] = new WB_Point(facadeCoordinates[j].x,facadeCoordinates[j].y,facadeCoordinates[j].z);
            }

            WB_Polygon wbfacade = new WB_Polygon(facadecoords);
            drawWB_Polygonwithclr(wbfacade,1,0,0,"TypeA");
        }*/

    }

    public double getBuildingArea(){
        return BuildingArea;
    }

    public double getTypeArea(){
        return polygon.getArea();
    }

}
