package us.elron.sassist.service;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Primitives {

    private static Map<Class<?>, Object> primitiveValues = new HashMap<Class<?>, Object>();

    static {
        primitiveValues.put(boolean.class, false);
        primitiveValues.put(char.class, 0);
        primitiveValues.put(byte.class, 0);
        primitiveValues.put(short.class, 0);
        primitiveValues.put(int.class, 0);
        primitiveValues.put(long.class, 0);
        primitiveValues.put(float.class, 0);
        primitiveValues.put(double.class, 0);
    }

    public static <T> T primitiveValueOrNullFor(Class<T> primitiveType) {
        return (T) primitiveValues.get(primitiveType);
    }

}