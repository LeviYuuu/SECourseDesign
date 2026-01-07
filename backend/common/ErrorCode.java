// common/ErrorCode.java
@Getter
@AllArgsConstructor
public enum ErrorCode {
  BAD_REQUEST(400, "bad request"),
  UNAUTHORIZED(401, "unauthorized"),
  FORBIDDEN(403, "forbidden"),
  NOT_FOUND(404, "not found"),
  CONFLICT(409, "conflict"),
  TOO_MANY_REQUESTS(429, "too many requests"),
  INTERNAL_ERROR(500, "internal error"),
  SERVICE_UNAVAILABLE(503, "service unavailable");

  private final int httpStatus;
  private final String defaultMsg;
}

// common/BizException.java
@Getter
public class BizException extends RuntimeException {
  private final ErrorCode errorCode;
  public BizException(ErrorCode code, String msg) {
    super(msg);
    this.errorCode = code;
  }
  public BizException(ErrorCode code) {
    super(code.getDefaultMsg());
    this.errorCode = code;
  }
}
