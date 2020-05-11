package com.connect5;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AppIT {

    @Autowired
    private GameController gameController;

    @Test
    public void contextLoads() {
        assertThat(gameController).isNotNull();
    }

}
