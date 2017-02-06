package org.projectsforge.swap.plugins.wcagcolorbestimprover;

import org.projectsforge.swap.core.environment.Environment;
import org.projectsforge.swap.core.mime.css.property.color.dictionary.SimpleColorDictionary.RuleSetManagement;
import org.projectsforge.swap.core.mime.css.property.color.dictionary.SimpleColorDictionary.TypeManagement;
import org.projectsforge.swap.proxy.webui.configuration.ConfigurationComponent;
import org.projectsforge.swap.proxy.webui.configuration.ConfigurationController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Component
@Controller
@RequestMapping(WCAGColorBestImproverConfigComponent.URL)
public class WCAGColorBestImproverConfigComponent extends ConfigurationComponent {

  /** The environment. */
  @Autowired
  private Environment environment;

  /** The Constant URL. */
  public static final String URL = ConfigurationController.URL
      + "/org.projectsforge.swap.plugins.wcagcolorbestimprover";

  @Override
  public String getDescription() {
    return "Configure the dichromacy deficiency to simulate";
  }

  @Override
  public String getName() {
    return "WCAG Color best improver";
  }

  @Override
  public int getPriority() {
    return Integer.MAX_VALUE;
  }

  @Override
  public String getUrl() {
    return WCAGColorBestImproverConfigComponent.URL;
  }

  /**
   * The GET form.
   * 
   * @return the model and view
   */
  @RequestMapping(value = "/", method = RequestMethod.GET)
  public ModelAndView handleGet() {
    if (!isActive()) {
      return getInactiveMAV();
    }

    final ModelAndView mav = new ModelAndView("org.projectsforge.swap.plugins.wcagcolorbestimprover/get");
    mav.addObject("brightnessDifferenceThreshold", WCAGColorBestImproverPropertyHolder.brightnessDifferenceThreshold);
    mav.addObject("tonalityDifferenceThreshold", WCAGColorBestImproverPropertyHolder.tonalityDifferenceThreshold);
    mav.addObject("contrastRatioThreshold", WCAGColorBestImproverPropertyHolder.contrastRatioThreshold);
    mav.addObject("brightnessDifferenceWeight", WCAGColorBestImproverPropertyHolder.brightnessDifferenceWeight);
    mav.addObject("contrastRatioWeight", WCAGColorBestImproverPropertyHolder.contrastRatioWeight);
    mav.addObject("tonalityDifferenceWeight", WCAGColorBestImproverPropertyHolder.tonalityDifferenceWeight);
    mav.addObject("typeManagement", WCAGColorBestImproverPropertyHolder.typeManagement);
    mav.addObject("ruleSetManagement", WCAGColorBestImproverPropertyHolder.ruleSetManagement);
    mav.addObject("rootline", getRootline());
    return mav;
  }

  /**
   * The POST form.
   * 
   * @param deficiency
   *          the deficiency
   * @return the model and view
   */
  @RequestMapping(value = "/", method = RequestMethod.POST)
  public ModelAndView handlePost(@RequestParam final double brightnessDifferenceThreshold,
      @RequestParam final double tonalityDifferenceThreshold, @RequestParam final double contrastRatioThreshold,
      @RequestParam final double brightnessDifferenceWeight, @RequestParam final double contrastRatioWeight,
      @RequestParam final double tonalityDifferenceWeight, @RequestParam final TypeManagement typeManagement,
      @RequestParam final RuleSetManagement ruleSetManagement) {
    if (!isActive()) {
      return getInactiveMAV();
    }

    WCAGColorBestImproverPropertyHolder.brightnessDifferenceThreshold.set(brightnessDifferenceThreshold);
    WCAGColorBestImproverPropertyHolder.tonalityDifferenceThreshold.set(tonalityDifferenceThreshold);
    WCAGColorBestImproverPropertyHolder.contrastRatioThreshold.set(contrastRatioThreshold);
    WCAGColorBestImproverPropertyHolder.brightnessDifferenceWeight.set(brightnessDifferenceWeight);
    WCAGColorBestImproverPropertyHolder.contrastRatioWeight.set(contrastRatioWeight);
    WCAGColorBestImproverPropertyHolder.tonalityDifferenceWeight.set(tonalityDifferenceWeight);
    WCAGColorBestImproverPropertyHolder.typeManagement.set(typeManagement);
    WCAGColorBestImproverPropertyHolder.ruleSetManagement.set(ruleSetManagement);

    environment.saveConfigurationProperties();

    return handleGet();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.projectsforge.swap.core.web.mvc.AbstractMVCComponent#isActive()
   */
  @Override
  public boolean isActive() {
    return true;
  }
}
