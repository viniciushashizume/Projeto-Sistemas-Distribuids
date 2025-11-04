package org.voteflix.cliente.gui;

import org.voteflix.cliente.servico.ServicoCliente;
import org.voteflix.model.Filme;
import org.voteflix.util.ProtocoloMensagem;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TelaFormularioFilme extends JDialog {

    private String token;
    private Filme filmeExistente; // Null se for criação
    private boolean salvo = false;

    private JTextField campoTitulo, campoDiretor, campoAno;
    private JTextArea areaSinopse;
    private JList<String> listaGeneros;

    // Gêneros pré-cadastrados (Requisito [source 64])
    private static final String[] GENEROS_PRECADASTRADOS = {
            "Ação", "Aventura", "Comédia", "Drama", "Fantasia",
            "Ficção Científica", "Terror", "Romance", "Documentário",
            "Musical", "Animação"
    };

    public TelaFormularioFilme(Dialog owner, String token, Filme filme) {
        super(owner, true);
        this.token = token;
        this.filmeExistente = filme;

        setTitle(filme == null ? "Adicionar Novo Filme" : "Editar Filme");
        setSize(500, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel painelCampos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        gbc.gridx = 0; gbc.gridy = 0;
        painelCampos.add(new JLabel("Título (min 3, max 30):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        campoTitulo = new JTextField(20);
        painelCampos.add(campoTitulo, gbc);

        // Diretor
        gbc.gridx = 0; gbc.gridy = 1;
        painelCampos.add(new JLabel("Diretor (min 3, max 30):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        campoDiretor = new JTextField(20);
        painelCampos.add(campoDiretor, gbc);

        // Ano
        gbc.gridx = 0; gbc.gridy = 2;
        painelCampos.add(new JLabel("Ano (3 ou 4 dígitos):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        campoAno = new JTextField(4);
        painelCampos.add(campoAno, gbc);

        // Gêneros
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.NORTH;
        painelCampos.add(new JLabel("Gêneros (pelo menos 1):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        listaGeneros = new JList<>(GENEROS_PRECADASTRADOS);
        listaGeneros.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollGeneros = new JScrollPane(listaGeneros);
        scrollGeneros.setPreferredSize(new Dimension(200, 150));
        painelCampos.add(scrollGeneros, gbc);

        // Sinopse
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.NORTH;
        painelCampos.add(new JLabel("Sinopse (max 250):"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        areaSinopse = new JTextArea(5, 20);
        areaSinopse.setLineWrap(true);
        areaSinopse.setWrapStyleWord(true);
        JScrollPane scrollSinopse = new JScrollPane(areaSinopse);
        painelCampos.add(scrollSinopse, gbc);

        add(painelCampos, BorderLayout.CENTER);

        // Botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton botaoSalvar = new JButton("Salvar");
        JButton botaoCancelar = new JButton("Cancelar");
        painelBotoes.add(botaoSalvar);
        painelBotoes.add(botaoCancelar);
        add(painelBotoes, BorderLayout.SOUTH);

        botaoSalvar.addActionListener(e -> salvarFilme());
        botaoCancelar.addActionListener(e -> dispose());

        // Preenche os campos se for edição
        if (filmeExistente != null) {
            preencherCampos();
        }
    }

    private void preencherCampos() {
        campoTitulo.setText(filmeExistente.getTitulo());
        campoDiretor.setText(filmeExistente.getDiretor());
        campoAno.setText(filmeExistente.getAno());
        areaSinopse.setText(filmeExistente.getSinopse());

        // Seleciona os gêneros existentes
        List<String> generosExistentes = filmeExistente.getGeneros();
        List<String> todosGeneros = Arrays.asList(GENEROS_PRECADASTRADOS);
        int[] indices = generosExistentes.stream()
                .mapToInt(todosGeneros::indexOf)
                .filter(i -> i >= 0)
                .toArray();
        listaGeneros.setSelectedIndices(indices);
    }

    private void salvarFilme() {
        // Validação (Requisitos [source 57-63])
        String titulo = campoTitulo.getText().trim();
        String diretor = campoDiretor.getText().trim();
        String ano = campoAno.getText().trim();
        String sinopse = areaSinopse.getText().trim();
        List<String> generos = listaGeneros.getSelectedValuesList();

        if (titulo.length() < 3 || titulo.length() > 30) {
            JOptionPane.showMessageDialog(this, "Título deve ter entre 3 e 30 caracteres.", "Erro de Validação", JOptionPane.ERROR_MESSAGE); return;
        }
        if (diretor.length() < 3 || diretor.length() > 30) {
            JOptionPane.showMessageDialog(this, "Diretor deve ter entre 3 e 30 caracteres.", "Erro de Validação", JOptionPane.ERROR_MESSAGE); return;
        }
        if (!ano.matches("\\d{3,4}")) { // Requisito [source 57, 60]
            JOptionPane.showMessageDialog(this, "Ano deve ter 3 ou 4 dígitos numéricos.", "Erro de Validação", JOptionPane.ERROR_MESSAGE); return;
        }
        if (generos.isEmpty()) { // Requisito [source 62]
            JOptionPane.showMessageDialog(this, "Selecione pelo menos um gênero.", "Erro de Validação", JOptionPane.ERROR_MESSAGE); return;
        }
        if (sinopse.length() > 250) { // Requisito [source 63]
            JOptionPane.showMessageDialog(this, "Sinopse deve ter no máximo 250 caracteres.", "Erro de Validação", JOptionPane.ERROR_MESSAGE); return;
        }

        // Constrói o objeto Filme
        int id = (filmeExistente != null) ? filmeExistente.getId() : 0;
        Filme filmeParaEnviar = new Filme(id, titulo, diretor, ano, sinopse, generos);

        // Prepara a requisição
        JSONObject requisicao = new JSONObject();
        requisicao.put("token", this.token);
        requisicao.put("filme", filmeParaEnviar.toJSONObject());

        String operacao = (filmeExistente == null) ? "CRIAR_FILME" : "EDITAR_FILME";
        requisicao.put("operacao", operacao);

        try {
            String respostaJson = ServicoCliente.getInstancia().enviarRequisicao(requisicao.toString());
            JSONObject resp = new JSONObject(respostaJson);
            String status = resp.getString("status").trim();
            String msg = ProtocoloMensagem.getByStatus(status).getMensagem();

            if ("201".equals(status) || "200".equals(status)) {
                JOptionPane.showMessageDialog(this, msg, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                this.salvo = true;
                dispose();
            } else {
                // Status 405 (Campos inválidos) ou 409 (Já existe)
                JOptionPane.showMessageDialog(this, msg, "Erro (" + status + ")", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro de comunicação: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSalvo() {
        return salvo;
    }
}