package cn.tealc.taygedo;

/**
 * 塔吉多API异常
 * 所有API请求失败、响应解析错误、业务错误码等情况均抛出此异常
 */
public class TaygedoException extends RuntimeException {
    public TaygedoException(String message) {
        super(message);
    }

    public TaygedoException(String message, Throwable cause) {
        super(message, cause);
    }
}
