package org.voteflix.servidor.servico;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.voteflix.bd.FilmeBD;
import org.voteflix.model.Filme;
import org.voteflix.util.JwtUtil;
import org.voteflix.util.ProtocoloMensagem;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors; // Importar

public class FilmeServico {

    private final FilmeBD filmeBD;
    private final Set<String> generosValidos;

    public FilmeServico() {
        this.filmeBD = new FilmeBD();
        Set<String> generosCarregados;
        try {
            Set<String> nomesLimpos = filmeBD.getNomesGenerosValidos();

            if (nomesLimpos.isEmpty()) {
                System.err.println("AVISO: Nenhum gênero encontrado no banco de dados.");
                generosCarregados = new HashSet<>();
            } else {
                generosCarregados = nomesLimpos.stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet());
            }
        } catch (SQLException e) {
            generosCarregados = new HashSet<>();
            System.err.println("ERRO FATAL: Não foi possível carregar gêneros do BD. " + e.getMessage());
            e.printStackTrace();
        }
        this.generosValidos = generosCarregados;
    }


    public JSONObject criarFilme(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            String token = requisicao.getString("token");
            String funcao = JwtUtil.getFuncaoFromToken(token);

            if (!"admin".equals(funcao)) {
                ProtocoloMensagem.ERRO_SEM_PERMISSAO.aplicar(resposta); // 403
                return resposta;
            }

            JSONObject dadosFilme = requisicao.getJSONObject("filme");
            String titulo = dadosFilme.getString("titulo");
            String diretor = dadosFilme.getString("diretor");
            String ano = dadosFilme.getString("ano");
            String sinopse = dadosFilme.getString("sinopse");
            JSONArray generosJson = dadosFilme.getJSONArray("genero");
            List<String> generos = new ArrayList<>();
            for (int i = 0; i < generosJson.length(); i++) {
                generos.add(generosJson.getString(i));
            }

            // PASSO 1: Validar campos nulos/faltantes (422)
            if (!validarCamposNulosFilme(titulo, diretor, ano, sinopse, generos)) {
                ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta); // 422
                return resposta;
            }

            // PASSO 2: Validar padrão/tamanho dos campos (405)
            if (!validarPadraoCamposFilme(titulo, diretor, ano, sinopse, generos)) {
                ProtocoloMensagem.ERRO_CAMPOS_INVALIDOS.aplicar(resposta); // 405
                return resposta;
            }

            if (filmeBD.verificarFilmeUnico(titulo, diretor, ano)) {
                ProtocoloMensagem.ERRO_RECURSO_JA_EXISTE.aplicar(resposta); // 409
                return resposta;
            }

            Filme novoFilme = new Filme(titulo, diretor, ano, sinopse, generos);
            boolean sucesso = filmeBD.adicionarFilme(novoFilme);

            if (sucesso) {
                ProtocoloMensagem.SUCESSO_RECURSO_CADASTRADO.aplicar(resposta); // 201
            } else {
                ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta); // 500
            }

        } catch (JSONException e) {
            ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta); // 422
            e.printStackTrace(); // LOG DE ERRO
        } catch (SQLException e) {
            ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta); // 500
            e.printStackTrace(); // LOG DE ERRO
        } catch (Exception e) {
            ProtocoloMensagem.ERRO_TOKEN_INVALIDO.aplicar(resposta); // 401
            e.printStackTrace(); // LOG DE ERRO
        }
        return resposta;
    }

    public JSONObject listarFilmes(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            String token = requisicao.getString("token");
            JwtUtil.getIdFromToken(token);

            List<Filme> filmes = filmeBD.listarTodosFilmes();
            JSONArray listaFilmesJson = new JSONArray();

            for (Filme f : filmes) {
                listaFilmesJson.put(f.toJSONObject());
            }

            ProtocoloMensagem.SUCESSO_OPERACAO.aplicar(resposta); // 200
            resposta.put("filmes", listaFilmesJson);

        } catch (JSONException e) {
            ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta); // 422
            e.printStackTrace(); // LOG DE ERRO
        } catch (SQLException e) {
            ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta); // 500
            e.printStackTrace(); // LOG DE ERRO
        } catch (Exception e) {
            ProtocoloMensagem.ERRO_TOKEN_INVALIDO.aplicar(resposta); // 401
            e.printStackTrace(); // LOG DE ERRO
        }
        return resposta;
    }

    public JSONObject editarFilme(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            String token = requisicao.getString("token");
            String funcao = JwtUtil.getFuncaoFromToken(token);

            if (!"admin".equals(funcao)) {
                ProtocoloMensagem.ERRO_SEM_PERMISSAO.aplicar(resposta); // 403
                return resposta;
            }

            JSONObject dadosFilme = requisicao.getJSONObject("filme");
            int id = Integer.parseInt(dadosFilme.getString("id"));
            String titulo = dadosFilme.getString("titulo");
            String diretor = dadosFilme.getString("diretor");
            String ano = dadosFilme.getString("ano");
            String sinopse = dadosFilme.getString("sinopse");
            JSONArray generosJson = dadosFilme.getJSONArray("genero");
            List<String> generos = new ArrayList<>();
            for (int i = 0; i < generosJson.length(); i++) {
                generos.add(generosJson.getString(i));
            }

            if (!validarCamposNulosFilme(titulo, diretor, ano, sinopse, generos)) {
                ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta); // 422
                return resposta;
            }

            if (id <= 0 || !validarPadraoCamposFilme(titulo, diretor, ano, sinopse, generos)) {
                ProtocoloMensagem.ERRO_CAMPOS_INVALIDOS.aplicar(resposta); // 405
                return resposta;
            }

            Filme filmeAtualizado = new Filme(titulo, diretor, ano, sinopse, generos);
            filmeAtualizado.setId(id);

            boolean sucesso = filmeBD.atualizarFilme(filmeAtualizado);

            if (sucesso) {
                ProtocoloMensagem.SUCESSO_OPERACAO.aplicar(resposta); // 200
            } else {
                ProtocoloMensagem.ERRO_RECURSO_INEXISTENTE.aplicar(resposta); // 404
            }

        } catch (NumberFormatException | JSONException e) {
            ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta); // 422
            e.printStackTrace(); // LOG DE ERRO
        } catch (SQLException e) {
            ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta); // 500
            e.printStackTrace(); // LOG DE ERRO
        } catch (Exception e) {
            ProtocoloMensagem.ERRO_TOKEN_INVALIDO.aplicar(resposta); // 401
            e.printStackTrace(); // LOG DE ERRO
        }
        return resposta;
    }

    public JSONObject excluirFilme(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            String token = requisicao.getString("token");
            String funcao = JwtUtil.getFuncaoFromToken(token);

            if (!"admin".equals(funcao)) {
                ProtocoloMensagem.ERRO_SEM_PERMISSAO.aplicar(resposta); // 403
                return resposta;
            }

            int id = Integer.parseInt(requisicao.getString("id"));
            boolean sucesso = filmeBD.excluirFilme(id);

            if (sucesso) {
                ProtocoloMensagem.SUCESSO_OPERACAO.aplicar(resposta); // 200
            } else {
                ProtocoloMensagem.ERRO_RECURSO_INEXISTENTE.aplicar(resposta); // 404
            }

        } catch (NumberFormatException | JSONException e) {
            ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta); // 422
            e.printStackTrace(); // LOG DE ERRO
        } catch (SQLException e) {
            ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta); // 500
            e.printStackTrace(); // LOG DE ERRO
        } catch (Exception e) {
            ProtocoloMensagem.ERRO_TOKEN_INVALIDO.aplicar(resposta); // 401
            e.printStackTrace(); // LOG DE ERRO
        }
        return resposta;
    }


    private boolean validarCamposNulosFilme(String titulo, String diretor, String ano, String sinopse, List<String> generos) {
        if (titulo == null || diretor == null || ano == null || sinopse == null) {
            return false; // Um campo de texto é nulo
        }
        if (generos == null || generos.isEmpty()) {
            return false; // Lista de gêneros é nula ou vazia
        }
        for (String g : generos) {
            if (g == null) return false; // Um item na lista de gêneros é nulo
        }
        return true;
    }

    private boolean validarPadraoCamposFilme(String titulo, String diretor, String ano, String sinopse, List<String> generos) {
        if (titulo.length() < 3 || titulo.length() > 30) return false;
        if (diretor.length() < 3 || diretor.length() > 30) return false;
        if (ano.length() < 3 || ano.length() > 4 || !ano.matches("\\d+")) return false;
        if (sinopse.length() > 250) return false;

        for (String g : generos) {
            if (!this.generosValidos.contains(g.toLowerCase())) return false; // Gênero não existe
        }

        return true;
    }
}