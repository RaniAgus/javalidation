package test.jakarta;

import jakarta.validation.constraints.*;
import java.time.*;

public record PastMonthDayRecord(@Past MonthDay value) {}
