package RenewalGeneration;

import Spacesyntax.Spacesyntax_00;
import igeo.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import processing.core.PApplet;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static Spacesyntax.Spacesyntax_00.*;
import static Spacesyntax.Spacesyntax_00.findShortestPath;
import static Spacesyntax.dataTransfer.*;
import static Spacesyntax.getSkeleton.drawRigesWithIGEO;

public class DepthWithPlots extends PApplet {
    public void settings(){size(1300, 900, IG.GL);}

    public void setup(){
        IConfig.syncDrawAndDynamics = true;
        IG.duration(150);
        IG.bg(1.0);
        IG.fillWire();
        IG.start();
    }

    public static void main(String[] args) {
        PApplet.main("RenewalGeneration.DepthWithPlots");

        GeometryFactory gf = new GeometryFactory();
        basic.ProjectPaths.open("private-data/Rhino data model.3dm");

        //读取最公共区域点
        IPoint[] pts = IG.layer("pubpnts").points(); //getting points
        Point[] pubpnts = new Point[pts.length];
        Point[] pubshortestpnts = new Point[pts.length];

        //读取ridegs
        ICurve[] ridegs = IG.layer("ridegs").curves();//getting ridegs
        List<LineString> ridegslist = new ArrayList<>();
        for (int i = 0; i < ridegs.length; i++) {
            ridegslist.add(ICurveToLineString(ridegs[i]));
        }

        //读取plotcrvs
        ICurve[] plotcrvs = IG.layer("renewalplotcrvs").curves(); //getting curves
        Point[] centralpoints = new Point[plotcrvs.length];
        Point[] shortestpoints = new Point[plotcrvs.length];
        WB_Point[][] plotpoints = new WB_Point[plotcrvs.length][];
        WB_Polygon[] plotpolygons = new WB_Polygon[plotcrvs.length];

        for (int i = 0; i < plotcrvs.length; i++) {
            plotpoints[i] = ICurveToWB_Point(plotcrvs[i]);
            plotpolygons[i] = ICurveToWB_Polygon(plotcrvs[i]);
        }

            List<LineString> newridegs = new ArrayList<LineString>();
            newridegs.addAll(ridegslist);

            //针对每一个公共点进行运算
            for (int k = 0; k < pts.length; k++) {
                pubpnts[k] = gf.createPoint(new Coordinate(pts[k].x(), pts[k].y(), pts[k].z()));
                pubshortestpnts[k] = gf.createPoint(getshortestDistancepoint(pubpnts[k],ridegslist));
                /*new ICurve(pubshortestpnts[k].getX(), pubshortestpnts[k].getY(), 0, pubpnts[k].getX(), pubpnts[k].getY(), 0);*/
                Coordinate[] newline = new Coordinate[]{
                        new Coordinate(pubshortestpnts[k].getX(), pubshortestpnts[k].getY(), 0),
                        new Coordinate(pubpnts[k].getX(), pubpnts[k].getY(), 0)
                };
                LineString newlineString = gf.createLineString(newline);
                newridegs.add(newlineString);  //给newridegs添加新生成的ridegs
            }

            //针对每一个plot进行运算
            for (int j = 0; j < plotcrvs.length; j++) {
                WB_Point p = plotpolygons[j].getCenter();
                centralpoints[j] = gf.createPoint(new Coordinate(p.xd(),p.yd(), 0));
                shortestpoints[j] = gf.createPoint(getshortestDistancepoint(centralpoints[j],ridegslist));
                Coordinate[] newline = new Coordinate[]{
                        new Coordinate(shortestpoints[j].getX(), shortestpoints[j].getY(), 0),
                        new Coordinate(centralpoints[j].getX(), centralpoints[j].getY(), 0)
                };
                LineString newlineString = gf.createLineString(newline);
                newridegs.add(newlineString);  //给newridegs添加新生成的ridegs
            }

            newridegs = cuttedLineStrings(newridegs);

            // 构建图并添加交点
            Map<Coordinate, List<Spacesyntax_00.Edge>> graph = buildGraphWithIntersections(newridegs);

            double[] minidist = new double[plotcrvs.length];
            for (int j = 0; j < plotcrvs.length; j++) {
                Coordinate p1 = new Coordinate(centralpoints[j].getX(), centralpoints[j].getY(), 0);
                Coordinate p2 = new Coordinate(pubshortestpnts[0].getX(), pubshortestpnts[0].getY(), 0);
                minidist[j] = findShortestPath(graph, p1, p2);
                for (int k = 0; k < pts.length; k++) {
                    p2 = new Coordinate(pubshortestpnts[k].getX(), pubshortestpnts[k].getY(), 0);
                    double dist = findShortestPath(graph, p1, p2);
                    if (dist < minidist[j]) {
                        minidist[j] = dist;
                    }
                }
            }

            //可视化
            double max_minidist = minidist[0];
            double min_minidist = minidist[0];
            for (int j = 0; j < plotcrvs.length; j++) {
                if (max_minidist < minidist[j]) {
                    max_minidist = minidist[j];
                }
                if (min_minidist > minidist[j]) {
                    min_minidist = minidist[j];
                }
            }
            System.out.println("min: " + min_minidist);
            System.out.println("max: " + max_minidist);

          /*  for (int j = 0; j < plotcrvs.length; j++) {
                drawWB_Polygonwithclr(plotpolygons[j], (minidist[j]-min_minidist)/(max_minidist-min_minidist), (max_minidist-minidist[j])/(max_minidist-min_minidist), 0, "liveplot");
            }*/

            // 使用IGEO绘制边界
            drawRigesWithIGEO(newridegs, "newridegs");



        ISurface[] srfs = IG.layer("01").surfaces(); //getting surfaces

        IMesh[] meshes = IG.layer("01").meshes(); //getting polygon meshes

        IBrep[] breps = IG.layer("01").breps(); //getting BReps

        basic.ProjectPaths.save("output/prepared/Rhino data model-Renewal.3dm");
    }
}
