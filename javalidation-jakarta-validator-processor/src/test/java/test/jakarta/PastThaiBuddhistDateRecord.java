package test.jakarta;

import jakarta.validation.constraints.*;
import java.time.chrono.*;

public record PastThaiBuddhistDateRecord(@Past ThaiBuddhistDate value) {}
