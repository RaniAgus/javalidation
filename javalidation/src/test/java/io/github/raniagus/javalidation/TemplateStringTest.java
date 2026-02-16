package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TemplateStringTest {

    @Nested
    class FactoryMethodTests {

        @Test
        void givenMessageAndVarargs_whenOf_thenCreatesTemplateString() {
            TemplateString ts = TemplateString.of("Age must be at least {0}", 18);

            assertThat(ts.message()).isEqualTo("Age must be at least {0}");
            assertThat(ts.args()).containsExactly(18);
        }

        @Test
        void givenNoArgs_whenOf_thenCreatesTemplateStringWithEmptyArgs() {
            TemplateString ts = TemplateString.of("Simple message");

            assertThat(ts.message()).isEqualTo("Simple message");
            assertThat(ts.args()).isEmpty();
        }

        @Test
        void givenMultipleArgs_whenOf_thenCreatesTemplateStringWithAllArgs() {
            TemplateString ts = TemplateString.of("Value must be between {0} and {1}", 10, 100);

            assertThat(ts.message()).isEqualTo("Value must be between {0} and {1}");
            assertThat(ts.args()).containsExactly(10, 100);
        }
    }

    @Nested
    class EqualsTests {

        @Test
        void givenSameMessageAndArgs_whenCompared_thenEqual() {
            TemplateString ts1 = TemplateString.of("Age must be {0}", 18);
            TemplateString ts2 = TemplateString.of("Age must be {0}", 18);

            assertThat(ts1).isEqualTo(ts2);
        }

        @Test
        void givenDifferentMessage_whenCompared_thenNotEqual() {
            TemplateString ts1 = TemplateString.of("Age must be {0}", 18);
            TemplateString ts2 = TemplateString.of("Age should be {0}", 18);

            assertThat(ts1).isNotEqualTo(ts2);
        }

        @Test
        void givenDifferentArgs_whenCompared_thenNotEqual() {
            TemplateString ts1 = TemplateString.of("Age must be {0}", 18);
            TemplateString ts2 = TemplateString.of("Age must be {0}", 21);

            assertThat(ts1).isNotEqualTo(ts2);
        }

        @Test
        void givenDifferentNumberOfArgs_whenCompared_thenNotEqual() {
            TemplateString ts1 = TemplateString.of("Value must be {0}", 10);
            TemplateString ts2 = TemplateString.of("Value must be {0}", 10, 100);

            assertThat(ts1).isNotEqualTo(ts2);
        }

        @Test
        void givenNoArgs_whenCompared_thenEqual() {
            TemplateString ts1 = TemplateString.of("Simple message");
            TemplateString ts2 = TemplateString.of("Simple message");

            assertThat(ts1).isEqualTo(ts2);
        }

        @Test
        void givenSelf_whenCompared_thenEqual() {
            TemplateString ts = TemplateString.of("Age must be {0}", 18);

            assertThat(ts).isEqualTo(ts);
        }

        @Test
        void givenNull_whenCompared_thenNotEqual() {
            TemplateString ts = TemplateString.of("Age must be {0}", 18);

            assertThat(ts).isNotEqualTo(null);
        }

        @Test
        void givenDifferentType_whenCompared_thenNotEqual() {
            TemplateString ts = TemplateString.of("Age must be {0}", 18);

            assertThat(ts).isNotEqualTo("Age must be {0}");
        }

        @Test
        void givenArraysWithSameElements_whenCompared_thenEqual() {
            TemplateString ts1 = TemplateString.of("Between {0} and {1}", 10, 100);
            TemplateString ts2 = TemplateString.of("Between {0} and {1}", 10, 100);

            assertThat(ts1).isEqualTo(ts2);
        }
    }

    @Nested
    class HashCodeTests {

        @Test
        void givenSameMessageAndArgs_whenHashCode_thenEqual() {
            TemplateString ts1 = TemplateString.of("Age must be {0}", 18);
            TemplateString ts2 = TemplateString.of("Age must be {0}", 18);

            assertThat(ts1.hashCode()).isEqualTo(ts2.hashCode());
        }

        @Test
        void givenDifferentMessage_whenHashCode_thenMayDiffer() {
            TemplateString ts1 = TemplateString.of("Age must be {0}", 18);
            TemplateString ts2 = TemplateString.of("Age should be {0}", 18);

            // Hash codes are not required to be different, but very likely will be
            assertThat(ts1.hashCode()).isNotEqualTo(ts2.hashCode());
        }

        @Test
        void givenDifferentArgs_whenHashCode_thenMayDiffer() {
            TemplateString ts1 = TemplateString.of("Age must be {0}", 18);
            TemplateString ts2 = TemplateString.of("Age must be {0}", 21);

            assertThat(ts1.hashCode()).isNotEqualTo(ts2.hashCode());
        }

        @Test
        void givenNoArgs_whenHashCode_thenConsistent() {
            TemplateString ts1 = TemplateString.of("Simple message");
            TemplateString ts2 = TemplateString.of("Simple message");

            assertThat(ts1.hashCode()).isEqualTo(ts2.hashCode());
        }

        @Test
        void givenSameInstance_whenHashCodeCalledMultipleTimes_thenConsistent() {
            TemplateString ts = TemplateString.of("Age must be {0}", 18);

            int hash1 = ts.hashCode();
            int hash2 = ts.hashCode();

            assertThat(hash1).isEqualTo(hash2);
        }
    }

    @Nested
    class ToStringTests {

        @Test
        void givenMessageAndArgs_whenToString_thenContainsBoth() {
            TemplateString ts = TemplateString.of("Age must be {0}", 18);

            String result = ts.toString();

            assertThat(result).contains("Age must be {0}");
            assertThat(result).contains("18");
            assertThat(result).contains("TemplateString");
        }

        @Test
        void givenNoArgs_whenToString_thenContainsMessageAndEmptyArray() {
            TemplateString ts = TemplateString.of("Simple message");

            String result = ts.toString();

            assertThat(result).contains("Simple message");
            assertThat(result).contains("[]");
            assertThat(result).contains("TemplateString");
        }

        @Test
        void givenMultipleArgs_whenToString_thenContainsAllArgs() {
            TemplateString ts = TemplateString.of("Between {0} and {1}", 10, 100);

            String result = ts.toString();

            assertThat(result).contains("Between {0} and {1}");
            assertThat(result).contains("10");
            assertThat(result).contains("100");
        }

        @Test
        void givenStringArg_whenToString_thenFormatsCorrectly() {
            TemplateString ts = TemplateString.of("User {0} is invalid", "Alice");

            String result = ts.toString();

            assertThat(result).contains("User {0} is invalid");
            assertThat(result).contains("Alice");
        }
    }
}
