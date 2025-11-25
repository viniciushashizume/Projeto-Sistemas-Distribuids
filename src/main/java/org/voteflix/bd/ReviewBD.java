package org.voteflix.bd;

import org.voteflix.model.Review;
import org.voteflix.util.ConexaoBancoDados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReviewBD {

    public boolean adicionarReview(Review review) throws SQLException {
        String sql = "INSERT INTO reviews(id_filme, nome_usuario, nota, titulo, descricao, data, editado) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexaoBancoDados.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, review.getIdFilme());
            pstmt.setString(2, review.getNomeUsuario());
            pstmt.setDouble(3, review.getNota());
            pstmt.setString(4, review.getTitulo());
            pstmt.setString(5, review.getDescricao());
            pstmt.setString(6, review.getData());
            pstmt.setBoolean(7, false); // Criado agora, não é editado
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean atualizarReview(Review review) throws SQLException {
        // Atualiza nota, titulo, descricao e marca como editado
        String sql = "UPDATE reviews SET nota = ?, titulo = ?, descricao = ?, editado = ? WHERE id = ?";
        try (Connection conn = ConexaoBancoDados.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, review.getNota());
            pstmt.setString(2, review.getTitulo());
            pstmt.setString(3, review.getDescricao());
            pstmt.setBoolean(4, true); // Marca como editado
            pstmt.setInt(5, review.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean excluirReview(int id) throws SQLException {
        String sql = "DELETE FROM reviews WHERE id = ?";
        try (Connection conn = ConexaoBancoDados.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Exclui todas as reviews de um filme (usado ao apagar um filme).
     */
    public void excluirReviewsPorFilme(int idFilme, Connection conn) throws SQLException {
        String sql = "DELETE FROM reviews WHERE id_filme = ?";
        // Se a conexão vier nula (uso isolado), cria uma nova
        if (conn == null) {
            try (Connection novaConn = ConexaoBancoDados.conectar();
                 PreparedStatement pstmt = novaConn.prepareStatement(sql)) {
                pstmt.setInt(1, idFilme);
                pstmt.executeUpdate();
            }
        } else {
            // Usa a conexão transacional fornecida
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, idFilme);
                pstmt.executeUpdate();
            }
        }
    }

    public List<Review> listarReviewsPorFilme(int idFilme) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE id_filme = ?";
        try (Connection conn = ConexaoBancoDados.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idFilme);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                reviews.add(mapearReview(rs));
            }
        }
        return reviews;
    }

    public Review buscarReviewPorId(int id) throws SQLException {
        String sql = "SELECT * FROM reviews WHERE id = ?";
        try (Connection conn = ConexaoBancoDados.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapearReview(rs);
            }
        }
        return null;
    }

    // Verifica se o usuário já fez review para este filme
    public boolean existeReviewDoUsuarioNoFilme(int idFilme, String nomeUsuario) throws SQLException {
        String sql = "SELECT id FROM reviews WHERE id_filme = ? AND nome_usuario = ?";
        try (Connection conn = ConexaoBancoDados.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idFilme);
            pstmt.setString(2, nomeUsuario);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    // Retorna um array [média, quantidade]
    public double[] calcularMediaEQuantidade(int idFilme) throws SQLException {
        String sql = "SELECT AVG(nota) as media, COUNT(id) as qtd FROM reviews WHERE id_filme = ?";
        try (Connection conn = ConexaoBancoDados.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idFilme);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new double[]{rs.getDouble("media"), rs.getInt("qtd")};
            }
        }
        return new double[]{0.0, 0.0};
    }

    // Helper para criar objeto Review a partir do ResultSet
    private Review mapearReview(ResultSet rs) throws SQLException {
        org.json.JSONObject json = new org.json.JSONObject();
        json.put("id", rs.getInt("id"));
        json.put("id_filme", rs.getInt("id_filme"));
        json.put("nome_usuario", rs.getString("nome_usuario"));
        json.put("nota", rs.getDouble("nota"));
        json.put("titulo", rs.getString("titulo"));
        json.put("descricao", rs.getString("descricao"));
        json.put("data", rs.getString("data"));
        json.put("editado", rs.getBoolean("editado"));
        return new Review(json);
    }
}