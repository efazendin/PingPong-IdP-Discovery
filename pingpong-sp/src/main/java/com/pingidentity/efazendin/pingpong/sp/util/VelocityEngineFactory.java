package com.pingidentity.efazendin.pingpong.sp.util;

import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;

public class VelocityEngineFactory {
	private static final Logger _logger = Logger.getLogger(VelocityEngineFactory.class);

	public static VelocityEngine getNewVelocityEngine(String velocityTemplatePath) {
		// Initialize a local velocity engine
		_logger.debug("Setting velocity's template path to: " + velocityTemplatePath);
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty("file.resource.loader.path", velocityTemplatePath);
		ve.setProperty("file.resource.loader.cache", true);
		try {
			ve.init();
		} catch (Exception e) {
			_logger.error("There was an error initializing velocity.", e);
		}

		return ve;
	}
}
