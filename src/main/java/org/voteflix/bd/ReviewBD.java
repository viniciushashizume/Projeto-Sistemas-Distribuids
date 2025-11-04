package org.voteflix.bd;

import org.voteflix.util.ConexaoBancoDados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * BD minimal para Reviews, focado em suportar o CRUD de Filmes.
 */
public class ReviewBD {

    /**
     * Exclui todas as reviews associadas a um ID de filme.
     * Usado ao excluir um filme.
     * @param idFilme ID do filme.
     * @param conn Conexão transacional (se nula, cria uma nova).
     * @throws SQLException
     */
    public void excluirReviewsPorFilme(int idFilme, Connection conn) throws SQLException {
        String sql = "DELETE FROM reviews WHERE id_filme = ?";

        // Se a conexão não for passada, cria uma nova (não transacional)
        if (conn != null) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, idFilme);
                pstmt.executeUpdate();
            }
        } else {
            // Cria conexão própria
            try (Connection newConn = ConexaoBancoDados.conectar();
                 PreparedStatement pstmt = newConn.prepareStatement(sql)) {
                pstmt.setInt(1, idFilme);
                pstmt.executeUpdate();
            }
        }
    }
}