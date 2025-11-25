package org.voteflix.model;

import org.json.JSONObject;

public class Review {
    private int id;
    private int idFilme;
    private String nomeUsuario;
    private double nota;
    private String titulo;
    private String descricao;
    private String data;
    private boolean editado;

    public Review(JSONObject json) {
        this.id = json.optInt("id", -1);
        this.idFilme = json.optInt("id_filme", -1);
        this.nomeUsuario = json.optString("nome_usuario");
        this.nota = json.optDouble("nota", 0.0);
        this.titulo = json.optString("titulo");
        this.descricao = json.optString("descricao");
        this.data = json.optString("data");
        // O campo "editado" vem como string "true"/"false" ou booleano no JSON
        this.editado = json.optBoolean("editado", Boolean.parseBoolean(json.optString("editado", "false")));
    }

    public int getId() { return id; }
    public int getIdFilme() { return idFilme; }
    public String getNomeUsuario() { return nomeUsuario; }
    public double getNota() { return nota; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getData() { return data; }
    public boolean isEditado() { return editado; }

    @Override
    public String toString() {
        return String.format("★ %.1f - %s (%s)", nota, titulo, nomeUsuario);
    }
}