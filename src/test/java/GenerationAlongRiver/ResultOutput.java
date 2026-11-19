package GenerationAlongRiver;

import NSGAII.Plots;
import igeo.ICurve;
import igeo.IG;
import igeo.IGeometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;

import static Spacesyntax.dataTransfer.ICurveToLineString;

public class ResultOutput extends PApplet {
    static IGeometry[][] geometries = new IGeometry[2][];  //门窗其他
    public void settings(){size(1300, 900, IG.GL);}

    public void setup() {
      /*  IConfig.syncDrawAndDynamics = true;
        IG.duration(150);
        IG.bg(1.0);
        IG.fillWire();
        IG.start();*/

    }

    //用于查看单个结果的生成结果

    public static void main(String[] args) {
        PApplet.main("GenerationAlongRiver.ResultOutput");
        basic.ProjectPaths.open("assets/components/door-and-window.3dm");
        geometries[0] = IG.layer("door").geometries();  //门
        geometries[1] = IG.layer("window").geometries();  //窗
        IG.delAll();

        GeometryFactory gf = new GeometryFactory();
        basic.ProjectPaths.open(basic.ProjectPaths.prepared("Rhino data model-north.3dm"));

        ICurve[] bound = IG.layer("northriverbound").curves();
        ICurve[] riverrideg = IG.layer("northriverrideg").curves();
        ICurve[] ridegs = IG.layer("northrivernewridegs").curves();
        ICurve[] plotcrvs = IG.layer("northriverplotcrvs").curves();
        ICurve[] qinhuairiver = IG.layer("qinhuairiver").curves();

        LineString ridegstring = ICurveToLineString(riverrideg[0]);
        LineString boundstring = ICurveToLineString(bound[0]);
        List<LineString> rideglist = new ArrayList<LineString>();
        double ridegstringlength = ridegstring.getLength();
        for (int i = 0; i < ridegs.length; i++) {
            rideglist.add(ICurveToLineString(ridegs[i]));
        }

        List<LineString> qinhuairiverstring = new ArrayList<>();
        for (int i = 0; i < qinhuairiver.length; i++) {
            qinhuairiverstring.add(ICurveToLineString(qinhuairiver[i]));
        }

        Double[] locs = {232.6643473185875,	103.83652230128676,	142.7442836290636,	124.60188272601992,	291.68292733648497,	174.81555831006406,	99.60533214387262,	198.84896477136817,	224.4124790471276,	131.96391393861262,	127.4664658412481,	66.68183835925711,	224.30428701575383,	278.1915367162129,	99.2091601042457,	96.13708718953565,	124.39678200145255	,128.82848630482042,	277.1968943202805,	164.60912287490424	,84.385965695124
        };
        char[] typology = {'A', 'A', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B'};
        float[] floor = {0, 0, 1, 1, 1, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};

        Plots plots = Plots.getsortedPlots(boundstring,ridegstring,locs,typology,floor,qinhuairiverstring);
        plots.DecideTypology("test", geometries);

    }
}
