package org.voteflix.servidor.gui;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class TelaServidor extends JFrame {

    private final JTextArea areaLogs;
    private final DefaultListModel<String> modelListaUsuarios;
    private final JList<String> listaUsuarios;

    public TelaServidor() {
        super("VoteFlix - Painel do Servidor");

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
        painelUsuarios.setBorder(BorderFactory.createTitledBorder("Usuários Ativos"));
        modelListaUsuarios = new DefaultListModel<>();
        listaUsuarios = new JList<>(modelListaUsuarios);
        JScrollPane scrollUsuarios = new JScrollPane(listaUsuarios);
        painelUsuarios.add(scrollUsuarios, BorderLayout.CENTER);
        splitPane.setRightComponent(painelUsuarios);
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