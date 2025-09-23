package org.voteflix.cliente.gui;

import org.voteflix.cliente.servico.ServicoCliente;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class TelaLogin extends JFrame {

    private JTextField campoUsuario;
    private JPasswordField campoSenha;

    public TelaLogin() {
        super("VoteFlix - Login");
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

        if (usuario.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Usuário e senha são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- CORREÇÃO AQUI ---
        // Monta o JSON de requisição conforme o Protocolo de Troca de Mensagens
        JSONObject requisicao = new JSONObject();
        requisicao.put("operacao", "LOGIN");
        // A chave para o nome de usuário deve ser "usuario", não "nome".
        requisicao.put("usuario", usuario);
        requisicao.put("senha", senha);
        // --- FIM DA CORREÇÃO ---

        try {
            String respostaJson = ServicoCliente.getInstancia().enviarRequisicao(requisicao.toString());
            JSONObject resposta = new JSONObject(respostaJson);

            // O protocolo especifica status "200 " com um espaço, é bom tratar isso.
            String status = resposta.getString("status").trim();

            if ("200".equals(status)) {
                String token = resposta.getString("token");
                JOptionPane.showMessageDialog(this, "Login realizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                abrirTelaPrincipal(token);
            } else {
                // Trata outros status de erro
                String mensagemErro = "Erro ao fazer login. Status: " + status;
                if ("401".equals(status)) mensagemErro = "Credenciais inválidas.";
                if ("404".equals(status)) mensagemErro = "Usuário não encontrado.";
                if ("500".equals(status)) mensagemErro = "Erro interno no servidor.";
                JOptionPane.showMessageDialog(this, mensagemErro, "Erro de Login", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro de comunicação com o servidor: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ocorreu um erro inesperado: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirTelaCadastro() {
        this.dispose();
        // Assumindo que você tem uma classe TelaCadastro
        // TelaCadastro telaCadastro = new TelaCadastro();
        // telaCadastro.setVisible(true);
    }

    private void abrirTelaPrincipal(String token) {
        this.dispose();
        // Assumindo que você tem uma classe TelaPrincipal
        // TelaPrincipal telaPrincipal = new TelaPrincipal(token);
        // telaPrincipal.setVisible(true);
    }
}