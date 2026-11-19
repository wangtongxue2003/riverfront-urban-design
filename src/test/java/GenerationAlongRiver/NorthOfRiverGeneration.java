package GenerationAlongRiver;

import igeo.ICurve;
import igeo.IG;
import igeo.IGeometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
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
import processing.core.PApplet;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import NSGAII.Plots;
import static NSGAII.Plots.*;
import static NSGAII.Plots.deltaHeight;
import static Spacesyntax.dataTransfer.ICurveToLineString;

public class NorthOfRiverGeneration extends PApplet {
    static IGeometry[][] geometries = new IGeometry[2][];  //门窗其他


    public void settings(){size(1300, 900, IG.GL);}

    public void setup(){
      /*  IConfig.syncDrawAndDynamics = true;
        IG.duration(150);
        IG.bg(1.0);
        IG.fillWire();
        IG.start();*/
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
        for(int i = 0; i < ridegs.length; i++){
            rideglist.add(ICurveToLineString(ridegs[i]));
        }

        List<LineString> qinhuairiverstring = new ArrayList<>();
        for(int i = 0; i < qinhuairiver.length; i++){
            qinhuairiverstring.add(ICurveToLineString(qinhuairiver[i]));
        }

        char[] typology = {'A','A','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B','B'};
        float[] floor = {0,0,1,1,1,1.5f,1.5f,1.5f,1.5f,1.5f,1.5f,2,2,2,2,2,2,2,2,2,2};

        org.uma.jmetal.util.pseudorandom.JMetalRandom.getInstance().setSeed(Long.getLong("urban.seed", 42L));

        //问题实例化
        ComposableDoubleProblem problem = new ComposableDoubleProblem()
                .name("UrbanSpaceSelection")
                .addVariable(0, ridegstringlength)
                .addVariable(0, ridegstringlength)
                .addVariable(0, ridegstringlength)
                .addVariable(0, ridegstringlength)
                .addVariable(0, ridegstringlength)
                .addVariable(0, ridegstringlength)
                .addVariable(0, ridegstringlength)
                .addVariable(0, ridegstringlength)
                .addVariable(0, ridegstringlength)
                .addVariable(0, ridegstringlength)
                .addVariable(0, ridegstringlength)
                .addVariable(0, ridegstringlength)
                .addVariable(0, ridegstringlength)
                .addVariable(0, ridegstringlength)
                .addVariable(0, ridegstringlength)
                .addVariable(0, ridegstringlength)
                .addVariable(0, ridegstringlength)
                .addVariable(0, ridegstringlength)
                .addVariable(0, ridegstringlength)
                .addVariable(0, ridegstringlength)
                .addVariable(0, ridegstringlength)

                .addFunction((x) ->
                        (-getSumBuildingArea(boundstring, ridegstring, x, typology, floor, qinhuairiverstring))) // 建筑面积
                .addFunction((x) ->
                        (getAverDist(boundstring, ridegstring, x, typology, floor, qinhuairiverstring, plotcrvs, rideglist)))  //平均距离
                .addFunction((x) ->
                        -deltaHeight(boundstring, ridegstring, x, typology, floor, qinhuairiverstring))
                .addFunction((x) ->
                        siteblength(boundstring, ridegstring, x, typology, floor, qinhuairiverstring));

        // 交叉操作配置
        double crossoverProbability = 0.9;
        double crossoverDistributionIndex = 20.0;
        CrossoverOperator<DoubleSolution> crossover = new SBXCrossover(crossoverProbability,
                crossoverDistributionIndex);

        // 变异操作配置
        double mutationProbability = 2.0 / problem.numberOfVariables();
        double mutationDistributionIndex = 20.0;
        MutationOperator<DoubleSolution> mutation = new PolynomialMutation(mutationProbability,
                mutationDistributionIndex);

        // 选择操作配置
        SelectionOperator<List<DoubleSolution>, DoubleSolution> selection = new BinaryTournamentSelection<>(
                new RankingAndCrowdingDistanceComparator<>());

        SolutionListEvaluator<DoubleSolution> evaluator = new MultiThreadedSolutionListEvaluator<DoubleSolution>(Math.max(1, Integer.getInteger("urban.threads", 4)));
        int populationSize = Math.max(4, Integer.getInteger("urban.population", 32));
        NSGAII<DoubleSolution> builder =
                new NSGAIIBuilder<>(problem, crossover, mutation, populationSize)
                        .setSelectionOperator(selection)
                        .setMaxEvaluations(Math.max(populationSize, Integer.getInteger("urban.evaluations", 3200)))
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

        List<Double[]> solutionvariables = new ArrayList<>();
        for (DoubleSolution solution : population) {
            solutionvariables.add(solution.variables().toArray(new Double[solution.variables().size()]));
        }

        // Keep the same order as population/CSV. iGeo scene creation is intentionally serial.
        List<Plots> plotstodraw = new ArrayList<>();
        for (Double[] variables : solutionvariables) {
            plotstodraw.add(getsortedPlots(boundstring, ridegstring, variables, typology, floor, qinhuairiverstring));
        }

        int i = 0;
        for (Plots plots : plotstodraw) {
            plots.DecideTypology("Solution_"+i+"_", geometries);
            i++;
        }
        // 创建CSV文件写入器
        try (FileWriter writer = new FileWriter(basic.ProjectPaths.outputPath("output/NorthGenerationData-1.csv"))) {
            writer.append("solution_id,estimated_floor_area,open_space_area,mean_network_distance,adjacent_floor_difference,width_penalty");
            for (int v = 0; v < problem.numberOfVariables(); v++) writer.append(",position_" + v);
            writer.append("\n");
            for (int count = 0; count < population.size(); count++) {
                DoubleSolution solution = population.get(count);
                Double[] variables = solutionvariables.get(count);
                double openSpaceArea = plotstodraw.get(count).getAArea();
                double widthPenalty = siteblength(boundstring, ridegstring, variables, typology, floor, qinhuairiverstring);
                writer.append("Solution_" + count + "," + (-solution.objectives()[0]) + "," + openSpaceArea
                        + "," + solution.objectives()[1] + "," + (-solution.objectives()[2]) + "," + widthPenalty);
                for (Double variable : variables) writer.append("," + variable);
                writer.append("\n");
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        basic.ProjectPaths.save("output/NorthGeneration-1.3dm");
    }

    public static void main(String[] args) {
        PApplet.main("GenerationAlongRiver.NorthOfRiverGeneration");


    }
}
