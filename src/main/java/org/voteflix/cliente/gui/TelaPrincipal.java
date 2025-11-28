package org.voteflix.cliente.gui;

import org.voteflix.cliente.servico.ServicoCliente;
import org.json.JSONObject;
import org.voteflix.util.ProtocoloMensagem;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TelaPrincipal extends JFrame {

    private String token;
    private boolean isAdmin;

    public TelaPrincipal(String token, boolean isAdmin) {
        super("VoteFlix - Painel do Usuário");
        this.token = token;
        this.isAdmin = isAdmin;

        // Intercepta o evento de fechamento da janela
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                desconectarEFechar();
            }
        });

        setSize(700, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        String textoBoasVindas = isAdmin ? "Bem-vindo, Administrador!" : "Bem-vindo ao VoteFlix!";
        JLabel labelBoasVindas = new JLabel(textoBoasVindas, SwingConstants.CENTER);
        labelBoasVindas.setFont(new Font("Arial", Font.BOLD, 20));
        add(labelBoasVindas, BorderLayout.CENTER);

        JPanel painelAcoes = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // --- Botões visíveis para TODOS ---
        JButton botaoListarFilmes = new JButton("Ver Filmes");
        JButton botaoMinhaConta = new JButton("Minha Conta");
        JButton botaoMinhasReviews = new JButton("Minhas Reviews"); // NOVO BOTÃO
        JButton botaoEditar = new JButton("Editar Minha Conta");
        JButton botaoLogout = new JButton("Logout");

        painelAcoes.add(botaoListarFilmes);
        painelAcoes.add(botaoMinhaConta);
        painelAcoes.add(botaoMinhasReviews); // ADICIONADO AO PAINEL
        painelAcoes.add(botaoEditar);
        painelAcoes.add(botaoLogout);

        // --- Botões específicos de permissão ---
        if (isAdmin) {
            JButton botaoGerenciarFilmes = new JButton("Gerenciar Filmes (Admin)");
            JButton botaoGerenciarUsuarios = new JButton("Gerenciar Usuários (Admin)");
            painelAcoes.add(botaoGerenciarFilmes);
            painelAcoes.add(botaoGerenciarUsuarios);

            botaoGerenciarFilmes.addActionListener(e -> abrirTelaGerenciarFilmes());
            botaoGerenciarUsuarios.addActionListener(e -> abrirTelaGerenciarUsuarios());
        } else {
            JButton botaoExcluir = new JButton("Excluir Minha Conta");
            painelAcoes.add(botaoExcluir);
            botaoExcluir.addActionListener(e -> confirmarExclusao());
        }

        add(painelAcoes, BorderLayout.SOUTH);

        // --- Listeners dos botões comuns ---
        botaoListarFilmes.addActionListener(e -> abrirTelaListarFilmes());
        botaoMinhaConta.addActionListener(e -> abrirTelaMinhaConta());
        botaoMinhasReviews.addActionListener(e -> abrirTelaMinhasReviews()); // NOVO LISTENER
        botaoEditar.addActionListener(e -> abrirTelaEdicao());
        botaoLogout.addActionListener(e -> realizarLogout());
    }

    private void abrirTelaListarFilmes() {
        TelaListarFilmes telaFilmes = new TelaListarFilmes(this, token, this.isAdmin, false);
        telaFilmes.setVisible(true);
    }

    private void abrirTelaGerenciarFilmes() {
        TelaListarFilmes telaFilmes = new TelaListarFilmes(this, token, this.isAdmin, true);
        telaFilmes.setVisible(true);
    }

    private void abrirTelaGerenciarUsuarios() {
        TelaGerenciarUsuarios telaUsuarios = new TelaGerenciarUsuarios(this, token);
        telaUsuarios.setVisible(true);
    }

    private void abrirTelaEdicao() {
        TelaEditarUsuario telaEditar = new TelaEditarUsuario(this, token);
        telaEditar.setVisible(true);
    }

    // --- NOVO MÉTODO ---
    private void abrirTelaMinhasReviews() {
        TelaMinhasReviews telaReviews = new TelaMinhasReviews(this, token);
        telaReviews.setVisible(true);
    }
    // -------------------

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

            String mensagemTraduzida = ProtocoloMensagem.getByStatus(status).getMensagem();

            if ("200".equals(status)) {
                JOptionPane.showMessageDialog(this, mensagemTraduzida, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                desconectarEVoltarParaLogin();
            } else {
                JOptionPane.showMessageDialog(this, mensagemTraduzida, "Erro (" + status + ")", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro de comunicação: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            desconectarEVoltarParaLogin();
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

    private void abrirTelaMinhaConta() {
        TelaMinhaConta telaMinhaConta = new TelaMinhaConta(this, token);
        telaMinhaConta.setVisible(true);
    }
}