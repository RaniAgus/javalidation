package io.github.raniagus.javalidation.format;

import io.github.raniagus.javalidation.FieldKey;
import io.github.raniagus.javalidation.FieldKeyPart;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * A {@link FieldKeyParser} that handles property-path notation.
 * <p>
 * Each bare name (optionally preceded by {@code .}) becomes a {@link FieldKeyPart.StringKey}.
 * Each {@code [n]} bracket must contain a valid integer and becomes a {@link FieldKeyPart.IntKey};
 * noninteger bracket content throws {@link IllegalArgumentException}.
 *
 * @see PropertyPathNotationFormatter
 */
public class PropertyPathNotationParser implements FieldKeyParser {
    private static final Pattern TOKEN = Pattern.compile(
            "(?:^|\\.)([^.\\[\\]]+)|\\[(0|-?[1-9]\\d*)]"
    );

    @Override
    public FieldKey parse(@Nullable String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Path must not be null or empty");
        }

        List<FieldKeyPart> parts = new ArrayList<>();
        Matcher m = TOKEN.matcher(path);
        int lastEnd = 0;

        while (m.find()) {
            if (m.start() != lastEnd) {
                throw new IllegalArgumentException(
                        "Invalid property-path notation at index " + lastEnd + " in: '" + path + "'");
            }
            lastEnd = m.end();

            String name = m.group(1);
            parts.add(name != null
                    ? new FieldKeyPart.StringKey(name)
                    : new FieldKeyPart.IntKey(Integer.parseInt(m.group(2)))
            );
        }

        if (lastEnd != path.length()) {
            throw new IllegalArgumentException(
                    "Invalid property-path notation at index " + lastEnd + " in: '" + path + "'");
        }

        return FieldKey.of(parts.toArray(FieldKeyPart[]::new));
    }
}
