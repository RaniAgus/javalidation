package io.github.raniagus.javalidation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PrefixStackTest {

    @Nested
    class EmptyTests {

        @Test
        void givenEmpty_whenIsEmpty_thenTrue() {
            assertThat(PrefixStack.empty().isEmpty()).isTrue();
        }

        @Test
        void givenEmpty_whenSize_thenZero() {
            assertThat(PrefixStack.empty().size()).isZero();
        }

        @Test
        void givenEmptyCalledTwice_whenCompared_thenSameInstance() {
            assertThat(PrefixStack.empty()).isSameAs(PrefixStack.empty());
        }

        @Test
        void givenEmptyConstant_whenComparedToEmptyMethod_thenSameInstance() {
            assertThat(PrefixStack.empty()).isSameAs(PrefixStack.EMPTY);
        }

        @Test
        void givenEmpty_whenToFieldKey_thenFieldKeyWithNoParts() {
            assertThat(PrefixStack.empty().toFieldKey().parts()).isEmpty();
        }
    }

    @Nested
    class OfTests {

        @Test
        void givenString_whenOf_thenSingleElementStackWithStringKey() {
            PrefixStack stack = PrefixStack.of("field");

            assertThat(stack.isEmpty()).isFalse();
            assertThat(stack.size()).isOne();
            assertThat(stack).isEqualTo(new PrefixStack.Cons(new FieldKeyPart.StringKey("field"), PrefixStack.EMPTY, 1));
        }

        @Test
        void givenInt_whenOf_thenSingleElementStackWithIntKey() {
            PrefixStack stack = PrefixStack.of(3);

            assertThat(stack.isEmpty()).isFalse();
            assertThat(stack.size()).isOne();
            assertThat(stack).isEqualTo(new PrefixStack.Cons(new FieldKeyPart.IntKey(3), PrefixStack.EMPTY, 1));
        }

        @Test
        void givenFieldKeyPart_whenOf_thenSingleElementStackWithThatPart() {
            FieldKeyPart part = new FieldKeyPart.StringKey("x");
            PrefixStack stack = PrefixStack.of(part);

            assertThat(stack.isEmpty()).isFalse();
            assertThat(stack.size()).isOne();
            assertThat(stack).isEqualTo(new PrefixStack.Cons(part, PrefixStack.EMPTY, 1));
        }

        @Test
        void givenString_whenOf_thenTailIsEmptySingleton() {
            PrefixStack.Cons cons = (PrefixStack.Cons) PrefixStack.of("field");

            assertThat(cons.tail()).isSameAs(PrefixStack.EMPTY);
        }

        @Test
        void givenInt_whenOf_thenTailIsEmptySingleton() {
            PrefixStack.Cons cons = (PrefixStack.Cons) PrefixStack.of(0);

            assertThat(cons.tail()).isSameAs(PrefixStack.EMPTY);
        }
    }

    @Nested
    class PrependTests {

        @Test
        void givenStringPrependedToEmpty_whenSize_thenOne() {
            assertThat(PrefixStack.empty().prepend("field").size()).isOne();
        }

        @Test
        void givenIntPrependedToEmpty_whenSize_thenOne() {
            assertThat(PrefixStack.empty().prepend(0).size()).isOne();
        }

        @Test
        void givenFieldKeyPartPrependedToEmpty_whenSize_thenOne() {
            assertThat(PrefixStack.empty().prepend(new FieldKeyPart.StringKey("x")).size()).isOne();
        }

        @Test
        void givenStringPrependedToSingleElement_whenSize_thenTwo() {
            assertThat(PrefixStack.of("a").prepend("b").size()).isEqualTo(2);
        }

        @Test
        void givenThreePrependsChained_whenSize_thenThree() {
            PrefixStack stack = PrefixStack.of("a").prepend("b").prepend(0);

            assertThat(stack.size()).isEqualTo(3);
        }

        @Test
        void givenStringPrepend_whenInspectingHead_thenStringKeyCreated() {
            PrefixStack.Cons cons = (PrefixStack.Cons) PrefixStack.empty().prepend("items");

            assertThat(cons.head()).isEqualTo(new FieldKeyPart.StringKey("items"));
        }

        @Test
        void givenIntPrepend_whenInspectingHead_thenIntKeyCreated() {
            PrefixStack.Cons cons = (PrefixStack.Cons) PrefixStack.empty().prepend(2);

            assertThat(cons.head()).isEqualTo(new FieldKeyPart.IntKey(2));
        }

        @Test
        void givenPrepend_whenInspectingTail_thenTailIsOriginalStack() {
            PrefixStack original = PrefixStack.of("a");
            PrefixStack.Cons cons = (PrefixStack.Cons) original.prepend("b");

            assertThat(cons.tail()).isSameAs(original);
        }

        @Test
        void givenOriginalStack_whenPrepend_thenOriginalIsUnmodified() {
            PrefixStack original = PrefixStack.of("a");
            original.prepend("b");

            assertThat(original.size()).isOne();
        }
    }

    @Nested
    class ToFieldKeyTests {

        @Test
        void givenSingleStringElement_whenToFieldKey_thenFieldKeyWithStringKey() {
            FieldKey key = PrefixStack.of("field").toFieldKey();

            assertThat(key).isEqualTo(FieldKey.of("field"));
        }

        @Test
        void givenSingleIntElement_whenToFieldKey_thenFieldKeyWithIntKey() {
            FieldKey key = PrefixStack.of(0).toFieldKey();

            assertThat(key).isEqualTo(FieldKey.of(new FieldKeyPart.IntKey(0)));
        }

        @Test
        void givenTwoElements_whenToFieldKey_thenOutermostSegmentFirst() {
            // Simulates WithPrefix("order") creating PrefixStack.of("order"),
            // then WithPrefix("items") calling incoming.prepend("items").
            // Expected FieldKey: ["order", "items"] — outermost first.
            FieldKey key = PrefixStack.of("order")
                    .prepend("items")
                    .toFieldKey();

            assertThat(key).isEqualTo(FieldKey.of("order", "items"));
        }

        @Test
        void givenThreeElements_whenToFieldKey_thenOutermostSegmentFirst() {
            // Simulates withPrefix("order", withPrefix("items", withIndex(toResultList())))
            // for element i=0: PrefixStack.of("order").prepend("items").prepend(0)
            // Expected: FieldKey(["order", "items", 0]) — outermost first.
            FieldKey key = PrefixStack.of("order")
                    .prepend("items")
                    .prepend(0)
                    .toFieldKey();

            assertThat(key).isEqualTo(FieldKey.of("order", "items", 0));
        }

        @Test
        void givenMixedStringAndIntElements_whenToFieldKey_thenCorrectPartsAndOrder() {
            FieldKey key = PrefixStack.of(new FieldKeyPart.StringKey("addresses"))
                    .prepend(new FieldKeyPart.IntKey(2))
                    .prepend(new FieldKeyPart.StringKey("street"))
                    .toFieldKey();

            assertThat(key.parts()).containsExactly(
                    new FieldKeyPart.StringKey("addresses"),
                    new FieldKeyPart.IntKey(2),
                    new FieldKeyPart.StringKey("street")
            );
        }

        @Test
        void givenToFieldKeyCalledTwice_whenCompared_thenBothEqual() {
            PrefixStack stack = PrefixStack.of("a").prepend("b");

            assertThat(stack.toFieldKey()).isEqualTo(stack.toFieldKey());
        }
    }

    @Nested
    class SizeTests {

        @Test
        void givenDeepChain_whenSize_thenMatchesDepth() {
            PrefixStack stack = PrefixStack.of("a")
                    .prepend("b")
                    .prepend("c")
                    .prepend("d")
                    .prepend("e");

            assertThat(stack.size()).isEqualTo(5);
        }

        @Test
        void givenEachPrependStep_whenSize_thenIncrementsBy1() {
            PrefixStack s1 = PrefixStack.of("a");
            PrefixStack s2 = s1.prepend("b");
            PrefixStack s3 = s2.prepend("c");

            assertThat(s1.size()).isEqualTo(1);
            assertThat(s2.size()).isEqualTo(2);
            assertThat(s3.size()).isEqualTo(3);
        }
    }
}
