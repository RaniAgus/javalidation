package io.github.raniagus.javalidation.function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FunctionTest {

    @Nested
    class TriFunctionTests {
        @Test
        void givenTriFunction_whenAndThen_thenAppliesTransformation() {
            TriFunction<String, String, String, String> triFunction = (a, b, c) -> a + b + c;
            TriFunction<String, String, String, String> triFunctionAndThen = triFunction.andThen((d) -> d + "d");

            assertThat(triFunctionAndThen.apply("a", "b", "c")).isEqualTo("abcd");
        }

        @Test
        void givenTriFunction_whenAndThenWithNull_thenThrowsNullPointerException() {
            TriFunction<String, String, String, String> triFunction = (a, b, c) -> a + b + c;

            assertThatThrownBy(() -> triFunction.andThen(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class QuadFunctionTests {
        @Test
        void givenQuadFunction_whenAndThen_thenAppliesTransformation() {
            QuadFunction<String, String, String, String, String> quadFunction = (a, b, c, d) -> a + b + c + d;
            QuadFunction<String, String, String, String, String> quadFunctionAndThen = quadFunction.andThen((e) -> e + "e");

            assertThat(quadFunctionAndThen.apply("a", "b", "c", "d")).isEqualTo("abcde");
        }

        @Test
        void givenQuadFunction_whenAndThenWithNull_thenThrowsNullPointerException() {
            QuadFunction<String, String, String, String, String> quadFunction = (a, b, c, d) -> a + b + c + d;

            assertThatThrownBy(() -> quadFunction.andThen(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class PentaFunctionTests {
        @Test
        void givenPentaFunction_whenAndThen_thenAppliesTransformation() {
            PentaFunction<String, String, String, String, String, String> pentaFunction = (a, b, c, d, e) -> a + b + c + d + e;
            PentaFunction<String, String, String, String, String, String> pentaFunctionAndThen = pentaFunction.andThen((f) -> f + "f");

            assertThat(pentaFunctionAndThen.apply("a", "b", "c", "d", "e")).isEqualTo("abcdef");
        }

        @Test
        void givenPentaFunction_whenAndThenWithNull_thenThrowsNullPointerException() {
            PentaFunction<String, String, String, String, String, String> pentaFunction = (a, b, c, d, e) -> a + b + c + d + e;

            assertThatThrownBy(() -> pentaFunction.andThen(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class HexFunctionTests {
        @Test
        void givenHexFunction_whenAndThen_thenAppliesTransformation() {
            HexFunction<String, String, String, String, String, String, String> hexFunction = (a, b, c, d, e, f) -> a + b + c + d + e + f;
            HexFunction<String, String, String, String, String, String, String> hexFunctionAndThen = hexFunction.andThen((g) -> g + "g");

            assertThat(hexFunctionAndThen.apply("a", "b", "c", "d", "e", "f")).isEqualTo("abcdefg");
        }

        @Test
        void givenHexFunction_whenAndThenWithNull_thenThrowsNullPointerException() {
            HexFunction<String, String, String, String, String, String, String> hexFunction = (a, b, c, d, e, f) -> a + b + c + d + e + f;

            assertThatThrownBy(() -> hexFunction.andThen(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class SeptaFunctionTests {
        @Test
        void givenSeptaFunction_whenAndThen_thenAppliesTransformation() {
            SeptaFunction<String, String, String, String, String, String, String, String> septaFunction = (a, b, c, d, e, f, g) -> a + b + c + d + e + f + g;
            SeptaFunction<String, String, String, String, String, String, String, String> septaFunctionAndThen = septaFunction.andThen((h) -> h + "h");

            assertThat(septaFunctionAndThen.apply("a", "b", "c", "d", "e", "f", "g")).isEqualTo("abcdefgh");
        }

        @Test
        void givenSeptaFunction_whenAndThenWithNull_thenThrowsNullPointerException() {
            SeptaFunction<String, String, String, String, String, String, String, String> septaFunction = (a, b, c, d, e, f, g) -> a + b + c + d + e + f + g;

            assertThatThrownBy(() -> septaFunction.andThen(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class OctaFunctionTests {
        @Test
        void givenOctaFunction_whenAndThen_thenAppliesTransformation() {
            OctaFunction<String, String, String, String, String, String, String, String, String> octaFunction = (a, b, c, d, e, f, g, h) -> a + b + c + d + e + f + g + h;
            OctaFunction<String, String, String, String, String, String, String, String, String> octaFunctionAndThen = octaFunction.andThen((i) -> i + "i");

            assertThat(octaFunctionAndThen.apply("a", "b", "c", "d", "e", "f", "g", "h")).isEqualTo("abcdefghi");
        }

        @Test
        void givenOctaFunction_whenAndThenWithNull_thenThrowsNullPointerException() {
            OctaFunction<String, String, String, String, String, String, String, String, String> octaFunction = (a, b, c, d, e, f, g, h) -> a + b + c + d + e + f + g + h;

            assertThatThrownBy(() -> octaFunction.andThen(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class NonaFunctionTests {
        @Test
        void givenNonaFunction_whenAndThen_thenAppliesTransformation() {
            NonaFunction<String, String, String, String, String, String, String, String, String, String> nonaFunction = (a, b, c, d, e, f, g, h, i) -> a + b + c + d + e + f + g + h + i;
            NonaFunction<String, String, String, String, String, String, String, String, String, String> nonaFunctionAndThen = nonaFunction.andThen((j) -> j + "j");

            assertThat(nonaFunctionAndThen.apply("a", "b", "c", "d", "e", "f", "g", "h", "i")).isEqualTo("abcdefghij");
        }

        @Test
        void givenNonaFunction_whenAndThenWithNull_thenThrowsNullPointerException() {
            NonaFunction<String, String, String, String, String, String, String, String, String, String> nonaFunction = (a, b, c, d, e, f, g, h, i) -> a + b + c + d + e + f + g + h + i;

            assertThatThrownBy(() -> nonaFunction.andThen(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class DecaFunctionTests {
        @Test
        void givenDecaFunction_whenAndThen_thenAppliesTransformation() {
            DecaFunction<String, String, String, String, String, String, String, String, String, String, String> decaFunction = (a, b, c, d, e, f, g, h, i, j) -> a + b + c + d + e + f + g + h + i + j;
            DecaFunction<String, String, String, String, String, String, String, String, String, String, String> decaFunctionAndThen = decaFunction.andThen((k) -> k + "k");

            assertThat(decaFunctionAndThen.apply("a", "b", "c", "d", "e", "f", "g", "h", "i", "j")).isEqualTo("abcdefghijk");
        }

        @Test
        void givenDecaFunction_whenAndThenWithNull_thenThrowsNullPointerException() {
            DecaFunction<String, String, String, String, String, String, String, String, String, String, String> decaFunction = (a, b, c, d, e, f, g, h, i, j) -> a + b + c + d + e + f + g + h + i + j;

            assertThatThrownBy(() -> decaFunction.andThen(null)).isInstanceOf(NullPointerException.class);
        }
    }

}
