package test.jakarta;

import jakarta.validation.constraints.*;
import java.time.*;

public record FutureRecord(@Future Instant value) {}
