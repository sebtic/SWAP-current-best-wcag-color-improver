package org.projectsforge.swap.plugins.wcagcolorbestimprover;

import org.projectsforge.swap.core.mime.css.property.color.W3CUtil;
import org.projectsforge.swap.core.mime.css.property.color.dictionary.SimpleColorDictionary.RuleSetManagement;
import org.projectsforge.swap.core.mime.css.property.color.dictionary.SimpleColorDictionary.TypeManagement;
import org.projectsforge.utils.propertyregistry.DoubleProperty;
import org.projectsforge.utils.propertyregistry.EnumProperty;
import org.projectsforge.utils.propertyregistry.PropertyHolder;

public class WCAGColorBestImproverPropertyHolder implements PropertyHolder {

  /** The brightness difference threshold. */
  public static final DoubleProperty brightnessDifferenceThreshold = new DoubleProperty(
      "org.projectsforge.swap.plugins.wcagcolorbestimprover.WCAGColorBestImproverPropertyHolder.brightnessDifferenceThreshold",
      W3CUtil.WCAG1_BRIGHTNESSDIFFERENCE);

  /** The tonality difference threshold. */
  public static final DoubleProperty tonalityDifferenceThreshold = new DoubleProperty(
      "org.projectsforge.swap.plugins.wcagcolorbestimprover.WCAGColorBestImproverPropertyHolder.tonalityDifferenceThreshold",
      W3CUtil.WCAG1_TONALITYDIFFERENCE);

  /** The contrast ratio threshold. */
  public static final DoubleProperty contrastRatioThreshold = new DoubleProperty(
      "org.projectsforge.swap.plugins.wcagcolorbestimprover.WCAGColorBestImproverPropertyHolder.contrastRatioThreshold",
      W3CUtil.WCAG2_LEVELAAA_CONTRASTRATIO);

  /** The brightness difference weight. */
  public static final DoubleProperty brightnessDifferenceWeight = new DoubleProperty(
      "org.projectsforge.swap.plugins.wcagcolorbestimprover.WCAGColorBestImproverPropertyHolder.brightnessDifferenceWeight",
      1.);

  /** The contrast ratio weight. */
  public static final DoubleProperty contrastRatioWeight = new DoubleProperty(
      "org.projectsforge.swap.plugins.wcagcolorbestimprover.WCAGColorBestImproverPropertyHolder.contrastRatioWeight",
      1.);

  /** The tonality difference weight. */
  public static final DoubleProperty tonalityDifferenceWeight = new DoubleProperty(
      "org.projectsforge.swap.plugins.wcagcolorbestimprover.WCAGColorBestImproverPropertyHolder.tonalityDifferenceWeight",
      1.);

  public static final EnumProperty<TypeManagement> typeManagement = new EnumProperty<>(
      "org.projectsforge.swap.plugins.wcagcolorbestimprover.WCAGColorBestImproverPropertyHolder.typeManagement",
      TypeManagement.class, TypeManagement.DIFFERENTIATE_FG_AND_BG);

  public static final EnumProperty<RuleSetManagement> ruleSetManagement = new EnumProperty<>(
      "org.projectsforge.swap.plugins.wcagcolorbestimprover.WCAGColorBestImproverPropertyHolder.ruleSetManagement",
      RuleSetManagement.class, RuleSetManagement.MERGED_RULES);

}
