package org.projectsforge.swap.plugins.wcagcolorbestimprover;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.Test;
import org.projectsforge.swap.core.handlers.HandlersPropertyHolder;
import org.projectsforge.swap.proxy.starter.ProxyEnvironment;
import org.projectsforge.swap.proxy.tester.SimpleResourceServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenDataset {

  private static Logger logger = LoggerFactory.getLogger(GenDataset.class);

  @Test
  public void genDataset() throws Exception {

    try {
      final ProxyEnvironment environment = new ProxyEnvironment("genDataset");
      HandlersPropertyHolder.disabledHandlers
          .set("org.projectsforge.swap.plugins.wcagcolorbestimprover.CssColorBestImproverTransformation");

      try {
        environment.start();
        final SimpleResourceServer srs = environment.autowireBean(new SimpleResourceServer());

        try {
          srs.start();

          final URL listUrls = Thread.currentThread().getClass().getResource("/TEST-WEB-INF/test001/list.url");

          final BufferedReader reader = new BufferedReader(new InputStreamReader(listUrls.openStream()));
          try {
            String url;
            while ((url = reader.readLine()) != null) {
              GenDataset.logger.warn("URL {}", url);
              try {
                srs.getContent(url);
              } catch (final IOException e) {
                if (GenDataset.logger.isWarnEnabled()) {
                  GenDataset.logger.warn("URL " + url + " caused an error", e);
                }
              }
            }
          } finally {
            reader.close();
          }
        } finally {
          srs.stop();
        }
      } finally {
        environment.stop();
      }
    } catch (final Exception e) {
      GenDataset.logger.error("An error occurred", e);
      throw e;
    }
  }
}
