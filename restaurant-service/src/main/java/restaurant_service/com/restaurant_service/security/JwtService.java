package restaurant_service.com.restaurant_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${secret.key}")
    String SECRET_KEY;
    private Claims extractClaims(String token)
    {
        return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
    }
    private <T> T extractClaim(String token , Function<Claims,T> claimMap)
    {
        Claims claims=extractClaims(token);
        return claimMap.apply(claims);
    }
    private Key getSignInKey()
    {
        byte [] bytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(bytes);
    }

    public Long fetchId(String token)
    {
        return extractClaim(token,claims -> claims.get("user_id", Long.class));
    }
    public String fetchRole(String token)
    {
        return extractClaim(token,claims -> claims.get("role", String.class));
    }

    public String fetchEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private java.util.Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new java.util.Date());
    }
}
