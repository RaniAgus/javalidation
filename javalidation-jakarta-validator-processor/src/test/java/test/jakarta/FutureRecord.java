package test.jakarta;

import io.github.raniagus.javalidation.validator.*;
import jakarta.validation.constraints.*;
import java.time.*;

@Validate
public record FutureRecord(@Future Instant value) {}
