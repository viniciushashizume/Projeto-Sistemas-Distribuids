package org.voteflix.servidor;

import org.json.JSONObject;
import org.voteflix.servico.UsuarioServico;
import org.voteflix.servidor.gui.TelaServidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClienteHandler extends Thread {

    private final Socket clienteSocket;
    private final UsuarioServico usuarioServico;
    private final TelaServidor telaServidor; // Referência para a GUI do servidor

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
            String requisicaoJson = in.readLine();
            if (requisicaoJson != null) {
                log("Requisição recebida de " + clienteSocket.getInetAddress() + ": " + requisicaoJson);
                JSONObject resposta = processarRequisicao(requisicaoJson);
                out.println(resposta.toString());
                log("Resposta enviada para " + clienteSocket.getInetAddress() + ": " + resposta.toString());
            }
        } catch (IOException e) {
            log("Erro de comunicação com o cliente " + clienteSocket.getInetAddress() + ": " + e.getMessage());
        } finally {
            try {
                clienteSocket.close();
                log("Conexão com o cliente " + clienteSocket.getInetAddress() + " fechada.");
            } catch (IOException e) {
                log("Erro ao fechar o socket do cliente " + clienteSocket.getInetAddress() + ": " + e.getMessage());
            }
        }
    }

    private JSONObject processarRequisicao(String requisicaoJson) {
        try {
            JSONObject requisicao = new JSONObject(requisicaoJson);
            String operacao = requisicao.getString("operacao");

            switch (operacao) {
                case "LOGIN":
                    return usuarioServico.realizarLogin(requisicao);
                case "CRIAR_USUARIO":
                    return usuarioServico.criarUsuario(requisicao);
                case "LOGOUT":
                    return usuarioServico.realizarLogout(requisicao);
                case "EDITAR_PROPRIO_USUARIO":
                    return usuarioServico.editarProprioUsuario(requisicao);
                case "EXCLUIR_PROPRIO_USUARIO":
                    return usuarioServico.excluirProprioUsuario(requisicao);
                default:
                    JSONObject resposta = new JSONObject();
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
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logFormatado = "[" + timestamp + "] " + mensagem;
        System.out.println(logFormatado);
        if (telaServidor != null) {
            telaServidor.adicionarLog(logFormatado);
        }
    }
}