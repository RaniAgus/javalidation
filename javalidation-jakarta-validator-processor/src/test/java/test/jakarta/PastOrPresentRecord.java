package test.jakarta;

import jakarta.validation.constraints.*;
import java.time.*;

public record PastOrPresentRecord(@PastOrPresent Instant value) {}
