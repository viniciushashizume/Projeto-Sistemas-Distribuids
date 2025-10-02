package org.voteflix.servidor;

import org.json.JSONObject;
import org.voteflix.servico.UsuarioServico;
import org.voteflix.servidor.gui.TelaServidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ClienteHandler extends Thread {

    private final Socket clienteSocket;
    private final UsuarioServico usuarioServico;
    private final TelaServidor telaServidor;
    private String nomeUsuarioLogado = null; // Armazena o nome do usuário após o login

    public ClienteHandler(Socket socket, TelaServidor tela) {
        this.clienteSocket = socket;
        this.usuarioServico = new UsuarioServico();
        this.telaServidor = tela;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clienteSocket.getOutputStream(), true)
        ) {
            String requisicaoJson;
            // Loop para ler múltiplas requisições do mesmo cliente
            while ((requisicaoJson = in.readLine()) != null) {
                log("Requisição recebida de " + clienteSocket.getInetAddress() + ": " + requisicaoJson);
                JSONObject resposta = processarRequisicao(new JSONObject(requisicaoJson));
                out.println(resposta.toString());
                log("Resposta enviada para " + clienteSocket.getInetAddress() + ": " + resposta.toString());

                // Se a operação foi LOGOUT, encerra o loop
                JSONObject requisicao = new JSONObject(requisicaoJson);
                if ("LOGOUT".equals(requisicao.optString("operacao"))) {
                    break;
                }
            }
        } catch (SocketException e) {
            log("Conexão com o cliente " + clienteSocket.getInetAddress() + " foi perdida ou fechada abruptamente.");
        } catch (IOException e) {
            log("Erro de comunicação com o cliente " + clienteSocket.getInetAddress() + ": " + e.getMessage());
        } finally {
            // Garante que o usuário seja removido da lista de ativos ao desconectar
            if (nomeUsuarioLogado != null) {
                Servidor.removerUsuarioAtivo(nomeUsuarioLogado);
            }
            try {
                clienteSocket.close();
                log("Conexão com o cliente " + clienteSocket.getInetAddress() + " fechada.");
            } catch (IOException e) {
                log("Erro ao fechar o socket do cliente " + clienteSocket.getInetAddress() + ": " + e.getMessage());
            }
        }
    }

    private JSONObject processarRequisicao(JSONObject requisicao) {
        try {
            String operacao = requisicao.getString("operacao");
            JSONObject resposta;

            switch (operacao) {
                case "LOGIN":
                    resposta = usuarioServico.realizarLogin(requisicao);
                    // Se o login for bem-sucedido, armazena o nome do usuário
                    if ("200 ".equals(resposta.optString("status").trim())) {
                        this.nomeUsuarioLogado = requisicao.getString("usuario");
                    }
                    return resposta;
                case "LOGOUT":
                    resposta = usuarioServico.realizarLogout(requisicao);
                    this.nomeUsuarioLogado = null; // Limpa o nome do usuário
                    return resposta;
                case "CRIAR_USUARIO":
                    return usuarioServico.criarUsuario(requisicao);
                case "EDITAR_PROPRIO_USUARIO":
                    return usuarioServico.editarProprioUsuario(requisicao);
                case "EXCLUIR_PROPRIO_USUARIO":
                    this.nomeUsuarioLogado = null; // Limpa o nome ao excluir
                    return usuarioServico.excluirProprioUsuario(requisicao);
                case "GET_PROPRIO_USUARIO":
                    return usuarioServico.getProprioUsuario(requisicao);
                default:
                    resposta = new JSONObject();
                    resposta.put("status", "400");
                    resposta.put("mensagem", "Operação desconhecida.");
                    return resposta;
            }
        } catch (Exception e) {
            log("Erro interno ao processar requisição: " + e.getMessage());
            JSONObject resposta = new JSONObject();
            resposta.put("status", "500");
            resposta.put("mensagem", "Erro interno no servidor: " + e.getMessage());
            return resposta;
        }
    }

    private void log(String mensagem) {
        if (telaServidor != null) {
            telaServidor.adicionarLog(mensagem);
        }
        System.out.println(mensagem);
    }
}