import org.cryptomator.hubcli.util.LogConfigurator;

open module org.cryptomator.hubcli {
    requires info.picocli;
    requires io.github.coffeelibs.tinyoauth2client;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.cryptomator.cryptolib;
    requires com.nimbusds.jose.jwt;
    requires org.slf4j;
	requires ch.qos.logback.core;
	requires ch.qos.logback.classic;

	provides ch.qos.logback.classic.spi.Configurator with LogConfigurator;

    exports org.cryptomator.hubcli.model to com.fasterxml.jackson.databind;
}