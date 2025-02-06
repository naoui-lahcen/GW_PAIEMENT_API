package ma.m2m.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/*
* @author  LAHCEN NAOUI
* @version 1.0
* @since   2023-07-01 / 2023-09-01 
 */

@Component
public class JwtTokenUtil implements Serializable {
	
	private static Logger logger = LogManager.getLogger(JwtTokenUtil.class);

    private static final long serialVersionUID = -2550185165626007488L;
    /**
     * JWT token validity duration.
     * The duration is set to 10 minutes (600,000 milliseconds).
     */
    public static final long JWT_TOKEN_VALIDITY = 600000; // 10 minutes in milliseconds
    
    @Getter @Setter
    private String secretKey;
    
    
    /**
     * Generates a JWT token for the specified user.
     * 
     * @param username the username of the user
     * @return the generated JWT token
     */
    public String generateToken(String username, String secret) {
        Date now = new Date();
        
        logger.info("JWT_TOKEN_VALIDITY : {} min" , JWT_TOKEN_VALIDITY/60000);
        
        Date expiryDate = new Date(now.getTime() + JWT_TOKEN_VALIDITY);

        secretKey = secret;
        
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
    
    /**
     * Generates a JWT token for the specified user.
     * 
     * @param username the username of the user
     * @return the generated JWT token
     */
    public String generateToken(String username, String secret, long jwtTokenValidity) {
        Date now = new Date();
        
        logger.info("jwtTokenValidity : {} min" , jwtTokenValidity/60000);
        
        Date expiryDate = new Date(now.getTime() + jwtTokenValidity);
        
        secretKey = secret;
        
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
    
    /**
     * recupere username from token.
     * 
     * @param username the username of the user
     * @return the username
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }


    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }
    

    public String getUsernameFromToken(String token, String secretKey) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
    
    public Date getExpirationDateFromToken(String token, String secretKey) {
        return getClaimFromToken(token, Claims::getExpiration, secretKey);
    }
    
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver, String secretKey) {
        final Claims claims = getAllClaimsFromToken(token, secretKey);
        return claimsResolver.apply(claims);
    }

    public Claims getAllClaimsFromToken(String token, String secretKey) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }
    
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Boolean isTokenExpired(String token, String secretKey) {
        final Date expiration = getExpirationDateFromToken(token, secretKey);
        return expiration.before(new Date());
    }

    public String doGenerateToken(Map<String, Object> claims, String subject) {
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + JWT_TOKEN_VALIDITY * 1000);

        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(createdDate)
                .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, secretKey).compact();
    }
    
    public Boolean validateToken() {
    	return false;
    }

}
