package org.voteflix.cliente.gui;

import org.voteflix.cliente.servico.ServicoCliente;
import org.json.JSONObject;
import org.voteflix.util.ProtocoloMensagem; // Importar o Enum

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class TelaEditarUsuario extends JDialog {

    private String token;
    private JPasswordField campoNovaSenha;
    // private JPasswordField campoConfirmarNovaSenha; // CAMPO REMOVIDO

    public TelaEditarUsuario(Frame owner, String token) {
        // ... (GUI sem alteração)
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

        // --- SEÇÃO REMOVIDA ---
        /*
        gbc.gridx = 0;
        gbc.gridy = 1;
        painel.add(new JLabel("Confirmar Nova Senha:"), gbc);

        gbc.gridx = 1;
        campoConfirmarNovaSenha = new JPasswordField(20);
        painel.add(campoConfirmarNovaSenha, gbc);
        */
        // --- FIM DA SEÇÃO REMOVIDA ---

        JButton botaoSalvar = new JButton("Salvar Alterações");
        gbc.gridx = 1;
        gbc.gridy = 1; // Posição Y atualizada de 2 para 1
        gbc.anchor = GridBagConstraints.EAST;
        painel.add(botaoSalvar, gbc);

        add(painel);

        botaoSalvar.addActionListener(e -> salvarNovaSenha());
    }

    private void salvarNovaSenha() {
        // ... (validação inicial sem alteração)
        String novaSenha = new String(campoNovaSenha.getPassword());
        // String confirmarSenha = new String(campoConfirmarNovaSenha.getPassword()); // LÓGICA REMOVIDA


        /*if (novaSenha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "A nova senha não pode estar em branco.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }*/


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
            String mensagemTraduzida = ProtocoloMensagem.getByStatus(status).getMensagem();

            if ("200".equals(status)) {
                // Exibe a mensagem "traduzida"
                JOptionPane.showMessageDialog(this, mensagemTraduzida, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
            } else {
                // Exibe a mensagem de erro "traduzida"
                JOptionPane.showMessageDialog(this, mensagemTraduzida, "Erro (" + status + ")", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro de comunicação: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}