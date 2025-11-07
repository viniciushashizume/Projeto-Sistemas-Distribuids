package org.voteflix.bd;

import org.voteflix.model.Usuario;
import org.voteflix.util.ConexaoBancoDados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioBD {

    public Usuario buscarUsuarioPorNome(String nome) throws SQLException {
        // Query ATUALIZADA para incluir 'funcao'
        String sql = "SELECT * FROM usuarios WHERE nome = ?";
        try (Connection conn = ConexaoBancoDados.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nome);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Usuario(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("senha"),
                        rs.getString("funcao") // CAMPO ADICIONADO
                );
            }
        }
        return null;
    }

    public boolean adicionarUsuario(Usuario usuario) throws SQLException {
        // Query ATUALIZADA para incluir 'funcao'
        String sql = "INSERT INTO usuarios(nome, senha, funcao) VALUES(?, ?, ?)";
        try (Connection conn = ConexaoBancoDados.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario.getNome());
            pstmt.setString(2, usuario.getSenha());
            pstmt.setString(3, usuario.getFuncao()); // CAMPO ADICIONADO
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public boolean atualizarSenha(int id, String novaSenha) throws SQLException {
        // (Sem alteração)
        String sql = "UPDATE usuarios SET senha = ? WHERE id = ?";
        try (Connection conn = ConexaoBancoDados.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, novaSenha);
            pstmt.setInt(2, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public boolean excluirUsuario(int id) throws SQLException {
        // (Sem alteração, mas agora usado pelo AdminExcluirUsuario)
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = ConexaoBancoDados.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public Usuario buscarUsuarioPorId(int id) throws SQLException {
        // Query ATUALIZADA para incluir 'funcao'
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (Connection conn = ConexaoBancoDados.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Usuario(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("senha"),
                        rs.getString("funcao") // CAMPO ADICIONADO
                );
            }
        }
        return null;
    }
    public List<Usuario> buscarTodosUsuarios() throws SQLException {
        // NOVO MÉTODO (para Admin)
        List<Usuario> usuarios = new ArrayList<>();
        // Query ATUALIZADA para incluir 'funcao' (como no seu arquivo)
        String sql = "SELECT * FROM usuarios";
        try (Connection conn = ConexaoBancoDados.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                usuarios.add(new Usuario(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("senha"),
                        rs.getString("funcao")
                ));
            }
        }
        return usuarios;
    }
}