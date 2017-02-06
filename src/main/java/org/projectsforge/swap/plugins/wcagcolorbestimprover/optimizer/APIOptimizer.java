package org.projectsforge.swap.plugins.wcagcolorbestimprover.optimizer;

import java.util.concurrent.ThreadLocalRandom;

import org.projectsforge.swap.core.mime.css.property.color.AbstractMutableSRGBColor;
import org.projectsforge.swap.core.mime.css.property.color.dictionary.ArrayBackedColorDictionary;
import org.projectsforge.swap.core.mime.css.property.color.dictionary.ColorDictionary;
import org.projectsforge.utils.meta.api.API;
import org.projectsforge.utils.meta.api.Ant;
import org.projectsforge.utils.meta.api.ClassicalAPIParameters;
import org.projectsforge.utils.meta.api.ClassicalAnt;
import org.projectsforge.utils.meta.api.ExplorationOperator;
import org.projectsforge.utils.meta.api.FixedNestPositionProvider;
import org.projectsforge.utils.meta.api.ScoredModel;
import org.projectsforge.utils.tasksexecutor.RecursiveTaskExecutorException;

public class APIOptimizer {

  class SolutionExplorationOperator implements ExplorationOperator<Solution> {
    @Override
    public ScoredModel<Solution> exploreHuntingSiteFromNest(final API<Solution> api, final Ant<Solution> ant,
        final ScoredModel<Solution> nestPosition) {
      final ClassicalAnt<Solution> classicalAnt = (ClassicalAnt<Solution>) ant;

      final ArrayBackedColorDictionary result = modifyByAmplitude(nestPosition.getModel().getColorDictionary(),
          Math.max(1, (int) Math.round(classicalAnt.getNestAmplitude() * 255)));
      Solution solution = new Solution(result, evaluator.evaluate(result).fitness);
      if (localOptimizer != null) {
        solution = localOptimizer.optimize(nestPosition.getModel(), solution);
      }
      return new ScoredModel<>(solution, solution.getFitness());
    }

    @Override
    public ScoredModel<Solution> explorePositionFromHuntingSite(final API<Solution> api, final Ant<Solution> ant,
        final ScoredModel<Solution> sitePosition) {

      final ClassicalAnt<Solution> classicalAnt = (ClassicalAnt<Solution>) ant;

      final ArrayBackedColorDictionary result = modifyByAmplitude(sitePosition.getModel().getColorDictionary(),
          Math.max(1, (int) Math.round(classicalAnt.getLocalAmplitude() * 255)));
      Solution solution = new Solution(result, evaluator.evaluate(result).fitness);
      if (localOptimizer != null) {
        solution = localOptimizer.optimize(sitePosition.getModel(), solution);
      }
      return new ScoredModel<>(solution, solution.getFitness());
    }

  }

  private final PrioritizedPenalizedEvaluator evaluator;

  private final int maxEvaluation;

  private final LocalOptimizer localOptimizer;

  public APIOptimizer(final PrioritizedPenalizedEvaluator evaluator, final int maxEvaluation,
      final LocalOptimizer localOptimizer) {
    this.evaluator = evaluator;
    this.maxEvaluation = maxEvaluation;
    this.localOptimizer = localOptimizer;
  }

  private ArrayBackedColorDictionary modifyByAmplitude(final ArrayBackedColorDictionary colorDictionary,
      final int amplitude) {
    final ThreadLocalRandom random = ThreadLocalRandom.current();
    final ArrayBackedColorDictionary result = new ArrayBackedColorDictionary(
        (ColorDictionary<AbstractMutableSRGBColor>) colorDictionary);

    final int[] array = result.getColorsAsArray();
    for (int i = 0; i < array.length; ++i) {
      array[i] += random.nextInt(2 * amplitude) - amplitude;
      if (array[i] < 0) {
        array[i] = 0;
      }
      if (array[i] > 255) {
        array[i] = 255;
      }
    }
    /*
     * for (int i = 0; i < result.size(); ++i) {
     * result
     * .getEntry(i)
     * .getColor()
     * .clampedMove((int) Math.round(random.nextDouble() * 2 * amplitude - amplitude),
     * (int) Math.round(random.nextDouble() * 2 * amplitude - amplitude),
     * (int) Math.round(random.nextDouble() * 2 * amplitude - amplitude));
     * }
     */
    return result;
  }

  public Solution optimize(final Solution current) throws RecursiveTaskExecutorException {

    final API<Solution> api = new API<>();

    final ClassicalAPIParameters<Solution> parameters = new ClassicalAPIParameters<>();

    parameters.setLocalPatience(10);
    parameters.setAmplitudeStrategy(1);
    parameters.setMinAmplitude(0.01); // 255*0.01 = 2.55
    parameters.setMaxAmplitude(1);
    parameters.setColonySize(10);
    parameters.setNestPatience(20);
    parameters.setNestToLocalAmplitudeFactor(0.1);
    parameters.setMaxIteration(maxEvaluation / parameters.getColonySize() + 1);
    parameters.setParallelExploration(false);
    parameters.setMaximize(false);
    parameters.setNestPrositionProvider(new FixedNestPositionProvider<Solution>(new ScoredModel<Solution>(current,
        current.getFitness())));
    parameters.setExplorationOperator(new SolutionExplorationOperator());

    api.setParameters(parameters);

    api.run();

    return api.getBestScoredModel().getModel();
  }
}
