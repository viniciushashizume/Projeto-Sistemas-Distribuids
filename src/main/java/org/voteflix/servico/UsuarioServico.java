package org.voteflix.servico;

import org.json.JSONObject;
import org.voteflix.dao.UsuarioDao;
import org.voteflix.model.Usuario;
import org.voteflix.util.JwtUtil;

import java.sql.SQLException;

public class UsuarioServico {

    private final UsuarioDao usuarioDao;

    public UsuarioServico() {
        this.usuarioDao = new UsuarioDao();
    }

    public JSONObject realizarLogin(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            String nomeUsuario = requisicao.getString("usuario");
            String senha = requisicao.getString("senha");

            Usuario usuario = usuarioDao.buscarUsuarioPorNome(nomeUsuario);

            if (usuario == null) {
                resposta.put("status", "404"); // Usuário não encontrado
                return resposta;
            }

            if (senha.equals(usuario.getSenha())) {
                String token = JwtUtil.gerarToken(usuario);
                resposta.put("status", "200 "); // Sucesso
                resposta.put("token", token);
            } else {
                resposta.put("status", "401"); // Credenciais inválidas
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados no login: " + e.getMessage());
            e.printStackTrace();
            resposta.put("status", "500");
            resposta.put("mensagem", "Erro de banco de dados.");
        }
        return resposta;
    }

    public JSONObject criarUsuario(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            JSONObject dadosUsuario = requisicao.getJSONObject("usuario");
            String nome = dadosUsuario.getString("nome");
            String senha = dadosUsuario.getString("senha");

            if (nome.length() < 3 || nome.length() > 20 || senha.length() < 3 || senha.length() > 20) {
                resposta.put("status", "422");
                return resposta;
            }

            if (usuarioDao.buscarUsuarioPorNome(nome) != null) {
                resposta.put("status", "409"); // Conflito, usuário já existe
                return resposta;
            }

            Usuario novoUsuario = new Usuario(nome, senha);
            boolean sucesso = usuarioDao.adicionarUsuario(novoUsuario);

            if (sucesso) {
                resposta.put("status", "201"); // Criado
            } else {
                // Isso agora é menos provável de acontecer, pois um erro lançaria uma SQLException
                resposta.put("status", "500");
                resposta.put("mensagem", "Não foi possível adicionar o usuário.");
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados ao criar usuário: " + e.getMessage());
            e.printStackTrace();
            resposta.put("status", "500");
            resposta.put("mensagem", "Erro ao acessar o banco de dados: " + e.getMessage());
        }
        return resposta;
    }
}