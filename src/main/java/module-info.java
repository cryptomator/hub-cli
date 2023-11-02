module org.cryptomator.hubcli {
    requires info.picocli;
    requires io.github.coffeelibs.tinyoauth2client;
    requires com.fasterxml.jackson.databind;

    opens org.cryptomator.hubcli to info.picocli;


}