package us.crast.mondochest.util;


import java.util.Collection;

public class GenericUtil {
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object x) {
        return (T) x;
    }

    public static <T> DecodeResults<T> decodeCollection(Object collection, Class<T> cls) {
        DecodeResults<T> results = new DecodeResults<T>();
        Collection<?> coll = cast(collection);
        for (Object item : coll) {
            if (cls.isAssignableFrom(item.getClass())) {
                T citem = cast(item);
                results.validValues.add(citem);
            } else {
                results.failedValues.add(item);
            }
        }
        return results;
    }
}
