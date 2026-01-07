// common/ApiResponse.java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
  private int code;     // 0 success, else error
  private String msg;
  private T data;

  public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(0, "ok", data);
  }
  public static <T> ApiResponse<T> fail(int code, String msg) {
    return new ApiResponse<>(code, msg, null);
  }
}
