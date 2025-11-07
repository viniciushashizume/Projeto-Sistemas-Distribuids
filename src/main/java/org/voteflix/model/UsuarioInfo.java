package org.voteflix.model;

import org.json.JSONObject;

/**
 * Modelo para representar informações resumidas do usuário (para listagem de admin).
 * Baseado no JSON Schema de LISTAR_USUARIOS (Protocolo de Troca de Mensagens-3.xlsx - JSON Schemas.csv)
 */
public class UsuarioInfo {
    private int id;
    private String nome;

    public UsuarioInfo(JSONObject json) {
        // O protocolo especifica que tudo é String
        this.id = Integer.parseInt(json.getString("id"));
        this.nome = json.getString("nome");
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    // Usado para exibição na JList
    @Override
    public String toString() {
        return nome + " (ID: " + id + ")";
    }
}