package org.voteflix.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo para representar um Filme (servidor e cliente).
 * Contém todos os construtores necessários.
 */
public class Filme {
    private int id;
    private String titulo;
    private String diretor;
    private String ano;
    private String sinopse;
    private double nota;
    private int qtdAvaliacoes;
    private List<String> generos;

    /**
     * CONSTRUTOR 1 (Para Cliente: TelaListarFilmes)
     * Usado para popular a partir do JSON recebido do servidor.
     */
    public Filme(JSONObject json) {
        this.id = Integer.parseInt(json.getString("id"));
        this.titulo = json.getString("titulo");
        this.diretor = json.getString("diretor");
        this.ano = json.getString("ano");
        this.sinopse = json.getString("sinopse");
        this.nota = json.has("nota") ? Double.parseDouble(json.getString("nota")) : 0.0;
        this.qtdAvaliacoes = json.has("qtd_avaliacoes") ? Integer.parseInt(json.getString("qtd_avaliacoes")) : 0;

        this.generos = new ArrayList<>();
        if (json.has("genero")) {
            JSONArray generosArray = json.getJSONArray("genero");
            for (int i = 0; i < generosArray.length(); i++) {
                this.generos.add(generosArray.getString(i));
            }
        }
    }

    /**
     * CONSTRUTOR 2 (Para Servidor: Criar)
     * Usado ao criar um novo filme (vem da requisição do admin).
     */
    public Filme(String titulo, String diretor, String ano, String sinopse, List<String> generos) {
        this.id = 0; // ID será gerado pelo BD
        this.titulo = titulo;
        this.diretor = diretor;
        this.ano = ano;
        this.sinopse = sinopse;
        this.generos = generos;
        this.nota = 0.0;
        this.qtdAvaliacoes = 0;
    }

    /**
     * CONSTRUTOR 3 (Para Servidor: Ler do BD)
     * Usado ao ler um filme completo do banco de dados.
     */
    public Filme(int id, String titulo, String diretor, String ano, String sinopse, double nota, int qtdAvaliacoes, List<String> generos) {
        this.id = id;
        this.titulo = titulo;
        this.diretor = diretor;
        this.ano = ano;
        this.sinopse = sinopse;
        this.nota = nota;
        this.qtdAvaliacoes = qtdAvaliacoes;
        this.generos = generos;
    }

    /**
     * CONSTRUTOR 4 (Para Cliente: TelaFormularioFilme)
     * Usado para criar um objeto Filme manualmente a partir dos campos da GUI.
     */
    public Filme(int id, String titulo, String diretor, String ano, String sinopse, List<String> generos) {
        this.id = id;
        this.titulo = titulo;
        this.diretor = diretor;
        this.ano = ano;
        this.sinopse = sinopse;
        this.generos = generos;
        // nota e qtdAvaliacoes não são definidos manualmente pelo formulário
        this.nota = 0;
        this.qtdAvaliacoes = 0;
    }


    // Getters
    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDiretor() { return diretor; }
    public String getAno() { return ano; }
    public String getSinopse() { return sinopse; }
    public double getNota() { return nota; }
    public int getQtdAvaliacoes() { return qtdAvaliacoes; }
    public List<String> getGeneros() { return generos; }

    // Setters (necessários para atualização no servidor)
    public void setId(int id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDiretor(String diretor) { this.diretor = diretor; }
    public void setAno(String ano) { this.ano = ano; }
    public void setSinopse(String sinopse) { this.sinopse = sinopse; }
    public void setGeneros(List<String> generos) { this.generos = generos; }

    /**
     * (SERVIDOR) Cria um JSONObject para enviar em respostas (ex: LISTAR_FILMES).
     * Segue o padrão snake_case do protocolo.
     */
    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();

        json.put("id", String.valueOf(this.id));
        json.put("titulo", this.titulo);
        json.put("diretor", this.diretor);
        json.put("ano", this.ano);
        json.put("sinopse", this.sinopse);

        // Campos de avaliação
        json.put("nota", String.format("%.1f", this.nota));
        json.put("qtd_avaliacoes", String.valueOf(this.qtdAvaliacoes));

        JSONArray generosArray = new JSONArray();
        if (this.generos != null) {
            for (String g : this.generos) {
                generosArray.put(g);
            }
        }
        json.put("genero", generosArray);

        return json;
    }

    /**
     * (CLIENTE) Usado para exibição na JList.
     */
    @Override
    public String toString() {
        // Formata a nota para sempre ter uma casa decimal (ex: 5.0)
        return String.format("%s (%s) - Nota: %.1f", titulo, ano, nota);
    }
}