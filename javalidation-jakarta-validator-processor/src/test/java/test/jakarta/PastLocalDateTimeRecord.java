package test.jakarta;

import jakarta.validation.constraints.*;
import java.time.*;

public record PastLocalDateTimeRecord(@Past LocalDateTime value) {}
