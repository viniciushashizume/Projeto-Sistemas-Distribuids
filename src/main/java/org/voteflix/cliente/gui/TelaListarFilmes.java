package org.voteflix.cliente.gui;

import org.voteflix.cliente.servico.ServicoCliente;
import org.voteflix.model.Filme;
import org.voteflix.util.ProtocoloMensagem;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TelaListarFilmes extends JDialog {

    private String token;
    private boolean isAdmin;
    private boolean modoGerenciamento;
    private JList<Filme> listaFilmes;
    private DefaultListModel<Filme> listModel;
    private List<Filme> todosFilmes;

    // ... (Construtor e demais métodos permanecem iguais até verDetalhes) ...
    // Estou omitindo o código repetido para focar na alteração.
    // Copie o restante da classe do arquivo original e substitua apenas o método abaixo.

    // ... código anterior ...

    public TelaListarFilmes(Frame owner, String token, boolean isAdmin, boolean modoGerenciamento) {
        // ... (código existente do construtor mantido) ...
        super(owner, "VoteFlix - Lista de Filmes", true);
        this.token = token;
        this.isAdmin = isAdmin;
        this.modoGerenciamento = modoGerenciamento;
        this.todosFilmes = new ArrayList<>();

        setSize(700, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // Painel de Filtro
        JPanel painelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelFiltro.add(new JLabel("Filtrar por Gênero:"));
        JComboBox<String> comboGeneros = new JComboBox<>(new String[]{"Todos", "Ação", "Aventura", "Comédia", "Drama", "Fantasia", "Ficção Científica", "Terror", "Romance", "Documentário", "Musical", "Animação"});
        comboGeneros.addActionListener(e -> filtrarFilmes((String) comboGeneros.getSelectedItem()));
        painelFiltro.add(comboGeneros);

        if(modoGerenciamento) {
            setTitle("VoteFlix - Gerenciar Filmes (Admin)");
        } else {
            setTitle("VoteFlix - Ver Filmes");
        }

        add(painelFiltro, BorderLayout.NORTH);

        // Lista de Filmes
        listModel = new DefaultListModel<>();
        listaFilmes = new JList<>(listModel);
        listaFilmes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(listaFilmes), BorderLayout.CENTER);

        // Painel de Botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton botaoVerDetalhes = new JButton("Ver Detalhes/Reviews");
        painelBotoes.add(botaoVerDetalhes);

        // Botões de Admin (CRUD de Filmes)
        if (this.isAdmin && this.modoGerenciamento) {
            JButton botaoAdicionar = new JButton("Adicionar Filme");
            JButton botaoEditar = new JButton("Editar Filme");
            JButton botaoExcluir = new JButton("Excluir Filme");

            painelBotoes.add(botaoAdicionar);
            painelBotoes.add(botaoEditar);
            painelBotoes.add(botaoExcluir);

            botaoAdicionar.addActionListener(e -> adicionarFilme());
            botaoEditar.addActionListener(e -> editarFilme());
            botaoExcluir.addActionListener(e -> excluirFilme());
        }

        add(painelBotoes, BorderLayout.SOUTH);

        // Listener para Ver Detalhes (ALTERADO)
        botaoVerDetalhes.addActionListener(e -> verDetalhes());

        carregarFilmes();
    }

    // ... (Métodos carregarFilmes e filtrarFilmes mantidos) ...

    private void carregarFilmes() {
        JSONObject requisicao = new JSONObject();
        requisicao.put("operacao", "LISTAR_FILMES");
        requisicao.put("token", this.token);

        try {
            String respostaJson = ServicoCliente.getInstancia().enviarRequisicao(requisicao.toString());
            JSONObject resposta = new JSONObject(respostaJson);
            String status = resposta.getString("status").trim();

            if ("200".equals(status)) {
                todosFilmes.clear();
                if (resposta.has("filmes")) {
                    JSONArray filmesArray = resposta.getJSONArray("filmes");
                    for (int i = 0; i < filmesArray.length(); i++) {
                        todosFilmes.add(new Filme(filmesArray.getJSONObject(i)));
                    }
                }
                filtrarFilmes("Todos");
                if (todosFilmes.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nenhum filme cadastrado no sistema.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                String msgErro = ProtocoloMensagem.getByStatus(status).getMensagem();
                JOptionPane.showMessageDialog(this, "Erro ao carregar filmes: " + msgErro, "Erro (" + status + ")", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro de comunicação: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filtrarFilmes(String genero) {
        listModel.clear();
        if ("Todos".equals(genero) || genero == null) {
            for (Filme f : todosFilmes) {
                listModel.addElement(f);
            }
        } else {
            List<Filme> filmesFiltrados = todosFilmes.stream()
                    .filter(f -> f.getGeneros().contains(genero))
                    .collect(Collectors.toList());
            for (Filme f : filmesFiltrados) {
                listModel.addElement(f);
            }
        }
    }

    // --- MÉTODO ALTERADO ---
    private void verDetalhes() {
        Filme selecionado = listaFilmes.getSelectedValue();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um filme para ver os detalhes.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Abre a nova tela de detalhes e reviews
        TelaDetalhesFilme telaDetalhes = new TelaDetalhesFilme(this, token, isAdmin, selecionado);
        telaDetalhes.setVisible(true);
    }

    // ... (Métodos de Admin mantidos) ...

    private void adicionarFilme() {
        TelaFormularioFilme formulario = new TelaFormularioFilme(this, token, null);
        formulario.setVisible(true);
        if (formulario.isSalvo()) {
            carregarFilmes();
        }
    }

    private void editarFilme() {
        Filme selecionado = listaFilmes.getSelectedValue();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um filme para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TelaFormularioFilme formulario = new TelaFormularioFilme(this, token, selecionado);
        formulario.setVisible(true);
        if (formulario.isSalvo()) {
            carregarFilmes();
        }
    }

    private void excluirFilme() {
        Filme selecionado = listaFilmes.getSelectedValue();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um filme para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int resposta = JOptionPane.showConfirmDialog(
                this,
                "Tem certeza que deseja excluir o filme \"" + selecionado.getTitulo() + "\"?\n(Requisito: Isso apagará todas as reviews associadas)",
                "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }

        JSONObject requisicao = new JSONObject();
        requisicao.put("operacao", "EXCLUIR_FILME");
        requisicao.put("token", this.token);
        requisicao.put("id", String.valueOf(selecionado.getId()));

        try {
            String respostaJson = ServicoCliente.getInstancia().enviarRequisicao(requisicao.toString());
            JSONObject resp = new JSONObject(respostaJson);
            String status = resp.getString("status").trim();
            String msg = ProtocoloMensagem.getByStatus(status).getMensagem();

            if ("200".equals(status)) {
                JOptionPane.showMessageDialog(this, msg, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarFilmes();
            } else {
                JOptionPane.showMessageDialog(this, msg, "Erro (" + status + ")", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro de comunicação: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}