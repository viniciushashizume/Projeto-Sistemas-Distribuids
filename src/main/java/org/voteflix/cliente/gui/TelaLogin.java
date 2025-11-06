package org.voteflix.cliente.gui;

import org.voteflix.cliente.servico.ServicoCliente;
import org.json.JSONObject;
import org.voteflix.util.ProtocoloMensagem; // Importar o Enum

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class TelaLogin extends JFrame {

    private JTextField campoUsuario;
    private JPasswordField campoSenha;

    public TelaLogin() {super("VoteFlix - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
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

        // Painel para os botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton botaoLogin = new JButton("Login");
        JButton botaoCadastrar = new JButton("Cadastrar");

        painelBotoes.add(botaoLogin);
        painelBotoes.add(botaoCadastrar);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        painel.add(painelBotoes, gbc);

        add(painel);

        botaoLogin.addActionListener(e -> realizarLogin());
        botaoCadastrar.addActionListener(e -> abrirTelaCadastro());
    }

    private void realizarLogin() {
        String usuario = campoUsuario.getText();
        String senha = new String(campoSenha.getPassword());

        /*if (usuario.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Usuário e senha são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }*/

        JSONObject requisicao = new JSONObject();
        requisicao.put("operacao", "LOGIN");
        requisicao.put("usuario", usuario);
        requisicao.put("senha", senha);

        try {
            String respostaJson = ServicoCliente.getInstancia().enviarRequisicao(requisicao.toString());
            JSONObject resposta = new JSONObject(respostaJson);

            String status = resposta.getString("status").trim();
            String mensagemTraduzida = ProtocoloMensagem.getByStatus(status).getMensagem();


            if ("200".equals(status)) {
                String token = resposta.getString("token");
                JOptionPane.showMessageDialog(this, mensagemTraduzida, "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                // --- ALTERAÇÃO AQUI ---
                // Verifica se é o usuário "admin" (conforme Requisitos PDF [source 74])
                boolean isAdmin = "admin".equals(usuario);
                abrirTelaPrincipal(token, isAdmin);
                // --- FIM DA ALTERAÇÃO ---

            } else {
                JOptionPane.showMessageDialog(this, mensagemTraduzida, "Erro (" + status + ")", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro de comunicação com o servidor: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ocorreu um erro inesperado: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirTelaCadastro() {
        this.dispose();
        TelaCadastro telaCadastro = new TelaCadastro();
        telaCadastro.setVisible(true);
    }

    // --- ALTERAÇÃO AQUI ---
    private void abrirTelaPrincipal(String token, boolean isAdmin) {
        this.dispose();
        TelaPrincipal telaPrincipal = new TelaPrincipal(token, isAdmin);
        telaPrincipal.setVisible(true);
    }
    // --- FIM DA ALTERAÇÃO ---
}