package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FieldKeyTest {

    @Nested
    class FactoryMethodTests {

        @Test
        void givenMessageAndVarargs_whenOf_thenCreatesFieldKey() {
            FieldKey ts = FieldKey.of("age", 18);

            assertThat(ts.parts()).containsExactly("age", 18);
        }

        @Test
        void givenNoArgs_whenOf_thenCreatesFieldKeyWithEmptyArgs() {
            FieldKey ts = FieldKey.of("message");

            assertThat(ts.parts()).containsExactly("message");
        }

        @Test
        void givenMultipleArgs_whenOf_thenCreatesFieldKeyWithAllArgs() {
            FieldKey ts = FieldKey.of("Value must be between {0} and {1}", 10, 100);

            assertThat(ts.parts()).containsExactly("Value must be between {0} and {1}", 10, 100);
        }
    }

    @Nested
    class EqualsTests {

        @Test
        void givenSameMessageAndArgs_whenCompared_thenEqual() {
            FieldKey ts1 = FieldKey.of("age", 18);
            FieldKey ts2 = FieldKey.of("age", 18);

            assertThat(ts1).isEqualTo(ts2);
        }

        @Test
        void givenDifferentMessage_whenCompared_thenNotEqual() {
            FieldKey ts1 = FieldKey.of("age", 18);
            FieldKey ts2 = FieldKey.of("Age should be {0}", 18);

            assertThat(ts1).isNotEqualTo(ts2);
        }

        @Test
        void givenDifferentArgs_whenCompared_thenNotEqual() {
            FieldKey ts1 = FieldKey.of("age", 18);
            FieldKey ts2 = FieldKey.of("age", 21);

            assertThat(ts1).isNotEqualTo(ts2);
        }

        @Test
        void givenDifferentNumberOfArgs_whenCompared_thenNotEqual() {
            FieldKey ts1 = FieldKey.of("Value must be {0}", 10);
            FieldKey ts2 = FieldKey.of("Value must be {0}", 10, 100);

            assertThat(ts1).isNotEqualTo(ts2);
        }

        @Test
        void givenNoArgs_whenCompared_thenEqual() {
            FieldKey ts1 = FieldKey.of("message");
            FieldKey ts2 = FieldKey.of("message");

            assertThat(ts1).isEqualTo(ts2);
        }

        @Test
        void givenSelf_whenCompared_thenEqual() {
            FieldKey ts = FieldKey.of("age", 18);

            assertThat(ts).isEqualTo(ts);
        }

        @Test
        void givenNull_whenCompared_thenNotEqual() {
            FieldKey ts = FieldKey.of("age", 18);

            assertThat(ts).isNotEqualTo(null);
        }

        @Test
        void givenDifferentType_whenCompared_thenNotEqual() {
            FieldKey ts = FieldKey.of("age", 18);

            assertThat(ts).isNotEqualTo("age");
        }

        @Test
        void givenArraysWithSameElements_whenCompared_thenEqual() {
            FieldKey ts1 = FieldKey.of("Between {0} and {1}", 10, 100);
            FieldKey ts2 = FieldKey.of("Between {0} and {1}", 10, 100);

            assertThat(ts1).isEqualTo(ts2);
        }
    }

    @Nested
    class HashCodeTests {

        @Test
        void givenSameMessageAndArgs_whenHashCode_thenEqual() {
            FieldKey ts1 = FieldKey.of("age", 18);
            FieldKey ts2 = FieldKey.of("age", 18);

            assertThat(ts1.hashCode()).isEqualTo(ts2.hashCode());
        }

        @Test
        void givenDifferentMessage_whenHashCode_thenMayDiffer() {
            FieldKey ts1 = FieldKey.of("age", 18);
            FieldKey ts2 = FieldKey.of("Age should be {0}", 18);

            // Hash codes are not required to be different, but very likely will be
            assertThat(ts1.hashCode()).isNotEqualTo(ts2.hashCode());
        }

        @Test
        void givenDifferentArgs_whenHashCode_thenMayDiffer() {
            FieldKey ts1 = FieldKey.of("age", 18);
            FieldKey ts2 = FieldKey.of("age", 21);

            assertThat(ts1.hashCode()).isNotEqualTo(ts2.hashCode());
        }

        @Test
        void givenNoArgs_whenHashCode_thenConsistent() {
            FieldKey ts1 = FieldKey.of("message");
            FieldKey ts2 = FieldKey.of("message");

            assertThat(ts1.hashCode()).isEqualTo(ts2.hashCode());
        }

        @Test
        void givenSameInstance_whenHashCodeCalledMultipleTimes_thenConsistent() {
            FieldKey ts = FieldKey.of("age", 18);

            int hash1 = ts.hashCode();
            int hash2 = ts.hashCode();

            assertThat(hash1).isEqualTo(hash2);
        }
    }

    @Nested
    class ToStringTests {

        @Test
        void givenMessageAndArgs_whenToString_thenContainsBoth() {
            FieldKey ts = FieldKey.of("age", 18);

            String result = ts.toString();

            assertThat(result).isEqualTo("""
                    FieldKey{parts=[age, 18]}\
                    """);
        }

        @Test
        void givenNoArgs_whenToString_thenContainsMessageAndEmptyArray() {
            FieldKey ts = FieldKey.of("message");

            String result = ts.toString();

            assertThat(result).isEqualTo("""
                    FieldKey{parts=[message]}\
                    """);
        }

        @Test
        void givenMultipleArgs_whenToString_thenContainsAllArgs() {
            FieldKey ts = FieldKey.of("items", 10, 100);

            String result = ts.toString();

            assertThat(result).isEqualTo("""
                    FieldKey{parts=[items, 10, 100]}\
                    """);
        }

        @Test
        void givenStringArg_whenToString_thenFormatsCorrectly() {
            FieldKey ts = FieldKey.of("user", "name");

            String result = ts.toString();

            assertThat(result).isEqualTo("""
                    FieldKey{parts=[user, name]}\
                    """);
        }
    }
}
