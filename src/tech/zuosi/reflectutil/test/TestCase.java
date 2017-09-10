package tech.zuosi.reflectutil.test;

/**
 * Created by iwar on 2017/8/30.
 */
public class TestCase {
    private int a = 1;
    private long b = 312;
    protected int c = 1;
    public double d = 3.0D;
    public int[] e = new int[]{3,4,5};

    public TestCase() {}

    public TestCase(int initVal) {
        this.a = initVal;
    }

    private TestCase(long initVal) {
        this.b = initVal;
    }

    public int onArrays(String[][] argsArray) {
        return 1;
    }
}
