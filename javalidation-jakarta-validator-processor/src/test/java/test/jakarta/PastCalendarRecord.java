package test.jakarta;

import jakarta.validation.constraints.*;
import java.util.*;

public record PastCalendarRecord(@Past Calendar value) {}
