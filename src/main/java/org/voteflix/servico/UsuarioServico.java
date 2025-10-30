package org.voteflix.servico;

import org.json.JSONObject;
import org.voteflix.bd.UsuarioBD;
import org.voteflix.model.Usuario;
import org.voteflix.servidor.Servidor;
import org.voteflix.util.JwtUtil;
import org.voteflix.util.ProtocoloMensagem;
import org.json.JSONException;

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
            if (nomeUsuario.length() < 3 || nomeUsuario.length() > 20 || senha.length() < 3 || senha.length() > 20) {
                ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta); // 422
                return resposta;
            }
            // 1. Usuário não encontrado
            // O código 404 não está no protocolo de LOGIN. Usando 403.
            if (usuario == null) {
                ProtocoloMensagem.ERRO_SEM_PERMISSAO.aplicar(resposta); // 403
                return resposta;
            }

            // 2. Usuário já logado
            // O código 409 não está no protocolo de LOGIN. Usando 403.
            if (Servidor.isUsuarioAtivo(usuario.getNome())) {
                ProtocoloMensagem.ERRO_SEM_PERMISSAO.aplicar(resposta); // 403
                return resposta;
            }

            // 3. Senha correta
            if (senha.equals(usuario.getSenha())) {
                String token = JwtUtil.gerarToken(usuario);
                ProtocoloMensagem.SUCESSO_OPERACAO.aplicar(resposta); // 200
                resposta.put("token", token);
                Servidor.adicionarUsuarioAtivo(usuario.getNome());
            } else {
                // 4. Senha incorreta
                // O código 401 não está no protocolo de LOGIN. Usando 403.
                ProtocoloMensagem.ERRO_SEM_PERMISSAO.aplicar(resposta); // 401
            }
        } catch (JSONException e) {
            // 5. Chaves faltantes (Protocolo Requisições.csv: 422) - CORRETO
            System.err.println("Erro de JSON no login: " + e.getMessage());
            ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta); // 422
        } catch (SQLException e) {
            // 6. Erro de banco de dados (Protocolo Requisições.csv: 500) - CORRETO
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
            // 1. Tratamento do Erro 422 (Chaves faltantes)
            // Se a chave "token" não existir, lança JSONException
            String token = requisicao.getString("token");

            // 2. Tratamento do Erro 401 (Token inválido)
            // Se o token for inválido (expirado, assinatura, etc.), lança Exceção
            String nomeUsuario = JwtUtil.getNomeFromToken(token);

            // 3. Tratamento do Erro 404 (Recurso inexistente)
            // O token é válido (não caiu no 401), mas o usuário não está na lista de ativos.
            if (!Servidor.isUsuarioAtivo(nomeUsuario)) {
                ProtocoloMensagem.ERRO_RECURSO_INEXISTENTE.aplicar(resposta); // 404
                return resposta;
            }

            // 4. Sucesso (200)
            // Remove o usuário da lista de ativos.
            // (Se isto falhar com RuntimeException, o ClienteHandler tratará como 500)
            Servidor.removerUsuarioAtivo(nomeUsuario);
            ProtocoloMensagem.SUCESSO_OPERACAO.aplicar(resposta); // 200

        } catch (JSONException e) {
            // Erro 422: A chave "token" não foi encontrada na requisição
            System.err.println("Erro de JSON no logout (chave 'token' faltante?): " + e.getMessage());
            ProtocoloMensagem.ERRO_CHAVES_FALTANTES.aplicar(resposta); // 422
        } catch (Exception e) {
            // Erro 401: O token é inválido (pega exceções do JwtUtil.getNomeFromToken)
            System.err.println("Erro de token no logout (token inválido?): " + e.getMessage());
            ProtocoloMensagem.ERRO_TOKEN_INVALIDO.aplicar(resposta); // 401
        }
        return resposta;
        // Erro 500: Será tratado pelo catch(Exception e) no ClienteHandler.java
        // Erro 400: Já é tratado pelo switch-default no ClienteHandler.java
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