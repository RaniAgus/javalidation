package test.jakarta;

import jakarta.validation.constraints.*;
import java.time.*;

public record PastLocalDateRecord(@Past LocalDate value) {}
