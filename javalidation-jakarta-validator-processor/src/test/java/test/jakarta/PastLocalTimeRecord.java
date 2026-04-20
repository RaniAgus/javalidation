package test.jakarta;

import jakarta.validation.constraints.*;
import java.time.*;

public record PastLocalTimeRecord(@Past LocalTime value) {}
