package tech.zuosi.reflectutil.test;

/**
 * Created by iwar on 2017/9/6.
 */
public final class WildcardParameterCase {
    public static final int METHOD_INT_DOUBLE = 1;
    public static final int METHOD_DOUBLE_INT = 2;
    public static final int METHOD_INT_STRING_STRING = 3;
    public static final int METHOD_INT_INT_STRING = 4;
    public static final int METHOD_STRING_INT_STRING = 5;

    public int method(int v1, double v2) {
        return METHOD_INT_DOUBLE;
    }
    public int method(double v1, int v2) {
        return METHOD_DOUBLE_INT;
    }
    public int method(int v1, String v2, String v3) {
        return METHOD_INT_STRING_STRING;
    }
    public int method(int v1, int v2, String v3) {
        return METHOD_INT_INT_STRING;
    }
    public int method(String v1, int v2, String v3) {
        return METHOD_STRING_INT_STRING;
    }
}
