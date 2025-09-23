package org.voteflix.cliente.servico;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServicoCliente {

    private static ServicoCliente instancia;
    private String ipServidor;
    private int portaServidor;

    // Construtor privado para implementar o padrão Singleton
    private ServicoCliente() {}

    // Padrão Singleton para garantir uma única instância
    public static synchronized ServicoCliente getInstancia() {
        if (instancia == null) {
            instancia = new ServicoCliente();
        }
        return instancia;
    }

    /**
     * Armazena os dados de conexão para serem usados em futuras requisições.
     * @param ip O IP do servidor.
     * @param porta A porta do servidor.
     */
    public void configurarConexao(String ip, int porta) {
        this.ipServidor = ip;
        this.portaServidor = porta;
    }

    /**
     * Abre uma nova conexão, envia uma requisição, recebe a resposta e fecha a conexão.
     * @param jsonRequisicao A string JSON da requisição.
     * @return A string JSON da resposta.
     * @throws IOException Se ocorrer um erro de comunicação.
     */
    public String enviarRequisicao(String jsonRequisicao) throws IOException {
        if (ipServidor == null || portaServidor == 0) {
            throw new IOException("A conexão com o servidor não foi configurada.");
        }

        // Usando try-with-resources para garantir que o socket e os streams sejam fechados
        try (
                Socket socket = new Socket(ipServidor, portaServidor);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            // Envia a requisição para o servidor
            out.println(jsonRequisicao);
            // Lê e retorna a resposta do servidor
            return in.readLine();
        } catch (UnknownHostException e) {
            throw new IOException("Host desconhecido: " + ipServidor, e);
        } catch (IOException e) {
            throw new IOException("Erro de E/S ao comunicar com o servidor: " + e.getMessage(), e);
        }
    }
}