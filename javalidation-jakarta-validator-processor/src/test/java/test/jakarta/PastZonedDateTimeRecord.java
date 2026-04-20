package test.jakarta;

import jakarta.validation.constraints.*;
import java.time.*;

public record PastZonedDateTimeRecord(@Past ZonedDateTime value) {}
