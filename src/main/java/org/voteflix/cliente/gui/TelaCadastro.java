package org.voteflix.cliente.gui;

import org.json.JSONObject;
import org.voteflix.cliente.servico.ServicoCliente; // Corrigi o nome do pacote para seguir o padrão
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import org.voteflix.cliente.gui.TelaLogin;

// A importação que faltava



public class TelaCadastro extends JFrame {

    private JTextField campoUsuario;
    private JPasswordField campoSenha;
    private JPasswordField campoConfirmarSenha;

    public TelaCadastro() {
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

        gbc.gridx = 0;
        gbc.gridy = 2;
        painel.add(new JLabel("Confirmar Senha:"), gbc);

        gbc.gridx = 1;
        campoConfirmarSenha = new JPasswordField(20);
        painel.add(campoConfirmarSenha, gbc);

        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton botaoConfirmar = new JButton("Confirmar Cadastro");
        JButton botaoVoltar = new JButton("Voltar para Login");
        painelBotoes.add(botaoConfirmar);
        painelBotoes.add(botaoVoltar);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        painel.add(painelBotoes, gbc);

        add(painel);

        botaoConfirmar.addActionListener(e -> realizarCadastro());
        botaoVoltar.addActionListener(e -> voltarParaLogin());
    }

    private void realizarCadastro() {
        String nome = campoUsuario.getText();
        String senha = new String(campoSenha.getPassword());
        String confirmarSenha = new String(campoConfirmarSenha.getPassword());

        if (nome.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Usuário e senha são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!senha.equals(confirmarSenha)) {
            JOptionPane.showMessageDialog(this, "As senhas não coincidem.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Monta o JSON de requisição com a estrutura aninhada correta
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

            if ("201".equals(status)) {
                JOptionPane.showMessageDialog(this, "Usuário cadastrado com sucesso! Você já pode fazer login.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                voltarParaLogin();
            } else {
                String mensagemErro = "Erro ao cadastrar. Status: " + status;
                if ("409".equals(status)) mensagemErro = "O nome de usuário já existe.";
                if ("422".equals(status)) mensagemErro = "Dados inválidos. Usuário e senha devem ter de 3 a 20 caracteres.";
                JOptionPane.showMessageDialog(this, mensagemErro, "Erro de Cadastro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro de comunicação com o servidor: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            // Adicionado um catch genérico para pegar outros possíveis erros de JSON
            JOptionPane.showMessageDialog(this, "Ocorreu um erro inesperado: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void voltarParaLogin() {
        this.dispose();
        // A classe TelaLogin precisa estar acessível aqui
        TelaLogin telaLogin = new TelaLogin();
        telaLogin.setVisible(true);
    }
}