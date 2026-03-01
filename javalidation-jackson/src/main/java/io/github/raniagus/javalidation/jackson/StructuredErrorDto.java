package io.github.raniagus.javalidation.jackson;

import io.github.raniagus.javalidation.TemplateString;
import io.github.raniagus.javalidation.format.TemplateStringFormatter;
import java.util.Arrays;
import java.util.Objects;

record StructuredErrorDto(String message, String code, Object[] args) {
    StructuredErrorDto {
        args = Arrays.copyOf(args, args.length);
    }

    static StructuredErrorDto from(TemplateString templateString, TemplateStringFormatter formatter) {
        return new StructuredErrorDto(
                formatter.format(templateString),
                templateString.message(),
                templateString.args()
        );
    }

    TemplateString toTemplateString() {
        return TemplateString.of(code, args);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StructuredErrorDto(String msg, String cd, Object[] argz))) return false;
        return Objects.equals(message, msg) 
            && Objects.equals(code, cd) 
            && Arrays.equals(args, argz);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(message);
        result = 31 * result + Objects.hashCode(code);
        result = 31 * result + Arrays.hashCode(args);
        return result;
    }

    @Override
    public String toString() {
        return "StructuredErrorDto{" +
                "message='" + message + '\'' +
                ", code='" + code + '\'' +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
