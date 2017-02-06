/**
 * Copyright 2010 Sébastien Aupetit <sebastien.aupetit@univ-tours.fr> This file
 * is part of SWAP. SWAP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version. SWAP is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with SWAP. If not, see
 * <http://www.gnu.org/licenses/>. $Id$
 */
package org.projectsforge.swap.plugins.wcagcolorbestimprover.optimizer;

import org.projectsforge.swap.core.mime.css.property.color.SRGBColor;
import org.projectsforge.swap.core.mime.css.property.color.W3CUtil;
import org.projectsforge.swap.core.mime.css.property.color.dictionary.ArrayBackedColorDictionary;
import org.projectsforge.swap.core.mime.css.property.color.dictionary.ColorDictionary;
import org.projectsforge.swap.core.mime.css.property.color.dictionary.FgBgEntry;
import org.projectsforge.swap.core.mime.css.property.color.dictionary.FgBgRegistry;

/**
 * The Class PrioritizedPenalizedEvaluator.
 */
public class PrioritizedPenalizedEvaluator {

  private static double satisfaction(final double value, final double threshold) {
    if (value < threshold) {
      return 1;
    } else {
      return 0;
    }
  }

  private static double scaleContribution(final double value, final double threshold, final double min) {
    if (threshold <= value) {
      // threshold attained
      return 0;
    } else {
      // threshold not attained, scale error on [0:1]
      return (threshold - value) / (threshold - min);
    }
  }

  /** The brightness difference threshold. */
  private double brightnessDifferenceThreshold;

  /** The tonality difference threshold. */
  private double tonalityDifferenceThreshold;

  /** The contrast ratio threshold. */
  public double contrastRatioThreshold;

  /** The brightness difference weight. */
  public double brightnessDifferenceWeight;

  /** The contrast ratio weight. */
  public double contrastRatioWeight;

  /** The tonality difference weight. */
  public double tonalityDifferenceWeight;

  /** The color dictionary. */
  private final ArrayBackedColorDictionary colorDictionary;

  /** The fg bg registry. */
  private final FgBgRegistry fgBgRegistry;

  private static final double maxDistanceLAB = 100 * Math.sqrt(3);

  /**
   * The Constructor.
   * 
   * @param statistics
   */
  public PrioritizedPenalizedEvaluator(final ArrayBackedColorDictionary colorDictionary, final FgBgRegistry fgBgRegistry) {
    this.colorDictionary = colorDictionary;
    this.fgBgRegistry = fgBgRegistry;
  }

  public final double deltaE(final SRGBColor c1, final SRGBColor c2) {
    return c1.distanceLABInCIELab(c2) / PrioritizedPenalizedEvaluator.maxDistanceLAB;
  }

