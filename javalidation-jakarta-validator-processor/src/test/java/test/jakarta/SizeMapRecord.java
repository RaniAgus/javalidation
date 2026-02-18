package test.jakarta;

import io.github.raniagus.javalidation.validator.*;
import jakarta.validation.constraints.*;
import java.util.*;

@Validate
public record SizeMapRecord(@Size(min = 1, max = 10) Map<String, String> value) {}
