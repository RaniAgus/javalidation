package test.jakarta;

import jakarta.validation.constraints.*;
import java.time.*;

public record FutureOrPresentRecord(@FutureOrPresent Instant value) {}
