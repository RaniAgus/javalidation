package io.github.raniagus.javalidation.format;

import io.github.raniagus.javalidation.FieldKey;
import org.jspecify.annotations.Nullable;

/**
 * Strategy interface for parsing a string into a {@link FieldKey}.
 * <p>
 * This is the inverse of {@link FieldKeyFormatter}: given a string produced by a formatter,
 * the matching parser reconstructs the original {@link FieldKey}.
 * <p>
 * The default implementation is {@link PropertyPathNotationParser}, which handles
 * paths such as {@code "items[0].price"}.
 *
 * @see FieldKeyFormatter
 */
@FunctionalInterface
public interface FieldKeyParser {
    /**
     * Parses the given string into a {@link FieldKey}.
     *
     * @param path the string representation of the field path
     * @return the parsed {@link FieldKey}
     * @throws IllegalArgumentException if the string is not valid for this notation
     */
    FieldKey parse(@Nullable String path);

    /**
     * Returns the default parser, which uses property-path notation (e.g. {@code "items[0].price"}).
     *
     * @return the default {@link FieldKeyParser}
     */
    static FieldKeyParser getDefault() {
        return new PropertyPathNotationParser();
    }
}
