package org.voteflix.bd;

import org.voteflix.model.Filme;
import org.voteflix.util.ConexaoBancoDados;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class FilmeBD {

    /**
     * Verifica se um filme com o mesmo título, diretor e ano já existe. [cite: 39]
     */
    public boolean verificarFilmeUnico(String titulo, String diretor, String ano) throws SQLException {
        String sql = "SELECT id FROM filmes WHERE titulo = ? AND diretor = ? AND ano = ?";
        try (Connection conn = ConexaoBancoDados.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, titulo);
            pstmt.setString(2, diretor);
            pstmt.setString(3, ano);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    /**
     * Adiciona um novo filme e seus gêneros (transacional).
     */
    public boolean adicionarFilme(Filme filme) throws SQLException {
        String sqlFilme = "INSERT INTO filmes(titulo, diretor, ano, sinopse, nota, qtd_avaliacoes) VALUES(?, ?, ?, ?, 0, 0)";
        String sqlGenero = "INSERT INTO filmes_generos(id_filme, genero) VALUES(?, ?)";
        Connection conn = null;
        try {
            conn = ConexaoBancoDados.conectar();
            conn.setAutoCommit(false); // Inicia transação

            // 1. Insere o filme
            PreparedStatement pstmtFilme = conn.prepareStatement(sqlFilme, Statement.RETURN_GENERATED_KEYS);
            pstmtFilme.setString(1, filme.getTitulo());
            pstmtFilme.setString(2, filme.getDiretor());
            pstmtFilme.setString(3, filme.getAno());
            pstmtFilme.setString(4, filme.getSinopse());
            int affectedRows = pstmtFilme.executeUpdate();

            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }

            // 2. Recupera o ID gerado
            int idFilme;
            try (ResultSet generatedKeys = pstmtFilme.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    idFilme = generatedKeys.getInt(1);
                } else {
                    conn.rollback();
                    return false;
                }
            }

            // 3. Insere os gêneros [cite: 62]
            PreparedStatement pstmtGenero = conn.prepareStatement(sqlGenero);
            for (String genero : filme.getGeneros()) {
                pstmtGenero.setInt(1, idFilme);
                pstmtGenero.setString(2, genero);
                pstmtGenero.addBatch();
            }
            pstmtGenero.executeBatch();

            conn.commit(); // Finaliza transação
            return true;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }

    /**
     * Lista todos os filmes, incluindo seus gêneros.
     */
    public List<Filme> listarTodosFilmes() throws SQLException {
        List<Filme> filmes = new ArrayList<>();
        // Query complexa para buscar filmes e agrupar gêneros
        String sql = "SELECT f.*, GROUP_CONCAT(fg.genero SEPARATOR ',') AS generos " +
                "FROM filmes f " +
                "LEFT JOIN filmes_generos fg ON f.id = fg.id_filme " +
                "GROUP BY f.id";

        try (Connection conn = ConexaoBancoDados.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                List<String> generosList = new ArrayList<>();
                String generosDb = rs.getString("generos");
                if (generosDb != null && !generosDb.isEmpty()) {
                    generosList.addAll(List.of(generosDb.split(",")));
                }

                filmes.add(new Filme(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("diretor"),
                        rs.getString("ano"),
                        rs.getString("sinopse"),
                        rs.getDouble("nota"),
                        rs.getInt("qtd_avaliacoes"),
                        generosList
                ));
            }
        }
        return filmes;
    }

    /**
     * Atualiza um filme e seus gêneros (transacional).
     */
    public boolean atualizarFilme(Filme filme) throws SQLException {
        String sqlFilme = "UPDATE filmes SET titulo = ?, diretor = ?, ano = ?, sinopse = ? WHERE id = ?";
        String sqlDeleteGeneros = "DELETE FROM filmes_generos WHERE id_filme = ?";
        String sqlInsertGeneros = "INSERT INTO filmes_generos(id_filme, genero) VALUES(?, ?)";
        Connection conn = null;

        try {
            conn = ConexaoBancoDados.conectar();
            conn.setAutoCommit(false); // Inicia transação

            // 1. Atualiza dados do filme
            PreparedStatement pstmtFilme = conn.prepareStatement(sqlFilme);
            pstmtFilme.setString(1, filme.getTitulo());
            pstmtFilme.setString(2, filme.getDiretor());
            pstmtFilme.setString(3, filme.getAno());
            pstmtFilme.setString(4, filme.getSinopse());
            pstmtFilme.setInt(5, filme.getId());
            int affectedRows = pstmtFilme.executeUpdate();

            if (affectedRows == 0) {
                conn.rollback(); // Filme não encontrado
                return false;
            }

            // 2. Deleta gêneros antigos
            PreparedStatement pstmtDeleteGen = conn.prepareStatement(sqlDeleteGeneros);
            pstmtDeleteGen.setInt(1, filme.getId());
            pstmtDeleteGen.executeUpdate();

            // 3. Insere novos gêneros
            PreparedStatement pstmtInsertGen = conn.prepareStatement(sqlInsertGeneros);
            for (String genero : filme.getGeneros()) {
                pstmtInsertGen.setInt(1, filme.getId());
                pstmtInsertGen.setString(2, genero);
                pstmtInsertGen.addBatch();
            }
            pstmtInsertGen.executeBatch();

            conn.commit(); // Finaliza transação
            return true;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }

    /**
     * Exclui um filme e suas reviews associadas (transacional).
     */
    public boolean excluirFilme(int id) throws SQLException {
        // Dependência do ReviewBD
        ReviewBD reviewBD = new ReviewBD();

        String sqlDeleteGeneros = "DELETE FROM filmes_generos WHERE id_filme = ?";
        String sqlDeleteFilme = "DELETE FROM filmes WHERE id = ?";
        Connection conn = null;

        try {
            conn = ConexaoBancoDados.conectar();
            conn.setAutoCommit(false); // Inicia transação

            // 1. Exclui reviews associadas (conforme requisito )
            reviewBD.excluirReviewsPorFilme(id, conn); // Passa a conexão transacional

            // 2. Deleta gêneros
            PreparedStatement pstmtDeleteGen = conn.prepareStatement(sqlDeleteGeneros);
            pstmtDeleteGen.setInt(1, id);
            pstmtDeleteGen.executeUpdate();

            // 3. Deleta filme
            PreparedStatement pstmtDeleteFilme = conn.prepareStatement(sqlDeleteFilme);
            pstmtDeleteFilme.setInt(1, id);
            int affectedRows = pstmtDeleteFilme.executeUpdate();

            conn.commit(); // Finaliza transação
            return affectedRows > 0;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }
}