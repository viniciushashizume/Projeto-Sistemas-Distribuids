package org.voteflix.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.voteflix.model.Usuario;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    private static final SecretKey CHAVE_SECRETA = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long TEMPO_EXPIRACAO = 86400000; // 24 horas

    public static String gerarToken(Usuario usuario) {
        return Jwts.builder()
                .setSubject(usuario.getNome())
                .claim("id", usuario.getId()) // Adiciona o ID do usuário ao token
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + TEMPO_EXPIRACAO))
                .signWith(CHAVE_SECRETA)
                .compact();
    }

    /**
     * Extrai o nome do usuário (subject) de um token JWT.
     * @param token O token JWT.
     * @return O nome do usuário.
     */
    public static String getNomeFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(CHAVE_SECRETA)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Extrai o ID do usuário de um token JWT. <-- NOVO MÉTODO
     * @param token O token JWT.
     * @return O ID do usuário.
     */
    public static int getIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(CHAVE_SECRETA)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("id", Integer.class);
    }
}