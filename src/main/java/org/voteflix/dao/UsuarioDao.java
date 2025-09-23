package org.voteflix.dao;

import org.voteflix.model.Usuario;
import org.voteflix.util.ConexaoBancoDados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDao {

    public Usuario buscarUsuarioPorNome(String nome) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE nome = ?";
        try (Connection conn = ConexaoBancoDados.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nome);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Usuario(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("senha")
                );
            }
        }
        return null;
    }

    public boolean adicionarUsuario(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios(nome, senha) VALUES(?, ?)";
        try (Connection conn = ConexaoBancoDados.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario.getNome());
            pstmt.setString(2, usuario.getSenha());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}