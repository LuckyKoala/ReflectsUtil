package tech.zuosi.reflectutil;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Class.forName;

/**
 * Created by iwar on 2017/8/29.
 */
public class Reflects {
    private String temp;
    private Class<?> clazz;
    private final Object object;
    //private static final Reflects EMPTY = new Reflects(); //For chain
    private static final Map<Class<?>, Class<?>> primitiveTypeWrapperMap = new HashMap<>(8);
    //---------------------------------------------------------------------------------------------------
    //Map primitive type to wrapper type
    static {
        primitiveTypeWrapperMap.put(boolean.class, Boolean.class);
        primitiveTypeWrapperMap.put(byte.class, Byte.class);
        primitiveTypeWrapperMap.put(char.class, Character.class);
        primitiveTypeWrapperMap.put(short.class, Short.class);
        primitiveTypeWrapperMap.put(int.class, Integer.class);
        primitiveTypeWrapperMap.put(float.class, Float.class);
        primitiveTypeWrapperMap.put(double.class, Double.class);
        primitiveTypeWrapperMap.put(long.class, Long.class);
    }
    //---------------------------------------------------------------------------------------------------
    //Mark class that indicates specific parameter of method can be any type
    public static class WildcardParameter {
        public static final WildcardParameter INSTANCE = new WildcardParameter();
        private WildcardParameter() {}
    }

    private Reflects(Class<?> clazz, Object object) {
        this.clazz = clazz;
        this.object = object;
    }

    /**
     * @param <T>
     * @return object whose type is T
     */
    public <T> T  get() {
        return (T) object;
    }


    /**
     * @param className
     * @return Reflects instance with class which was returned by Class.forName(className)
     * @throws ReflectException
     */
    public static Reflects on(String className) throws ReflectException {
        try {
            Class<?> cls = forName(className);
            return new Reflects(cls, cls); //type() == object means that it will find static field
        } catch (ClassNotFoundException e) {
            throw new ReflectException(e);
        }
    }

    /**
     * @param clazz
     * @return Reflects instance with clazz
     */
    public static Reflects on(Class<?> clazz) {
        return new Reflects(clazz, clazz); //type() == object means that it will find static field
    }

    public static Reflects on(Object object) {
        return new Reflects(object.getClass(), object);
    }

    public static Reflects on(Class<?> clazz, Object object) {
        return new Reflects(clazz, object);
    }

