package org.voteflix.servidor;

import org.json.JSONObject;
import org.voteflix.servico.UsuarioServico;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteHandler extends Thread {

    private final Socket clienteSocket;
    private final UsuarioServico usuarioServico;

    public ClienteHandler(Socket socket) {
        this.clienteSocket = socket;
        this.usuarioServico = new UsuarioServico();
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clienteSocket.getOutputStream(), true)
        ) {
            String requisicaoJson;
            while ((requisicaoJson = in.readLine()) != null) {
                System.out.println("Requisição recebida: " + requisicaoJson);
                JSONObject resposta = processarRequisicao(requisicaoJson);
                out.println(resposta.toString());
                System.out.println("Resposta enviada: " + resposta.toString());
            }
        } catch (IOException e) {
            System.err.println("Erro de comunicação com o cliente: " + e.getMessage());
        } finally {
            try {
                clienteSocket.close();
                System.out.println("Conexão com o cliente fechada.");
            } catch (IOException e) {
                System.err.println("Erro ao fechar o socket do cliente: " + e.getMessage());
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
                default:
                    JSONObject resposta = new JSONObject();
                    resposta.put("status", "400");
                    resposta.put("mensagem", "Operação desconhecida.");
                    return resposta;
            }
        } catch (Exception e) {
            JSONObject resposta = new JSONObject();
            resposta.put("status", "500");
            resposta.put("mensagem", "Erro interno no servidor: " + e.getMessage());
            return resposta;
        }
    }
}