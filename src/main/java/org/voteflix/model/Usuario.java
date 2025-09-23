package org.voteflix.model;

public class Usuario {
    private int id;
    private String nome;
    private String senha;

    // Construtor para criar um novo usuário (sem id)
    public Usuario(String nome, String senha) {
        this.nome = nome;
        this.senha = senha;
    }

    // Construtor para ler um usuário do banco (com id)
    public Usuario(int id, String nome, String senha) {
        this.id = id;
        this.nome = nome;
        this.senha = senha;
    }

    // Getters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getSenha() { return senha; }
}