  /**
   * Evaluate.
   * 
   * @param currentColorDictionary
   *          the color dictionary
   * @return the double
   */
  public <TColor extends SRGBColor> Evaluation evaluate(final ColorDictionary<TColor> currentColorDictionary) {
    final Evaluation evaluation = new Evaluation();
    double weightSum = 0;

    double brightnessDifferenceSum = 0;
    double tonalityDifferenceSum = 0;
    double contrastRatioSum = 0;
    double colorChangeSum = 0;

    double brightnessSatisfactionCount = 0;
    double tonalityDifferenceSatisfactionCount = 0;
    double contrastRatioSatisfactionCountAA = 0;
    double contrastRatioSatisfactionCountAAA = 0;

    for (final FgBgEntry entry : fgBgRegistry.getCombinedComponent().getEntries()) {
      final SRGBColor initialForeground = entry.getForegroundColor(colorDictionary);
      final SRGBColor initialBackground = entry.getBackgroundColor(colorDictionary);
      final SRGBColor currentForeground = entry.getForegroundColor(currentColorDictionary);
      final SRGBColor currentBackground = entry.getBackgroundColor(currentColorDictionary);
      final double weight = entry.getWeight();

      weightSum += weight;

      // **** brightness ****
      final double brightnessDifference = W3CUtil.brightnessDifference(currentForeground, currentBackground);

      brightnessDifferenceSum += PrioritizedPenalizedEvaluator.scaleContribution(brightnessDifference,
          brightnessDifferenceThreshold, W3CUtil.BRIGHTNESSDIFFERENCE_MINVALUE) * weight;

      brightnessSatisfactionCount += PrioritizedPenalizedEvaluator.satisfaction(brightnessDifference,
          brightnessDifferenceThreshold) * weight;

      // **** tonality ****
      final double tonalityDifference = W3CUtil.tonalityDifference(currentForeground, currentBackground);

      tonalityDifferenceSum += PrioritizedPenalizedEvaluator.scaleContribution(tonalityDifference,
          tonalityDifferenceThreshold, W3CUtil.TONALITYDIFFERENCE_MINVALUE) * weight;

      tonalityDifferenceSatisfactionCount += PrioritizedPenalizedEvaluator.satisfaction(tonalityDifference,
          tonalityDifferenceThreshold) * weight;

      // **** contrast ratio ****
      final double contrastRatio = W3CUtil.contrastRatio(currentForeground, currentBackground);

      contrastRatioSum += PrioritizedPenalizedEvaluator.scaleContribution(contrastRatio, contrastRatioThreshold,
          W3CUtil.CONTRASTRATIO_MINVALUE) * weight;

      contrastRatioSatisfactionCountAA += PrioritizedPenalizedEvaluator.satisfaction(contrastRatio,
          W3CUtil.WCAG2_LEVELAA_CONTRASTRATIO) * weight;
      contrastRatioSatisfactionCountAAA += PrioritizedPenalizedEvaluator.satisfaction(contrastRatio,
          W3CUtil.WCAG2_LEVELAAA_CONTRASTRATIO) * weight;

      colorChangeSum += (deltaE(initialForeground, currentForeground) + deltaE(initialBackground, currentBackground))
          * weight / 2.;

    }

    if (weightSum > 0) {
      brightnessDifferenceSum /= weightSum;
      brightnessSatisfactionCount /= weightSum;
      tonalityDifferenceSum /= weightSum;
      tonalityDifferenceSatisfactionCount /= weightSum;
      contrastRatioSum /= weightSum;
      contrastRatioSatisfactionCountAA /= weightSum;
      contrastRatioSatisfactionCountAAA /= weightSum;
      colorChangeSum /= weightSum;
    }

    final double violatedConstraints = (brightnessDifferenceWeight * brightnessDifferenceSum + tonalityDifferenceWeight
        * tonalityDifferenceSum + contrastRatioWeight * contrastRatioSum)
        / (brightnessDifferenceWeight + tonalityDifferenceWeight + contrastRatioWeight);

    final double scale = 10000;

    if (colorChangeSum < 0 || colorChangeSum > 1) {
      System.err.println("invalid domain for " + colorChangeSum);
    }

    evaluation.fitness = Math.floor(violatedConstraints * scale) + colorChangeSum;
    evaluation.brightnessSatisfaction = brightnessSatisfactionCount;
    evaluation.tonalityDifferenceSatisfaction = tonalityDifferenceSatisfactionCount;
    evaluation.contrastRatioSatisfactionCountAA = contrastRatioSatisfactionCountAA;
    evaluation.contrastRatioSatisfactionCountAAA = contrastRatioSatisfactionCountAAA;

    return evaluation;
  }

  public void setBrightnessDifferenceThreshold(final double brightnessDifferenceThreshold) {
    this.brightnessDifferenceThreshold = brightnessDifferenceThreshold;
  }

  public void setBrightnessDifferenceWeight(final double brightnessDifferenceWeight) {
    this.brightnessDifferenceWeight = brightnessDifferenceWeight;
  }

  public void setContrastRatioThreshold(final double contrastRatioThreshold) {
    this.contrastRatioThreshold = contrastRatioThreshold;
  }

  public void setContrastRatioWeight(final double contrastRatioWeight) {
    this.contrastRatioWeight = contrastRatioWeight;
  }

  public void setTonalityDifferenceThreshold(final double tonalityDifferenceThreshold) {
    this.tonalityDifferenceThreshold = tonalityDifferenceThreshold;
  }

  public void setTonalityDifferenceWeight(final double tonalityDifferenceWeight) {
    this.tonalityDifferenceWeight = tonalityDifferenceWeight;
  }
}
