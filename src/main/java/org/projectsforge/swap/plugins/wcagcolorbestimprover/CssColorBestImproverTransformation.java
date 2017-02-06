package org.projectsforge.swap.plugins.wcagcolorbestimprover;

import java.util.ArrayList;
import java.util.List;

import org.projectsforge.swap.core.environment.Environment;
import org.projectsforge.swap.core.handlers.Handler;
import org.projectsforge.swap.core.handlers.HandlerContext;
import org.projectsforge.swap.core.handlers.Resource;
import org.projectsforge.swap.core.http.Response;
import org.projectsforge.swap.core.mime.css.GlobalStyleSheets;
import org.projectsforge.swap.core.mime.css.nodes.Media;
import org.projectsforge.swap.core.mime.css.property.color.ImmutableSRGBColor;
import org.projectsforge.swap.core.mime.css.property.color.dictionary.ArrayBackedColorDictionary;
import org.projectsforge.swap.core.mime.css.property.color.dictionary.FgBgRegistry;
import org.projectsforge.swap.core.mime.css.property.color.dictionary.SimpleColorDictionary;
import org.projectsforge.swap.core.mime.css.resolver.PropertyResolver;
import org.projectsforge.swap.core.mime.css.resolver.RuleSetProcessor;
import org.projectsforge.swap.core.mime.css.resolver.StateRecorder;
import org.projectsforge.swap.core.mime.css.resolver.color.BackgroundColorRuleSetProcessor;
import org.projectsforge.swap.core.mime.css.resolver.color.ColorDOMUpdater;
import org.projectsforge.swap.core.mime.css.resolver.color.ColorRuleSetProcessor;
import org.projectsforge.swap.core.mime.css.resolver.color.ColorStateRecorder;
import org.projectsforge.swap.core.mime.html.nodes.Document;
import org.projectsforge.swap.handlers.html.HtmlDomTransformation;
import org.projectsforge.swap.handlers.html.HtmlTransformation;
import org.projectsforge.swap.handlers.mime.StatisticsCollector;
import org.projectsforge.swap.plugins.wcagcolorbestimprover.optimizer.APIOptimizer;
import org.projectsforge.swap.plugins.wcagcolorbestimprover.optimizer.LineDescentLocalOptimizer;
import org.projectsforge.swap.plugins.wcagcolorbestimprover.optimizer.PrioritizedPenalizedEvaluator;
import org.projectsforge.swap.plugins.wcagcolorbestimprover.optimizer.Solution;
import org.springframework.beans.factory.annotation.Autowired;

@Handler(singleton = true)
public class CssColorBestImproverTransformation extends HtmlDomTransformation {

  @Autowired
  Environment environment;

  @Autowired
  private GlobalStyleSheets globalStyleSheets;

  @Autowired
  private ColorDOMUpdater colorDOMUpdater;

  private static final int maxEvaluation = 5000;

  @Override
  public boolean transform(final HandlerContext<HtmlTransformation> context,
      final StatisticsCollector statisticsCollector, final Response response, final Resource<Document> document)
      throws Exception {

    final SimpleColorDictionary<ImmutableSRGBColor> simpleColorDictionary = new SimpleColorDictionary<>(
        ImmutableSRGBColor.class, WCAGColorBestImproverPropertyHolder.typeManagement.get(),
        WCAGColorBestImproverPropertyHolder.ruleSetManagement.get());
    final FgBgRegistry registry = new FgBgRegistry();

    final List<RuleSetProcessor> ruleSetProcessors = new ArrayList<>();
    ruleSetProcessors.add(new ColorRuleSetProcessor());
    ruleSetProcessors.add(new BackgroundColorRuleSetProcessor());

    final List<StateRecorder> stateRecorders = new ArrayList<>();
    stateRecorders.add(new ColorStateRecorder<>(simpleColorDictionary, registry));

    document.lockRead();
    try {
      final PropertyResolver propertyResolver = environment.autowireBean(new PropertyResolver(document.get(), response
          .getRequest(), Media.SCREEN, globalStyleSheets.getUserAgentStylesheet(), globalStyleSheets
          .getUserStylesheet(), ruleSetProcessors, stateRecorders));

      propertyResolver.resolve();

      final ArrayBackedColorDictionary initialColorDictionary = new ArrayBackedColorDictionary(simpleColorDictionary);

      final PrioritizedPenalizedEvaluator evaluator = new PrioritizedPenalizedEvaluator(initialColorDictionary,
          registry);
      evaluator.setBrightnessDifferenceThreshold(WCAGColorBestImproverPropertyHolder.brightnessDifferenceThreshold
          .get());
      evaluator.setBrightnessDifferenceWeight(WCAGColorBestImproverPropertyHolder.brightnessDifferenceWeight.get());
      evaluator.setContrastRatioThreshold(WCAGColorBestImproverPropertyHolder.contrastRatioThreshold.get());
      evaluator.setContrastRatioWeight(WCAGColorBestImproverPropertyHolder.contrastRatioWeight.get());
      evaluator.setTonalityDifferenceThreshold(WCAGColorBestImproverPropertyHolder.tonalityDifferenceThreshold.get());
      evaluator.setTonalityDifferenceWeight(WCAGColorBestImproverPropertyHolder.tonalityDifferenceWeight.get());

      System.err.println("#component " + registry.getComponents().size());

      final APIOptimizer optimizer = new APIOptimizer(evaluator, CssColorBestImproverTransformation.maxEvaluation,
          new LineDescentLocalOptimizer(evaluator));
      final double initialFitness = evaluator.evaluate(initialColorDictionary).fitness;
      Solution bestOfTheBest = null;
      for (int i = 0; i < 1; ++i) {
        final Solution best = optimizer.optimize(new Solution(initialColorDictionary, initialFitness));
        // System.err.println("" + initialFitness + " => " + best.getFitness());
        if (bestOfTheBest == null || bestOfTheBest.getFitness() > best.getFitness()) {
          bestOfTheBest = best;
        }
      }
      // System.err.println("#best" + initialFitness + " => " + bestOfTheBest.getFitness());

      document.lockWrite();
      try {
        colorDOMUpdater.update(document.get(), bestOfTheBest.getColorDictionary());
      } finally {
        document.unlockWrite();
      }
    } finally {
      document.unlockRead();
    }
    return true;
  }

}
