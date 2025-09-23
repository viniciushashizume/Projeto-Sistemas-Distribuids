package org.voteflix.cliente.servico;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServicoCliente {

    private static ServicoCliente instancia;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    // Construtor privado para implementar o padrão Singleton
    private ServicoCliente() {}

    // Padrão Singleton para garantir uma única instância do serviço de conexão
    public static synchronized ServicoCliente getInstancia() {
        if (instancia == null) {
            instancia = new ServicoCliente();
        }
        return instancia;
    }

    public void conectar(String ip, int porta) throws UnknownHostException, IOException {
        // Evita múltiplas conexões
        if (socket != null && socket.isConnected()) {
            return;
        }
        socket = new Socket(ip, porta);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public String enviarRequisicao(String jsonRequisicao) throws IOException {
        if (out == null || in == null) {
            throw new IOException("Cliente não conectado ao servidor.");
        }
        // Envia a requisição para o servidor
        out.println(jsonRequisicao);
        // Retorna a resposta do servidor
        return in.readLine();
    }

    public void desconectar() throws IOException {
        if (in != null) in.close();
        if (out != null) out.close();
        if (socket != null) socket.close();
        socket = null;
        in = null;
        out = null;
    }

    public boolean isConectado() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}