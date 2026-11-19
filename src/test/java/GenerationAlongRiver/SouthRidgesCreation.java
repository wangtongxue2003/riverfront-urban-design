package GenerationAlongRiver;

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

import static Spacesyntax.Spacesyntax_00.*;
import static Spacesyntax.Spacesyntax_00.findShortestPath;
import static Spacesyntax.dataTransfer.*;
import static Spacesyntax.getSkeleton.drawRigesWithIGEO;

public class SouthRidgesCreation extends PApplet {
    public void settings(){size(1300, 900, IG.GL);}

    public void setup(){
        IConfig.syncDrawAndDynamics = true;
        IG.duration(150);
        IG.bg(1.0);
        IG.fillWire();
        IG.start();
    }

    public static void main(String[] args) {
        PApplet.main("GenerationAlongRiver.SouthRidgesCreation");

        GeometryFactory gf = new GeometryFactory();
        basic.ProjectPaths.open("private-data/Rhino data model.3dm");

        //读取ridegs
        ICurve[] ridegs = IG.layer("ridegs").curves();//getting ridegs
        List<LineString> ridegslist = new ArrayList<>();
        for (int i = 0; i < ridegs.length; i++) {
            ridegslist.add(ICurveToLineString(ridegs[i]));
        }

        //读取plotcrvs
        ICurve[] plotcrvs = IG.layer("southriverplotcrvs").curves(); //getting curves
        Point[] centralpoints = new Point[plotcrvs.length];
        Point[] shortestpoints = new Point[plotcrvs.length];

        WB_Polygon[] plotpolygons = new WB_Polygon[plotcrvs.length];

        for (int i = 0; i < plotcrvs.length; i++) {

            plotpolygons[i] = ICurveToWB_Polygon(plotcrvs[i]);
        }

        List<LineString> newridegs = new ArrayList<LineString>();
        newridegs.addAll(ridegslist);

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

        // 使用IGEO绘制边界
        drawRigesWithIGEO(newridegs, "southrivernewridegs");

        basic.ProjectPaths.save("output/prepared/Rhino data model-south.3dm");
    }
}
