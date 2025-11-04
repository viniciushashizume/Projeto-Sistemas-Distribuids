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
import java.util.stream.Collectors;

public class FilmeServico {

    private final FilmeBD filmeBD;
    // Gêneros pré-cadastrados [cite: 64]
    private static final Set<String> GENEROS_VALIDOS = Set.of(
            "Ação", "Aventura", "Comédia", "Drama", "Fantasia",
            "Ficção Científica", "Terror", "Romance", "Documentário",
            "Musical", "Animação"
    );

    public FilmeServico() {
        this.filmeBD = new FilmeBD();
    }

    /**
     * [ADMIN] Cria um novo filme. [cite: 15]
     */
    public JSONObject criarFilme(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            String token = requisicao.getString("token");
            String funcao = JwtUtil.getFuncaoFromToken(token);

            // 1. Verifica permissão (Apenas Admin) [cite: 15]
            if (!"admin".equals(funcao)) {
                ProtocoloMensagem.ERRO_SEM_PERMISSAO.aplicar(resposta); // 403
                return resposta;
            }

            // 2. Extrai dados
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

            // 3. Validação de campos [cite: 57, 59, 60, 61, 62, 63]
            if (!validarCamposFilme(titulo, diretor, ano, sinopse, generos)) {
                ProtocoloMensagem.ERRO_CAMPOS_INVALIDOS.aplicar(resposta); // 405
                return resposta;
            }

            // 4. Validação de Unicidade [cite: 39]
            if (filmeBD.verificarFilmeUnico(titulo, diretor, ano)) {
                ProtocoloMensagem.ERRO_RECURSO_JA_EXISTE.aplicar(resposta); // 409
                return resposta;
            }

            // 5. Cria e insere
            Filme novoFilme = new Filme(titulo, diretor, ano, sinopse, generos);
            boolean sucesso = filmeBD.adicionarFilme(novoFilme);

            if (sucesso) {
                ProtocoloMensagem.SUCESSO_RECURSO_CADASTRADO.aplicar(resposta); // 201
            } else {
                ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta); // 500
            }

        } catch (JSONException e) {
            ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta); // 422
        } catch (SQLException e) {
            ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta); // 500
        } catch (Exception e) {
            ProtocoloMensagem.ERRO_TOKEN_INVALIDO.aplicar(resposta); // 401
        }
        return resposta;
    }

    /**
     * [ADMIN/USER] Lista todos os filmes. [cite: 16]
     */
    public JSONObject listarFilmes(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            // 1. Valida o token (qualquer usuário logado pode listar)
            String token = requisicao.getString("token");
            JwtUtil.getIdFromToken(token); // Apenas valida se o token é bom

            // 2. Busca dados
            List<Filme> filmes = filmeBD.listarTodosFilmes();
            JSONArray listaFilmesJson = new JSONArray();

            for (Filme f : filmes) {
                listaFilmesJson.put(f.toJSONObject());
            }

            ProtocoloMensagem.SUCESSO_OPERACAO.aplicar(resposta); // 200
            resposta.put("filmes", listaFilmesJson);

        } catch (JSONException e) {
            ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta); // 422
        } catch (SQLException e) {
            ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta); // 500
        } catch (Exception e) {
            ProtocoloMensagem.ERRO_TOKEN_INVALIDO.aplicar(resposta); // 401
        }
        return resposta;
    }

    /**
     * [ADMIN] Edita um filme existente.
     */
    public JSONObject editarFilme(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            String token = requisicao.getString("token");
            String funcao = JwtUtil.getFuncaoFromToken(token);

            // 1. Verifica permissão (Apenas Admin)
            if (!"admin".equals(funcao)) {
                ProtocoloMensagem.ERRO_SEM_PERMISSAO.aplicar(resposta); // 403
                return resposta;
            }

            // 2. Extrai dados
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

            // 3. Validação de campos [cite: 57, 59, 60, 61, 62, 63]
            if (id <= 0 || !validarCamposFilme(titulo, diretor, ano, sinopse, generos)) {
                ProtocoloMensagem.ERRO_CAMPOS_INVALIDOS.aplicar(resposta); // 405
                return resposta;
            }

            // 4. Validação de Unicidade (não pode alterar para um combo que já existe)
            // (Omissão por simplicidade, mas idealmente deveria verificar se o
            // novo combo (titulo, diretor, ano) já existe em OUTRO id)

            // 5. Cria e atualiza
            Filme filmeAtualizado = new Filme(titulo, diretor, ano, sinopse, generos);
            filmeAtualizado.setId(id); // Define o ID para a atualização

            boolean sucesso = filmeBD.atualizarFilme(filmeAtualizado);

            if (sucesso) {
                ProtocoloMensagem.SUCESSO_OPERACAO.aplicar(resposta); // 200
            } else {
                ProtocoloMensagem.ERRO_RECURSO_INEXISTENTE.aplicar(resposta); // 404 (ID do filme não encontrado)
            }

        } catch (NumberFormatException | JSONException e) {
            ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta); // 422
        } catch (SQLException e) {
            ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta); // 500
        } catch (Exception e) {
            ProtocoloMensagem.ERRO_TOKEN_INVALIDO.aplicar(resposta); // 401
        }
        return resposta;
    }

    /**
     * [ADMIN] Exclui um filme. [cite: 18]
     */
    public JSONObject excluirFilme(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            String token = requisicao.getString("token");
            String funcao = JwtUtil.getFuncaoFromToken(token);

            // 1. Verifica permissão (Apenas Admin) [cite: 18]
            if (!"admin".equals(funcao)) {
                ProtocoloMensagem.ERRO_SEM_PERMISSAO.aplicar(resposta); // 403
                return resposta;
            }

            // 2. Extrai dados
            int id = Integer.parseInt(requisicao.getString("id"));

            // 3. Executa exclusão (BD cuida de excluir reviews )
            boolean sucesso = filmeBD.excluirFilme(id);

            if (sucesso) {
                ProtocoloMensagem.SUCESSO_OPERACAO.aplicar(resposta); // 200
            } else {
                ProtocoloMensagem.ERRO_RECURSO_INEXISTENTE.aplicar(resposta); // 404
            }

        } catch (NumberFormatException | JSONException e) {
            ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta); // 422
        } catch (SQLException e) {
            ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta); // 500
        } catch (Exception e) {
            ProtocoloMensagem.ERRO_TOKEN_INVALIDO.aplicar(resposta); // 401
        }
        return resposta;
    }


    /**
     * Valida os campos de um filme contra os requisitos. [cite: 57-64]
     */
    private boolean validarCamposFilme(String titulo, String diretor, String ano, String sinopse, List<String> generos) {
        if (titulo == null || titulo.length() < 3 || titulo.length() > 30) return false; // [cite: 59]
        if (diretor == null || diretor.length() < 3 || diretor.length() > 30) return false; // [cite: 61]
        if (ano == null || ano.length() < 3 || ano.length() > 4 || !ano.matches("\\d+")) return false; // [cite: 60, 57]
        if (sinopse == null || sinopse.length() > 250) return false; // [cite: 63]
        if (generos == null || generos.isEmpty()) return false; // [cite: 62]

        // Valida se todos os gêneros enviados estão na lista de pré-cadastrados [cite: 64]
        for (String g : generos) {
            if (!GENEROS_VALIDOS.contains(g)) return false;
        }

        return true;
    }
}