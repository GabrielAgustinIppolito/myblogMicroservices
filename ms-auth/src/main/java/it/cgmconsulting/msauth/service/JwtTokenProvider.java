package it.cgmconsulting.msauth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import it.cgmconsulting.msauth.entity.User;
import it.cgmconsulting.msauth.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JwtTokenProvider {

    public String generateToken(User user) {
    	
    	Map<String, Object> payloadClaims = new HashMap<String, Object>();
    	payloadClaims.put("roles", user.getAuthorities().stream().map(a->a.getAuthorityName()).toList());
    	payloadClaims.put("isEnabled", user.isEnabled());
    	payloadClaims.put("id", user.getId());
    	
        JWTCreator.Builder builder = JWT.create()
        		.withSubject(user.getUsername()); 			// sub: username
        final Instant now = Instant.now();
        builder
        	.withIssuedAt(Date.from(now)) // iat: jwt creation date
        	.withExpiresAt(Date.from(now.plus(Constants.JWT_EXPIRATION, ChronoUnit.SECONDS))); // exp: jwt expiration date

        if (payloadClaims.isEmpty()) {
            log.warn("You are building a JWT without header claims");
        }
        for (Map.Entry<String, Object> entry : payloadClaims.entrySet()) {
            builder.withClaim(entry.getKey(), entry.getValue().toString());
        }
        return builder.sign(Algorithm.HMAC512(Constants.JWT_SECRET));
    }

}
