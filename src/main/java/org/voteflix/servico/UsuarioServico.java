package org.voteflix.servico;

import org.json.JSONObject;
import org.voteflix.bd.UsuarioBD;
import org.voteflix.model.Usuario;
import org.voteflix.servidor.Servidor;
import org.voteflix.util.JwtUtil;
import org.voteflix.util.ProtocoloMensagem; // Importar o Enum

import java.sql.SQLException;

public class UsuarioServico {

    private final UsuarioBD usuarioBD;

    public UsuarioServico() {
        this.usuarioBD = new UsuarioBD();
    }

    public JSONObject realizarLogin(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            String nomeUsuario = requisicao.getString("usuario");
            String senha = requisicao.getString("senha");

            Usuario usuario = usuarioBD.buscarUsuarioPorNome(nomeUsuario);

            if (usuario == null) {
                ProtocoloMensagem.ERRO_RECURSO_INEXISTENTE.aplicar(resposta); // 404
                return resposta;
            }

            if (Servidor.isUsuarioAtivo(usuario.getNome())) {
                ProtocoloMensagem.ERRO_RECURSO_JA_EXISTE.aplicar(resposta); // 409
                return resposta;
            }

            if (senha.equals(usuario.getSenha())) {
                String token = JwtUtil.gerarToken(usuario);
                ProtocoloMensagem.SUCESSO_OPERACAO.aplicar(resposta); // 200
                resposta.put("token", token);
                Servidor.adicionarUsuarioAtivo(usuario.getNome());
            } else {
                ProtocoloMensagem.ERRO_TOKEN_INVALIDO.aplicar(resposta); // 401
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados no login: " + e.getMessage());
            e.printStackTrace();
            ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta); // 500
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
                ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta); // 422
                return resposta;
            }

            if (usuarioBD.buscarUsuarioPorNome(nome) != null) {
                ProtocoloMensagem.ERRO_RECURSO_JA_EXISTE.aplicar(resposta); // 409
                return resposta;
            }

            Usuario novoUsuario = new Usuario(nome, senha);
            boolean sucesso = usuarioBD.adicionarUsuario(novoUsuario);

            if (sucesso) {
                ProtocoloMensagem.SUCESSO_RECURSO_CADASTRADO.aplicar(resposta); // 201
            } else {
                ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta); // 500
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados ao criar usuário: " + e.getMessage());
            e.printStackTrace();
            ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta); // 500
        }
        return resposta;
    }

    public JSONObject realizarLogout(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            String token = requisicao.getString("token");
            String nomeUsuario = JwtUtil.getNomeFromToken(token);
            Servidor.removerUsuarioAtivo(nomeUsuario);
            ProtocoloMensagem.SUCESSO_OPERACAO.aplicar(resposta); // 200
        } catch (Exception e) {
            ProtocoloMensagem.SUCESSO_OPERACAO.aplicar(resposta); // 200
        }
        return resposta;
    }

    public JSONObject editarProprioUsuario(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            String token = requisicao.getString("token");
            int usuarioId = JwtUtil.getIdFromToken(token);

            JSONObject dadosUsuario = requisicao.getJSONObject("usuario");
            String novaSenha = dadosUsuario.getString("senha");

            if (novaSenha.length() < 3 || novaSenha.length() > 20) {
                ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta); // 422
                return resposta;
            }

            boolean sucesso = usuarioBD.atualizarSenha(usuarioId, novaSenha);

            if (sucesso) {
                ProtocoloMensagem.SUCESSO_OPERACAO.aplicar(resposta); // 200
            } else {
                ProtocoloMensagem.ERRO_RECURSO_INEXISTENTE.aplicar(resposta); // 404
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados ao editar usuário: " + e.getMessage());
            e.printStackTrace();
            ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta); // 500
        } catch (Exception e) {
            System.err.println("Erro ao processar token ou requisição: " + e.getMessage());
            e.printStackTrace();
            ProtocoloMensagem.ERRO_TOKEN_INVALIDO.aplicar(resposta); // 401
        }
        return resposta;
    }

    public JSONObject excluirProprioUsuario(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            String token = requisicao.getString("token");
            int usuarioId = JwtUtil.getIdFromToken(token);
            String nomeUsuario = JwtUtil.getNomeFromToken(token);

            Servidor.removerUsuarioAtivo(nomeUsuario);
            boolean sucesso = usuarioBD.excluirUsuario(usuarioId);

            if (sucesso) {
                ProtocoloMensagem.SUCESSO_OPERACAO.aplicar(resposta); // 200
            } else {
                ProtocoloMensagem.ERRO_RECURSO_INEXISTENTE.aplicar(resposta); // 404
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados ao excluir usuário: " + e.getMessage());
            e.printStackTrace();
            ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta); // 500
        } catch (Exception e) {
            System.err.println("Erro ao processar token para exclusão: " + e.getMessage());
            e.printStackTrace();
            ProtocoloMensagem.ERRO_TOKEN_INVALIDO.aplicar(resposta); // 401
        }
        return resposta;
    }

    public JSONObject listarProprioUsuario(JSONObject requisicao) {
        JSONObject resposta = new JSONObject();
        try {
            String token = requisicao.getString("token");
            int usuarioId = JwtUtil.getIdFromToken(token);

            Usuario usuario = usuarioBD.buscarUsuarioPorId(usuarioId);

            if (usuario != null) {
                ProtocoloMensagem.SUCESSO_OPERACAO.aplicar(resposta); // 200
                resposta.put("usuario", usuario.getNome());
            } else {
                ProtocoloMensagem.ERRO_RECURSO_INEXISTENTE.aplicar(resposta); // 404
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados ao buscar usuário: " + e.getMessage());
            e.printStackTrace();
            ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta); // 500
        } catch (Exception e) {
            System.err.println("Erro ao processar token ou requisição: " + e.getMessage());
            e.printStackTrace();
            ProtocoloMensagem.ERRO_TOKEN_INVALIDO.aplicar(resposta); // 401
        }
        return resposta;
    }
}