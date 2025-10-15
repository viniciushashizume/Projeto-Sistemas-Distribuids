package org.voteflix.gui;

import org.voteflix.cliente.servico.ServicoCliente;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class TelaMinhaConta extends JDialog {

    private final String token;
    private JLabel labelNomeUsuario;
    private JLabel labelSenha; // Apenas para fins de demonstração

    public TelaMinhaConta(Frame owner, String token) {
        super(owner, "Minha Conta", true);
        this.token = token;

        setSize(350, 200);
        setLocationRelativeTo(owner);
        setLayout(new GridLayout(3, 2, 10, 10));

        add(new JLabel("Usuário:"));
        labelNomeUsuario = new JLabel("Carregando...");
        add(labelNomeUsuario);

        add(new JLabel("Senha:"));
        labelSenha = new JLabel("Carregando...");
        add(labelSenha);

        JButton botaoFechar = new JButton("Fechar");
        botaoFechar.addActionListener(e -> dispose());
        add(new JLabel()); // Espaço em branco
        add(botaoFechar);

        carregarDadosUsuario();
    }

    private void carregarDadosUsuario() {
        JSONObject requisicao = new JSONObject();
        requisicao.put("operacao", "LISTAR_PROPRIO_USUARIO");
        requisicao.put("token", this.token);

        try {
            String respostaJson = ServicoCliente.getInstancia().enviarRequisicao(requisicao.toString());
            JSONObject resposta = new JSONObject(respostaJson);

            if ("200".equals(resposta.getString("status"))) {
                JSONObject dadosUsuario = resposta.getJSONObject("usuario");
                labelNomeUsuario.setText(dadosUsuario.getString("nome"));
                labelSenha.setText(dadosUsuario.getString("senha"));
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao carregar dados: " + resposta.optString("mensagem", "Status: " + resposta.getString("status")), "Erro", JOptionPane.ERROR_MESSAGE);
                dispose();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro de comunicação: " + e.getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }
}
