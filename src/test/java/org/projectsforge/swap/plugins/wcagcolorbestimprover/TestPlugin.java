package org.projectsforge.swap.plugins.wcagcolorbestimprover;

import java.util.Arrays;

import org.junit.Test;
import org.projectsforge.swap.core.environment.startermonitor.GraphicStarterMonitor;
import org.projectsforge.swap.core.mime.css.property.color.ColorSpaceUtil;
import org.projectsforge.swap.proxy.starter.ProxyEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestPlugin {

	private static Logger logger = LoggerFactory.getLogger(TestPlugin.class);

	@Test
	public void test() throws Exception {

		double ml = Integer.MAX_VALUE, Ml = Integer.MIN_VALUE;
		double ma = Integer.MAX_VALUE, Ma = Integer.MIN_VALUE;
		double mb = Integer.MAX_VALUE, Mb = Integer.MIN_VALUE;
		double[] lab = new double[3];

		for (int r = 0; r < 256; ++r) {
			for (int g = 0; g < 256; g++) {
				for (int b = 0; b < 256; b++) {
					ColorSpaceUtil.SRGBToLab(r, g, b, lab);
					ml = Math.min(ml, lab[0]);
					ma = Math.min(ma, lab[1]);
					mb = Math.min(mb, lab[2]);
					Ml = Math.max(Ml, lab[0]);
					Ma = Math.max(Ma, lab[1]);
					Mb = Math.max(Mb, lab[2]);
				}
			}
		}

		System.err.println(Arrays.toString(ColorSpaceUtil.SRGBToXYZ(0, 0, 0)));
		System.err.println(Arrays.toString(ColorSpaceUtil.SRGBToXYZ(255, 255, 255)));

		System.err.println(Arrays.toString(new double[] { ml, ma, mb, Ml, Ma,
				Mb }));

		System.err.println(Arrays.toString(ColorSpaceUtil.SRGBToLab(0, 0, 0)));
		System.err.println(Arrays.toString(ColorSpaceUtil.SRGBToLab(255, 255, 255)));

		if (System.in != null)
			return;

		final ProxyEnvironment environment = new ProxyEnvironment("test");
		environment.setAllowUserInteraction(false);
		environment.setStarterMonitor(new GraphicStarterMonitor());
		try {
			environment.start();
			// environment.waitForStop();
		} catch (final Exception e) {
			environment.stop();
			TestPlugin.logger.error("An error occurred", e);
			throw e;
		}
	}
}
