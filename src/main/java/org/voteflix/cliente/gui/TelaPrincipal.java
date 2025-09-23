package org.voteflix.cliente.gui;

import org.voteflix.cliente.servico.ServicoCliente;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class TelaPrincipal extends JFrame {

    private String token; // Armazena o token do usuário logado

    public TelaPrincipal(String token) {
        super("VoteFlix - Painel do Usuário");
        this.token = token;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        // Layout Principal
        setLayout(new BorderLayout());

        // Mensagem de boas-vindas
        // Para exibir o nome, precisaríamos decodificar o JWT, o que é mais complexo.
        // Por enquanto, uma mensagem genérica.
        JLabel labelBoasVindas = new JLabel("Bem-vindo ao VoteFlix!", SwingConstants.CENTER);
        labelBoasVindas.setFont(new Font("Arial", Font.BOLD, 20));
        add(labelBoasVindas, BorderLayout.CENTER);

        // Painel de botões de ação
        JPanel painelAcoes = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton botaoEditar = new JButton("Editar Minha Conta");
        JButton botaoExcluir = new JButton("Excluir Minha Conta");
        JButton botaoLogout = new JButton("Logout");

        painelAcoes.add(botaoEditar);
        painelAcoes.add(botaoExcluir);
        painelAcoes.add(botaoLogout);

        add(painelAcoes, BorderLayout.SOUTH);

        // Ações dos botões
        botaoEditar.addActionListener(e -> abrirTelaEdicao());
        botaoExcluir.addActionListener(e -> confirmarExclusao());
        botaoLogout.addActionListener(e -> realizarLogout());
    }

    private void abrirTelaEdicao() {
        TelaEditarUsuario telaEditar = new TelaEditarUsuario(this, token);
        telaEditar.setVisible(true);
    }

    private void confirmarExclusao() {
        int resposta = JOptionPane.showConfirmDialog(
                this,
                "Tem certeza que deseja excluir sua conta? Esta ação é irreversível.",
                "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (resposta == JOptionPane.YES_OPTION) {
            excluirConta();
        }
    }

    private void excluirConta() {
        // Conforme o protocolo, a operação para excluir a própria conta [cite: 16]
        JSONObject requisicao = new JSONObject();
        requisicao.put("operacao", "EXCLUIR_PROPRIO_USUARIO");
        requisicao.put("token", this.token);

        try {
            String respostaJson = ServicoCliente.getInstancia().enviarRequisicao(requisicao.toString());
            JSONObject resposta = new JSONObject(respostaJson);
            String status = resposta.getString("status").trim();

            if ("200".equals(status)) {
                JOptionPane.showMessageDialog(this, "Conta excluída com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                voltarParaLogin();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao excluir a conta. Status: " + status, "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro de comunicação: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void realizarLogout() {
        JSONObject requisicao = new JSONObject();
        requisicao.put("operacao", "LOGOUT");
        requisicao.put("token", this.token);

        try {
            // O servidor pode ou não invalidar o token, mas o cliente deve tratar o logout
            ServicoCliente.getInstancia().enviarRequisicao(requisicao.toString());
        } catch (IOException e) {
            // Mesmo com erro, prosseguir com o logout no cliente
            System.err.println("Erro ao notificar servidor sobre logout: " + e.getMessage());
        } finally {
            JOptionPane.showMessageDialog(this, "Você foi desconectado.", "Logout", JOptionPane.INFORMATION_MESSAGE);
            voltarParaLogin();
        }
    }

    private void voltarParaLogin() {
        this.dispose();
        TelaLogin telaLogin = new TelaLogin();
        telaLogin.setVisible(true);
    }
}