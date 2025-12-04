package org.voteflix.cliente.gui;

import org.json.JSONArray;
import org.json.JSONObject;
import org.voteflix.cliente.servico.ServicoCliente;
import org.voteflix.model.Filme;
import org.voteflix.model.Review;
import org.voteflix.util.ProtocoloMensagem;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TelaMinhasReviews extends JDialog {

    private final String token;
    private final Frame owner; // Referência ao pai para abrir janelas filhas
    private DefaultListModel<String> listModel;
    private JList<String> listaReviews;
    private List<Review> minhasReviews; // Lista para manter os objetos Review

    public TelaMinhasReviews(Frame owner, String token) {
        super(owner, "Minhas Reviews", true);
        this.token = token;
        this.owner = owner;
        this.minhasReviews = new ArrayList<>();

        setSize(600, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // Lista
        listModel = new DefaultListModel<>();
        listaReviews = new JList<>(listModel);
        listaReviews.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(listaReviews), BorderLayout.CENTER);

        // Botões
        JPanel painelBotoes = new JPanel();
        JButton btnAtualizar = new JButton("Atualizar Lista");
        JButton btnEditar = new JButton("Editar");
        JButton btnExcluir = new JButton("Excluir");
        JButton btnFechar = new JButton("Fechar");

        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnEditar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnFechar);
        add(painelBotoes, BorderLayout.SOUTH);

        // Ações
        btnAtualizar.addActionListener(e -> carregarReviews());
        btnFechar.addActionListener(e -> dispose());

        btnEditar.addActionListener(e -> editarReviewSelecionada());
        btnExcluir.addActionListener(e -> excluirReviewSelecionada());

        // Carrega ao abrir
        carregarReviews();
    }

    private void carregarReviews() {
        listModel.clear();
        minhasReviews.clear();

        JSONObject requisicao = new JSONObject();
        requisicao.put("operacao", "LISTAR_REVIEWS_USUARIO");
        requisicao.put("token", this.token);

        try {
            String respostaJson = ServicoCliente.getInstancia().enviarRequisicao(requisicao.toString());
            JSONObject resposta = new JSONObject(respostaJson);
            String status = resposta.getString("status");

            if ("200".equals(status)) {
                if (resposta.has("reviews")) {
                    JSONArray array = resposta.getJSONArray("reviews");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        Review r = new Review(obj);
                        minhasReviews.add(r);

                        // Formatação para exibição na lista
                        String item = String.format("Filme ID: %d | Nota: %.2f | Título: %s",
                                r.getIdFilme(), r.getNota(), r.getTitulo());
                        listModel.addElement(item);
                    }
                }
                if (listModel.isEmpty()) {
                    listModel.addElement("Nenhuma review encontrada.");
                }
            } else {
                String msg = ProtocoloMensagem.getByStatus(status).getMensagem();
                JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro de conexão: " + ex.getMessage());
        }
    }

    private void editarReviewSelecionada() {
        int index = listaReviews.getSelectedIndex();
        if (index < 0 || minhasReviews.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione uma review para editar.");
            return;
        }

        Review reviewSelecionada = minhasReviews.get(index);

        // 1. Busca o filme associado para passar ao formulário (Correção do erro)
        Filme filmeAssociado = buscarFilme(reviewSelecionada.getIdFilme());

        if (filmeAssociado != null) {
            // 2. Agora passamos os 4 argumentos exigidos pelo construtor
            TelaFormularioReview telaEdicao = new TelaFormularioReview(owner, token, filmeAssociado, reviewSelecionada);
            telaEdicao.setVisible(true);
            if (telaEdicao.isSalvo()) {
                carregarReviews();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Não foi possível carregar os dados do filme para edição.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método auxiliar para buscar o Filme pelo ID
    private Filme buscarFilme(int idFilme) {
        JSONObject requisicao = new JSONObject();
        requisicao.put("operacao", "BUSCAR_FILME_ID");
        requisicao.put("id_filme", String.valueOf(idFilme));
        requisicao.put("token", this.token);

        try {
            String respostaJson = ServicoCliente.getInstancia().enviarRequisicao(requisicao.toString());
            JSONObject resposta = new JSONObject(respostaJson);

            if ("200".equals(resposta.optString("status"))) {
                return new Filme(resposta.getJSONObject("filme"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void excluirReviewSelecionada() {
        int index = listaReviews.getSelectedIndex();
        if (index < 0 || minhasReviews.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione uma review para excluir.");
            return;
        }

        Review review = minhasReviews.get(index);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir sua review \"" + review.getTitulo() + "\"?",
                "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            JSONObject req = new JSONObject();
            req.put("operacao", "EXCLUIR_REVIEW");
            req.put("token", token);
            req.put("id", String.valueOf(review.getId()));

            try {
                String respStr = ServicoCliente.getInstancia().enviarRequisicao(req.toString());
                JSONObject resp = new JSONObject(respStr);
                String status = resp.getString("status");

                String msg = ProtocoloMensagem.getByStatus(status).getMensagem();
                JOptionPane.showMessageDialog(this, msg);

                if ("200".equals(status)) {
                    carregarReviews();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}