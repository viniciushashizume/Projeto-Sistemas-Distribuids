package org.voteflix.servidor;

import org.voteflix.servidor.gui.TelaConfiguracaoServidor;
import org.voteflix.servidor.gui.TelaServidor;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Servidor {

    private static TelaServidor telaServidor;
    private static final Set<String> usuariosAtivos = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        // 1. Exibe a tela de configuração para obter a porta
        TelaConfiguracaoServidor configTela = new TelaConfiguracaoServidor(null);
        configTela.setVisible(true);

        int porta = configTela.getPortaSelecionada();
        if (porta == -1) {
            System.out.println("Nenhuma porta selecionada. O servidor não foi iniciado.");
            return; // Encerra se a janela de configuração for fechada
        }

        // 2. Inicializa a GUI principal do servidor na Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            telaServidor = new TelaServidor();
            telaServidor.setVisible(true);
            log("Servidor aguardando conexões na porta " + porta);
        });

        // 3. Inicia o servidor na porta selecionada
        iniciarServidor(porta);
    }

    private static void iniciarServidor(int porta) {
        try (ServerSocket servidorSocket = new ServerSocket(porta)) {
            while (true) {
                Socket clienteSocket = servidorSocket.accept();
                log("Cliente conectado: " + clienteSocket.getInetAddress());

                // Cria uma nova thread para lidar com o cliente, passando a tela
                ClienteHandler handler = new ClienteHandler(clienteSocket, telaServidor);
                handler.start();
            }
        } catch (IOException e) {
            log("Erro crítico no servidor: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Não foi possível iniciar o servidor na porta " + porta + ".\nVerifique se a porta já está em uso.", "Erro de Servidor", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void adicionarUsuarioAtivo(String nome) {
        usuariosAtivos.add(nome);
        telaServidor.atualizarListaUsuarios(usuariosAtivos);
        log("Usuário '" + nome + "' entrou. Ativos: " + usuariosAtivos.size());
    }

    public static void removerUsuarioAtivo(String nome) {
        if (nome != null && !nome.isEmpty()) {
            usuariosAtivos.remove(nome);
            telaServidor.atualizarListaUsuarios(usuariosAtivos);
            log("Usuário '" + nome + "' saiu. Ativos: " + usuariosAtivos.size());
        }
    }

    private static void log(String mensagem) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logFormatado = "[" + timestamp + "] " + mensagem;
        System.out.println(logFormatado);
        if (telaServidor != null) {
            telaServidor.adicionarLog(logFormatado);
        }
    }
}