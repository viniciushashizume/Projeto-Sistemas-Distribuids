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
                .claim("id", usuario.getId()) // [cite: 34]
                .claim("funcao", usuario.getFuncao()) // NOVO CLAIM
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + TEMPO_EXPIRACAO))
                .signWith(CHAVE_SECRETA)
                .compact();
    }

    private static Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(CHAVE_SECRETA)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extrai o nome do usuário (subject) de um token JWT. [cite: 35]
     * @param token O token JWT.
     * @return O nome do usuário.
     */
    public static String getNomeFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extrai o ID do usuário de um token JWT. [cite: 34]
     * @param token O token JWT.
     * @return O ID do usuário.
     */
    public static int getIdFromToken(String token) {
        return getClaims(token).get("id", Integer.class);
    }

    /**
     * Extrai a FUNÇÃO (role) do usuário de um token JWT. <-- NOVO MÉTODO
     * @param token O token JWT.
     * @return A função ("user" ou "admin").
     */
    public static String getFuncaoFromToken(String token) {
        return getClaims(token).get("funcao", String.class);
    }
}