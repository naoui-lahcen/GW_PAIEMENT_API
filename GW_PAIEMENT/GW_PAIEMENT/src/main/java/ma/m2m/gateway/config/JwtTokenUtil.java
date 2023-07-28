package ma.m2m.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = -2550185165626007488L;

    public static final long JWT_TOKEN_VALIDITY = 600000; // 10 min (en milliseconds)
    
    @Getter @Setter
    private String secretKey;
    
    
    // Génère un jeton pour l'utilisateur spécifié
    public String generateToken(String username, String secret) {
        Date now = new Date();
        
        System.out.println("JWT_TOKEN_VALIDITY : " + JWT_TOKEN_VALIDITY/60000 + " min");
        
        Date expiryDate = new Date(now.getTime() + JWT_TOKEN_VALIDITY);

        secretKey = secret;
        
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
 // Récupère le nom d'utilisateur à partir du jeton
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

    // Génère un jeton pour l'utilisateur spécifié
    //public String generateToken(UserDetails userDetails) {
      //  Map<String, Object> claims = new HashMap<>();
        //return doGenerateToken(claims, userDetails.getUsername());
    //}

    // Récupère la date d'expiration à partir du jeton
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    // Fonction générique pour récupérer les informations du jeton (claim)
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // Récupère toutes les informations du jeton
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    // Vérifie si le jeton a expiré
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // Génère le jeton en utilisant les informations (claims) spécifiées
    public String doGenerateToken(Map<String, Object> claims, String subject) {
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + JWT_TOKEN_VALIDITY * 1000);

        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(createdDate)
                .setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, secretKey).compact();
    }
    // Récupère le nom d'utilisateur à partir du jeton
    //public String getUsernameFromToken(String token) {
      //return getClaimFromToken(token, Claims::getSubject);
	//}
    
    // Valide le jeton pour l'utilisateur spécifié
    public Boolean validateToken() {
    	return false;
    }

}
