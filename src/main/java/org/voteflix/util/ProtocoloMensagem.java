package org.voteflix.util;

import org.json.JSONObject;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * Enum que centraliza todas as mensagens e códigos de status
 * baseados no Protocolo de Troca de Mensagens.
 */
public enum ProtocoloMensagem {

    // --- SUCESSO ---
    SUCESSO_OPERACAO("200", "Sucesso: operação realizada com sucesso"),
    SUCESSO_RECURSO_CADASTRADO("201", "Sucesso: Recurso cadastrado"),

    // --- ERROS CLIENTE (4xx) ---
    ERRO_OPERACAO_INVALIDA("400", "Erro: Operação não encontrada ou inválida"),
    ERRO_TOKEN_INVALIDO("401", "Erro: Token inválido"), // Usado para login/senha errada também
    ERRO_SEM_PERMISSAO("403", "Erro: sem permissão"),
    ERRO_RECURSO_INEXISTENTE("404", "Erro: Recurso inexistente"), // Usado para usuário não encontrado
    ERRO_CAMPOS_INVALIDOS("405", "Erro: Campos inválidos, verifique o tipo e quantidade de caractere"),
    ERRO_RECURSO_JA_EXISTE("409", "Erro: Recurso ja existe"), // Usado para usuário já logado ou usuário já existe
    ERRO_CHAVES_FALTANTES("422", "Erro: Chaves faltantes ou invalidas"), // Usado para validação (tamanho de campos)

    // --- ERROS SERVIDOR (5xx) ---
    ERRO_FALHA_INTERNA("500", "Erro: Falha interna do servidor"),

    // --- MENSAGEM PADRÃO ---
    RESPOSTA_INESPERADA("000", "Resposta inesperada do servidor.");


    private final String status;
    private final String mensagem;

    // --- Mapa estático para busca rápida por status ---
    private static final Map<String, ProtocoloMensagem> MAPA_POR_STATUS;

    static {
        Map<String, ProtocoloMensagem> mapa = new HashMap<>();
        for (ProtocoloMensagem pm : values()) {
            mapa.put(pm.status, pm);
        }
        MAPA_POR_STATUS = Collections.unmodifiableMap(mapa);
    }
    // --- Fim do bloco estático ---

    ProtocoloMensagem(String status, String mensagem) {
        this.status = status;
        this.mensagem = mensagem;
    }

    public String getStatus() {
        return status;
    }

    public String getMensagem() {
        return mensagem;
    }

    /**
     * Aplica o status e a mensagem a um JSONObject de resposta.
     * (Usado pelo Servidor)
     * @param resposta O JSONObject a ser modificado.
     */
    public void aplicar(JSONObject resposta) {
        resposta.put("status", this.status);
        resposta.put("mensagem", this.mensagem);
    }

    /**
     * Busca a mensagem correspondente ao código de status.
     * (Usado pelo Cliente)
     * @param status O código de status (ex: "200", "404").
     * @return O Enum ProtocoloMensagem correspondente.
     */
    public static ProtocoloMensagem getByStatus(String status) {
        ProtocoloMensagem pm = MAPA_POR_STATUS.get(status.trim());
        if (pm != null) {
            return pm;
        }
        // Se o status não for encontrado no mapa, retorna um padrão
        return RESPOSTA_INESPERADA;
    }
}