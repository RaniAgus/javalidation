package test.jakarta;

import jakarta.validation.constraints.*;
import java.time.*;

public record PastOffsetDateTimeRecord(@Past OffsetDateTime value) {}
