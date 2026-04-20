package test.jakarta;

import jakarta.validation.constraints.*;
import java.time.chrono.*;

public record PastMinguoDateRecord(@Past MinguoDate value) {}
