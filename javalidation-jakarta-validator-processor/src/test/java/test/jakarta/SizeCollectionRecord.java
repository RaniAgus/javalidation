package test.jakarta;

import jakarta.validation.constraints.*;
import java.util.*;

public record SizeCollectionRecord(@Size(min = 1, max = 10) List<String> value) {}
