package org.voteflix.servidor.gui;

import org.voteflix.bd.UsuarioBD;
import org.voteflix.model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Set;

public class TelaServidor extends JFrame {

    private final JTextArea areaLogs;
    private final DefaultListModel<String> modelListaUsuarios;
    private final JList<String> listaUsuarios;
    private final UsuarioBD usuarioBD;

    public TelaServidor() {
        super("VoteFlix - Painel do Servidor");
        this.usuarioBD = new UsuarioBD();

        // Configurações da janela principal
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Título
        JLabel labelTitulo = new JLabel("Logs de Atividade e Usuários Ativos", SwingConstants.CENTER);
        labelTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        add(labelTitulo, BorderLayout.NORTH);

        // Painel principal dividido
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.7); // 70% do espaço para os logs
        add(splitPane, BorderLayout.CENTER);

        // Painel de Logs
        JPanel painelLogs = new JPanel(new BorderLayout());
        painelLogs.setBorder(BorderFactory.createTitledBorder("Logs de Conexões e Requisições"));
        areaLogs = new JTextArea();
        areaLogs.setEditable(false);
        areaLogs.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollLogs = new JScrollPane(areaLogs);
        painelLogs.add(scrollLogs, BorderLayout.CENTER);
        splitPane.setLeftComponent(painelLogs);

        // Painel de Usuários Ativos
        JPanel painelUsuarios = new JPanel(new BorderLayout());
        painelUsuarios.setBorder(BorderFactory.createTitledBorder("Usuários Ativos (clique duplo para ver detalhes)"));
        modelListaUsuarios = new DefaultListModel<>();
        listaUsuarios = new JList<>(modelListaUsuarios);
        JScrollPane scrollUsuarios = new JScrollPane(listaUsuarios);
        painelUsuarios.add(scrollUsuarios, BorderLayout.CENTER);
        splitPane.setRightComponent(painelUsuarios);

        listaUsuarios.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) { // Duplo clique
                    int index = listaUsuarios.locationToIndex(evt.getPoint());
                    String nomeUsuario = modelListaUsuarios.getElementAt(index);
                    exibirDetalhesUsuario(nomeUsuario);
                }
            }
        });
    }

    private void exibirDetalhesUsuario(String nomeUsuario) {
        try {
            Usuario usuario = usuarioBD.buscarUsuarioPorNome(nomeUsuario);
            if (usuario != null) {
                TelaDetalhesUsuario telaDetalhes = new TelaDetalhesUsuario(this, usuario);
                telaDetalhes.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Usuário não encontrado no banco de dados.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao acessar o banco de dados: " + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Adiciona uma mensagem de log à área de texto.
     * Este método é thread-safe.
     * @param mensagem A mensagem a ser registrada.
     */
    public void adicionarLog(String mensagem) {
        SwingUtilities.invokeLater(() -> {
            areaLogs.append(mensagem + "\n");
            // Rola automaticamente para o final
            areaLogs.setCaretPosition(areaLogs.getDocument().getLength());
        });
    }

    /**
     * Atualiza a lista de usuários ativos na interface.
     * Este método é thread-safe.
     * @param usuarios O conjunto de nomes de usuários ativos.
     */
    public void atualizarListaUsuarios(Set<String> usuarios) {
        SwingUtilities.invokeLater(() -> {
            modelListaUsuarios.clear();
            for (String usuario : usuarios) {
                modelListaUsuarios.addElement(usuario);
            }
        });
    }
}