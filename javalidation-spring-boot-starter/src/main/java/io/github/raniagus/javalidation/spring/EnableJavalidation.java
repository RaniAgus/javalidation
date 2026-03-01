package io.github.raniagus.javalidation.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

/**
 * Enables Javalidation support in Spring applications by importing all Javalidation
 * autoconfiguration classes.
 *
 * <p>This annotation is primarily useful in test scenarios where Spring Boot's
 * autoconfiguration is disabled (e.g., {@code @WebMvcTest}, {@code @DataJpaTest}),
 * but you want Javalidation features available. In regular Spring Boot applications,
 * Javalidation is automatically enabled when the starter dependency is present.
 *
 * <p>This annotation imports:
 * <ul>
 *   <li>{@link JavalidationAutoConfiguration} - Core formatters and MessageSource integration</li>
 *   <li>{@link JavalidationJacksonAutoConfiguration} - Jackson serializers for Result, ValidationErrors, etc.</li>
 *   <li>{@link JavalidationValidatorAutoConfiguration} - Spring MVC validator integration</li>
 * </ul>
 *
 * <p>Example usage in a test:
 * <pre>{@code
 * @WebMvcTest(MyController.class)
 * @EnableJavalidation
 * class MyControllerTest {
 *     // Javalidation features are now available
 * }
 * }</pre>
 *
 * <p>To disable specific features in regular applications, use the
 * {@link JavalidationProperties} configuration properties or Spring Boot's
 * exclusion mechanism:
 * <pre>{@code
 * spring.autoconfigure.exclude=io.github.raniagus.javalidation.spring.JavalidationJacksonAutoConfiguration
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ImportAutoConfiguration({
        JavalidationAutoConfiguration.class,
        JavalidationJacksonAutoConfiguration.class,
        JavalidationValidatorAutoConfiguration.class,
})
public @interface EnableJavalidation {
}
