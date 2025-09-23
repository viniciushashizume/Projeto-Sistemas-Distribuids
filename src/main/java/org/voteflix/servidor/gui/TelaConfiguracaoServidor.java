package org.voteflix.servidor.gui;

import javax.swing.*;
import java.awt.*;

public class TelaConfiguracaoServidor extends JDialog {

    private JTextField campoPorta;
    private JButton botaoIniciar;
    private int portaSelecionada = -1;

    public TelaConfiguracaoServidor(Frame owner) {
        super(owner, "Configurar Porta do Servidor", true);
        setSize(350, 150);
        setLocationRelativeTo(owner);
        setLayout(new GridBagLayout());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Porta do Servidor:"), gbc);

        gbc.gridx = 1;
        campoPorta = new JTextField("12345", 10);
        add(campoPorta, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        botaoIniciar = new JButton("Iniciar Servidor");
        add(botaoIniciar, gbc);

        botaoIniciar.addActionListener(e -> iniciar());
    }

    private void iniciar() {
        String portaStr = campoPorta.getText().trim();
        try {
            int porta = Integer.parseInt(portaStr);
            if (porta < 1024 || porta > 65535) {
                JOptionPane.showMessageDialog(this, "Por favor, insira um número de porta válido (1024-65535).", "Erro de Porta", JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.portaSelecionada = porta;
            dispose(); // Fecha o diálogo
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "A porta deve ser um número.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    public int getPortaSelecionada() {
        return portaSelecionada;
    }
}