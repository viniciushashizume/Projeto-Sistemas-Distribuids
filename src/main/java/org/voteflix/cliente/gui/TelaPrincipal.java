package org.voteflix.cliente.gui;

import org.voteflix.cliente.servico.ServicoCliente;
import org.json.JSONObject;
import org.voteflix.util.ProtocoloMensagem; // Importar o Enum

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TelaPrincipal extends JFrame {

    private String token;

    public TelaPrincipal(String token) {
        // ... (construtor e GUI sem alteração)
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

        botaoMinhaConta.addActionListener(e -> abrirTelaMinhaConta());
        botaoEditar.addActionListener(e -> abrirTelaEdicao());
        botaoExcluir.addActionListener(e -> confirmarExclusao());
        botaoLogout.addActionListener(e -> realizarLogout());
    }

    private void abrirTelaEdicao() {
        // ... (sem alterações)
        TelaEditarUsuario telaEditar = new TelaEditarUsuario(this, token);
        telaEditar.setVisible(true);
    }

    private void confirmarExclusao() {
        // ... (sem alterações)
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

            // --- ALTERAÇÃO AQUI ---
            // "Traduz" o status para a mensagem local do Enum
            String mensagemTraduzida = ProtocoloMensagem.getByStatus(status).getMensagem();
            // --- FIM DA ALTERAÇÃO ---

            if ("200".equals(status)) {
                // Exibe a mensagem "traduzida"
                JOptionPane.showMessageDialog(this, mensagemTraduzida, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                desconectarEVoltarParaLogin();
            } else {
                // Exibe a mensagem de erro "traduzida"
                JOptionPane.showMessageDialog(this, mensagemTraduzida, "Erro (" + status + ")", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro de comunicação: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            desconectarEVoltarParaLogin();
        }
    }

    private void realizarLogout() {
        // ... (sem alterações)
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

    private void desconectarEFechar() {
        // ... (sem alterações)
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

    private void desconectarEVoltarParaLogin() {
        // ... (sem alterações)
        try {
            ServicoCliente.getInstancia().desconectar();
        } catch (IOException e) {
            System.err.println("Erro ao desconectar: " + e.getMessage());
        }
        this.dispose();
        TelaLogin telaLogin = new TelaLogin();
        telaLogin.setVisible(true);
    }

    private void abrirTelaMinhaConta() {
        // ... (sem alterações)
        TelaMinhaConta telaMinhaConta = new TelaMinhaConta(this, token);
        telaMinhaConta.setVisible(true);
    }
}