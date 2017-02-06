package org.projectsforge.swap.plugins.wcagcolorbestimprover;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.projectsforge.swap.core.environment.Environment;
import org.projectsforge.swap.core.handlers.Handler;
import org.projectsforge.swap.core.handlers.HandlerContext;
import org.projectsforge.swap.core.handlers.Resource;
import org.projectsforge.swap.core.http.Response;
import org.projectsforge.swap.core.mime.css.GlobalStyleSheets;
import org.projectsforge.swap.core.mime.css.nodes.Media;
import org.projectsforge.swap.core.mime.css.property.color.ImmutableSRGBColor;
import org.projectsforge.swap.core.mime.css.property.color.dictionary.FgBgComponent;
import org.projectsforge.swap.core.mime.css.property.color.dictionary.FgBgEntry;
import org.projectsforge.swap.core.mime.css.property.color.dictionary.FgBgRegistry;
import org.projectsforge.swap.core.mime.css.property.color.dictionary.SimpleColorDictionary;
import org.projectsforge.swap.core.mime.css.resolver.PropertyResolver;
import org.projectsforge.swap.core.mime.css.resolver.RuleSetProcessor;
import org.projectsforge.swap.core.mime.css.resolver.StateRecorder;
import org.projectsforge.swap.core.mime.css.resolver.color.BackgroundColorRuleSetProcessor;
import org.projectsforge.swap.core.mime.css.resolver.color.ColorRuleSetProcessor;
import org.projectsforge.swap.core.mime.css.resolver.color.ColorStateRecorder;
import org.projectsforge.swap.core.mime.html.nodes.Document;
import org.projectsforge.swap.handlers.html.HtmlDomTransformation;
import org.projectsforge.swap.handlers.html.HtmlTransformation;
import org.projectsforge.swap.handlers.mime.StatisticsCollector;
import org.springframework.beans.factory.annotation.Autowired;

@Handler(singleton = true)
public class SaveDataTransformation extends HtmlDomTransformation {

  @Autowired
  Environment environment;

  @Autowired
  private GlobalStyleSheets globalStyleSheets;

  private static AtomicInteger counter = new AtomicInteger(0);

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

      try (PrintStream out = new PrintStream("colorproblem" + SaveDataTransformation.counter.incrementAndGet() + ".dat")) {
        out.println(simpleColorDictionary.size());
        out.println();
        for (int i = 0; i < simpleColorDictionary.size(); ++i) {
          final ImmutableSRGBColor color = simpleColorDictionary.getEntry(i).getColor();
          out.println(color.getR() + " " + color.getG() + " " + color.getB());
        }
        out.println();
        final FgBgComponent components = registry.getCombinedComponent();
        out.println(components.getEntries().size());
        for (final FgBgEntry entry : components.getEntries()) {
          out.println(entry.getForegroundIndex() + " " + entry.getBackgroundIndex());
        }
      }
    } finally {
      document.unlockRead();
    }
    return true;
  }

}