    //---------------------------------------------------------------------------------------------------
    //Helper method to handle some checked reflection exception when invoke method or constructor
    private static Reflects on(Method method, Object object, Object... args) throws ReflectException {
        if(method.getReturnType() == void.class) {
            return on(object);
        }
        try {
            return on(method.invoke(object, args));
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }
    private static Reflects on(Constructor<?> constructor, Object... initArgs) throws ReflectException {
        try {
            return on(accessible(constructor).newInstance(initArgs));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ReflectException(e);
        }
    }
    /**---------------------------------------------------------------------------------------------------
     * Make object which is instance of Member accessible
     * Can not use Member to be the type of argument because that Member is only a interface,and
     *   public final class Method extends Executable
     *   public abstract class Executable extends AccessibleObject implements Member, GenericDeclaration
     * So if we want to use AccessibleObject.setAccessible() then we should use AccessibleObject
     *   be type of argument
     */
    private static <T extends AccessibleObject> T accessible(T accessibleObject) {
        if(null == accessibleObject) return null;
        if(accessibleObject instanceof Member) {
            Member member = (Member) accessibleObject;
            if(Modifier.isPublic(member.getModifiers())
                    && Modifier.isPublic(member.getDeclaringClass().getModifiers())) {
                return accessibleObject;
            }
        }
        if(!accessibleObject.isAccessible()) {
            accessibleObject.setAccessible(true);
        }
        return accessibleObject;
    }
    //---------------------------------------------------------------------------------------------------
    //Create instance
    public Reflects create() throws ReflectException {
        try {
            //Try public nullary constructor first
            return on(type().newInstance());
        } catch (InstantiationException e) {
            throw new ReflectException(e);
        } catch (IllegalAccessException e) {
            //Then try private nullary constructor
            return create(new Object[0]);
        }
    }
    public Reflects create(Object... initArgs) throws ReflectException {
        Class<?>[] actualTypes = getTypes(initArgs);
        try {
            Constructor<?> constructor = type().getDeclaredConstructor(actualTypes);
            return on(constructor, initArgs);
        } catch (NoSuchMethodException e) {
            Constructor<?>[] constructors = type().getDeclaredConstructors();
            for(Constructor<?> constructor : constructors) {
                if(typesMatch(constructor.getParameterTypes(), actualTypes)) {
                    return on(constructor, initArgs);
                }
            }
        }
        throw new ReflectException("Can't not find suitable constructor to create new instance");
    }
    //---------------------------------------------------------------------------------------------------
    //Method finding
    private Method findExactMethod(String name, Class<?>[] types) throws NoSuchMethodException {
        Class<?> cls = clazz;
        //Try public method with exact signature first
        try {
            return cls.getMethod(name, types);
        } catch (NoSuchMethodException e) {
            //Then try private method
            do {
                try {
                    return cls.getDeclaredMethod(name, types);
                } catch (NoSuchMethodException ignored) {}

                cls = cls.getSuperclass();
            } while (cls != null);

            throw new NoSuchMethodException("Can't find any method with exact signature");
        }
    }
    private Method findSimilarMethod(String name, Class<?>[] types) throws NoSuchMethodException {
        Class<?> cls = type();
        //Try public method with exact signature first
        Method[] methods = cls.getMethods();
        for(Method method : methods) {
            if(isSimilarSignature(method, name, types)) return method;
        }
        Method[] declaredMethods;
        do {
            declaredMethods = cls.getDeclaredMethods();
            for(Method method : declaredMethods) {
                if (isSimilarSignature(method, name, types)) return method;
            }

            cls = cls.getSuperclass();
        } while (cls != null);

        throw new NoSuchMethodException("Can't find any method with similar signature");
    }
    //---------------------------------------------------------------------------------------------------
    //Check signature of method
    private boolean isSimilarSignature(Method possiblyMethod, String desiredName, Class<?>[] desiredTypes) {
        return possiblyMethod.getName().equals(desiredName)
                && typesMatch(possiblyMethod.getParameterTypes(), desiredTypes);
    }
    private boolean typesMatch(Class<?>[] declaredTypes, Class<?>[] actualTypes) {
        if(declaredTypes.length == actualTypes.length) {
            for (int i = 0; i < actualTypes.length; i++) {
                if(actualTypes[i] == WildcardParameter.class) continue;
                if(wrap(declaredTypes[i]).isAssignableFrom(wrap(actualTypes[i]))) continue;
                return false;
            }
            return true;
        }
        return false;
    }
    //---------------------------------------------------------------------------------------------------
    //Wrapper for primitive class
    private static Class<?> wrap(Class<?> type) {
        if(null == type) return null;
        else if(type.isPrimitive()) {
            return primitiveTypeWrapperMap.get(type);
        } else {
            return type;
        }
    }
    /** ---------------------------------------------------------------------------------------------------
     * Invoke method
     *  There is no need to find whether the method is static, because if the method is static,
     *    Method.invoke(Object obj, Object... initArgs) will ignore obj argument.
     */
    public Reflects call(String name) throws ReflectException {
        return call(name, new Object[0]);
    }
    public Reflects call(String name, Object... initArgs) throws ReflectException {
        Class<?>[] types = getTypes(initArgs);
        try {
            return callExact(name, types, initArgs);
        } catch (NoSuchMethodException e) {
            try {
                Method method = findSimilarMethod(name, types);
                return on(method, object, initArgs);
            } catch (Exception ee) {
                throw new ReflectException(ee);
            }
        }
    }
    public Reflects callExact(String name, Class<?>[] initClazz, Object[] initArgs) throws NoSuchMethodException {
        Method method = findExactMethod(name, initClazz);
        return on(method, object, initArgs);
    }
    //---------------------------------------------------------------------------------------------------
    //Field getting and setting
    public void set(String fieldName, Object value) throws ReflectException {
        try {
            field0(fieldName).set(object, value);
        } catch (IllegalAccessException e) {
            throw new ReflectException(e);
        }
    }
    public <T> T get(String name) throws ReflectException {
        return field(name).<T>get();
    }
    public Reflects field(String name) throws ReflectException {
        try {
            Field field = field0(name);
            return on(field.getType(), field.get(object));
        } catch (IllegalAccessException e) {
            throw new ReflectException(e);
        }
    }
    private Field field0(String name) throws ReflectException {
        Class<?> type = type();

        try {
            return accessible(type.getField(name));
        } catch (NoSuchFieldException e) {
            do {
                try {
                    return accessible(type.getDeclaredField(name));
                } catch (NoSuchFieldException ignore) {}

                type = type.getSuperclass();
            } while (type != null);

            throw new ReflectException(e);
        }
    }
    public Map<String, Reflects> fields() throws ReflectException {
        Class<?> type = type();
        Map<String, Reflects> reflectsMap = new HashMap<>();
        do {
            for(Field field : type.getDeclaredFields()) {
                boolean isStaticField = Modifier.isStatic(field.getModifiers());
                boolean suit = isStaticModeOn() == isStaticField;
                if(suit) {
                    try {
                        reflectsMap.putIfAbsent(field.getName(), on(field.getType(), accessible(field).get(object)));
                    } catch (IllegalAccessException e) {
                        throw new ReflectException(e);
                    }
                }
            }

            type = type.getSuperclass();
        } while (type != null);
        return reflectsMap;
    }

    private Class<?>[] getTypes(Object[] initArgs) {
        Class<?>[] types = new Class[initArgs.length];
        for (int i = 0; i < initArgs.length; i++) {
            types[i] = initArgs[i].getClass();
        }
        return types;
    }

    private Class<?> type() {
        return clazz == null ? (clazz = object.getClass()) : clazz;
    }

    private boolean isStaticModeOn() { return type()==object; }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj instanceof Reflects) {
            Reflects reflects = (Reflects) obj;
            return this.object.equals(reflects.object);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.object.hashCode();
    }

    /**
     * Fluent style
     *  on(String.class).create("Hello").method("substring").withArgs(0).get()
     * Normal style
     *  on(String.class).create("Hello").call("substring", 0).get()
     */
    //FIXME 太Low了 想想办法写得和Stream的中间类差不多就不错
    public Reflects method(String name) {
        this.temp = name;
        return this;
    }
    public Reflects withArgs(Object... args) {
        return call(temp, args);
    }
    public Reflects withoutArgs() {
        return call(temp);
    }
}
