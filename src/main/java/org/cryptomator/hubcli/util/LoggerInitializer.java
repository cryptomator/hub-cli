package org.cryptomator.hubcli.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Read JUL config at build time, see <a href="https://www.graalvm.org/latest/reference-manual/native-image/guides/add-logging-to-native-executable/">GraalVM logging docs</a>.
 * This requires <code>--initialize-at-build-time</code> during native image generation
 */
class LoggerInitializer {

	static {
		try {
			LogManager.getLogManager().readConfiguration(LoggerInitializer.class.getResourceAsStream("/logging.properties"));
		} catch (IOException | SecurityException | ExceptionInInitializerError ex) {
			Logger.getLogger(LoggerInitializer.class.getName()).log(Level.SEVERE, "Failed to read logging.properties file", ex);
		}
	}

}
