package NSGAII;

import Spacesyntax.Spacesyntax_00;
import igeo.IConfig;
import igeo.ICurve;
import igeo.IG;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.SBXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.doubleproblem.impl.ComposableDoubleProblem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.MultiThreadedSolutionListEvaluator;
import peasy.PeasyCam;
import processing.core.PApplet;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static Spacesyntax.Spacesyntax_00.*;
import static Spacesyntax.Spacesyntax_00.findShortestPath;
import static Spacesyntax.dataTransfer.*;

public class test01 extends PApplet {

    private static final double OPEN_EDGE = 0.5;  // 开敞度计算密度
    double minr = 5;    //地块最小半径
    double maxr = 20;   //地块最大半径

    List<double[]> objectiveValues = new ArrayList<>();

    public void settings(){
        IConfig.syncDrawAndDynamics = true;
        size(1300, 900, IG.GL);
        IG.duration(150);
    }

    public static void main(String[] args) {
        PApplet.main("NSGAII.test01");
    }

    public void setup() {
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
        double ridegstringlength = ridegstring.getLength();

        List<LineString> qinhuairiverstring = new ArrayList<>();
        for(int i = 0; i < qinhuairiver.length; i++){
            qinhuairiverstring.add(ICurveToLineString(qinhuairiver[i]));
        }

        //问题实例化
        ComposableDoubleProblem problem = new ComposableDoubleProblem()
                .name("UrbanOpenSpaceSelection")
                .addVariable(maxr, ridegstringlength-maxr)
                .addVariable(maxr, ridegstringlength-maxr)
                .addVariable(maxr, ridegstringlength-maxr)
                .addVariable(maxr, ridegstringlength-maxr)
                .addVariable(minr, maxr)
                .addVariable(minr, maxr)
                .addVariable(minr, maxr)
                .addVariable(minr, maxr)

                .addFunction((x) ->
                        -parallelOpennessFunction(
                                calculateplots(x, boundstring, ridegstring, qinhuairiverstring))) // 开敞度
                .addFunction((x) ->
                        parallelAverageDistanceFunction(
                                calculateplots(x, boundstring, ridegstring, qinhuairiverstring), plotcrvs, rideglist)) // 平均距离
                .addFunction((x) ->
                        parallelPlotAreaFunction(
                                calculateplots(x, boundstring, ridegstring, qinhuairiverstring))); // 地块面积
        /*      .addConstraint((x) -> 1.0 - (x[0] * x[0] + x[1] * x[1]) / 225.0)
                .addConstraint((x) -> (3.0 * x[1] - x[0]) / 10.0 - 1.0);*/


        // 交叉操作配置
        double crossoverProbability = 0.9;
        double crossoverDistributionIndex = 20.0;
        CrossoverOperator<DoubleSolution> crossover = new SBXCrossover(crossoverProbability,
                crossoverDistributionIndex);

        // 变异操作配置
        double mutationProbability = 1.0 / problem.numberOfVariables();
        double mutationDistributionIndex = 20.0;
        MutationOperator<DoubleSolution> mutation = new PolynomialMutation(mutationProbability,
                mutationDistributionIndex);

        // 选择操作配置
        SelectionOperator<List<DoubleSolution>, DoubleSolution> selection = new BinaryTournamentSelection<>(
                new RankingAndCrowdingDistanceComparator<>());

        // NSGAII算法配置
    /*    int populationSize = 6;
        NSGAII<DoubleSolution> algorithm =
                new NSGAIIBuilder<>(problem, crossover, mutation, populationSize)
                        .setSelectionOperator(selection)
                        .setMaxEvaluations(10)
                        .build();

        // 运行算法
        AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

        // 获取结果
        List<DoubleSolution> population = algorithm.result();*/

        SolutionListEvaluator<DoubleSolution> evaluator = new MultiThreadedSolutionListEvaluator<DoubleSolution>(16);
        int populationSize = 50;
        NSGAII<DoubleSolution> builder =
                new NSGAIIBuilder<>(problem, crossover, mutation, populationSize)
                        .setSelectionOperator(selection)
                        .setMaxEvaluations(50000)
                        .setSolutionListEvaluator(evaluator)
                        .build();

        // 运行算法
        try {
            new AlgorithmRunner.Executor(builder).execute();
        } finally {
            evaluator.shutdown();
        }

        // 获取结果
        List<DoubleSolution> population = builder.getPopulation();


     /*   SparkConf sparkConf = new SparkConf()
                .setMaster("local[8]") // 8 cores
                .setAppName("NSGA-II with Spark");

        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        SolutionListEvaluator<DoubleSolution> evaluator = new SparkSolutionListEvaluator<>(sparkContext) ;

        algorithm = new NSGAIIBuilder<>(problem, crossover, mutation, populationSize)
                .setSelectionOperator(selection)
                .setMaxEvaluations(maxEvaluations)
                .setSolutionListEvaluator(new SparkSolutionListEvaluator<>(sparkContext))
                .build();

        algorithm.run();
        List<DoubleSolution> population = algorithm.getResult();

        evaluator.shutdown();

*/

        List<Double[]> solutionvariables = new ArrayList<>();
        for (DoubleSolution solution : population) {
            solutionvariables.add(solution.variables().toArray(new Double[solution.variables().size()]));
        }

        for(int i = 0; i < solutionvariables.size(); i++){
            List<Plot> plotstodraw = calculateplots(solutionvariables.get(i), boundstring, ridegstring, qinhuairiverstring);
            for (Plot plot : plotstodraw) {
                drawWB_Polygonwithlayer(plot.getWB_Polygon(), "Solution_" + i + "openplot_" + i);
                plot.setOpenEdgewithlayer(OPEN_EDGE, "Solution_" + i + "openness_" + i);
            }
            plotVisiable(plotstodraw, plotcrvs, rideglist, "Solution_" + i + "liveplot_" + i);
        }

        // 创建CSV文件写入器
        try (FileWriter writer = new FileWriter(basic.ProjectPaths.outputPath("output/output_data-1.csv"))) {
            // 写入CSV文件头
            writer.append("Openness,AverageDistance,Area\n");

            // 将每个解的目标函数值写入文件
            for (DoubleSolution solution : population) {
                double openness = parallelOpennessFunction(calculateplots(solution.variables().toArray(new Double[0]), boundstring, ridegstring, qinhuairiverstring));
                double avgDistance = parallelAverageDistanceFunction(calculateplots(solution.variables().toArray(new Double[0]), boundstring, ridegstring, qinhuairiverstring), plotcrvs, rideglist);
                double area = parallelPlotAreaFunction(calculateplots(solution.variables().toArray(new Double[0]), boundstring, ridegstring, qinhuairiverstring));

                // 写入目标函数值
                writer.append(openness + "," + avgDistance + "," + area + "\n");
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }

        basic.ProjectPaths.save("output/1216 plot_test_output-1.3dm");


    }

    public void plotVisiable(List<Plot> plots, ICurve[] plotcrvs, List<LineString> ridegs, String layer){
        double[] minidist = getPlotsDistances(plots,plotcrvs,ridegs);
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

        WB_Polygon[] plotpolygons = new WB_Polygon[plotcrvs.length];
        for (int i = 0; i < plotcrvs.length; i++) {
            plotpolygons[i] = ICurveToWB_Polygon(plotcrvs[i]);
        }

        for (int j = 0; j < plotcrvs.length; j++) {
            drawWB_Polygonwithclr(plotpolygons[j], (minidist[j]-min_minidist)/(max_minidist-min_minidist), (max_minidist-minidist[j])/(max_minidist-min_minidist), 0, layer);
        }

    }

    //计算plots
    private List<Plot> calculateplots(Double[] variables, LineString boundary, LineString rideg, List<LineString> ridegs){
        List<Plot> plots = new ArrayList<>();
        int maxplotvar = variables.length / 2;
        IndexValue[] plotvars = new IndexValue[maxplotvar];
        for (int i = 0; i < maxplotvar; i++) {
            plotvars[i] = new IndexValue(variables[i], i);
        }
        Arrays.sort(plotvars, Comparator.comparingDouble(IndexValue::getValue));

        double x1 = plotvars[0].value-variables[plotvars[0].index+maxplotvar];  //初始定义plot左端点x1
        double x2;
        for (int i = 0; i < maxplotvar-1; i++) {
            if(plotvars[i].value+variables[plotvars[i].index+maxplotvar] < plotvars[i+1].value-variables[plotvars[i].index+maxplotvar]){
                x2 = plotvars[i].value + variables[plotvars[i].index+maxplotvar];  //当前，不合并
                plots.add(new Plot(boundary, rideg, x1, x2, ridegs, maxplotvar));   //添加新的plot
                x1 = plotvars[i+1].value-variables[plotvars[i+1].index+maxplotvar];  //修改x1为下一个plot的左端点
            }
            else {
                x2 = plotvars[i+1].value + variables[plotvars[i+1].index+maxplotvar];    //改x2为下一个plot的右端点
            }
        }
        plots.add(new Plot(boundary, rideg, x1, plotvars[maxplotvar-1].value + variables[plotvars[maxplotvar-1].index+maxplotvar], ridegs, maxplotvar));

        return plots;
    }

    // 并行化的目标函数计算：开敞度
    private double parallelOpennessFunction(List<Plot> plots) {
       /* return CompletableFuture.supplyAsync(() -> getOpenness(plots, OPEN_EDGE), executorService)
                .join();  // 使用 CompletableFuture 并避免阻塞*/
        return getOpenness(plots, OPEN_EDGE);
    }

    // 并行化的目标函数计算：平均距离
    public static double parallelAverageDistanceFunction(List<Plot> plots, ICurve[] plotcrvs, List<LineString> ridegs) {
        System.out.println("averdistance" + averdistance(plots, plotcrvs, ridegs));
        return averdistance(plots, plotcrvs, ridegs);
    }

    // 并行化的目标函数计算：地块面积
    private double parallelPlotAreaFunction(List<Plot> plots) {
        double areasum = 0.0;
        for (Plot plot : plots) {
            areasum += plot.plotarea();
        }
        return areasum;
    }
    // 获取开敞度
    public static double getOpenness(List<Plot> plots, double distance) {
        double openness = 0.0;
        for (Plot plot : plots) {
            openness += plot.calculateOpenness(distance)*plot.plotarea();
        }
        return openness;
    }

    public static double[] getPlotsDistances(List<Plot> plots, ICurve[] plotcrvs, List<LineString> ridegs) {
        List<LineString> newridegs = new ArrayList<>();
        newridegs.addAll(ridegs);
        System.out.println("newridegsLength0" + newridegs.size());

        List<Coordinate> plotsnewcoords = new ArrayList<>();

        GeometryFactory gf = new GeometryFactory();
        for (Plot plot : plots) {
            Coordinate[] coordinates = plot.getPlotpolygon().getCoordinates();
            int pointindex = plot.getopenEdgeindex(ridegs);
            Point[] thispoints = new Point[2];
            thispoints[0] = gf.createPoint(new Coordinate(coordinates[(pointindex+2)%4].x,coordinates[(pointindex+2)%4].y,0));
            thispoints[1] = gf.createPoint(new Coordinate(coordinates[(pointindex+3)%4].x,coordinates[(pointindex+3)%4].y,0));
            plotsnewcoords.add(new Coordinate(coordinates[(pointindex+2)%4].x,coordinates[(pointindex+2)%4].y,0));
            plotsnewcoords.add(new Coordinate(coordinates[(pointindex+3)%4].x,coordinates[(pointindex+3)%4].y,0));

            Point thispoint0 = gf.createPoint(getshortestDistancepoint(thispoints[0],newridegs));
            Point thispoint1 = gf.createPoint(getshortestDistancepoint(thispoints[1],newridegs));
            Coordinate[] newline0 = new Coordinate[]{
                    new Coordinate(thispoint0.getX(), thispoint0.getY(), 0),
                    new Coordinate(thispoints[0].getX(), thispoints[0].getY(), 0)
            };
            Coordinate[] newline1 = new Coordinate[]{
                    new Coordinate(thispoint1.getX(), thispoint1.getY(), 0),
                    new Coordinate(thispoints[1].getX(), thispoints[1].getY(), 0)
            };
            LineString newlineString0 = gf.createLineString(newline0);
            LineString newlineString1 = gf.createLineString(newline1);
            newridegs.add(newlineString0);
            newridegs.add(newlineString1);
        }
        System.out.println("newridegsLength2" + newridegs.size());
        newridegs = cuttedLineStrings(newridegs);
        System.out.println("newridegsLength3" + newridegs.size());

        Map<Coordinate, List<Spacesyntax_00.Edge>> graph = buildGraphWithIntersections(newridegs);


        double[] minidist = new double[plotcrvs.length];
        Coordinate[] plotcoords = new Coordinate[plotcrvs.length];
        WB_Polygon[] plotpolygons = new WB_Polygon[plotcrvs.length];
        for (int i = 0; i < plotcrvs.length; i++) {
            plotpolygons[i] = ICurveToWB_Polygon(plotcrvs[i]);
        }
        for(int i = 0; i < plotcrvs.length; i++) {
            WB_Point p = plotpolygons[i].getCenter();
            plotcoords[i] = new Coordinate(p.xd(),p.yd(), 0);
            double minidistance = findShortestPath(graph, plotsnewcoords.get(0), plotcoords[i]);
            for (int j = 0; j < plotsnewcoords.size(); j++) {
                double distance = findShortestPath(graph, plotsnewcoords.get(j), plotcoords[i]);
                if (distance < minidistance) {minidistance = distance;}
            }
            minidist[i] = minidistance;
        }
        System.out.println("minidist: " + Arrays.toString(minidist));
        return minidist;
    }

    // 获取平均距离
    public static double averdistance(List<Plot> plots, ICurve[] plotcrvs, List<LineString> ridegs) {

        double[] minidist = getPlotsDistances(plots, plotcrvs, ridegs);
        int length = minidist.length;
        double sum = 0;
        for (int i = 0; i < length; i++) {
            sum += minidist[i];
        }
        return sum/length;
    }

    // 自定义类用于保存元素值和索引
    static class IndexValue {
        private final double value;
        private final int index;

        public IndexValue(double value, int index) {
            this.value = value;
            this.index = index;
        }

        public double getValue() {return value;}
        public int getIndex() {return index;}
    }
}