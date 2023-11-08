module org.cryptomator.hubcli {
    requires info.picocli;
    requires io.github.coffeelibs.tinyoauth2client;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.cryptomator.cryptolib;
    requires com.nimbusds.jose.jwt;

    opens org.cryptomator.hubcli to info.picocli;
	opens org.cryptomator.hubcli.model to info.picocli;

    exports org.cryptomator.hubcli to com.fasterxml.jackson.databind;
}