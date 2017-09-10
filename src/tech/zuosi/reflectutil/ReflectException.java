package tech.zuosi.reflectutil;

/**
 * Created by iwar on 2017/8/30.
 * Custom exception which extends RuntimeException will
 *  be thrown to wrap any checked reflection exception happened
 *  to be thrown, so the user don't have to handle these checked
 *  exception:
 * <ul>
 * <li> {@link NoSuchMethodException}</li>
 * </ul>
 */
public class ReflectException extends RuntimeException {
    public ReflectException(String message) {
        super(message);
    }

    public ReflectException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReflectException() {
        super();
    }

    public ReflectException(Throwable cause) {
        super(cause);
    }
}
