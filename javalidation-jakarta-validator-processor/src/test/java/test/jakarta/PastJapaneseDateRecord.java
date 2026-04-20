package test.jakarta;

import jakarta.validation.constraints.*;
import java.time.chrono.*;

public record PastJapaneseDateRecord(@Past JapaneseDate value) {}
