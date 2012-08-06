package us.crast.mondochest.util;

public class GenericUtil {
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object x) {
        return (T) x;
    }
}
