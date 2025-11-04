package org.voteflix.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Modelo para representar um Filme no lado do cliente.
 * Baseado no JSON Schema.
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

    // Construtor usado para popular a partir do JSON do servidor
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

    // Getters
    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDiretor() { return diretor; }
    public String getAno() { return ano; }
    public String getSinopse() { return sinopse; }
    public double getNota() { return nota; }
    public int getQtdAvaliacoes() { return qtdAvaliacoes; }
    public List<String> getGeneros() { return generos; }

    /**
     * Cria um JSONObject para enviar em requisições de CRIAR ou EDITAR.
     */
    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        // ID é incluído apenas para edição, não para criação
        if (this.id > 0) {
            json.put("id", String.valueOf(this.id));
        }
        json.put("titulo", this.titulo);
        json.put("diretor", this.diretor);
        json.put("ano", this.ano);
        json.put("sinopse", this.sinopse);

        JSONArray generosArray = new JSONArray();
        for (String g : this.generos) {
            generosArray.put(g);
        }
        json.put("genero", generosArray);

        return json;
    }

    // Construtor manual para TelaFormularioFilme
    public Filme(int id, String titulo, String diretor, String ano, String sinopse, List<String> generos) {
        this.id = id;
        this.titulo = titulo;
        this.diretor = diretor;
        this.ano = ano;
        this.sinopse = sinopse;
        this.generos = generos;
        // nota e qtdAvaliacoes não são definidos manualmente
        this.nota = 0;
        this.qtdAvaliacoes = 0;
    }


    // Usado para exibição na JList
    @Override
    public String toString() {
        return String.format("%s (%s) - Nota: %.1f", titulo, ano, nota);
    }
}