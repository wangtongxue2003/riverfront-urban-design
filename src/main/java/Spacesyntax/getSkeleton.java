package Spacesyntax;

import igeo.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import processing.core.PApplet;
import Zshare.Skeleton.ZSkeleton_share;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import java.util.List;
import static Spacesyntax.dataTransfer.*;


public class getSkeleton extends PApplet{
    ZSkeleton_share skeletonExtractor;

    public static void main(String[] args) {
        PApplet.main("Spacesyntax.getSkeleton");
    }

    public void settings(){
        size(1300, 900, IG.GL);

    }
    public void setup() {

        //读取Rhino文件
        basic.ProjectPaths.open("private-data/1213.3dm");
        IPoint[] pts = IG.layer("01").points(); //getting points

        ICurve[] crvs = IG.layer("crvs").curves(); //getting curves
        WB_Point[][] points = new WB_Point[crvs.length][];
        for(int i = 0; i < crvs.length; i++) {
            points[i] = ICurveToWB_Point(crvs[i]);
        }

        ICurve[] innercrvs = IG.layer("innercrvs").curves(); //getting curves
        WB_Point[][] innerpoints = new WB_Point[innercrvs.length][];
        for (int i = 0; i < innercrvs.length; i++){
            innerpoints[i] = ICurveToWB_Point(innercrvs[i]);
        }

        WB_Polygon[] polygons = new WB_Polygon[crvs.length];
        for(int i = 0; i < crvs.length; i++){
            polygons[i] = new WB_Polygon(points[i], innerpoints);

            skeletonExtractor = new ZSkeleton_share(polygons[i]);
            List<LineString> ridegs = skeletonExtractor.getRidges();
            List<WB_Polygon> polygonslist = skeletonExtractor.getAllFacePolysWB();

            // 打印提取的所有边界
            List<LineString> allEdges = skeletonExtractor.getAllEdges();
            println("Number of edges: " + allEdges.size());

            // 使用IGEO绘制边界

            drawRigesWithIGEO(ridegs, "newridegs");
            drawEdgesWithIGEO(allEdges);

            for (WB_Polygon p : polygonslist){
                drawWB_Polygon(p);
            }

            for (LineString lineString : ridegs) {
                System.out.println(lineString.getNumPoints());
                Coordinate[] coordinates = lineString.getCoordinates();
                new ICurve(coordinates[0].x,coordinates[0].y,0,coordinates[1].x,coordinates[1].y,0).layer("edegs");
            }
        }


        ISurface[] srfs = IG.layer("01").surfaces(); //getting surfaces

        IMesh[] meshes = IG.layer("01").meshes(); //getting polygon meshes

        IBrep[] breps = IG.layer("01").breps(); //getting BReps

        basic.ProjectPaths.save("output/1213_output_file.3dm");
    }

    public static void drawEdgesWithIGEO(List<LineString> allEdges) {
        // 使用IGEO绘制边界
        for (LineString edge : allEdges) {
            IPoint[] ipoints = new IPoint[edge.getCoordinates().length];
            for (int i = 0; i < edge.getCoordinates().length; i++) {
                Coordinate c = edge.getCoordinates()[i];
                ipoints[i] = new IPoint(c.x, c.y, 0);
            }
            new ICurve(ipoints);
        }
    }

    public static void drawRigesWithIGEO(List<LineString> allEdges, String layername) {
        // 使用IGEO绘制riges
        for (LineString edge : allEdges) {
            IPoint[] ipoints = new IPoint[edge.getCoordinates().length];
            for (int i = 0; i < edge.getCoordinates().length; i++) {
                Coordinate c = edge.getCoordinates()[i];
                ipoints[i] = new IPoint(c.x, c.y, 0).layer(layername);
            }
            new ICurve(ipoints).clr(255,0,0).layer(layername);
        }
    }


}

