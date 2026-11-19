package NSGAII;

import Zshare.advancedGeometry.largestRectangle.ZLargestRectangle;
import basic.AJLoader;
import basic.Vec;
import igeo.IConfig;
import igeo.ICurve;
import igeo.IG;
import igeo.IVec;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import processing.core.PApplet;
import wblut.geom.WB_Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static Spacesyntax.dataTransfer.*;

public class test02 extends PApplet {
    private static final int NUM_THREADS = 16; // 可根据机器的核心数调整
    private static final String BASE_PATH = "private-data/flexurban_export ";

    AJLoader loader;
    public void settings(){
        size(1300, 900, IG.GL);

    }

    public static void main(String[] args) {

        PApplet.main("NSGAII.test02");

        GeometryFactory gf = new GeometryFactory();
        basic.ProjectPaths.open("private-data/1216 plot_test.3dm");

        ICurve[] bound = IG.layer("bound").curves();
        ICurve[] riverrideg = IG.layer("riverrideg").curves();
        ICurve[] ridegs = IG.layer("newridegs").curves();
        ICurve[] plotcrvs = IG.layer("plotcrvs").curves();
        ICurve[] qinhuairiver = IG.layer("qinhuairiver").curves();

        LineString ridegstring = ICurveToLineString(riverrideg[0]);
        LineString boundstring = ICurveToLineString(bound[0]);
        List<LineString> rideglist = new ArrayList<LineString>();
        for(int i = 0; i < ridegs.length; i++){
            rideglist.add(ICurveToLineString(ridegs[i]));
        }

        List<LineString> qinhuairiverstring = new ArrayList<>();
        for(int i = 0; i < qinhuairiver.length; i++){
            qinhuairiverstring.add(ICurveToLineString(qinhuairiver[i]));
        }

        double[] locations = {5,5.1,5.2,5.3,110,125,140,150,175,200,225,240,275,320};
        char[] typology = {'B','B','B','A','B','A','B','A','B','C','B','B','A','B'};
        float[] floor = {1,2,1,4,3,2,2,2,3,4,1,2,1,2};

        Plots plots = new Plots(boundstring,ridegstring,locations,typology, floor,qinhuairiverstring);

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        // 提交每个x的任务
        for (int x = 0; x < 1; x++) {
            final int index = x;  // 由于lambda表达式需要final或有效final变量
            executor.submit(() -> {
                AJLoader loader = new AJLoader(BASE_PATH + "(" + index + ").archijson");
                Vec[][] flexurbanplots = loader.getPlotVecs();
                IVec[][] newplots = new IVec[flexurbanplots.length][];

                // 处理 newplots
                for (int i = 0; i < flexurbanplots.length; i++) {
                    newplots[i] = new IVec[flexurbanplots[i].length];
                    for (int j = 0; j < flexurbanplots[i].length; j++) {
                        newplots[i][j] = new IVec(flexurbanplots[i][j].x, flexurbanplots[i][j].y, flexurbanplots[i][j].z);
                    }
                }


                // 处理 newplotcrvs 和 newplotsrectangle
                ICurve[] newplotcrvs = new ICurve[flexurbanplots.length];
                WB_Polygon[] newplotspolygon = new WB_Polygon[flexurbanplots.length];
                WB_Polygon[] newplotsrectangle = new WB_Polygon[flexurbanplots.length];
                Coordinate[][] rectanglecoordinates = new Coordinate[flexurbanplots.length][];
                IVec[][] rectangles = new IVec[flexurbanplots.length][];
                for (int i = 0; i < flexurbanplots.length; i++) {
                    newplotcrvs[i] = new ICurve(newplots[i]).layer("flexurban_" + index + "_newplots");
                    newplotspolygon[i] = ICurveToWB_Polygon(newplotcrvs[i]);
                    System.out.println(newplotcrvs[i]);
                    ZLargestRectangle largestRectangle = new ZLargestRectangle(newplotspolygon[i]);
                    largestRectangle.cal();
                    newplotsrectangle[i] = largestRectangle.getRectResult_WB();
                    rectangles[i] = new IVec[newplotsrectangle[i].getNumberOfPoints()];
                    System.out.println(newplotsrectangle[i].getNumberOfPoints());
                    for (int j = 0; j < newplotsrectangle[i].getNumberOfPoints(); j++){
                        rectangles[i][j] = new IVec(newplotsrectangle[i].getPoint(j).xd(), newplotsrectangle[i].getPoint(j).yd(), newplotsrectangle[i].getPoint(j).zd());
                    }
                    new ICurve(rectangles[i]).layer("Rectangle_" + index);
                    drawWB_Polygonwithclr(newplotsrectangle[i], 0, 0, 0, "Rectangle_" + index);
                }
            });
        }

        // 关闭 executor
        executor.shutdown();

        try {
            // 等待所有任务完成
            if (!executor.awaitTermination(60, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

    /*    for (int x = 0; x < 1; x++){
            this.loader = new AJLoader("private-data/flexurban_export (" + x + ").archijson");
            Vec[][] flexurbanplots = loader.getPlotVecs();
            IVec[][] newplots = new IVec[flexurbanplots.length][];
            for (int i = 0; i < flexurbanplots.length; i++) {
                newplots[i] = new IVec[flexurbanplots[i].length];
                for (int j = 0; j < flexurbanplots[i].length; j++) {
                    newplots[i][j] = new IVec(flexurbanplots[i][j].x, flexurbanplots[i][j].y, flexurbanplots[i][j].z);
                }
            }
            ICurve[] newplotcrvs = new ICurve[flexurbanplots.length];
            WB_Polygon[] newplotspolygon = new WB_Polygon[flexurbanplots.length];
            WB_Polygon[] newplotsrectangle = new WB_Polygon[flexurbanplots.length];
            for (int i = 0; i < flexurbanplots.length; i++) {
                newplotcrvs[i] = new ICurve(newplots[i]).layer("flexurban_" + x + "_newplots");
            newplotspolygon[i] = ICurveToWB_Polygon(newplotcrvs[i]);
            System.out.println(newplotcrvs[i]);
            ZLargestRectangle largestRectangle = new ZLargestRectangle(newplotspolygon[i]);
            largestRectangle.cal();
            newplotsrectangle[i] = largestRectangle.getRectResult_WB();
            drawWB_Polygonwithclr(newplotsrectangle[i], 0,0,0,"Rectangle_" + x);
            }

        }*/

        basic.ProjectPaths.save("output/1226 plot_test.3dm");
        /*    plots.DecideTypology("test");*/
    }

    public void setup() {
        IConfig.syncDrawAndDynamics = true;
        IG.duration(150);
        IG.bg(1.0);
        IG.fillWire();
        IG.start();


    }
}
