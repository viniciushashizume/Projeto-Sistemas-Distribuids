package org.voteflix.cliente.gui;

import org.voteflix.cliente.servico.ServicoCliente;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class TelaEditarUsuario extends JDialog {

    private String token;
    private JPasswordField campoNovaSenha;
    private JPasswordField campoConfirmarNovaSenha;

    public TelaEditarUsuario(Frame owner, String token) {
        super(owner, "Editar Minha Senha", true); // true para ser modal
        this.token = token;

        setSize(400, 200);
        setLocationRelativeTo(owner);

        JPanel painel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        painel.add(new JLabel("Nova Senha:"), gbc);

        gbc.gridx = 1;
        campoNovaSenha = new JPasswordField(20);
        painel.add(campoNovaSenha, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        painel.add(new JLabel("Confirmar Nova Senha:"), gbc);

        gbc.gridx = 1;
        campoConfirmarNovaSenha = new JPasswordField(20);
        painel.add(campoConfirmarNovaSenha, gbc);

        JButton botaoSalvar = new JButton("Salvar Alterações");
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        painel.add(botaoSalvar, gbc);

        add(painel);

        botaoSalvar.addActionListener(e -> salvarNovaSenha());
    }

    private void salvarNovaSenha() {
        String novaSenha = new String(campoNovaSenha.getPassword());
        String confirmarSenha = new String(campoConfirmarNovaSenha.getPassword());

        if (novaSenha.isEmpty() || !novaSenha.equals(confirmarSenha)) {
            JOptionPane.showMessageDialog(this, "As senhas não coincidem ou estão em branco.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Conforme o protocolo, a operação para editar o próprio usuário [cite: 15]
        JSONObject dadosUsuario = new JSONObject();
        dadosUsuario.put("senha", novaSenha);

        JSONObject requisicao = new JSONObject();
        requisicao.put("operacao", "EDITAR_PROPRIO_USUARIO");
        requisicao.put("usuario", dadosUsuario);
        requisicao.put("token", this.token);

        try {
            String respostaJson = ServicoCliente.getInstancia().enviarRequisicao(requisicao.toString());
            JSONObject resposta = new JSONObject(respostaJson);
            String status = resposta.getString("status").trim();

            if ("200".equals(status)) {
                JOptionPane.showMessageDialog(this, "Senha alterada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                this.dispose(); // Fecha a janela de edição
            } else {
                JOptionPane.showMessageDialog(this, "Não foi possível alterar a senha. Status: " + status, "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro de comunicação: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}