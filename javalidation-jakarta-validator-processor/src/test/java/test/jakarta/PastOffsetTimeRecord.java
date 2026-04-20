package test.jakarta;

import jakarta.validation.constraints.*;
import java.time.*;

public record PastOffsetTimeRecord(@Past OffsetTime value) {}
