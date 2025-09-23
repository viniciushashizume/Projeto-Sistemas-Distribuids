package org.voteflix.servico;

import org.json.JSONObject;
import org.voteflix.bd.UsuarioBD;
import org.voteflix.model.Usuario;
import org.voteflix.servidor.Servidor;
import org.voteflix.util.JwtUtil;

import java.sql.SQLException;

public class UsuarioServico {

    private final UsuarioBD usuarioBD;

    public UsuarioServico() {
        this.usuarioBD = new UsuarioBD();
    }

    // ... (métodos realizarLogin, criarUsuario, realizarLogout, editarProprioUsuario existentes)
    public JSONObject realizarLogin(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            String nomeUsuario = requisicao.getString("usuario");
            String senha = requisicao.getString("senha");

            Usuario usuario = usuarioBD.buscarUsuarioPorNome(nomeUsuario);

            if (usuario == null) {
                resposta.put("status", "404"); // Usuário não encontrado
                return resposta;
            }

            if (senha.equals(usuario.getSenha())) {
                String token = JwtUtil.gerarToken(usuario);
                resposta.put("status", "200 "); // Sucesso
                resposta.put("token", token);
                // Adiciona usuário à lista de ativos
                Servidor.adicionarUsuarioAtivo(usuario.getNome());
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

            if (usuarioBD.buscarUsuarioPorNome(nome) != null) {
                resposta.put("status", "409"); // Conflito, usuário já existe
                return resposta;
            }

            Usuario novoUsuario = new Usuario(nome, senha);
            boolean sucesso = usuarioBD.adicionarUsuario(novoUsuario);

            if (sucesso) {
                resposta.put("status", "201"); // Criado
            } else {
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

    public JSONObject realizarLogout(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            String token = requisicao.getString("token");
            String nomeUsuario = JwtUtil.getNomeFromToken(token);
            // Remove o usuário da lista de ativos
            Servidor.removerUsuarioAtivo(nomeUsuario);
            resposta.put("status", "200");
            resposta.put("mensagem", "Logout realizado com sucesso.");
        } catch (Exception e) {
            // Mesmo que o token seja inválido, o cliente já se desconectou.
            // Apenas registramos o fato e retornamos sucesso.
            resposta.put("status", "200");
            resposta.put("mensagem", "Sessão finalizada.");
        }
        return resposta;
    }

    public JSONObject editarProprioUsuario(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            String token = requisicao.getString("token");
            int usuarioId = JwtUtil.getIdFromToken(token); // Usa ID para atualizações seguras

            JSONObject dadosUsuario = requisicao.getJSONObject("usuario");
            String novaSenha = dadosUsuario.getString("senha");

            if (novaSenha.length() < 3 || novaSenha.length() > 20) {
                resposta.put("status", "422"); // Unprocessable Entity
                resposta.put("mensagem", "Senha deve ter entre 3 e 20 caracteres.");
                return resposta;
            }

            boolean sucesso = usuarioBD.atualizarSenha(usuarioId, novaSenha);

            if (sucesso) {
                resposta.put("status", "200"); // OK
                resposta.put("mensagem", "Senha alterada com sucesso.");
            } else {
                // Isso pode acontecer se o ID do usuário no token não existir mais no BD
                resposta.put("status", "404");
                resposta.put("mensagem", "Usuário não encontrado para atualização.");
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados ao editar usuário: " + e.getMessage());
            e.printStackTrace();
            resposta.put("status", "500");
            resposta.put("mensagem", "Erro ao acessar o banco de dados.");
        } catch (Exception e) {
            System.err.println("Erro ao processar token ou requisição: " + e.getMessage());
            e.printStackTrace();
            resposta.put("status", "401"); // Unauthorized ou token inválido
            resposta.put("mensagem", "Token inválido ou requisição malformada.");
        }
        return resposta;
    }

    /**
     * Exclui a conta do próprio usuário. <-- NOVO MÉTODO
     * @param requisicao A requisição JSON contendo o token do usuário.
     * @return Um JSONObject com o status da operação.
     */
    public JSONObject excluirProprioUsuario(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            String token = requisicao.getString("token");
            int usuarioId = JwtUtil.getIdFromToken(token);
            String nomeUsuario = JwtUtil.getNomeFromToken(token);

            // Primeiro, remove da lista de ativos para a GUI ser atualizada
            Servidor.removerUsuarioAtivo(nomeUsuario);

            // Depois, exclui do banco de dados
            boolean sucesso = usuarioBD.excluirUsuario(usuarioId);

            if (sucesso) {
                resposta.put("status", "200");
                resposta.put("mensagem", "Conta excluída com sucesso.");
            } else {
                resposta.put("status", "404");
                resposta.put("mensagem", "Usuário não encontrado para exclusão.");
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados ao excluir usuário: " + e.getMessage());
            e.printStackTrace();
            resposta.put("status", "500");
            resposta.put("mensagem", "Erro ao acessar o banco de dados.");
        } catch (Exception e) {
            System.err.println("Erro ao processar token para exclusão: " + e.getMessage());
            e.printStackTrace();
            resposta.put("status", "401");
            resposta.put("mensagem", "Token inválido ou requisição malformada.");
        }
        return resposta;
    }
}