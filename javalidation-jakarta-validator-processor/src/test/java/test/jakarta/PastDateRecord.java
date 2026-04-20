package test.jakarta;

import jakarta.validation.constraints.*;
import java.util.*;

public record PastDateRecord(@Past Date value) {}
