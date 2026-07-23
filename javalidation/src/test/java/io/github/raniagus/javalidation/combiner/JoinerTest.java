package io.github.raniagus.javalidation.combiner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

import io.github.raniagus.javalidation.JavalidationException;
import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.TemplateString;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.util.ErrorStrings;
import io.github.raniagus.javalidation.util.Person;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JoinerTest {

    @Nested
    class ResultCombiner2Tests {

        @Test
        void givenTwoOkResults_whenCombine_thenCombinesValues() {
            Result<String> result1 = Result.ok("Agustin");
            Result<Integer> result2 = Result.ok(23);

            Result<Person> combined = result1
                    .and(result2)
                    .combine(Person::new);

            assertThat(combined.getOrThrow()).isEqualTo(new Person("Agustin", 23));
        }

        @Test
        void givenTwoErrResults_whenCombine_thenAccumulatesErrors() {
            Result<String> result1 = Result.error(ErrorStrings.ERROR_1);
            Result<Integer> result2 = Result.error(ErrorStrings.ERROR_2);

            Result<Person> combined = result1
                    .and(result2)
                    .combine(Person::new);

            assertThat(combined.errors())
                    .extracting(ValidationErrors::rootErrors)
                    .asInstanceOf(list(TemplateString.class))
                    .map(TemplateString::message)
                    .containsExactly("Error 1", "Error 2");
        }

        @Test
        void givenTwoOkResults_whenGetLast_thenReturnsSecondValue() {
            var result = Result.ok("Agustin")
                    .and(Result.ok(23))
                    .getLast();

            assertThat(result.getOrThrow()).isEqualTo(23);
        }

        @Test
        void givenTwoErrResults_whenGetLast_thenAccumulatesErrors() {
            var result = Result.<String>error(ErrorStrings.ERROR_1)
                    .and(Result.<Integer>error(ErrorStrings.ERROR_2))
                    .getLast();

            assertThat(result.errors())
                    .extracting(ValidationErrors::rootErrors)
                    .asInstanceOf(list(TemplateString.class))
                    .map(TemplateString::message)
                    .containsExactly("Error 1", "Error 2");
        }
    }

    @Nested
    class DependentAndTests {

        @Test
        void givenOkResult_whenDependentAnd_thenSuppliesContainedValue() {
            var combined = Result.ok("Agustin")
                    .and(name -> Result.ok(name.length()))
                    .combine((name, length) -> name + ":" + length);

            assertThat(combined.getOrThrow()).isEqualTo("Agustin:7");
        }

        @Test
        void givenErrResult_whenDependentAnd_thenSkipsFunctionAndAccumulatesIndependentErrors() {
            var called = new AtomicBoolean(false);

            var combined = Result.<String>error(ErrorStrings.ERROR_1)
                    .and(name -> {
                        called.set(true);
                        return Result.ok(name.length());
                    })
                    .and(Result.error(ErrorStrings.ERROR_2))
                    .combine((name, length, ignored) -> name + length);

            assertThat(called).isFalse();
            assertThat(combined.errors())
                    .extracting(ValidationErrors::rootErrors)
                    .asInstanceOf(list(TemplateString.class))
                    .map(TemplateString::message)
                    .containsExactly("Error 1", "Error 2");
        }

        @Test
        void givenDependentAndReturnsErr_whenIndependentAndFollows_thenAccumulatesBothErrors() {
            var combined = Result.ok("Agustin")
                    .and(name -> Result.<Integer>error(ErrorStrings.ERROR_2))
                    .and(Result.error(ErrorStrings.ERROR_3))
                    .combine((name, length, ignored) -> name + length);

            assertThat(combined.errors())
                    .extracting(ValidationErrors::rootErrors)
                    .asInstanceOf(list(TemplateString.class))
                    .map(TemplateString::message)
                    .containsExactly("Error 2", "Error 3");
        }

        @Test
        void givenPriorDependentAndReturnsErr_whenNextDependentAnd_thenSkipsFunctionAndAccumulatesIndependentErrors() {
            var called = new AtomicBoolean(false);

            var combined = Result.ok(1)
                    .and(value -> Result.<Integer>error(ErrorStrings.ERROR_2))
                    .and((first, second) -> {
                        called.set(true);
                        return Result.ok(first + second);
                    })
                    .and(Result.<Integer>error(ErrorStrings.ERROR_4))
                    .combine((first, second, third, fourth) -> first + second + third + fourth);

            assertThat(called).isFalse();
            assertThat(combined.errors())
                    .extracting(ValidationErrors::rootErrors)
                    .asInstanceOf(list(TemplateString.class))
                    .map(TemplateString::message)
                    .containsExactly("Error 2", "Error 4");
        }

        @Test
        void givenDependentAndThrowsJavalidationException_whenCombine_thenCatchesAndReturnsErr() {
            var combined = Result.ok(1)
                    .and(value -> {
                        if (value > 0) {
                            throw JavalidationException.of(ErrorStrings.ERROR_1);
                        }
                        return Result.ok(0);
                    })
                    .combine((first, second) -> first + second);

            assertThat(combined.errors())
                    .extracting(ValidationErrors::rootErrors)
                    .asInstanceOf(list(TemplateString.class))
                    .map(TemplateString::message)
                    .containsExactly("Error 1");
        }

        @Test
        void givenDependentAndThrowsOtherException_whenAnd_thenPropagatesException() {
            assertThatThrownBy(() -> Result.ok(1)
                    .and(value -> {
                        throw new IllegalStateException("unexpected error");
                    }))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("unexpected error");
        }

        @Test
        void givenDependentAndChain_whenGetLast_thenReturnsLastValue() {
            var result = Result.ok("Agustin")
                    .and(name -> Result.ok(name.length()))
                    .and((name, length) -> Result.ok(name.substring(0, length - 1)))
                    .getLast();

            assertThat(result.getOrThrow()).isEqualTo("Agusti");
        }

        @Test
        void givenSkippedDependentAndIndependentError_whenGetLast_thenAccumulatesIndependentErrors() {
            var called = new AtomicBoolean(false);

            var result = Result.<String>error(ErrorStrings.ERROR_1)
                    .and(name -> {
                        called.set(true);
                        return Result.ok(name.length());
                    })
                    .and(Result.<Integer>error(ErrorStrings.ERROR_2))
                    .getLast();

            assertThat(called).isFalse();
            assertThat(result.errors())
                    .extracting(ValidationErrors::rootErrors)
                    .asInstanceOf(list(TemplateString.class))
                    .map(TemplateString::message)
                    .containsExactly("Error 1", "Error 2");
        }
    }

    @Nested
    class ResultCombiner3Tests {

        @Test
        void givenThreeOkResults_whenCombine_thenCombinesValues() {
            Result<Integer> result1 = Result.ok(1);
            Result<Integer> result2 = Result.ok(2);
            Result<Integer> result3 = Result.ok(3);

            Result<Integer> combined = result1
                    .and(result2)
                    .and(result3)
                    .combine((a, b, c) -> a + b + c);

            assertThat(combined.getOrThrow()).isEqualTo(6);
        }

        @Test
        void givenThreeErrResults_whenCombine_thenAccumulatesErrors() {
            Result<Integer> result1 = Result.error(ErrorStrings.ERROR_1);
            Result<Integer> result2 = Result.error(ErrorStrings.ERROR_2);
            Result<Integer> result3 = Result.error(ErrorStrings.ERROR_3);

            Result<Integer> combined = result1
                    .and(result2)
                    .and(result3)
                    .combine((a, b, c) -> a + b + c);

            assertThat(combined.errors())
                    .extracting(ValidationErrors::rootErrors)
                    .asInstanceOf(list(TemplateString.class))
                    .map(TemplateString::message)
                    .containsExactly("Error 1", "Error 2", "Error 3");
        }
    }

    @Nested
    class ResultCombiner4Tests {

        @Test
        void givenFourOkResults_whenCombine_thenCombinesValues() {
            Result<Integer> result1 = Result.ok(1);
            Result<Integer> result2 = Result.ok(2);
            Result<Integer> result3 = Result.ok(3);
            Result<Integer> result4 = Result.ok(4);

            Result<Integer> combined = result1
                    .and(result2)
                    .and(result3)
                    .and(result4)
                    .combine((a, b, c, d) -> a + b + c + d);

            assertThat(combined.getOrThrow()).isEqualTo(10);
        }

        @Test
        void givenFourErrResults_whenCombine_thenAccumulatesErrors() {
            Result<Integer> result1 = Result.error(ErrorStrings.ERROR_1);
            Result<Integer> result2 = Result.error(ErrorStrings.ERROR_2);
            Result<Integer> result3 = Result.error(ErrorStrings.ERROR_3);
            Result<Integer> result4 = Result.error(ErrorStrings.ERROR_4);

            Result<Integer> combined = result1
                    .and(result2)
                    .and(result3)
                    .and(result4)
                    .combine((a, b, c, d) -> a + b + c + d);

            assertThat(combined.errors())
                    .extracting(ValidationErrors::rootErrors)
                    .asInstanceOf(list(TemplateString.class))
                    .map(TemplateString::message)
                    .containsExactly("Error 1", "Error 2", "Error 3", "Error 4");
        }
    }

    @Nested
    class ResultCombiner5Tests {

        @Test
        void givenFiveOkResults_whenCombine_thenCombinesValues() {
            Result<Integer> result1 = Result.ok(1);
            Result<Integer> result2 = Result.ok(2);
            Result<Integer> result3 = Result.ok(3);
            Result<Integer> result4 = Result.ok(4);
            Result<Integer> result5 = Result.ok(5);

            Result<Integer> combined = result1
                    .and(result2)
                    .and(result3)
                    .and(result4)
                    .and(result5)
                    .combine((a, b, c, d, e) -> a + b + c + d + e);

            assertThat(combined.getOrThrow()).isEqualTo(15);
        }

        @Test
        void givenFiveErrResults_whenCombine_thenAccumulatesErrors() {
            Result<Integer> result1 = Result.error(ErrorStrings.ERROR_1);
            Result<Integer> result2 = Result.error(ErrorStrings.ERROR_2);
            Result<Integer> result3 = Result.error(ErrorStrings.ERROR_3);
            Result<Integer> result4 = Result.error(ErrorStrings.ERROR_4);
            Result<Integer> result5 = Result.error(ErrorStrings.ERROR_5);

            Result<Integer> combined = result1
                    .and(result2)
                    .and(result3)
                    .and(result4)
                    .and(result5)
                    .combine((a, b, c, d, e) -> a + b + c + d + e);

            assertThat(combined.errors())
                    .extracting(ValidationErrors::rootErrors)
                    .asInstanceOf(list(TemplateString.class))
                    .map(TemplateString::message)
                    .containsExactly("Error 1", "Error 2", "Error 3", "Error 4", "Error 5");
        }
    }

    @Nested
    class ResultCombiner6Tests {

        @Test
        void givenSixOkResults_whenCombine_thenCombinesValues() {
            Result<Integer> result1 = Result.ok(1);
            Result<Integer> result2 = Result.ok(2);
            Result<Integer> result3 = Result.ok(3);
            Result<Integer> result4 = Result.ok(4);
            Result<Integer> result5 = Result.ok(5);
            Result<Integer> result6 = Result.ok(6);

            Result<Integer> combined = result1
                    .and(result2)
                    .and(result3)
                    .and(result4)
                    .and(result5)
                    .and(result6)
                    .combine((a, b, c, d, e, f) -> a + b + c + d + e + f);

            assertThat(combined.getOrThrow()).isEqualTo(21);
        }

        @Test
        void givenSixErrResults_whenCombine_thenAccumulatesErrors() {
            Result<Integer> result1 = Result.error(ErrorStrings.ERROR_1);
            Result<Integer> result2 = Result.error(ErrorStrings.ERROR_2);
            Result<Integer> result3 = Result.error(ErrorStrings.ERROR_3);
            Result<Integer> result4 = Result.error(ErrorStrings.ERROR_4);
            Result<Integer> result5 = Result.error(ErrorStrings.ERROR_5);
            Result<Integer> result6 = Result.error(ErrorStrings.ERROR_6);

            Result<Integer> combined = result1
                    .and(result2)
                    .and(result3)
                    .and(result4)
                    .and(result5)
                    .and(result6)
                    .combine((a, b, c, d, e, f) -> a + b + c + d + e + f);

            assertThat(combined.errors())
                    .extracting(ValidationErrors::rootErrors)
                    .asInstanceOf(list(TemplateString.class))
                    .map(TemplateString::message)
                    .containsExactly("Error 1", "Error 2", "Error 3", "Error 4", "Error 5", "Error 6");
        }
    }

    @Nested
    class ResultCombiner7Tests {

        @Test
        void givenSevenOkResults_whenCombine_thenCombinesValues() {
            Result<Integer> result1 = Result.ok(1);
            Result<Integer> result2 = Result.ok(2);
            Result<Integer> result3 = Result.ok(3);
            Result<Integer> result4 = Result.ok(4);
            Result<Integer> result5 = Result.ok(5);
            Result<Integer> result6 = Result.ok(6);
            Result<Integer> result7 = Result.ok(7);

            Result<Integer> combined = result1
                    .and(result2)
                    .and(result3)
                    .and(result4)
                    .and(result5)
                    .and(result6)
                    .and(result7)
                    .combine((a, b, c, d, e, f, g) -> a + b + c + d + e + f + g);

            assertThat(combined.getOrThrow()).isEqualTo(28);
        }

        @Test
        void givenSevenErrResults_whenCombine_thenAccumulatesErrors() {
            Result<Integer> result1 = Result.error(ErrorStrings.ERROR_1);
            Result<Integer> result2 = Result.error(ErrorStrings.ERROR_2);
            Result<Integer> result3 = Result.error(ErrorStrings.ERROR_3);
            Result<Integer> result4 = Result.error(ErrorStrings.ERROR_4);
            Result<Integer> result5 = Result.error(ErrorStrings.ERROR_5);
            Result<Integer> result6 = Result.error(ErrorStrings.ERROR_6);
            Result<Integer> result7 = Result.error(ErrorStrings.ERROR_7);

            Result<Integer> combined = result1
                    .and(result2)
                    .and(result3)
                    .and(result4)
                    .and(result5)
                    .and(result6)
                    .and(result7)
                    .combine((a, b, c, d, e, f, g) -> a + b + c + d + e + f + g);

            assertThat(combined.errors())
                    .extracting(ValidationErrors::rootErrors)
                    .asInstanceOf(list(TemplateString.class))
                    .map(TemplateString::message)
                    .containsExactly("Error 1", "Error 2", "Error 3", "Error 4", "Error 5", "Error 6", "Error 7");
        }
    }

    @Nested
    class ResultCombiner8Tests {

        @Test
        void givenEightOkResults_whenCombine_thenCombinesValues() {
            Result<Integer> result1 = Result.ok(1);
            Result<Integer> result2 = Result.ok(2);
            Result<Integer> result3 = Result.ok(3);
            Result<Integer> result4 = Result.ok(4);
            Result<Integer> result5 = Result.ok(5);
            Result<Integer> result6 = Result.ok(6);
            Result<Integer> result7 = Result.ok(7);
            Result<Integer> result8 = Result.ok(8);

            Result<Integer> combined = result1
                    .and(result2)
                    .and(result3)
                    .and(result4)
                    .and(result5)
                    .and(result6)
                    .and(result7)
                    .and(result8)
                    .combine((a, b, c, d, e, f, g, h) -> a + b + c + d + e + f + g + h);

            assertThat(combined.getOrThrow()).isEqualTo(36);
        }

        @Test
        void givenEightErrResults_whenCombine_thenAccumulatesErrors() {
            Result<Integer> result1 = Result.error(ErrorStrings.ERROR_1);
            Result<Integer> result2 = Result.error(ErrorStrings.ERROR_2);
            Result<Integer> result3 = Result.error(ErrorStrings.ERROR_3);
            Result<Integer> result4 = Result.error(ErrorStrings.ERROR_4);
            Result<Integer> result5 = Result.error(ErrorStrings.ERROR_5);
            Result<Integer> result6 = Result.error(ErrorStrings.ERROR_6);
            Result<Integer> result7 = Result.error(ErrorStrings.ERROR_7);
            Result<Integer> result8 = Result.error(ErrorStrings.ERROR_8);

            Result<Integer> combined = result1
                    .and(result2)
                    .and(result3)
                    .and(result4)
                    .and(result5)
                    .and(result6)
                    .and(result7)
                    .and(result8)
                    .combine((a, b, c, d, e, f, g, h) -> a + b + c + d + e + f + g + h);

            assertThat(combined.errors())
                    .extracting(ValidationErrors::rootErrors)
                    .asInstanceOf(list(TemplateString.class))
                    .map(TemplateString::message)
                    .containsExactly("Error 1", "Error 2", "Error 3", "Error 4", "Error 5", "Error 6", "Error 7", "Error 8");
        }
    }

    @Nested
    class ResultCombiner9Tests {

        @Test
        void givenNineOkResults_whenCombine_thenCombinesValues() {
            Result<Integer> result1 = Result.ok(1);
            Result<Integer> result2 = Result.ok(2);
            Result<Integer> result3 = Result.ok(3);
            Result<Integer> result4 = Result.ok(4);
            Result<Integer> result5 = Result.ok(5);
            Result<Integer> result6 = Result.ok(6);
            Result<Integer> result7 = Result.ok(7);
            Result<Integer> result8 = Result.ok(8);
            Result<Integer> result9 = Result.ok(9);

            Result<Integer> combined = result1
                    .and(result2)
                    .and(result3)
                    .and(result4)
                    .and(result5)
                    .and(result6)
                    .and(result7)
                    .and(result8)
                    .and(result9)
                    .combine((a, b, c, d, e, f, g, h, i) -> a + b + c + d + e + f + g + h + i);

            assertThat(combined.getOrThrow()).isEqualTo(45);
        }

        @Test
        void givenNineErrResults_whenCombine_thenAccumulatesErrors() {
            Result<Integer> result1 = Result.error(ErrorStrings.ERROR_1);
            Result<Integer> result2 = Result.error(ErrorStrings.ERROR_2);
            Result<Integer> result3 = Result.error(ErrorStrings.ERROR_3);
            Result<Integer> result4 = Result.error(ErrorStrings.ERROR_4);
            Result<Integer> result5 = Result.error(ErrorStrings.ERROR_5);
            Result<Integer> result6 = Result.error(ErrorStrings.ERROR_6);
            Result<Integer> result7 = Result.error(ErrorStrings.ERROR_7);
            Result<Integer> result8 = Result.error(ErrorStrings.ERROR_8);
            Result<Integer> result9 = Result.error(ErrorStrings.ERROR_9);

            Result<Integer> combined = result1
                    .and(result2)
                    .and(result3)
                    .and(result4)
                    .and(result5)
                    .and(result6)
                    .and(result7)
                    .and(result8)
                    .and(result9)
                    .combine((a, b, c, d, e, f, g, h, i) -> a + b + c + d + e + f + g + h + i);

            assertThat(combined.errors())
                    .extracting(ValidationErrors::rootErrors)
                    .asInstanceOf(list(TemplateString.class))
                    .map(TemplateString::message)
                    .containsExactly("Error 1", "Error 2", "Error 3", "Error 4", "Error 5", "Error 6", "Error 7", "Error 8", "Error 9");
        }
    }

    @Nested
    class ResultCombiner10Tests {

        @Test
        void givenTenOkResults_whenCombine_thenCombinesValues() {
            Result<Integer> result1 = Result.ok(1);
            Result<Integer> result2 = Result.ok(2);
            Result<Integer> result3 = Result.ok(3);
            Result<Integer> result4 = Result.ok(4);
            Result<Integer> result5 = Result.ok(5);
            Result<Integer> result6 = Result.ok(6);
            Result<Integer> result7 = Result.ok(7);
            Result<Integer> result8 = Result.ok(8);
            Result<Integer> result9 = Result.ok(9);
            Result<Integer> result10 = Result.ok(10);

            Result<Integer> combined = result1
                    .and(result2)
                    .and(result3)
                    .and(result4)
                    .and(result5)
                    .and(result6)
                    .and(result7)
                    .and(result8)
                    .and(result9)
                    .and(result10)
                    .combine((a, b, c, d, e, f, g, h, i, j) -> a + b + c + d + e + f + g + h + i + j);

            assertThat(combined.getOrThrow()).isEqualTo(55);
        }

        @Test
        void givenTenErrResults_whenCombine_thenAccumulatesErrors() {
            Result<Integer> result1 = Result.error(ErrorStrings.ERROR_1);
            Result<Integer> result2 = Result.error(ErrorStrings.ERROR_2);
            Result<Integer> result3 = Result.error(ErrorStrings.ERROR_3);
            Result<Integer> result4 = Result.error(ErrorStrings.ERROR_4);
            Result<Integer> result5 = Result.error(ErrorStrings.ERROR_5);
            Result<Integer> result6 = Result.error(ErrorStrings.ERROR_6);
            Result<Integer> result7 = Result.error(ErrorStrings.ERROR_7);
            Result<Integer> result8 = Result.error(ErrorStrings.ERROR_8);
            Result<Integer> result9 = Result.error(ErrorStrings.ERROR_9);
            Result<Integer> result10 = Result.error(ErrorStrings.ERROR_10);

            Result<Integer> combined = result1
                    .and(result2)
                    .and(result3)
                    .and(result4)
                    .and(result5)
                    .and(result6)
                    .and(result7)
                    .and(result8)
                    .and(result9)
                    .and(result10)
                    .combine((a, b, c, d, e, f, g, h, i, j) -> a + b + c + d + e + f + g + h + i + j);

            assertThat(combined.errors())
                    .extracting(ValidationErrors::rootErrors)
                    .asInstanceOf(list(TemplateString.class))
                    .map(TemplateString::message)
                    .containsExactly("Error 1", "Error 2", "Error 3", "Error 4", "Error 5", "Error 6", "Error 7", "Error 8", "Error 9", "Error 10");
        }
    }
}
