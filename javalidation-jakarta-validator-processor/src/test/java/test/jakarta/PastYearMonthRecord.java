package test.jakarta;

import jakarta.validation.constraints.*;
import java.time.*;

public record PastYearMonthRecord(@Past YearMonth value) {}
