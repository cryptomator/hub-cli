package org.cryptomator.hubcli.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.layout.TTLLLayout;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ConfiguratorRank;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.status.OnErrorConsoleStatusListener;

@ConfiguratorRank(ConfiguratorRank.CUSTOM_NORMAL_PRIORITY)
public class LogConfigurator extends ContextAwareBase implements Configurator {

	@Override
	public ExecutionStatus configure(LoggerContext lc) {
		var onConsoleListener = new OnErrorConsoleStatusListener();
		lc.getStatusManager().add(onConsoleListener);

		// same as
		// PatternLayout layout = new PatternLayout();
		// layout.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
		var layout = new TTLLLayout();
		layout.setContext(lc);
		layout.start();

		var encoder = new LayoutWrappingEncoder<ILoggingEvent>();
		encoder.setContext(lc);
		encoder.setLayout(layout);

		var appender = new OutputStreamAppender<ILoggingEvent>();
		appender.setContext(lc);
		appender.setOutputStream(System.err);
		appender.setName("stderr");
		appender.setEncoder(encoder);
		appender.start();

		var rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
		rootLogger.setAdditive(false);
		rootLogger.addAppender(appender);

		// let the caller decide
		return ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY;
	}
}
