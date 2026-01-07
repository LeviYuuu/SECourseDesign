// common/GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BizException.class)
  public ResponseEntity<ApiResponse<Void>> handleBiz(BizException e) {
    var code = e.getErrorCode();
    return ResponseEntity.status(code.getHttpStatus())
        .body(ApiResponse.fail(code.getHttpStatus(), e.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValid(MethodArgumentNotValidException e) {
    return ResponseEntity.status(400)
        .body(ApiResponse.fail(400, "validation failed"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleOther(Exception e) {
    return ResponseEntity.status(500)
        .body(ApiResponse.fail(500, "internal error"));
  }
}
