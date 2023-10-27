package org.cryptomator.hubcli;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AppTest {

    @Test
    void testHello() {
        Assertions.assertDoesNotThrow(() -> App.main(new String[] {}));
    }
}
