package test.jakarta;

import jakarta.validation.constraints.*;
import java.time.*;

public record PastRecord(@Past Instant value) {}
