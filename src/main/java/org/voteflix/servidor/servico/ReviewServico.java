package org.voteflix.servidor.servico;

import org.json.JSONException;
import org.json.JSONObject;
import org.voteflix.bd.FilmeBD;
import org.voteflix.bd.ReviewBD;
import org.voteflix.model.Review;
import org.voteflix.util.JwtUtil;
import org.voteflix.util.ProtocoloMensagem;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReviewServico {

    private final ReviewBD reviewBD;
    private final FilmeBD filmeBD;

    public ReviewServico() {
        this.reviewBD = new ReviewBD();
        this.filmeBD = new FilmeBD();
    }

    public JSONObject criarReview(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            // 1. Validar Token e Identificar Usuário
            String token = requisicao.getString("token");
            // CORREÇÃO: Método correto é getNomeFromToken
            String nomeUsuario = JwtUtil.getNomeFromToken(token);

            JSONObject reviewJson = requisicao.getJSONObject("review");
            // Insere o nome do usuário no JSON para criar o objeto Review
            reviewJson.put("nome_usuario", nomeUsuario);

            // Adiciona data atual
            String dataAtual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            reviewJson.put("data", dataAtual);

            Review novaReview = new Review(reviewJson);

            // 2. Validações de Campos (422 e 405)
            if (!validarCamposReview(novaReview)) {
                ProtocoloMensagem.ERRO_CAMPOS_INVALIDOS.aplicar(resposta);
                return resposta;
            }

            // 3. Verificar se Filme existe (404)
            // Certifique-se de que FilmeBD possui o método buscarFilmePorId
            if (filmeBD.buscarFilmePorId(novaReview.getIdFilme()) == null) {
                ProtocoloMensagem.ERRO_RECURSO_INEXISTENTE.aplicar(resposta);
                return resposta;
            }

            // 4. Verificar se Usuário já fez review para este filme (409)
            if (reviewBD.existeReviewDoUsuarioNoFilme(novaReview.getIdFilme(), nomeUsuario)) {
                ProtocoloMensagem.ERRO_RECURSO_JA_EXISTE.aplicar(resposta);
                return resposta;
            }

            // 5. Persistir Review
            boolean sucesso = reviewBD.adicionarReview(novaReview);

            if (sucesso) {
                recalcularNotaFilme(novaReview.getIdFilme());
                ProtocoloMensagem.SUCESSO_RECURSO_CADASTRADO.aplicar(resposta);
            } else {
                ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta);
            }

        } catch (JSONException e) {
            ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta);
        } catch (SQLException e) {
            e.printStackTrace();
            ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta);
        } catch (Exception e) {
            e.printStackTrace();
            ProtocoloMensagem.ERRO_TOKEN_INVALIDO.aplicar(resposta);
        }
        return resposta;
    }

    public JSONObject editarReview(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            String token = requisicao.getString("token");
            // CORREÇÃO: Método correto é getNomeFromToken
            String nomeUsuario = JwtUtil.getNomeFromToken(token);

            JSONObject reviewJson = requisicao.getJSONObject("review");
            int idReview = reviewJson.optInt("id", -1);

            // Buscar review existente
            Review reviewExistente = reviewBD.buscarReviewPorId(idReview);

            // 1. Validar existência (404)
            if (reviewExistente == null) {
                ProtocoloMensagem.ERRO_RECURSO_INEXISTENTE.aplicar(resposta);
                return resposta;
            }

            // 2. Validar permissão (Apenas o dono pode editar)
            if (!reviewExistente.getNomeUsuario().equals(nomeUsuario)) {
                ProtocoloMensagem.ERRO_SEM_PERMISSAO.aplicar(resposta);
                return resposta;
            }

            // Validação de campos (405)
            double novaNota = reviewJson.optDouble("nota", -1);
            String novoTitulo = reviewJson.optString("titulo");
            String novaDescricao = reviewJson.optString("descricao");

            if (novaNota < 0 || novaNota > 5 || novoTitulo.isEmpty() || novaDescricao.isEmpty()) {
                ProtocoloMensagem.ERRO_CAMPOS_INVALIDOS.aplicar(resposta);
                return resposta;
            }

            // Prepara objeto para atualização (mantendo dados sensíveis originais)
            JSONObject updateJson = reviewJson;
            updateJson.put("id", idReview);
            updateJson.put("id_filme", reviewExistente.getIdFilme());
            updateJson.put("nome_usuario", nomeUsuario);
            updateJson.put("data", reviewExistente.getData());
            Review reviewAtualizada = new Review(updateJson);

            boolean sucesso = reviewBD.atualizarReview(reviewAtualizada);

            if (sucesso) {
                recalcularNotaFilme(reviewExistente.getIdFilme());
                ProtocoloMensagem.SUCESSO_OPERACAO.aplicar(resposta);
            } else {
                ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta);
            }

        } catch (JSONException e) {
            ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta);
        } catch (SQLException e) {
            e.printStackTrace();
            ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta);
        } catch (Exception e) {
            e.printStackTrace();
            ProtocoloMensagem.ERRO_TOKEN_INVALIDO.aplicar(resposta);
        }
        return resposta;
    }

    public JSONObject excluirReview(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            String token = requisicao.getString("token");
            // CORREÇÃO: Método correto é getNomeFromToken
            String nomeUsuario = JwtUtil.getNomeFromToken(token);
            String funcao = JwtUtil.getFuncaoFromToken(token);

            int idReview = Integer.parseInt(requisicao.getString("id"));
            Review review = reviewBD.buscarReviewPorId(idReview);

            if (review == null) {
                ProtocoloMensagem.ERRO_RECURSO_INEXISTENTE.aplicar(resposta);
                return resposta;
            }

            // Permissão: Admin pode tudo, Usuário Comum só o dele
            boolean isAdmin = "admin".equalsIgnoreCase(funcao);
            boolean isDono = review.getNomeUsuario().equals(nomeUsuario);

            if (!isAdmin && !isDono) {
                ProtocoloMensagem.ERRO_SEM_PERMISSAO.aplicar(resposta);
                return resposta;
            }

            boolean sucesso = reviewBD.excluirReview(idReview);

            if (sucesso) {
                recalcularNotaFilme(review.getIdFilme());
                ProtocoloMensagem.SUCESSO_OPERACAO.aplicar(resposta);
            } else {
                ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta);
            }

        } catch (NumberFormatException | JSONException e) {
            ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta);
        } catch (SQLException e) {
            e.printStackTrace();
            ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta);
        } catch (Exception e) {
            e.printStackTrace();
            ProtocoloMensagem.ERRO_TOKEN_INVALIDO.aplicar(resposta);
        }
        return resposta;
    }

    private void recalcularNotaFilme(int idFilme) {
        try {
            double[] dados = reviewBD.calcularMediaEQuantidade(idFilme);
            // Certifique-se de que FilmeBD possui o método atualizarNotaFilme
            filmeBD.atualizarNotaFilme(idFilme, dados[0], (int) dados[1]);
        } catch (SQLException e) {
            System.err.println("Erro ao recalcular nota do filme " + idFilme);
            e.printStackTrace();
        }
    }

    private boolean validarCamposReview(Review review) {
        if (review.getTitulo() == null || review.getTitulo().length() > 50) return false;
        if (review.getDescricao() == null || review.getDescricao().length() > 250) return false;
        if (review.getNota() < 0 || review.getNota() > 5) return false;
        return review.getIdFilme() > 0;
    }
}