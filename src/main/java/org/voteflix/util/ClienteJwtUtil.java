package org.voteflix.util;

import org.json.JSONObject;
import java.util.Base64;

/**
 * Classe utilitária do CLIENTE para ler claims de um token JWT.
 * (Não valida a assinatura, apenas lê o payload).
 */
public class ClienteJwtUtil {

    /**
     * Extrai a FUNÇÃO (role) de um token JWT.
     * @param token O token JWT recebido do servidor.
     * @return A função ("user" ou "admin").
     */
    public static String getFuncaoFromToken(String token) {
        try {
            String[] chunks = token.split("\\.");
            if (chunks.length < 2) {
                // Token inválido ou malformado
                return "user"; // Retorna o padrão menos privilegiado
            }

            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));

            JSONObject jsonPayload = new JSONObject(payload);

            // Retorna a "funcao" conforme definido nos requisitos
            return jsonPayload.getString("funcao");

        } catch (Exception e) {
            System.err.println("Erro ao decodificar o token no cliente: " + e.getMessage());
            // Em caso de falha, assume o papel menos privilegiado.
            return "user";
        }
    }
}