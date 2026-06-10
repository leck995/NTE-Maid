package cn.tealc.taygedo;

/**
 * 塔吉多API异常
 * 所有API请求失败、响应解析错误、业务错误码等情况均抛出此异常
 */
public class TaygedoException extends RuntimeException {
    private int code = -1;
    private String body;
    public TaygedoException(String message) {
        super(message);
    }



    public TaygedoException(String message, Throwable cause) {
        super(message, cause);
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getCode() {
        return code;
    }
}
