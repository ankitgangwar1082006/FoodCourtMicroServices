package auth_service.com.auth_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
@RefreshScope
public class JwtService {
    @Value("${secret.key}")
    String SECRET_KEY="";
    public String generateToken(String email,Long id,String role)
    {
        return Jwts.builder().
                setSubject(email).
                claim("user_id",id).claim("role",role).
                setIssuedAt(new Date()).
                setExpiration(new Date(System.currentTimeMillis()+1000*60*60*24)).
                signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    private Date getExpirationDate(String token)
    {
        return extractClaim(token,Claims::getExpiration);
    }
    public  boolean validateToken(String token , UserDetails userDetails)
    {
        return getExpirationDate(token).after(new Date()) && userDetails.getUsername().equals(extractClaim(token,Claims::getSubject));
    }
    private Key getSignKey()
    {
        byte[] bytes= Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(bytes);
    }

    public String getEmail(String token)
    {
        return extractClaim(token,Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver)
    {
        return claimsResolver.apply(extractClaims(token));
    }
    public Long getUserId(String token)
    {
        return extractClaim(token,claims -> claims.get("user_id",Long.class));
    }
    private Claims extractClaims(String token)
    {
        return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
    }
}
