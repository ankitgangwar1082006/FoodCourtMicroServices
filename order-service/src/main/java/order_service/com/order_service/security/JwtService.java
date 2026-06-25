package order_service.com.order_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
@RefreshScope
public class JwtService {

    @Value("${secret.key}")
    private String SECRET_KEY;

    public Claims validateToken(String token)
    {
        return Jwts.parserBuilder().setSigningKey(getSigningKey())
                .build().parseClaimsJws(token)
                .getBody();
    }
    public String getEmail(String token)
    {
        return extractClaim(Claims::getSubject,token);
    }
    public String getRole(String token)
    {
        return extractClaim(claims -> claims.get("role", String.class),token);
    }
    public Long getUserId(String token){
        return extractClaim(claims -> claims.get("user_id",Long.class),token);
    }
    public boolean isExpired(String token)
    {
        return extractClaim(Claims::getExpiration,token).before(new Date());
    }
    private Key getSigningKey()
    {
        byte bytes[] = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(bytes);
    }
    private <T> T extractClaim(Function<Claims,T> claimParser ,String token)
    {
        return claimParser.apply(validateToken(token));
    }
}
