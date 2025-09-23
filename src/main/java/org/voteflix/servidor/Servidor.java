package org.voteflix.servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    public static void main(String[] args) {
        int porta = 12345;
        System.out.println("Iniciando o servidor VoteFlix na porta " + porta);

        try (ServerSocket servidorSocket = new ServerSocket(porta)) {
            while (true) {
                System.out.println("Aguardando conexão de um novo cliente...");
                Socket clienteSocket = servidorSocket.accept();
                System.out.println("Cliente conectado: " + clienteSocket.getInetAddress());

                // Cria uma nova thread para lidar com o cliente
                ClienteHandler handler = new ClienteHandler(clienteSocket);
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}