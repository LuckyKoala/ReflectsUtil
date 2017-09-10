package tech.zuosi.reflectutil.test;

import org.junit.Test;
import tech.zuosi.reflectutil.ReflectException;
import tech.zuosi.reflectutil.Reflects;

import java.util.Map;

import static org.junit.Assert.*;
import static tech.zuosi.reflectutil.Reflects.on;

/**
 * Created by iwar on 2017/8/30.
 */
public class ReflectsTest {

    /*@Before
    public void setUp() throws Exception {

    }*/

    @Test
    public void testOn() {
        assertEquals(on(Object.class), on("java.lang.Object"));
        assertEquals(on(Object.class).<Object>get(), on("java.lang.Object").get());
        assertEquals(Object.class, on(Object.class).get());
        assertEquals("abc", on((Object) "abc").get());
        assertEquals(1, (int) (Integer) on(1).get());

        try {
            on("asdf");
            fail();
        }
        catch (ReflectException expected) {}
    }

    @Test
    public void testCreate() {
        assertEquals("", on(String.class).create().get());
        assertEquals("abc", on(String.class).create("abc").get());
        assertEquals("abc", on(String.class).create(new Object[]{"abc".getBytes()}).get());
        assertEquals("abc", on(String.class).create(new Object[]{"abc".toCharArray()}).get());
        assertEquals("b", on(String.class).create("abc".toCharArray(), 1, 1).get());

        try {
            on(String.class).create(new Object());
            fail();
        }
        catch (ReflectException expected) {}
        //Test private constructor
        assertEquals(3, (int)(Integer) on(TestCase.class).create(3).get("a"));
    }

    @Test
    public void testCall() {
        // Instance methods
        // ----------------
        assertEquals("", on((Object) " ").call("trim").get());
        assertEquals("12", on((Object) " 12 ").call("trim").get());
        assertEquals("34", on((Object) "1234").call("substring", 2).get());
        assertEquals("12", on((Object) "1234").call("substring", 0, 2).get());
        assertEquals("1234", on((Object) "12").call("concat", "34").get());
        assertEquals("123456", on((Object) "12").call("concat", "34").call("concat", "56").get());
        assertEquals(2, (int) (Integer) on((Object) "1234").call("indexOf", "3").get());
        assertEquals(2.0f, on((Object) "1234").call("indexOf", "3").call("floatValue").get(), 0.0f);
        assertEquals("2", on((Object) "1234").call("indexOf", "3").call("toString").get());
        //arrays
        assertEquals(1, (int)(Integer) on(TestCase.class)
                .create()
                .method("onArrays")
                .withArgs(new Object[]{new String[][]{{"1"},{"2"}}})
                .get());
        assertArrayEquals(new int[]{3,4,5}, on(TestCase.class).create().get("e"));

        Reflects testCase = on(TestCase.class).create();
        testCase.set("e", new int[]{1,2,3});
        assertArrayEquals(new int[]{1,2,3}, testCase.get("e"));

        // Static methods
        // --------------
        assertEquals("true", on(String.class).call("valueOf", true).get());
        assertEquals("1", on(String.class).call("valueOf", 1).get());
        assertEquals("abc", on(String.class).call("valueOf", new Object[]{"abc".toCharArray()}).get());
        assertEquals("abc", on(String.class).call("copyValueOf", new Object[]{"abc".toCharArray()}).get());
        assertEquals("b", on(String.class).call("copyValueOf", "abc".toCharArray(), 1, 1).get());
    }

    @Test
    public void testField() {
        Reflects testCase = on(TestCase.class).create();

        assertEquals(1, (int)(Integer) testCase.get("a"));
        assertEquals(312, ((Long) testCase.get("b")).longValue());
        assertEquals(1, ((Integer) testCase.get("c")).intValue());
        assertEquals(3.0D, testCase.get("d"), 0.0f);

        Map<String, Reflects> map = testCase.fields();
        assertEquals(1, (int)(Integer) map.get("a").get());
        assertEquals(312, ((Long) map.get("b").get()).longValue());
        assertEquals(1, ((Integer) map.get("c").get()).intValue());
        assertEquals(3.0D, map.get("d").get(), 0.0f);

        testCase.set("a", 3);
        assertEquals(3, (int)(Integer) testCase.get("a"));
    }
}