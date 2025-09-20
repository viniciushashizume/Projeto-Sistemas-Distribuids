package org.voteflix.gui;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import org.voteflix.servico.ServicoCliente;

public class TelaConexao extends JFrame {

    private JTextField campoIp;
    private JTextField campoPorta;
    private JButton botaoConectar;

    public TelaConexao() {
        super("Conectar ao Servidor");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 200);
        setLocationRelativeTo(null); // Centraliza a janela
        setLayout(new GridLayout(3, 2, 10, 10));

        // Painel para organizar os componentes
        JPanel painel = new JPanel(new GridLayout(3, 2, 10, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        painel.add(new JLabel("IP do Servidor:"));
        campoIp = new JTextField("127.0.0.1"); // IP local como padrão
        painel.add(campoIp);

        painel.add(new JLabel("Porta:"));
        campoPorta = new JTextField("12345"); // Porta comum como padrão
        painel.add(campoPorta);

        painel.add(new JLabel()); // Espaço em branco
        botaoConectar = new JButton("Conectar");
        painel.add(botaoConectar);

        add(painel);

        botaoConectar.addActionListener(e -> conectarAoServidor());
    }

    private void conectarAoServidor() {
        String ip = campoIp.getText().trim();
        String portaStr = campoPorta.getText().trim();

        if (ip.isEmpty() || portaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "IP e Porta são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int porta = Integer.parseInt(portaStr);
            ServicoCliente.getInstancia().conectar(ip, porta);

            // Se a conexão for bem-sucedida, abre a tela de login
            JOptionPane.showMessageDialog(this, "Conectado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            abrirTelaLogin();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "A porta deve ser um número válido.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao conectar ao servidor: " + ex.getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirTelaLogin() {
        this.dispose(); // Fecha a tela de conexão
        TelaLogin telaLogin = new TelaLogin();
        telaLogin.setVisible(true);
    }
}