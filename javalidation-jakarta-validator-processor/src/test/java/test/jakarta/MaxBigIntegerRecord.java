package test.jakarta;

import jakarta.validation.constraints.Max;
import java.math.BigInteger;

public record MaxBigIntegerRecord(@Max(100) BigInteger value) {}
