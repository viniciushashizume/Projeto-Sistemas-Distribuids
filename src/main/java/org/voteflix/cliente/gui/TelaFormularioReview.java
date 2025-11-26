package org.voteflix.cliente.gui;

import org.json.JSONObject;
import org.voteflix.cliente.servico.ServicoCliente;
import org.voteflix.model.Filme;
import org.voteflix.model.Review;
import org.voteflix.util.ProtocoloMensagem;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class TelaFormularioReview extends JDialog {

    private String token;
    private Filme filme;
    private Review reviewEdicao; // Se null, é criação. Se não, é edição.
    private boolean salvo = false;

    private JTextField txtTitulo;
    private JTextArea txtDescricao;
    private JComboBox<String> comboNota;

    public TelaFormularioReview(Window owner, String token, Filme filme, Review reviewEdicao) {
        super(owner, reviewEdicao == null ? "Nova Review" : "Editar Review", ModalityType.APPLICATION_MODAL);
        this.token = token;
        this.filme = filme;
        this.reviewEdicao = reviewEdicao;

        setSize(400, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título
        formPanel.add(new JLabel("Título:"));
        txtTitulo = new JTextField();
        formPanel.add(txtTitulo);

        // Nota
        formPanel.add(new JLabel("Nota (0.0 - 5.0):"));
        // Opções de nota de 0 a 5 com decimais simples (ex: 0, 0.5, 1, 1.5...) ou apenas inteiros.
        // O requisito pede apenas validação numérica, vamos usar inteiros/meios para facilitar.
        String[] notas = {"0", "1", "2", "3", "4", "5"};
        comboNota = new JComboBox<>(notas);
        formPanel.add(comboNota);

        // Descrição
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        descPanel.add(new JLabel("Descrição:"), BorderLayout.NORTH);
        txtDescricao = new JTextArea(5, 20);
        txtDescricao.setLineWrap(true);
        descPanel.add(new JScrollPane(txtDescricao), BorderLayout.CENTER);

        // Preencher dados se for edição
        if (reviewEdicao != null) {
            txtTitulo.setText(reviewEdicao.getTitulo());
            txtDescricao.setText(reviewEdicao.getDescricao());
            comboNota.setSelectedItem(String.valueOf((int) reviewEdicao.getNota()));
        }

        // Botões
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");

        btnSalvar.addActionListener(e -> salvar());
        btnCancelar.addActionListener(e -> dispose());

        btnPanel.add(btnSalvar);
        btnPanel.add(btnCancelar);

        add(formPanel, BorderLayout.NORTH);
        add(descPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void salvar() {
        String titulo = txtTitulo.getText().trim();
        String descricao = txtDescricao.getText().trim();
        String nota = (String) comboNota.getSelectedItem();

        /*if (titulo.isEmpty() || descricao.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }*/

        JSONObject jsonReview = new JSONObject();
        jsonReview.put("titulo", titulo);
        jsonReview.put("descricao", descricao);
        jsonReview.put("nota", nota);

        JSONObject requisicao = new JSONObject();
        requisicao.put("token", token);

        if (reviewEdicao == null) {
            // CRIAR
            requisicao.put("operacao", "CRIAR_REVIEW");
            jsonReview.put("id_filme", String.valueOf(filme.getId()));
            requisicao.put("review", jsonReview);
        } else {
            // EDITAR
            requisicao.put("operacao", "EDITAR_REVIEW");
            jsonReview.put("id", String.valueOf(reviewEdicao.getId()));
            requisicao.put("review", jsonReview);
        }

        try {
            String respJson = ServicoCliente.getInstancia().enviarRequisicao(requisicao.toString());
            JSONObject resposta = new JSONObject(respJson);
            String status = resposta.getString("status");

            if ("201".equals(status) || "200".equals(status)) {
                JOptionPane.showMessageDialog(this, resposta.optString("mensagem", "Sucesso!"), "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                this.salvo = true;
                dispose();
            } else {
                String msgErro = ProtocoloMensagem.getByStatus(status).getMensagem();
                JOptionPane.showMessageDialog(this, msgErro, "Erro " + status, JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro de conexão: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSalvo() {
        return salvo;
    }
}