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
            // Validação 422: Verifica se as chaves principais existem antes de processar
            if (!requisicao.has("token") || !requisicao.has("review")) {
                ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta);
                return resposta;
            }

            JSONObject reviewJson = requisicao.getJSONObject("review");

            // Validação 422: Campos obrigatórios dentro do objeto review
            if (!reviewJson.has("id_filme") || !reviewJson.has("titulo") ||
                    !reviewJson.has("descricao") || !reviewJson.has("nota")) {
                ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta);
                return resposta;
            }

            String token = requisicao.getString("token");
            String nomeUsuario = JwtUtil.getNomeFromToken(token);

            // Montagem do objeto
            reviewJson.put("nome_usuario", nomeUsuario);
            String dataAtual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            reviewJson.put("data", dataAtual);

            Review novaReview = new Review(reviewJson);

            // Validação 405: Conteúdo inválido (vazio, tamanho excedido, nota fora do range)
            if (!validarCamposReview(novaReview)) {
                ProtocoloMensagem.ERRO_CAMPOS_INVALIDOS.aplicar(resposta);
                return resposta;
            }

            // 3. Verificar se Filme existe (404)
            if (filmeBD.buscarFilmePorId(novaReview.getIdFilme()) == null) {
                ProtocoloMensagem.ERRO_RECURSO_INEXISTENTE.aplicar(resposta);
                return resposta;
            }

            // 4. Verificar se Usuário já fez review para este filme (409)
            if (reviewBD.existeReviewDoUsuarioNoFilme(novaReview.getIdFilme(), nomeUsuario)) {
                ProtocoloMensagem.ERRO_RECURSO_JA_EXISTE.aplicar(resposta);
                return resposta;
            }

            boolean sucesso = reviewBD.adicionarReview(novaReview);

            if (sucesso) {
                recalcularNotaFilme(novaReview.getIdFilme());
                ProtocoloMensagem.SUCESSO_RECURSO_CADASTRADO.aplicar(resposta);
            } else {
                ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta);
            }

        } catch (NumberFormatException e) {
            // ID do filme inválido (ex: texto onde deveria ser número) -> 400 Bad Request
            ProtocoloMensagem.ERRO_OPERACAO_INVALIDA.aplicar(resposta);
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
            if (!requisicao.has("token") || !requisicao.has("review")) {
                ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta);
                return resposta;
            }

            JSONObject reviewJson = requisicao.getJSONObject("review");

            // Validação 422: Garante que todos os campos para edição foram enviados
            if (!reviewJson.has("id") || !reviewJson.has("titulo") ||
                    !reviewJson.has("descricao") || !reviewJson.has("nota")) {
                ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta);
                return resposta;
            }

            String token = requisicao.getString("token");
            String nomeUsuario = JwtUtil.getNomeFromToken(token);

            int idReview = reviewJson.getInt("id");
            Review reviewExistente = reviewBD.buscarReviewPorId(idReview);

            // 1. Validar existência (404)
            if (reviewExistente == null) {
                ProtocoloMensagem.ERRO_RECURSO_INEXISTENTE.aplicar(resposta);
                return resposta;
            }

            // 2. Validar permissão (403)
            if (!reviewExistente.getNomeUsuario().equals(nomeUsuario)) {
                ProtocoloMensagem.ERRO_SEM_PERMISSAO.aplicar(resposta);
                return resposta;
            }

            // Validação 405: Valores dos campos
            double novaNota = reviewJson.getDouble("nota");
            String novoTitulo = reviewJson.getString("titulo");
            String novaDescricao = reviewJson.getString("descricao");

            if (novaNota < 0 || novaNota > 5 || novoTitulo.trim().isEmpty() ||
                    novoTitulo.length() > 50 || novaDescricao.trim().isEmpty() || novaDescricao.length() > 250) {
                ProtocoloMensagem.ERRO_CAMPOS_INVALIDOS.aplicar(resposta);
                return resposta;
            }

            // Prepara objeto para atualização
            JSONObject updateJson = new JSONObject();
            updateJson.put("id", idReview);
            updateJson.put("id_filme", reviewExistente.getIdFilme());
            updateJson.put("nome_usuario", nomeUsuario);
            updateJson.put("titulo", novoTitulo);
            updateJson.put("descricao", novaDescricao);
            updateJson.put("nota", String.valueOf(novaNota));
            updateJson.put("data", reviewExistente.getData());
            updateJson.put("editado", true);

            Review reviewAtualizada = new Review(updateJson);
            boolean sucesso = reviewBD.atualizarReview(reviewAtualizada);

            if (sucesso) {
                recalcularNotaFilme(reviewExistente.getIdFilme());
                ProtocoloMensagem.SUCESSO_OPERACAO.aplicar(resposta);
            } else {
                ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta);
            }

        } catch (NumberFormatException e) {
            ProtocoloMensagem.ERRO_OPERACAO_INVALIDA.aplicar(resposta); // 400
        } catch (JSONException e) {
            ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta); // 422
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
            if (!requisicao.has("token") || !requisicao.has("id")) {
                ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta);
                return resposta;
            }

            String token = requisicao.getString("token");
            String nomeUsuario = JwtUtil.getNomeFromToken(token);
            String funcao = JwtUtil.getFuncaoFromToken(token);

            // CORREÇÃO: Tratamento de erro de ID inválido (NumberFormatException)
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

        } catch (NumberFormatException e) {
            // Se o ID não for um número válido, retorna 400 (Bad Request) conforme protocolo
            ProtocoloMensagem.ERRO_OPERACAO_INVALIDA.aplicar(resposta);
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

    private void recalcularNotaFilme(int idFilme) {
        try {
            double[] dados = reviewBD.calcularMediaEQuantidade(idFilme);
            filmeBD.atualizarNotaFilme(idFilme, dados[0], (int) dados[1]);
        } catch (SQLException e) {
            System.err.println("Erro ao recalcular nota do filme " + idFilme);
            e.printStackTrace();
        }
    }

    private boolean validarCamposReview(Review review) {
        if (review.getTitulo() == null || review.getTitulo().trim().isEmpty() || review.getTitulo().length() > 50) return false;
        if (review.getDescricao() == null || review.getDescricao().trim().isEmpty() || review.getDescricao().length() > 250) return false;
        if (review.getNota() < 0 || review.getNota() > 5) return false;
        return review.getIdFilme() > 0;
    }
}