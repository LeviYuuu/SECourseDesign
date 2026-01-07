// auth/JwtProvider.java
@Component
public class JwtProvider {
  @Value("${JWT_SECRET:dev-secret-change-me}")
  private String secret;

  public String generateToken(Long userId) {
    Date now = new Date();
    Date exp = new Date(now.getTime() + 7L * 24 * 3600 * 1000);
    return Jwts.builder()
        .setSubject(String.valueOf(userId))
        .setIssuedAt(now)
        .setExpiration(exp)
        .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
        .compact();
  }

  public Long parseUserId(String token) {
    String sub = Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
    return Long.valueOf(sub);
  }
}
