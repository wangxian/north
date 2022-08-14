package top.xiqiu.north.core;

public class NorthException extends RuntimeException {
    static final long serialVersionUID = -7034897190745766939L;

    public NorthException() {
        super();
    }

    public NorthException(String message) {
        super(message);
    }

    public NorthException(Throwable e) {
        super(e);
    }
}
