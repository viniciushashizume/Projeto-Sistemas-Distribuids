package org.voteflix.util;

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
}