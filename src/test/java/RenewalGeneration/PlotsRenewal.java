package RenewalGeneration;

import Typologies.RecTypeB;
import Zshare.advancedGeometry.largestRectangle.ZLargestRectangle;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static Spacesyntax.Spacesyntax_00.*;
import static Spacesyntax.Spacesyntax_00.findShortestPath;
import static Spacesyntax.dataTransfer.*;

public class PlotsRenewal extends PApplet {
    static double minidistance = 33.5;  //最近地块距离
    static double maxidistance = 463.8;  //最远地块距离
    static double distanceMax = 450;  //最大能盖房子的距离，超出此值不盖房子
    static double buffer = 0;  //矩形地块退界
    static IGeometry[][] geometries = new IGeometry[2][];  //门窗其他

    public void settings(){size(1300, 900, IG.GL);}

    public void setup(){
        IConfig.syncDrawAndDynamics = true;
        IG.duration(150);
        IG.bg(1.0);
        IG.fillWire();
        IG.start();
    }

    public static void main(String[] args) {
        PApplet.main("RenewalGeneration.PlotsRenewal");
        basic.ProjectPaths.open("assets/components/door-and-window.3dm");
        geometries[0] = IG.layer("door").geometries();  //门
        geometries[1] = IG.layer("window").geometries();  //窗
        IG.delAll();

        GeometryFactory gf = new GeometryFactory();
        basic.ProjectPaths.open(basic.ProjectPaths.prepared("Rhino data model-Renewal.3dm"));

        //读取最公共区域点
        IPoint[] pts = IG.layer("pubpnts").points(); //getting points
        Point[] pubpnts = new Point[pts.length];

        //读取ridegs
        ICurve[] ridegs = IG.layer("newridegs").curves();//getting ridegs
        IPoint[] ridegpnts = IG.layer("newridegs").points();
        List<LineString> ridegslist = new ArrayList<>();
        for (int i = 0; i < ridegs.length; i++) {
            ridegslist.add(ICurveToLineString(ridegs[i]));
        }
        IG.del(ridegs);
        IG.del(ridegpnts);

        //删plotcrvs
        ICurve[] plotcrvs1 = IG.layer("plotcrvs").curves();

        //读取plotcrvs
        ICurve[] plotcrvs = IG.layer("renewalplotcrvs").curves(); //getting curves
        Point[] centralpoints = new Point[plotcrvs.length];
        WB_Polygon[] plotpolygons = new WB_Polygon[plotcrvs.length];

        for (int i = 0; i < plotcrvs.length; i++) {
            plotpolygons[i] = ICurveToWB_Polygon(plotcrvs[i]);
        }
        //针对每一个公共点
        for (int k = 0; k < pts.length; k++) {
            pubpnts[k] = gf.createPoint(new Coordinate(pts[k].x(), pts[k].y(), pts[k].z()));
        }
        //针对每一个plot
        for (int j = 0; j < plotcrvs.length; j++) {
            WB_Point p = plotpolygons[j].getCenter();
            centralpoints[j] = gf.createPoint(new Coordinate(p.xd(),p.yd(), 0));
        }
        // 构建图并添加交点
        Map<Coordinate, List<Edge>> graph = buildGraphWithIntersections(ridegslist);

        double[] minidist = new double[plotcrvs.length];

        WB_Polygon[] newplotRecs = new WB_Polygon[plotcrvs.length];
        IVec[][] rects = new IVec[plotcrvs.length][];

        //并行计算器
        // iGeo uses a global scene; generate sequentially to avoid shared-scene races.

        //针对每个地块来算
        for (int i = 0; i < plotcrvs.length; i++) {
            final int j = i;  // 由于lambda表达式需要final或有效final变量
            {
                        Coordinate p1 = new Coordinate(centralpoints[j].getX(), centralpoints[j].getY(), 0);
                        Coordinate p2 = new Coordinate(pubpnts[0].getX(), pubpnts[0].getY(), 0);
                        minidist[j] = findShortestPath(graph, p1, p2);
                        for (int k = 0; k < pts.length; k++) {
                            p2 = new Coordinate(pubpnts[k].getX(), pubpnts[k].getY(), 0);
                            double dist = findShortestPath(graph, p1, p2);
                            if (dist < minidist[j]) {
                                minidist[j] = dist;
                            }
                        }

                  /*  //绘制地块颜色
                    drawWB_Polygonwithclr(plotpolygons[j], (minidist[j]-minidistance)/(maxidistance-minidistance),
                            (maxidistance-minidist[j])/(maxidistance-minidistance),
                            0, "liveplot");*/

                 //判断深度值
                 //作为公共空间
                 if (minidist[j] > distanceMax) {
                     //栽一棵树
                 }

                 //更新地块
                 if (minidist[j] <= distanceMax) {
                       //计算最大内接矩形
                     ZLargestRectangle largestRectangle = new ZLargestRectangle(plotpolygons[j]);
                        largestRectangle.cal();
                        newplotRecs[j] = largestRectangle.getRectResult_WB();
                        rects[j] = new IVec[newplotRecs[j].getNumberOfPoints()];
                        for (int k = 0; k < newplotRecs[j].getNumberOfPoints(); k++) {
                            rects[j][k] = new IVec(newplotRecs[j].getPoint(k).xd(), newplotRecs[j].getPoint(k).yd(), newplotRecs[j].getPoint(k).zd());
                        }
                        ICurve crvj = new ICurve(rects[j]).layer("Plots_Rectangle");

                        //盖房子
                     RecTypeB plotj = new RecTypeB(newplotRecs[j], buffer, geometries);
                        plotj.drawRealRecTypeB("Plots_Renewal",ridegslist );
                 }
            }
        }

         basic.ProjectPaths.save("output/Renewal-Output-6.3dm");
    }
}
