package org.voteflix.model;

public class Usuario {
    private int id;
    private String nome;
    private String senha;
    private String funcao; // NOVO CAMPO

    // Construtor para criar um novo usuário (sem id, funcao padrao 'user')
    public Usuario(String nome, String senha) {
        this.nome = nome;
        this.senha = senha;
        this.funcao = "user"; // Por padrão, novos usuários são "user"
    }

    // Construtor para ler um usuário do banco (com id e funcao)
    public Usuario(int id, String nome, String senha, String funcao) {
        this.id = id;
        this.nome = nome;
        this.senha = senha;
        this.funcao = funcao;
    }

    // Getters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getSenha() { return senha; }
    public String getFuncao() { return funcao; } // NOVO GETTER
}