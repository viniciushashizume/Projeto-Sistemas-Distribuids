package org.voteflix.cliente.gui;

import org.json.JSONObject;
import org.voteflix.cliente.servico.ServicoCliente;
import org.voteflix.util.ProtocoloMensagem; // Importar o Enum

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class TelaMinhaConta extends JDialog {

    private final String token;

    public TelaMinhaConta(Frame owner, String token) {
        // ... (GUI sem alteração)
        super(owner, "Minhas Informações", true);
        this.token = token;

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
        JTextField campoUsuario = new JTextField(20);
        campoUsuario.setEditable(false);
        painel.add(campoUsuario, gbc);

        /*gbc.gridx = 0;
        gbc.gridy = 1;
        painel.add(new JLabel("Senha:"), gbc);*/

        gbc.gridx = 1;
        JTextField campoSenha = new JTextField(20);
        campoSenha.setEditable(false);
        painel.add(campoSenha, gbc);

        add(painel, BorderLayout.CENTER);

        JButton botaoFechar = new JButton("Fechar");
        botaoFechar.addActionListener(e -> dispose());
        JPanel painelBotao = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelBotao.add(botaoFechar);
        add(painelBotao, BorderLayout.SOUTH);


        carregarInformacoesUsuario(campoUsuario, campoSenha);
    }

    private void carregarInformacoesUsuario(JTextField campoUsuario, JTextField campoSenha) {
        JSONObject requisicao = new JSONObject();
        requisicao.put("operacao", "LISTAR_PROPRIO_USUARIO");
        requisicao.put("token", this.token);

        try {
            String respostaJson = ServicoCliente.getInstancia().enviarRequisicao(requisicao.toString());
            JSONObject resposta = new JSONObject(respostaJson);
            String status = resposta.getString("status").trim();

            // --- ALTERAÇÃO AQUI ---
            // "Traduz" o status para a mensagem local do Enum
            String mensagemTraduzida = ProtocoloMensagem.getByStatus(status).getMensagem();
            // --- FIM DA ALTERAÇÃO ---

            if ("200".equals(status)) {
                String nomeUsuario = resposta.getString("usuario");
                campoUsuario.setText(nomeUsuario);
                //campoSenha.setText(usuario.getString("senha"));
            } else {
                // Exibe a mensagem de erro "traduzida"
                JOptionPane.showMessageDialog(this, mensagemTraduzida, "Erro (" + status + ")", JOptionPane.ERROR_MESSAGE);
                dispose();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro de comunicação: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }
}