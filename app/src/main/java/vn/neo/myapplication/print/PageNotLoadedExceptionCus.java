package vn.neo.myapplication.print;

/**
 * Page not load exception.
 *
 * @author feiq
 */

public class PageNotLoadedExceptionCus extends RuntimeException {
    public PageNotLoadedExceptionCus() {
    }

    public PageNotLoadedExceptionCus(String message) {
        super(message);
    }

    public PageNotLoadedExceptionCus(String message, Throwable cause) {
        super(message, cause);
    }

    public PageNotLoadedExceptionCus(Throwable cause) {
        super(cause);
    }
}
