package org.voteflix.cliente.gui;

import org.voteflix.cliente.servico.ServicoCliente;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class TelaConexao extends JFrame {

    private JTextField campoIp;
    private JTextField campoPorta;
    private JButton botaoConectar;

    public TelaConexao() {
        super("Conectar ao Servidor");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 200);
        setLocationRelativeTo(null);

        JPanel painel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        painel.add(new JLabel("IP do Servidor:"), gbc);

        gbc.gridx = 1;
        campoIp = new JTextField("127.0.0.1");
        painel.add(campoIp, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        painel.add(new JLabel("Porta:"), gbc);

        gbc.gridx = 1;
        campoPorta = new JTextField("12345");
        painel.add(campoPorta, gbc);

        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        botaoConectar = new JButton("Conectar");
        painel.add(botaoConectar, gbc);

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

            // Tenta estabelecer a conexão persistente
            ServicoCliente.getInstancia().conectar(ip, porta);

            JOptionPane.showMessageDialog(this, "Conexão com o servidor estabelecida com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            abrirTelaLogin();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "A porta deve ser um número válido.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Falha ao conectar ao servidor.\nVerifique o IP, a porta e se o servidor está ativo.\n\nErro: " + ex.getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirTelaLogin() {
        this.dispose();
        TelaLogin telaLogin = new TelaLogin();
        telaLogin.setVisible(true);
    }
}