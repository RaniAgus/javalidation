package io.github.raniagus.javalidation.spring;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

abstract class AutoConfigurationTest {
    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }
}
