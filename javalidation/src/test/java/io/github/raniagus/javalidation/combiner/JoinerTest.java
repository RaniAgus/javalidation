package io.github.raniagus.javalidation.combiner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

import io.github.raniagus.javalidation.Result;
import io.github.raniagus.javalidation.ValidationErrors;
import io.github.raniagus.javalidation.TemplateString;
import io.github.raniagus.javalidation.util.ErrorStrings;
import io.github.raniagus.javalidation.util.Person;
import org.junit.jupiter.api.Test;

class JoinerTest {

    // -- ResultCombiner2 --

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

        assertThat(combined.getErrors())
                .extracting(ValidationErrors::rootErrors)
                .asInstanceOf(list(TemplateString.class))
                .map(TemplateString::message)
                .containsExactly("Error 1", "Error 2");
    }

    // -- ResultCombiner3 --

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

        assertThat(combined.getErrors())
                .extracting(ValidationErrors::rootErrors)
                .asInstanceOf(list(TemplateString.class))
                .map(TemplateString::message)
                .containsExactly("Error 1", "Error 2", "Error 3");
    }

    // -- ResultCombiner4 --

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

        assertThat(combined.getErrors())
                .extracting(ValidationErrors::rootErrors)
                .asInstanceOf(list(TemplateString.class))
                .map(TemplateString::message)
                .containsExactly("Error 1", "Error 2", "Error 3", "Error 4");
    }

    // -- ResultCombiner5 --

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

        assertThat(combined.getErrors())
                .extracting(ValidationErrors::rootErrors)
                .asInstanceOf(list(TemplateString.class))
                .map(TemplateString::message)
                .containsExactly("Error 1", "Error 2", "Error 3", "Error 4", "Error 5");
    }

    // -- ResultCombiner6 --

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

        assertThat(combined.getErrors())
                .extracting(ValidationErrors::rootErrors)
                .asInstanceOf(list(TemplateString.class))
                .map(TemplateString::message)
                .containsExactly("Error 1", "Error 2", "Error 3", "Error 4", "Error 5", "Error 6");
    }

    // -- ResultCombiner7 --

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

        assertThat(combined.getErrors())
                .extracting(ValidationErrors::rootErrors)
                .asInstanceOf(list(TemplateString.class))
                .map(TemplateString::message)
                .containsExactly("Error 1", "Error 2", "Error 3", "Error 4", "Error 5", "Error 6", "Error 7");
    }

    // -- ResultCombiner8 --

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

        assertThat(combined.getErrors())
                .extracting(ValidationErrors::rootErrors)
                .asInstanceOf(list(TemplateString.class))
                .map(TemplateString::message)
                .containsExactly("Error 1", "Error 2", "Error 3", "Error 4", "Error 5", "Error 6", "Error 7", "Error 8");
    }

    // -- ResultCombiner9 --

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

        assertThat(combined.getErrors())
                .extracting(ValidationErrors::rootErrors)
                .asInstanceOf(list(TemplateString.class))
                .map(TemplateString::message)
                .containsExactly("Error 1", "Error 2", "Error 3", "Error 4", "Error 5", "Error 6", "Error 7", "Error 8", "Error 9");
    }

    // -- ResultCombiner10 --

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

        assertThat(combined.getErrors())
                .extracting(ValidationErrors::rootErrors)
                .asInstanceOf(list(TemplateString.class))
                .map(TemplateString::message)
                .containsExactly("Error 1", "Error 2", "Error 3", "Error 4", "Error 5", "Error 6", "Error 7", "Error 8", "Error 9", "Error 10");
    }
}
