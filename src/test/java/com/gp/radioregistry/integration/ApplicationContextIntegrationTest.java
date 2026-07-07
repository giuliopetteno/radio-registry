package com.gp.radioregistry.integration;

import com.gp.radioregistry.base.AbstractPostgresContainerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationContextIntegrationTest extends AbstractPostgresContainerTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Spring application context loads successfully with all beans wired")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }
}
