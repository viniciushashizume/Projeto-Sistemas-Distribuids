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

public class TelaDetalhesFilme extends JDialog {

    private String token;
    private boolean isAdmin;
    private Filme filmeBasico; // Filme vindo da lista anterior
    private DefaultListModel<Review> listModelReviews;
    private JList<Review> listaReviews;
    private String meuNomeUsuario; // Para verificar dono da review

    private JButton btnCriarReview;
    private JButton btnEditarReview;
    private JButton btnExcluirReview;

    public TelaDetalhesFilme(Window owner, String token, boolean isAdmin, Filme filmeBasico) {
        super(owner, "Detalhes do Filme - " + filmeBasico.getTitulo(), ModalityType.APPLICATION_MODAL);
        this.token = token;
        this.isAdmin = isAdmin;
        this.filmeBasico = filmeBasico;

        setSize(800, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // Carregar nome do usuário atual para validações
        carregarUsuarioLogado();

        // Painel Superior: Detalhes do Filme
        JPanel painelInfo = new JPanel();
        painelInfo.setLayout(new BoxLayout(painelInfo, BoxLayout.Y_AXIS));
        painelInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        painelInfo.add(new JLabel("Título: " + filmeBasico.getTitulo()));
        painelInfo.add(new JLabel("Diretor: " + filmeBasico.getDiretor()));
        painelInfo.add(new JLabel("Ano: " + filmeBasico.getAno()));
        painelInfo.add(new JLabel("Gêneros: " + String.join(", ", filmeBasico.getGeneros())));
        JTextArea txtSinopse = new JTextArea("Sinopse: " + filmeBasico.getSinopse());
        txtSinopse.setLineWrap(true);
        txtSinopse.setWrapStyleWord(true);
        txtSinopse.setEditable(false);
        txtSinopse.setBackground(this.getBackground());
        painelInfo.add(txtSinopse);

        add(painelInfo, BorderLayout.NORTH);

        // Centro: Lista de Reviews
        listModelReviews = new DefaultListModel<>();
        listaReviews = new JList<>(listModelReviews);
        listaReviews.setCellRenderer(new ReviewRenderer());
        listaReviews.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Listener de seleção para habilitar/desabilitar botões
        listaReviews.addListSelectionListener(e -> atualizarBotoes());

        add(new JScrollPane(listaReviews), BorderLayout.CENTER);

        // Painel Inferior: Botões
        JPanel painelBotoes = new JPanel(new FlowLayout());

        btnCriarReview = new JButton("Escrever Review");
        btnEditarReview = new JButton("Editar Minha Review");
        btnExcluirReview = new JButton("Excluir Review");
        JButton btnVoltar = new JButton("Voltar");

        btnCriarReview.addActionListener(e -> abrirFormulario(null));
        btnEditarReview.addActionListener(e -> abrirFormulario(listaReviews.getSelectedValue()));
        btnExcluirReview.addActionListener(e -> excluirReview());
        btnVoltar.addActionListener(e -> dispose());

        painelBotoes.add(btnCriarReview);
        painelBotoes.add(btnEditarReview);
        painelBotoes.add(btnExcluirReview);
        painelBotoes.add(btnVoltar);

        add(painelBotoes, BorderLayout.SOUTH);

        // Carregar dados reais (reviews)
        carregarDadosFilme();
    }

    private void carregarUsuarioLogado() {
        // Requisito: Precisamos saber quem é o usuário para habilitar botões de edição/exclusão própria
        JSONObject req = new JSONObject();
        req.put("operacao", "LISTAR_PROPRIO_USUARIO");
        req.put("token", token);

        try {
            String res = ServicoCliente.getInstancia().enviarRequisicao(req.toString());
            JSONObject json = new JSONObject(res);
            if ("200".equals(json.getString("status"))) {
                this.meuNomeUsuario = json.getString("usuario"); // Protocolo retorna chave "usuario" com o nome
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.meuNomeUsuario = "";
        }
    }

    private void carregarDadosFilme() {
        JSONObject req = new JSONObject();
        req.put("operacao", "BUSCAR_FILME_ID");
        req.put("id_filme", String.valueOf(filmeBasico.getId()));
        req.put("token", token);

        try {
            String res = ServicoCliente.getInstancia().enviarRequisicao(req.toString());
            JSONObject json = new JSONObject(res);

            if ("200".equals(json.getString("status"))) {
                // Atualizar lista de reviews
                listModelReviews.clear();
                if (json.has("reviews")) {
                    JSONArray arr = json.getJSONArray("reviews");
                    for (int i = 0; i < arr.length(); i++) {
                        listModelReviews.addElement(new Review(arr.getJSONObject(i)));
                    }
                }
                atualizarBotoes();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao carregar detalhes.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro de conexão.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarBotoes() {
        Review selecionada = listaReviews.getSelectedValue();
        boolean temReviewMinha = false;
        boolean selecionadaEhMinha = false;

        // Verificar se usuário já fez review neste filme
        for (int i = 0; i < listModelReviews.getSize(); i++) {
            if (listModelReviews.get(i).getNomeUsuario().equals(meuNomeUsuario)) {
                temReviewMinha = true;
                break;
            }
        }

        if (selecionada != null && selecionada.getNomeUsuario().equals(meuNomeUsuario)) {
            selecionadaEhMinha = true;
        }

        btnCriarReview.setEnabled(!temReviewMinha && !isAdmin);

        btnEditarReview.setEnabled(selecionadaEhMinha && !isAdmin);

        btnExcluirReview.setEnabled(selecionada != null && (selecionadaEhMinha || isAdmin));
    }

    private void abrirFormulario(Review reviewParaEditar) {
        TelaFormularioReview form = new TelaFormularioReview(this, token, filmeBasico, reviewParaEditar);
        form.setVisible(true);
        if (form.isSalvo()) {
            carregarDadosFilme(); // Recarrega tudo para atualizar
        }
    }

    private void excluirReview() {
        Review selecionada = listaReviews.getSelectedValue();
        if (selecionada == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir esta review?", "Confirmar", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        JSONObject req = new JSONObject();
        req.put("operacao", "EXCLUIR_REVIEW");
        req.put("id", String.valueOf(selecionada.getId()));
        req.put("token", token);

        try {
            String res = ServicoCliente.getInstancia().enviarRequisicao(req.toString());
            JSONObject json = new JSONObject(res);

            if ("200".equals(json.getString("status"))) {
                JOptionPane.showMessageDialog(this, json.getString("mensagem"));
                carregarDadosFilme();
            } else {
                String msg = ProtocoloMensagem.getByStatus(json.getString("status")).getMensagem();
                JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro de conexão.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class ReviewRenderer extends JPanel implements ListCellRenderer<Review> {
        private JLabel lblTitulo = new JLabel();
        private JTextArea txtDesc = new JTextArea();
        private JLabel lblInfo = new JLabel();

        public ReviewRenderer() {
            setLayout(new BorderLayout(5, 5));
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

            lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));

            txtDesc.setLineWrap(true);
            txtDesc.setWrapStyleWord(true);
            txtDesc.setOpaque(false);

            lblInfo.setFont(new Font("SansSerif", Font.ITALIC, 11));
            lblInfo.setForeground(Color.DARK_GRAY);

            add(lblTitulo, BorderLayout.NORTH);
            add(txtDesc, BorderLayout.CENTER);
            add(lblInfo, BorderLayout.SOUTH);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Review> list, Review value, int index, boolean isSelected, boolean cellHasFocus) {
            lblTitulo.setText(String.format("Nota: %.2f - %s", value.getNota(), value.getTitulo()));
            txtDesc.setText(value.getDescricao());
            String editadoStr = value.isEditado() ? "(Editado)" : "";
            lblInfo.setText(String.format("Por: %s em %s %s", value.getNomeUsuario(), value.getData(), editadoStr));

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            // Ajuste de tamanho fixo para visualização melhor
            setPreferredSize(new Dimension(list.getWidth() - 20, 100));
            return this;
        }
    }
}