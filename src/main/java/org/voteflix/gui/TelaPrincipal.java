package org.voteflix.gui;

import org.voteflix.servico.ServicoCliente;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.voteflix.cliente.gui.TelaMinhaConta;
import org.voteflix.cliente.gui.TelaLogin;
import org.voteflix.cliente.gui.TelaEditarUsuario;

public class TelaPrincipal extends JFrame {

    private String token; // Armazena o token do usuário logado

    public TelaPrincipal(String token) {
        super("VoteFlix - Painel do Usuário");
        this.token = token;

        // Intercepta o evento de fechamento da janela
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                desconectarEFechar();
            }
        });

        setSize(500, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel labelBoasVindas = new JLabel("Bem-vindo ao VoteFlix!", SwingConstants.CENTER);
        labelBoasVindas.setFont(new Font("Arial", Font.BOLD, 20));
        add(labelBoasVindas, BorderLayout.CENTER);

        // Painel de botões de ação
        JPanel painelAcoes = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton botaoMinhaConta = new JButton("Minha Conta");
        JButton botaoEditar = new JButton("Editar Minha Conta");
        JButton botaoExcluir = new JButton("Excluir Minha Conta");
        JButton botaoLogout = new JButton("Logout");

        painelAcoes.add(botaoMinhaConta);
        painelAcoes.add(botaoEditar);
        painelAcoes.add(botaoExcluir);
        painelAcoes.add(botaoLogout);

        add(painelAcoes, BorderLayout.SOUTH);

        // Ações dos botões
        botaoMinhaConta.addActionListener(e -> abrirTelaMinhaConta());
        botaoEditar.addActionListener(e -> abrirTelaEdicao());
        botaoExcluir.addActionListener(e -> confirmarExclusao());
        botaoLogout.addActionListener(e -> realizarLogout());
    }

    private void abrirTelaMinhaConta() {
        TelaMinhaConta telaMinhaConta = new TelaMinhaConta(this, token);
        telaMinhaConta.setVisible(true);
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
        JSONObject requisicao = new JSONObject();
        requisicao.put("operacao", "EXCLUIR_PROPRIO_USUARIO");
        requisicao.put("token", this.token);

        try {
            String respostaJson = ServicoCliente.getInstancia().enviarRequisicao(requisicao.toString());
            JSONObject resposta = new JSONObject(respostaJson);
            String status = resposta.getString("status").trim();

            if ("200".equals(status)) {
                JOptionPane.showMessageDialog(this, "Conta excluída com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                desconectarEVoltarParaLogin();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao excluir a conta. Status: " + status, "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro de comunicação: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } finally {
            desconectarEFechar(); // Garante a desconexão
        }
    }

    private void realizarLogout() {
        JSONObject requisicao = new JSONObject();
        requisicao.put("operacao", "LOGOUT");
        requisicao.put("token", this.token);

        try {
            ServicoCliente.getInstancia().enviarRequisicao(requisicao.toString());
        } catch (IOException e) {
            System.err.println("Erro ao notificar servidor sobre logout: " + e.getMessage());
        } finally {
            JOptionPane.showMessageDialog(this, "Você foi desconectado.", "Logout", JOptionPane.INFORMATION_MESSAGE);
            desconectarEVoltarParaLogin();
        }
    }

    /**
     * Envia a notificação de logout, desconecta e encerra a aplicação.
     * Chamado quando o usuário fecha a janela pelo botão 'X'.
     */
    private void desconectarEFechar() {
        JSONObject requisicao = new JSONObject();
        requisicao.put("operacao", "LOGOUT");
        requisicao.put("token", this.token);

        try {
            if (ServicoCliente.getInstancia().isConectado()) {
                ServicoCliente.getInstancia().enviarRequisicao(requisicao.toString());
            }
        } catch (IOException e) {
            System.err.println("Erro ao notificar servidor sobre logout no fechamento: " + e.getMessage());
        } finally {
            try {
                ServicoCliente.getInstancia().desconectar();
            } catch (IOException e) {
                System.err.println("Erro ao desconectar: " + e.getMessage());
            }
            dispose();
            System.exit(0);
        }
    }

    /**
     * Desconecta e abre a tela de login.
     */
    private void desconectarEVoltarParaLogin() {
        try {
            ServicoCliente.getInstancia().desconectar();
        } catch (IOException e) {
            System.err.println("Erro ao desconectar: " + e.getMessage());
        }
        this.dispose();
        TelaLogin telaLogin = new TelaLogin();
        telaLogin.setVisible(true);
    }
}