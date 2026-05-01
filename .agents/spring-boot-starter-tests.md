# Adding tests in `javalidation-spring-boot-starter`

## Structure

All test classes **must extend `AutoConfigurationTest`**, which provides the nested `TestApplication` entry point:

```java
// AutoConfigurationTest.java
abstract class AutoConfigurationTest {
    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }
}
```

Tests for different configuration combinations live as **nested static classes** inside an outer class that extends `AutoConfigurationTest`. Each nested static class maps to one configuration variant.

## Minimal example

```java
class MyFeatureAutoConfigurationTest extends AutoConfigurationTest {

    @SpringBootTest(classes = TestApplication.class)
    static class DefaultsTest {
        @Autowired(required = false)
        private MyFeatureBean bean;

        @Test
        void givenAutoConfiguration_whenStartup_thenBeanIsPresent() {
            assertThat(bean).isNotNull();
        }
    }

    @SpringBootTest(classes = TestApplication.class)
    @TestPropertySource(properties = "io.github.raniagus.javalidation.my-feature=false")
    static class DisabledTest {
        @Autowired(required = false)
        private MyFeatureBean bean;

        @Test
        void givenFeatureDisabled_whenStartup_thenBeanIsAbsent() {
            assertThat(bean).isNull();
        }
    }
}
```

## Key conventions

- `@SpringBootTest(classes = TestApplication.class)` goes on the **nested static class**, not the outer class.
- Property overrides use `@TestPropertySource(properties = "key=value")` on the nested static class.
- The `JsonMapper` bean (from Jackson auto-config) is autowired — it is the Spring-configured one. Do not build it manually.
- `required = false` on `@Autowired` lets you assert that a bean is absent without failing on startup.

## Inheritance for shared tests

If two configuration variants share the same assertions, use class inheritance. The `@TestPropertySource` on the subclass overrides the parent. See `TemplateStringFormatterAutoConfigurationTest` for an example where `UseMessageSourceEnabledTest extends UseMessageSourceUnsetTest` — the tests are inherited, only the property differs.

## Running tests

```bash
# All starter tests
mvn test -pl javalidation-spring-boot-starter

# One test class
mvn test -pl javalidation-spring-boot-starter \
  -Dtest=TemplateStringFormatterAutoConfigurationTest
```
