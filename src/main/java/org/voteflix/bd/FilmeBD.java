package org.voteflix.bd;

import org.voteflix.model.Filme;
import org.voteflix.util.ConexaoBancoDados;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FilmeBD {

    /**
     * Verifica se um filme com o mesmo título, diretor e ano já existe.
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
     * CORREÇÃO: Removidas as colunas 'nota' e 'qtd_avaliacoes' do INSERT.
     */
    public boolean adicionarFilme(Filme filme) throws SQLException {
        // CORREÇÃO: Query ajustada para inserir apenas os campos que existem
        String sqlFilme = "INSERT INTO filmes(titulo, diretor, ano, sinopse) VALUES(?, ?, ?, ?)";
        String sqlGenero = "INSERT INTO filmes_generos(id_filme, id_genero) VALUES(?, (SELECT id FROM generos WHERE LOWER(TRIM(nome)) = LOWER(?)))";
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

            // 3. Insere os gêneros
            PreparedStatement pstmtGenero = conn.prepareStatement(sqlGenero);
            for (String generoNome : filme.getGeneros()) {
                pstmtGenero.setInt(1, idFilme);
                pstmtGenero.setString(2, generoNome);
                pstmtGenero.addBatch();
            }
            pstmtGenero.executeBatch();

            conn.commit(); // Finaliza transação
            return true;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            e.printStackTrace(); // Deixando o log para o caso de outro erro
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }

    /**
     * Lista todos os filmes, incluindo seus gêneros.
     * CORREÇÃO: Seleciona colunas explícitas (sem f.*) e passa 0.0 e 0 para o construtor do Filme.
     */
    public List<Filme> listarTodosFilmes() throws SQLException {
        List<Filme> filmes = new ArrayList<>();
        // CORREÇÃO: Seleciona colunas explícitas e não usa f.*
        String sql = "SELECT f.id, f.titulo, f.diretor, f.ano, f.sinopse, GROUP_CONCAT(TRIM(g.nome) SEPARATOR ',') AS generos " +
                "FROM filmes f " +
                "LEFT JOIN filmes_generos fg ON f.id = fg.id_filme " +
                "LEFT JOIN generos g ON fg.id_genero = g.id " +
                "GROUP BY f.id, f.titulo, f.diretor, f.ano, f.sinopse"; // GROUP BY atualizado

        try (Connection conn = ConexaoBancoDados.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                List<String> generosList = new ArrayList<>();
                String generosDb = rs.getString("generos");
                if (generosDb != null && !generosDb.isEmpty()) {
                    generosList.addAll(List.of(generosDb.split(",")));
                }

                // CORREÇÃO: Passa 0.0 e 0 manualmente, pois não vêm do BD
                filmes.add(new Filme(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("diretor"),
                        rs.getString("ano"),
                        rs.getString("sinopse"),
                        0.0, // Hardcoded nota
                        0,   // Hardcoded qtd_avaliacoes
                        generosList
                ));
            }
        }
        return filmes;
    }

    /**
     * Atualiza um filme e seus gêneros (transacional).
     * (Esta query já estava correta, pois não tocava em 'nota' ou 'qtd_avaliacoes')
     */
    public boolean atualizarFilme(Filme filme) throws SQLException {
        String sqlFilme = "UPDATE filmes SET titulo = ?, diretor = ?, ano = ?, sinopse = ? WHERE id = ?";
        String sqlDeleteGeneros = "DELETE FROM filmes_generos WHERE id_filme = ?";
        String sqlInsertGeneros = "INSERT INTO filmes_generos(id_filme, id_genero) VALUES(?, (SELECT id FROM generos WHERE LOWER(TRIM(nome)) = LOWER(?)))";
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
            for (String generoNome : filme.getGeneros()) {
                pstmtInsertGen.setInt(1, filme.getId());
                pstmtInsertGen.setString(2, generoNome);
                pstmtInsertGen.addBatch();
            }
            pstmtInsertGen.executeBatch();

            conn.commit(); // Finaliza transação
            return true;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            e.printStackTrace();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }

    /**
     * Exclui um filme (transacional).
     */
    public boolean excluirFilme(int id) throws SQLException {
        String sqlDeleteGeneros = "DELETE FROM filmes_generos WHERE id_filme = ?";
        String sqlDeleteFilme = "DELETE FROM filmes WHERE id = ?";
        Connection conn = null;

        try {
            conn = ConexaoBancoDados.conectar();
            conn.setAutoCommit(false); // Inicia transação

            // 1. Deleta gêneros
            PreparedStatement pstmtDeleteGen = conn.prepareStatement(sqlDeleteGeneros);
            pstmtDeleteGen.setInt(1, id);
            pstmtDeleteGen.executeUpdate();

            // 2. Deleta filme
            PreparedStatement pstmtDeleteFilme = conn.prepareStatement(sqlDeleteFilme);
            pstmtDeleteFilme.setInt(1, id);
            int affectedRows = pstmtDeleteFilme.executeUpdate();

            conn.commit(); // Finaliza transação
            return affectedRows > 0;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            e.printStackTrace();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }

    /**
     * Busca os nomes de gêneros válidos do banco de dados.
     */
    public Set<String> getNomesGenerosValidos() throws SQLException {
        Set<String> generos = new HashSet<>();
        String sql = "SELECT TRIM(nome) as nome FROM generos";
        try (Connection conn = ConexaoBancoDados.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                generos.add(rs.getString("nome"));
            }
        }
        return generos;
    }
}