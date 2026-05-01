package io.github.raniagus.javalidation;

/**
 * An immutable cons-list of {@link FieldKeyPart} segments used to accumulate field-path prefixes
 * as they flow through a chain of {@link ResultCollectorWrapper} instances.
 *
 * <h2>Structure</h2>
 * <pre>
 * PrefixStack
 *   ├─ Empty    — the end of the list (singleton via {@link #EMPTY})
 *   └─ Cons     — a head segment prepended to a tail PrefixStack, carrying its own size
 * </pre>
 *
 * <h2>Building a stack</h2>
 * Use the typed factory and builder methods to avoid constructing {@link FieldKeyPart} explicitly:
 * <ul>
 *   <li>{@link #of(String)} / {@link #of(int)} — one-element stack from a string or int segment</li>
 *   <li>{@link #of(FieldKeyPart)} — one-element stack from an explicit part (e.g. inside
 *       {@link ResultCollectorWrapper.WithPrefix} which already holds a {@link FieldKeyPart})</li>
 *   <li>{@link #prepend(String)} / {@link #prepend(int)} / {@link #prepend(FieldKeyPart)} —
 *       extend an existing stack with one more segment</li>
 * </ul>
 *
 * <h2>Ordering contract</h2>
 * The list is built <em>innermost-first</em>: each wrapper in the collector chain receives the
 * stack accumulated so far and prepends its own segment. Because the outermost wrapper acts first
 * and the innermost acts last, the outermost segment ends up deepest in the tail chain (furthest
 * from the head).
 *
 * <p>For example, given {@code withPrefix("order", withPrefix("items", withIndex(toResultList())))}:
 * <ol>
 *   <li>{@code WithPrefix("order")} creates {@code PrefixStack.of("order")} → {@code Cons(StringKey("order"), EMPTY, 1)}</li>
 *   <li>{@code WithPrefix("items")} prepends: {@code Cons(StringKey("items"), Cons(StringKey("order"), EMPTY, 1), 2)}</li>
 *   <li>{@code WithIndex} prepends: {@code Cons(IntKey(i), Cons(StringKey("items"), …, 2), 3)}</li>
 *   <li>The leaf collector calls {@link #toFieldKey()}, which allocates an array of {@link #size()}
 *       and fills it from the last index toward 0 — producing
 *       {@code [StringKey("order"), StringKey("items"), IntKey(i)]} in outermost-first order,
 *       then wraps it in a {@link FieldKey}.</li>
 * </ol>
 *
 * <h2>Why {@code size} is stored in {@code Cons}</h2>
 * {@link #toFieldKey()} is called once per stream element (N elements → N calls, each on a fresh
 * instance). Storing {@code size} eliminates the counting traversal on every one of those N calls,
 * halving the total traversal work from N×2D to N×D (where D is wrapper depth).
 *
 * @see ResultCollectorWrapper
 * @see FieldKeyPart
 */
public sealed interface PrefixStack {

    /** Singleton empty stack — use {@link #empty()} rather than constructing directly. */
    Empty EMPTY = new Empty();

    /** The empty terminus of every {@code PrefixStack} chain. */
    record Empty() implements PrefixStack {
        @Override
        public int size() {
            return 0;
        }
    }

    /**
     * A non-empty node prepending {@code head} in front of {@code tail}.
     * <p>
     * {@code size} is always {@code tail.size() + 1} — maintained automatically by
     * {@link #prepend(FieldKeyPart)}, {@link #prepend(String)}, {@link #prepend(int)},
     * {@link #of(FieldKeyPart)}, {@link #of(String)}, and {@link #of(int)}.
     */
    record Cons(FieldKeyPart head, PrefixStack tail, int size) implements PrefixStack {}

    /** Returns the number of segments in this stack. O(1). */
    int size();

    /** Returns the singleton empty stack. */
    static PrefixStack empty() {
        return EMPTY;
    }

    /**
     * Returns a single-element stack containing a {@link FieldKeyPart.StringKey} for {@code part}.
     *
     * @param part the string segment
     * @return {@code Cons(StringKey(part), EMPTY, 1)}
     */
    static PrefixStack of(String part) {
        return new Cons(new FieldKeyPart.StringKey(part), EMPTY, 1);
    }

    /**
     * Returns a single-element stack containing a {@link FieldKeyPart.IntKey} for {@code part}.
     *
     * @param part the integer segment
     * @return {@code Cons(IntKey(part), EMPTY, 1)}
     */
    static PrefixStack of(int part) {
        return new Cons(new FieldKeyPart.IntKey(part), EMPTY, 1);
    }

    /**
     * Returns a single-element stack containing the given {@link FieldKeyPart}.
     * <p>
     * Prefer {@link #of(String)} or {@link #of(int)} unless you already hold a
     * {@link FieldKeyPart} instance (e.g. inside {@link ResultCollectorWrapper.WithPrefix}).
     *
     * @param part the segment
     * @return {@code Cons(part, EMPTY, 1)}
     */
    static PrefixStack of(FieldKeyPart part) {
        return new Cons(part, EMPTY, 1);
    }

    /**
     * Returns a new stack with a {@link FieldKeyPart.StringKey} for {@code part} prepended.
     *
     * @param part the string segment to prepend
     * @return {@code Cons(StringKey(part), this, this.size() + 1)}
     */
    default PrefixStack prepend(String part) {
        return new Cons(new FieldKeyPart.StringKey(part), this, size() + 1);
    }

    /**
     * Returns a new stack with a {@link FieldKeyPart.IntKey} for {@code part} prepended.
     *
     * @param part the integer segment to prepend
     * @return {@code Cons(IntKey(part), this, this.size() + 1)}
     */
    default PrefixStack prepend(int part) {
        return new Cons(new FieldKeyPart.IntKey(part), this, size() + 1);
    }

    /**
     * Returns a new stack with the given {@link FieldKeyPart} prepended.
     * <p>
     * Prefer {@link #prepend(String)} or {@link #prepend(int)} unless you already hold a
     * {@link FieldKeyPart} instance (e.g. inside {@link ResultCollectorWrapper.WithPrefix}).
     *
     * @param part the segment to prepend
     * @return {@code Cons(part, this, this.size() + 1)}
     */
    default PrefixStack prepend(FieldKeyPart part) {
        return new Cons(part, this, size() + 1);
    }

    /** Returns {@code true} if this stack contains no segments. */
    default boolean isEmpty() {
        return this instanceof Empty;
    }

    /**
     * Converts this stack to a {@link FieldKey} in outermost-first segment order.
     * <p>
     * Because the cons-list is built innermost-first (the outermost wrapper's segment sits deepest
     * in the tail chain), this method allocates an array of {@link #size()} and fills it from the
     * last index toward index 0. The result is wrapped in a {@link FieldKey} ready for use in
     * {@link Validation#addAllAt(FieldKey, ValidationErrors)}.
     *
     * @return a {@link FieldKey} with segments in outermost-first order, or an empty-array
     *         {@link FieldKey} if this stack is {@link Empty}
     */
    default FieldKey toFieldKey() {
        FieldKeyPart[] arr = new FieldKeyPart[size()];
        int i = size() - 1;
        for (PrefixStack s = this; s instanceof Cons c; s = c.tail()) {
            arr[i--] = c.head();
        }
        return FieldKey.of(arr);
    }
}
