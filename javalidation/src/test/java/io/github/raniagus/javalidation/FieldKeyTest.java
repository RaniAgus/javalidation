package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FieldKeyTest {

    @Nested
    class FactoryMethodTests {

        @Test
        void givenStringSegments_whenOf_thenCreatesPathWithStringKeys() {
            FieldKey key = FieldKey.of("user", "address");

            assertThat(key.parts()).containsExactly(
                    new FieldKeyPart.StringKey("user"),
                    new FieldKeyPart.StringKey("address")
            );
        }

        @Test
        void givenMixedSegments_whenOf_thenCreatesPathWithStringAndIntKeys() {
            FieldKey key = FieldKey.of("addresses", 0, "street");

            assertThat(key.parts()).containsExactly(
                    new FieldKeyPart.StringKey("addresses"),
                    new FieldKeyPart.IntKey(0),
                    new FieldKeyPart.StringKey("street")
            );
        }

        @Test
        void givenUnknownSegmentTypes_whenOf_thenThrowsIllegalArgumentException() {
            assertThatThrownBy(() -> FieldKey.of("user", 3.14, true))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unsupported key type: java.lang.Boolean");
        }

        @Test
        void givenSingleSegment_whenOf_thenCreatesPathWithOneKey() {
            FieldKey key = FieldKey.of("name");

            assertThat(key.parts()).containsExactly(new FieldKeyPart.StringKey("name"));
        }

        @Test
        void givenFieldKeyPartArray_whenOf_thenCreatesPathFromParts() {
            FieldKey key = FieldKey.of(
                    new FieldKeyPart.StringKey("items"),
                    new FieldKeyPart.IntKey(2)
            );

            assertThat(key.parts()).containsExactly(
                    new FieldKeyPart.StringKey("items"),
                    new FieldKeyPart.IntKey(2)
            );
        }

        @Test
        void givenPrefixAndAdditionalParts_whenOf_thenPrependsPrefixToPath() {
            List<FieldKeyPart> prefix = List.of(
                    new FieldKeyPart.StringKey("user"),
                    new FieldKeyPart.StringKey("profile")
            );

            FieldKey key = FieldKey.of(prefix, new FieldKeyPart.StringKey("email"));

            assertThat(key.parts()).containsExactly(
                    new FieldKeyPart.StringKey("user"),
                    new FieldKeyPart.StringKey("profile"),
                    new FieldKeyPart.StringKey("email")
            );
        }

        @Test
        void givenExistingKey_whenWithPrefix_thenPrependsSegmentsToPath() {
            FieldKey key = FieldKey.of("street");

            FieldKey prefixed = key.withPrefix(
                    new FieldKeyPart.StringKey("user"),
                    new FieldKeyPart.StringKey("address")
            );

            assertThat(prefixed.parts()).containsExactly(
                    new FieldKeyPart.StringKey("user"),
                    new FieldKeyPart.StringKey("address"),
                    new FieldKeyPart.StringKey("street")
            );
        }
    }

    @Nested
    class EqualsTests {

        @Test
        void givenSamePath_whenCompared_thenEqual() {
            FieldKey key1 = FieldKey.of("user", "address", "street");
            FieldKey key2 = FieldKey.of("user", "address", "street");

            assertThat(key1).isEqualTo(key2);
        }

        @Test
        void givenDifferentStringSegment_whenCompared_thenNotEqual() {
            FieldKey key1 = FieldKey.of("user", "address");
            FieldKey key2 = FieldKey.of("user", "profile");

            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        void givenDifferentIndexSegment_whenCompared_thenNotEqual() {
            FieldKey key1 = FieldKey.of("items", 0);
            FieldKey key2 = FieldKey.of("items", 1);

            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        void givenDifferentDepth_whenCompared_thenNotEqual() {
            FieldKey key1 = FieldKey.of("user", "address");
            FieldKey key2 = FieldKey.of("user", "address", "street");

            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        void givenSingleSegmentPath_whenCompared_thenEqual() {
            FieldKey key1 = FieldKey.of("name");
            FieldKey key2 = FieldKey.of("name");

            assertThat(key1).isEqualTo(key2);
        }

        @Test
        void givenSelf_whenCompared_thenEqual() {
            FieldKey key = FieldKey.of("user", "address");

            assertThat(key).isEqualTo(key);
        }

        @Test
        void givenNull_whenCompared_thenNotEqual() {
            FieldKey key = FieldKey.of("user", "address");

            assertThat(key).isNotEqualTo(null);
        }

        @Test
        void givenDifferentType_whenCompared_thenNotEqual() {
            FieldKey key = FieldKey.of("user");

            assertThat(key).isNotEqualTo("user");
        }
    }

    @Nested
    class HashCodeTests {

        @Test
        void givenSamePath_whenHashCode_thenEqual() {
            FieldKey key1 = FieldKey.of("user", "address", "street");
            FieldKey key2 = FieldKey.of("user", "address", "street");

            assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
        }

        @Test
        void givenDifferentStringSegment_whenHashCode_thenMayDiffer() {
            FieldKey key1 = FieldKey.of("user", "address");
            FieldKey key2 = FieldKey.of("user", "profile");

            assertThat(key1.hashCode()).isNotEqualTo(key2.hashCode());
        }

        @Test
        void givenDifferentIndexSegment_whenHashCode_thenMayDiffer() {
            FieldKey key1 = FieldKey.of("items", 0);
            FieldKey key2 = FieldKey.of("items", 1);

            assertThat(key1.hashCode()).isNotEqualTo(key2.hashCode());
        }

        @Test
        void givenSingleSegmentPath_whenHashCode_thenConsistent() {
            FieldKey key1 = FieldKey.of("name");
            FieldKey key2 = FieldKey.of("name");

            assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
        }

        @Test
        void givenSameInstance_whenHashCodeCalledMultipleTimes_thenConsistent() {
            FieldKey key = FieldKey.of("user", "address");

            int hash1 = key.hashCode();
            int hash2 = key.hashCode();

            assertThat(hash1).isEqualTo(hash2);
        }
    }

    @Nested
    class CompareToTests {

        @Test
        void givenEqualPaths_whenCompareTo_thenReturnsZero() {
            FieldKey key1 = FieldKey.of("user", "address");
            FieldKey key2 = FieldKey.of("user", "address");

            assertThat(key1.compareTo(key2)).isEqualTo(0);
        }

        @Test
        void givenAlphabeticallyLesserFirstSegment_whenCompareTo_thenReturnsNegative() {
            FieldKey key1 = FieldKey.of("address");
            FieldKey key2 = FieldKey.of("user");

            assertThat(key1.compareTo(key2)).isLessThan(0);
        }

        @Test
        void givenAlphabeticallyGreaterFirstSegment_whenCompareTo_thenReturnsPositive() {
            FieldKey key1 = FieldKey.of("user");
            FieldKey key2 = FieldKey.of("address");

            assertThat(key1.compareTo(key2)).isGreaterThan(0);
        }

        @Test
        void givenSamePrefixButShorterPath_whenCompareTo_thenShorterIsLess() {
            FieldKey key1 = FieldKey.of("user");
            FieldKey key2 = FieldKey.of("user", "address");

            assertThat(key1.compareTo(key2)).isLessThan(0);
        }

        @Test
        void givenSamePrefixButLongerPath_whenCompareTo_thenLongerIsGreater() {
            FieldKey key1 = FieldKey.of("user", "address");
            FieldKey key2 = FieldKey.of("user");

            assertThat(key1.compareTo(key2)).isGreaterThan(0);
        }

        @Test
        void givenLesserIndexSegment_whenCompareTo_thenReturnsNegative() {
            FieldKey key1 = FieldKey.of("items", 0);
            FieldKey key2 = FieldKey.of("items", 1);

            assertThat(key1.compareTo(key2)).isLessThan(0);
        }

        @Test
        void givenGreaterIndexSegment_whenCompareTo_thenReturnsPositive() {
            FieldKey key1 = FieldKey.of("items", 2);
            FieldKey key2 = FieldKey.of("items", 1);

            assertThat(key1.compareTo(key2)).isGreaterThan(0);
        }

        @Test
        void givenStringKeyVsIntKeyAtSamePosition_whenCompareTo_thenStringIsLess() {
            // StringKey < IntKey regardless of values
            FieldKey key1 = FieldKey.of(new FieldKeyPart.StringKey("z"));
            FieldKey key2 = FieldKey.of(new FieldKeyPart.IntKey(0));

            assertThat(key1.compareTo(key2)).isLessThan(0);
        }

        @Test
        void givenIntKeyVsStringKeyAtSamePosition_whenCompareTo_thenIntIsGreater() {
            // IntKey > StringKey regardless of values
            FieldKey key1 = FieldKey.of(new FieldKeyPart.IntKey(0));
            FieldKey key2 = FieldKey.of(new FieldKeyPart.StringKey("z"));

            assertThat(key1.compareTo(key2)).isGreaterThan(0);
        }

        @Test
        void givenFirstSegmentDiffers_whenCompareTo_thenLaterSegmentsAreIgnored() {
            FieldKey key1 = FieldKey.of("a", "z");
            FieldKey key2 = FieldKey.of("b", "a");

            assertThat(key1.compareTo(key2)).isLessThan(0);
        }

        @Test
        void givenCompareToIsConsistentWithEquals_whenBothEqualAndCompared_thenZero() {
            FieldKey key1 = FieldKey.of("user", "addresses", 0, "street");
            FieldKey key2 = FieldKey.of("user", "addresses", 0, "street");

            assertThat(key1.compareTo(key2)).isEqualTo(0);
            assertThat(key1).isEqualTo(key2);
        }
    }

    @Nested
    class ToStringTests {

        @Test
        void givenStringPath_whenToString_thenShowsSegments() {
            FieldKey key = FieldKey.of("user", "address");

            assertThat(key.toString()).isEqualTo("FieldKey{parts=[user, address]}");
        }

        @Test
        void givenMixedPath_whenToString_thenShowsStringAndIntSegments() {
            FieldKey key = FieldKey.of("addresses", 0, "street");

            assertThat(key.toString()).isEqualTo("FieldKey{parts=[addresses, 0, street]}");
        }

        @Test
        void givenSingleSegmentPath_whenToString_thenShowsOneSegment() {
            FieldKey key = FieldKey.of("name");

            assertThat(key.toString()).isEqualTo("FieldKey{parts=[name]}");
        }

        @Test
        void givenIndexOnlyPath_whenToString_thenShowsIntSegment() {
            FieldKey key = FieldKey.of(new FieldKeyPart.IntKey(3));

            assertThat(key.toString()).isEqualTo("FieldKey{parts=[3]}");
        }
    }
}
