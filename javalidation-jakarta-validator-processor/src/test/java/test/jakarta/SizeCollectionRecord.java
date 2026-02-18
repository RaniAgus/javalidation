package test.jakarta;

import io.github.raniagus.javalidation.validator.*;
import jakarta.validation.constraints.*;
import java.util.*;

@Validate
public record SizeCollectionRecord(@Size(min = 1, max = 10) List<String> value) {}
