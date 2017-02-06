package org.projectsforge.swap.plugins.wcagcolorbestimprover.optimizer;

import org.bouncycastle.util.Arrays;
import org.projectsforge.swap.core.mime.css.property.color.AbstractMutableSRGBColor;
import org.projectsforge.swap.core.mime.css.property.color.dictionary.ArrayBackedColorDictionary;
import org.projectsforge.swap.core.mime.css.property.color.dictionary.ColorDictionary;

public class LineDescentLocalOptimizer implements LocalOptimizer {

  private final PrioritizedPenalizedEvaluator evaluator;

  public LineDescentLocalOptimizer(final PrioritizedPenalizedEvaluator evaluator) {
    this.evaluator = evaluator;
  }

  @Override
  public Solution optimize(final Solution initialSolution, final Solution newSolution) {
    // best things
    final ArrayBackedColorDictionary bestDictionary = new ArrayBackedColorDictionary(
        (ColorDictionary<AbstractMutableSRGBColor>) newSolution.getColorDictionary());
    double bestFitness = newSolution.getFitness();

    // compute the delta vector
    final int[] initialValues = initialSolution.getColorDictionary().getColorsAsArray();
    final int[] newValues = newSolution.getColorDictionary().getColorsAsArray();
    final int[] delta = new int[newValues.length];
    int maxDelta = 0;
    for (int i = 0; i < delta.length; ++i) {
      delta[i] = initialValues[i] - newValues[i];
      maxDelta = Math.max(maxDelta, Math.abs(delta[i]));
    }

    if (maxDelta > 0) {
      // do the line search ensuring there is at least one value modified

      // create temporary variables outside of the loop
      final ArrayBackedColorDictionary candidate = new ArrayBackedColorDictionary(
          (ColorDictionary<AbstractMutableSRGBColor>) newSolution.getColorDictionary());
      final int[] candidateValues = candidate.getColorsAsArray();

      int stepNum = 1;
      boolean again = false;
      do {
        for (int i = 0; i < candidateValues.length; ++i) {
          candidateValues[i] = newValues[i] + delta[i] * stepNum / maxDelta;
          if (candidateValues[i] < 0) {
            candidateValues[i] = 0;
          }
          if (candidateValues[i] > 255) {
            candidateValues[i] = 255;
          }
        }
        final double candidateFitness = evaluator.evaluate(candidate).fitness;

        again = false;

        if (candidateFitness < bestFitness) {
          again = true;
        } else if (candidateFitness == bestFitness) {
          if (!Arrays.areEqual(candidateValues, bestDictionary.getColorsAsArray())) {
            again = true;
          }
        }

        if (again) {
          // improved solution or an equal quality solution but which is different
          bestFitness = candidateFitness;
          bestDictionary.copy(candidate);
          stepNum++;
        }

      } while (again);
    }
    return new Solution(bestDictionary, bestFitness);
  }

}
