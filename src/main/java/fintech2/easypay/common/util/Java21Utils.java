package fintech2.easypay.common.util;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Java 21: Utility class for Java 21 features
 */
public class Java21Utils {
    
    /**
     * Java 21: Pattern matching for Optional with custom mapping
     */
    public static <T, R> Optional<R> mapOptional(Optional<T> optional, Function<T, R> mapper) {
        return switch (optional) {
            case Optional<T> opt when opt.isPresent() -> Optional.of(mapper.apply(opt.get()));
            case Optional<T> opt -> Optional.empty();
        };
    }
    
    /**
     * Java 21: Pattern matching for Optional with custom filtering
     */
    public static <T> Optional<T> filterOptional(Optional<T> optional, Predicate<T> predicate) {
        return switch (optional) {
            case Optional<T> opt when opt.isPresent() && predicate.test(opt.get()) -> opt;
            case Optional<T> opt -> Optional.empty();
        };
    }
    
    /**
     * Java 21: Enhanced switch expression for string validation
     */
    public static boolean isValidString(String value) {
        return switch (value) {
            case null -> false;
            case String s when s.trim().isEmpty() -> false;
            default -> true;
        };
    }
    
    /**
     * Java 21: Pattern matching for number validation
     */
    public static boolean isValidNumber(Number value) {
        return switch (value) {
            case null -> false;
            case Integer i when i <= 0 -> false;
            case Long l when l <= 0 -> false;
            case Double d when d <= 0 -> false;
            case Float f when f <= 0 -> false;
            default -> true;
        };
    }
    
    /**
     * Java 21: Enhanced switch for list operations
     */
    public static <T> boolean isNotEmpty(List<T> list) {
        return switch (list) {
            case null -> false;
            case List<T> l when l.isEmpty() -> false;
            default -> true;
        };
    }
    
    /**
     * Java 21: Pattern matching for map operations
     */
    public static <K, V> boolean isNotEmpty(Map<K, V> map) {
        return switch (map) {
            case null -> false;
            case Map<K, V> m when m.isEmpty() -> false;
            default -> true;
        };
    }
    
    /**
     * Java 21: Enhanced switch for safe string operations
     */
    public static String safeTrim(String value) {
        return switch (value) {
            case null -> "";
            case String s -> s.trim();
        };
    }
    
    /**
     * Java 21: Pattern matching for null-safe operations
     */
    public static <T> T safeGet(T value, T defaultValue) {
        return switch (value) {
            case null -> defaultValue;
            default -> value;
        };
    }
    
    /**
     * Java 21: Enhanced switch for boolean operations
     */
    public static boolean safeBoolean(Boolean value) {
        return switch (value) {
            case null -> false;
            case Boolean b -> b;
        };
    }
    
    /**
     * Java 21: Pattern matching for collection filtering
     */
    public static <T> List<T> filterNotNull(List<T> list) {
        return switch (list) {
            case null -> List.of();
            case List<T> l -> l.stream()
                .filter(item -> switch (item) {
                    case null -> false;
                    default -> true;
                })
                .collect(Collectors.toList());
        };
    }
    
    /**
     * Java 21: Enhanced switch for string formatting
     */
    public static String formatString(String template, Object... args) {
        return switch (template) {
            case null -> "";
            case String t when t.contains("%s") -> String.format(t, args);
            case String t -> t;
        };
    }
    
    /**
     * Java 21: Pattern matching for type checking
     */
    public static String getTypeName(Object obj) {
        return switch (obj) {
            case null -> "null";
            case String s -> "String";
            case Integer i -> "Integer";
            case Long l -> "Long";
            case Double d -> "Double";
            case Boolean b -> "Boolean";
            case List<?> list -> "List";
            case Map<?, ?> map -> "Map";
            default -> obj.getClass().getSimpleName();
        };
    }
    
    /**
     * Java 21: Enhanced switch for conditional operations
     */
    public static <T> T conditional(T value, Predicate<T> condition, T alternative) {
        return switch (value) {
            case T v when condition.test(v) -> v;
            default -> alternative;
        };
    }
    
    /**
     * Java 21: Pattern matching for safe conversion
     */
    public static Integer safeToInteger(String value) {
        return switch (value) {
            case null -> null;
            case String s when s.trim().isEmpty() -> null;
            case String s -> {
                try {
                    yield Integer.parseInt(s.trim());
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
        };
    }
    
    /**
     * Java 21: Enhanced switch for safe parsing
     */
    public static Long safeToLong(String value) {
        return switch (value) {
            case null -> null;
            case String s when s.trim().isEmpty() -> null;
            case String s -> {
                try {
                    yield Long.parseLong(s.trim());
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
        };
    }
    
    /**
     * Java 21: Pattern matching for safe double parsing
     */
    public static Double safeToDouble(String value) {
        return switch (value) {
            case null -> null;
            case String s when s.trim().isEmpty() -> null;
            case String s -> {
                try {
                    yield Double.parseDouble(s.trim());
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
        };
    }
} 