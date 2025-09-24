package org.voteflix.servidor.gui;

import org.voteflix.model.Usuario;

import javax.swing.*;
import java.awt.*;

public class TelaDetalhesUsuario extends JDialog {

    public TelaDetalhesUsuario(Frame owner, Usuario usuario) {
        super(owner, "Detalhes do Usuário", true);

        setSize(400, 200);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel painel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        painel.add(new JLabel("Usuário:"), gbc);

        gbc.gridx = 1;
        JTextField campoUsuario = new JTextField(usuario.getNome(), 20);
        campoUsuario.setEditable(false);
        painel.add(campoUsuario, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        painel.add(new JLabel("Senha:"), gbc);

        gbc.gridx = 1;
        JTextField campoSenha = new JTextField(usuario.getSenha(), 20);
        campoSenha.setEditable(false);
        painel.add(campoSenha, gbc);

        add(painel, BorderLayout.CENTER);

        JButton botaoFechar = new JButton("Fechar");
        botaoFechar.addActionListener(e -> dispose());
        JPanel painelBotao = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelBotao.add(botaoFechar);
        add(painelBotao, BorderLayout.SOUTH);
    }
}