package NSGAII;

import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.SBXCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.doubleproblem.impl.ComposableDoubleProblem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.errorchecking.JMetalException;
import java.io.IOException;
import java.util.List;


public class NSGAIIRunner extends AbstractAlgorithmRunner {
    // Import code

    public static void main(String[] args) throws JMetalException, IOException {


        //问题实例化
        ComposableDoubleProblem problem = new ComposableDoubleProblem()
                .name("UrbanOpenSpaceSelection")
                .addVariable(-20, 20.0)
                .addVariable(-20.0, 20.0)
                .addFunction((x) -> 2.0 + (x[0] - 2.0) *  (x[0] - 2.0) + (x[1] - 1.0) * (x[1] - 1.0))
                .addFunction((x) -> 9.0 * x[0] - (x[1] - 1.0) * (x[1] - 1.0))
                .addConstraint((x) -> 1.0 - (x[0] * x[0] + x[1] * x[1]) / 225.0)
                .addConstraint((x) -> (3.0 * x[1] - x[0]) / 10.0 - 1.0);


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
        int populationSize = 100;
        NSGAII<DoubleSolution> algorithm =
                new NSGAIIBuilder<>(problem, crossover, mutation, populationSize)
                        .setSelectionOperator(selection)
                        .setMaxEvaluations(25000)
                        .build();

        // 运行算法
        AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

        // 获取结果
        List<DoubleSolution> population = algorithm.result();

        // 打印最终解集
        printFinalSolutionSet(population);
    }


}
