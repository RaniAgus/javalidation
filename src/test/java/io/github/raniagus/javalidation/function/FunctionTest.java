package io.github.raniagus.javalidation.function;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.junit.jupiter.api.Test;

class FunctionTest {
  @Test
  void triFunctionAndThen() {
    TriFunction<String, String, String, String> triFunction = (a, b, c) -> a + b + c;
    TriFunction<String, String, String, String> triFunctionAndThen = triFunction.andThen((d) -> d + "d");

    assertThat(triFunctionAndThen.apply("a", "b", "c")).isEqualTo("abcd");
  }

  @Test
  void triFunctionAndThenWithNull() {
    TriFunction<String, String, String, String> triFunction = (a, b, c) -> a + b + c;

    assertThatThrownBy(() -> triFunction.andThen(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void tetraFunctionAndThen() {
    TetraFunction<String, String, String, String, String> tetraFunction = (a, b, c, d) -> a + b + c + d;
    TetraFunction<String, String, String, String, String> tetraFunctionAndThen = tetraFunction.andThen((e) -> e + "e");

    assertThat(tetraFunctionAndThen.apply("a", "b", "c", "d")).isEqualTo("abcde");
  }

  @Test
  void tetraFunctionAndThenWithNull() {
    TetraFunction<String, String, String, String, String> tetraFunction = (a, b, c, d) -> a + b + c + d;

    assertThatThrownBy(() -> tetraFunction.andThen(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void pentaFunctionAndThen() {
    PentaFunction<String, String, String, String, String, String> pentaFunction = (a, b, c, d, e) -> a + b + c + d + e;
    PentaFunction<String, String, String, String, String, String> pentaFunctionAndThen = pentaFunction.andThen((f) -> f + "f");

    assertThat(pentaFunctionAndThen.apply("a", "b", "c", "d", "e")).isEqualTo("abcdef");
  }

  @Test
  void pentaFunctionAndThenWithNull() {
    PentaFunction<String, String, String, String, String, String> pentaFunction = (a, b, c, d, e) -> a + b + c + d + e;

    assertThatThrownBy(() -> pentaFunction.andThen(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void hexaFunctionAndThen() {
    HexaFunction<String, String, String, String, String, String, String> hexaFunction = (a, b, c, d, e, f) -> a + b + c + d + e + f;
    HexaFunction<String, String, String, String, String, String, String> hexaFunctionAndThen = hexaFunction.andThen((g) -> g + "g");

    assertThat(hexaFunctionAndThen.apply("a", "b", "c", "d", "e", "f")).isEqualTo("abcdefg");
  }

  @Test
  void hexaFunctionAndThenWithNull() {
    HexaFunction<String, String, String, String, String, String, String> hexaFunction = (a, b, c, d, e, f) -> a + b + c + d + e + f;

    assertThatThrownBy(() -> hexaFunction.andThen(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void heptaFunctionAndThen() {
    HeptaFunction<String, String, String, String, String, String, String, String> heptaFunction = (a, b, c, d, e, f, g) -> a + b + c + d + e + f + g;
    HeptaFunction<String, String, String, String, String, String, String, String> heptaFunctionAndThen = heptaFunction.andThen((h) -> h + "h");

    assertThat(heptaFunctionAndThen.apply("a", "b", "c", "d", "e", "f", "g")).isEqualTo("abcdefgh");
  }

  @Test
  void heptaFunctionAndThenWithNull() {
    HeptaFunction<String, String, String, String, String, String, String, String> heptaFunction = (a, b, c, d, e, f, g) -> a + b + c + d + e + f + g;

    assertThatThrownBy(() -> heptaFunction.andThen(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void octaFunctionAndThen() {
    OctaFunction<String, String, String, String, String, String, String, String, String> octaFunction = (a, b, c, d, e, f, g, h) -> a + b + c + d + e + f + g + h;
    OctaFunction<String, String, String, String, String, String, String, String, String> octaFunctionAndThen = octaFunction.andThen((i) -> i + "i");

    assertThat(octaFunctionAndThen.apply("a", "b", "c", "d", "e", "f", "g", "h")).isEqualTo("abcdefghi");
  }

  @Test
  void octaFunctionAndThenWithNull() {
    OctaFunction<String, String, String, String, String, String, String, String, String> octaFunction = (a, b, c, d, e, f, g, h) -> a + b + c + d + e + f + g + h;

    assertThatThrownBy(() -> octaFunction.andThen(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void nonaFunctionAndThen() {
    NonaFunction<String, String, String, String, String, String, String, String, String, String> nonaFunction = (a, b, c, d, e, f, g, h, i) -> a + b + c + d + e + f + g + h + i;
    NonaFunction<String, String, String, String, String, String, String, String, String, String> nonaFunctionAndThen = nonaFunction.andThen((j) -> j + "j");

    assertThat(nonaFunctionAndThen.apply("a", "b", "c", "d", "e", "f", "g", "h", "i")).isEqualTo("abcdefghij");
  }

  @Test
  void nonaFunctionAndThenWithNull() {
    NonaFunction<String, String, String, String, String, String, String, String, String, String> nonaFunction = (a, b, c, d, e, f, g, h, i) -> a + b + c + d + e + f + g + h + i;

    assertThatThrownBy(() -> nonaFunction.andThen(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void decaFunctionAndThen() {
    DecaFunction<String, String, String, String, String, String, String, String, String, String, String> decaFunction = (a, b, c, d, e, f, g, h, i, j) -> a + b + c + d + e + f + g + h + i + j;
    DecaFunction<String, String, String, String, String, String, String, String, String, String, String> decaFunctionAndThen = decaFunction.andThen((k) -> k + "k");

    assertThat(decaFunctionAndThen.apply("a", "b", "c", "d", "e", "f", "g", "h", "i", "j")).isEqualTo("abcdefghijk");
  }

  @Test
  void decaFunctionAndThenWithNull() {
    DecaFunction<String, String, String, String, String, String, String, String, String, String, String> decaFunction = (a, b, c, d, e, f, g, h, i, j) -> a + b + c + d + e + f + g + h + i + j;

    assertThatThrownBy(() -> decaFunction.andThen(null)).isInstanceOf(NullPointerException.class);
  }

}
