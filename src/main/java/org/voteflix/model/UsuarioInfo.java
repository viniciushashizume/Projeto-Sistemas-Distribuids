package org.voteflix.model;

import org.json.JSONObject;

/**
 * Modelo para representar informações resumidas do usuário (para listagem de admin).
 * Baseado no JSON Schema de LISTAR_USUARIOS.
 */
public class UsuarioInfo {
    private int id;
    private String nome;

    public UsuarioInfo(JSONObject json) {
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