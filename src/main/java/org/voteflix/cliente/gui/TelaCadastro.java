package org.voteflix.cliente.gui;

import org.json.JSONObject;
import org.voteflix.cliente.servico.ServicoCliente;
import org.voteflix.util.ProtocoloMensagem; // Importar o Enum

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class TelaCadastro extends JFrame {

    private JTextField campoUsuario;
    private JPasswordField campoSenha;
    // private JPasswordField campoConfirmarSenha; // CAMPO REMOVIDO

    public TelaCadastro() {
        // ... (GUI sem alteração)
        super("VoteFlix - Cadastro de Usuário");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel painel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        painel.add(new JLabel("Usuário:"), gbc);

        gbc.gridx = 1;
        campoUsuario = new JTextField(20);
        painel.add(campoUsuario, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        painel.add(new JLabel("Senha:"), gbc);

        gbc.gridx = 1;
        campoSenha = new JPasswordField(20);
        painel.add(campoSenha, gbc);

        // --- SEÇÃO REMOVIDA ---
        /*
        gbc.gridx = 0;
        gbc.gridy = 2;
        painel.add(new JLabel("Confirmar Senha:"), gbc);

        gbc.gridx = 1;
        campoConfirmarSenha = new JPasswordField(20);
        painel.add(campoConfirmarSenha, gbc);
        */
        // --- FIM DA SEÇÃO REMOVIDA ---

        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton botaoConfirmar = new JButton("Confirmar Cadastro");
        JButton botaoVoltar = new JButton("Voltar para Login");
        painelBotoes.add(botaoConfirmar);
        painelBotoes.add(botaoVoltar);

        gbc.gridx = 0;
        gbc.gridy = 2; // Posição Y atualizada de 3 para 2
        gbc.gridwidth = 2;
        painel.add(painelBotoes, gbc);

        add(painel);

        botaoConfirmar.addActionListener(e -> realizarCadastro());
        botaoVoltar.addActionListener(e -> voltarParaLogin());
    }

    private void realizarCadastro() {
        // ... (validação inicial sem alteração)
        String nome = campoUsuario.getText();
        String senha = new String(campoSenha.getPassword());
        // String confirmarSenha = new String(campoConfirmarSenha.getPassword()); // LÓGICA REMOVIDA

        if (nome.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Usuário e senha são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- VALIDAÇÃO REMOVIDA ---
        /*
        if (!senha.equals(confirmarSenha)) {
            JOptionPane.showMessageDialog(this, "As senhas não coincidem.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        */
        // --- FIM DA VALIDAÇÃO REMOVIDA ---

        JSONObject dadosUsuario = new JSONObject();
        dadosUsuario.put("nome", nome);
        dadosUsuario.put("senha", senha);

        JSONObject requisicao = new JSONObject();
        requisicao.put("operacao", "CRIAR_USUARIO");
        requisicao.put("usuario", dadosUsuario);

        try {
            String respostaJson = ServicoCliente.getInstancia().enviarRequisicao(requisicao.toString());
            JSONObject resposta = new JSONObject(respostaJson);
            String status = resposta.getString("status").trim();

            // --- ALTERAÇÃO AQUI ---
            // "Traduz" o status para a mensagem local do Enum
            String mensagemTraduzida = ProtocoloMensagem.getByStatus(status).getMensagem();
            // --- FIM DA ALTERAÇÃO ---

            if ("201".equals(status)) {
                // Exibe a mensagem "traduzida"
                JOptionPane.showMessageDialog(this, mensagemTraduzida, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                voltarParaLogin();
            } else {
                // Exibe a mensagem de erro "traduzida"
                JOptionPane.showMessageDialog(this, mensagemTraduzida, "Erro (" + status + ")", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro de comunicação com o servidor: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ocorreu um erro inesperado: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void voltarParaLogin() {
        // ... (sem alterações)
        this.dispose();
        TelaLogin telaLogin = new TelaLogin();
        telaLogin.setVisible(true);
    }
}