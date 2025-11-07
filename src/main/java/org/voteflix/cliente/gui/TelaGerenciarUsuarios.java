package org.voteflix.cliente.gui;

import org.voteflix.cliente.servico.ServicoCliente;
import org.voteflix.model.UsuarioInfo; // Importa o modelo criado
import org.voteflix.util.ProtocoloMensagem;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException; // <-- IMPORT ADICIONADO

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TelaGerenciarUsuarios extends JDialog {

    private String token;
    private JList<UsuarioInfo> listaUsuarios;
    private DefaultListModel<UsuarioInfo> listModel;

    public TelaGerenciarUsuarios(Frame owner, String token) {
        super(owner, "Gerenciar Usuários (Admin)", true);
        this.token = token;

        setSize(500, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        listaUsuarios = new JList<>(listModel);
        listaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(listaUsuarios), BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton botaoEditarSenha = new JButton("Editar Senha");
        JButton botaoExcluir = new JButton("Excluir Usuário");
        painelBotoes.add(botaoEditarSenha);
        painelBotoes.add(botaoExcluir);
        add(painelBotoes, BorderLayout.SOUTH);

        botaoEditarSenha.addActionListener(e -> editarSenhaUsuario());
        botaoExcluir.addActionListener(e -> excluirUsuario());

        carregarUsuarios();
    }

    private void carregarUsuarios() {
        JSONObject requisicao = new JSONObject();
        // Protocolo: "LISTAR_USUARIOS"
        requisicao.put("operacao", "LISTAR_USUARIOS");
        requisicao.put("token", this.token);

        try {
            String respostaJson = ServicoCliente.getInstancia().enviarRequisicao(requisicao.toString());
            JSONObject resposta = new JSONObject(respostaJson);
            String status = resposta.getString("status").trim();

            if ("200".equals(status)) {
                listModel.clear();
                if (resposta.has("usuarios")) {
                    JSONArray usuariosArray = resposta.getJSONArray("usuarios");
                    for (int i = 0; i < usuariosArray.length(); i++) {
                        // Usa o UsuarioInfo para popular a lista
                        listModel.addElement(new UsuarioInfo(usuariosArray.getJSONObject(i)));
                    }
                }
            } else {
                // --- CORREÇÃO ---
                // Lê a mensagem de erro vinda do servidor, ao invés de usar ProtocoloMensagem.getByStatus
                String msgErro = resposta.getString("mensagem");
                JOptionPane.showMessageDialog(this, "Erro ao carregar usuários: " + msgErro, "Erro (" + status + ")", JOptionPane.ERROR_MESSAGE);
                dispose(); // Fecha a tela se não tiver permissão (ex: 403)
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro de comunicação: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (JSONException e) {
            // Adicionado para tratar erros de parse do JSON ou falta das chaves "status"/"mensagem"
            JOptionPane.showMessageDialog(this, "Erro ao processar resposta do servidor: " + e.getMessage(), "Erro de Protocolo", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarSenhaUsuario() {
        UsuarioInfo selecionado = listaUsuarios.getSelectedValue();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String novaSenha = JOptionPane.showInputDialog(this, "Digite a nova senha para " + selecionado.getNome() + ":");
        if (novaSenha == null) return; // Cancelado

        // --- REMOVIDO ---
        // A validação de tamanho (min 3, max 20) foi removida.
        // O servidor (UsuarioServico) já faz essa validação e retornará 405 se for inválida.
        /*
        if (novaSenha.trim().length() < 3 || novaSenha.trim().length() > 20) {
            JOptionPane.showMessageDialog(this, "Senha deve ter entre 3 e 20 caracteres.", "Erro de Validação (405)", JOptionPane.ERROR_MESSAGE);
            return;
        }
        */

        JSONObject requisicao = new JSONObject();
        // Protocolo: "ADMIN_EDITAR_USUARIO"
        requisicao.put("operacao", "ADMIN_EDITAR_USUARIO");
        requisicao.put("token", this.token);
        // Protocolo: "id" no nível principal
        requisicao.put("id", String.valueOf(selecionado.getId()));

        // Protocolo: "usuario": {"senha": "..."}
        JSONObject usuarioJson = new JSONObject();
        usuarioJson.put("senha", novaSenha.trim());
        requisicao.put("usuario", usuarioJson);

        // --- CORREÇÃO ---
        // Chama o método auxiliar sem passar a mensagem de sucesso hardcoded
        enviarRequisicaoAdmin(requisicao);
    }

    private void excluirUsuario() {
        UsuarioInfo selecionado = listaUsuarios.getSelectedValue();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- REMOVIDO ---
        // A validação que impede a exclusão do 'admin' foi removida.
        // O servidor (UsuarioServico) já faz essa verificação e retornará 403 se for o admin.
        /*
        if ("admin".equalsIgnoreCase(selecionado.getNome())) {
            JOptionPane.showMessageDialog(this, "O usuário 'admin' não pode ser excluído.", "Ação Proibida (403)", JOptionPane.ERROR_MESSAGE);
            return;
        }
        */

        int resposta = JOptionPane.showConfirmDialog(
                this,
                "Tem certeza que deseja excluir o usuário " + selecionado.getNome() + "?",
                "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }

        JSONObject requisicao = new JSONObject();
        // Protocolo: "ADMIN_EXCLUIR_USUARIO"
        requisicao.put("operacao", "ADMIN_EXCLUIR_USUARIO");
        requisicao.put("token", this.token);
        // Protocolo: "id"
        requisicao.put("id", String.valueOf(selecionado.getId()));

        // --- CORREÇÃO ---
        // Chama o método auxiliar sem passar a mensagem de sucesso hardcoded
        enviarRequisicaoAdmin(requisicao);
    }

    // --- MÉTODO CORRIGIDO ---
    // Método auxiliar para enviar requisições e tratar respostas (200 ou Erro)
    // Removemos o parâmetro "msgSucesso", pois a mensagem virá do servidor.
    private void enviarRequisicaoAdmin(JSONObject requisicao) {
        try {
            String respostaJson = ServicoCliente.getInstancia().enviarRequisicao(requisicao.toString());
            JSONObject resp = new JSONObject(respostaJson);
            String status = resp.getString("status").trim();

            // CORREÇÃO: Lê a chave "mensagem" vinda do JSON de resposta.
            // O servidor envia essa chave tanto em caso de sucesso (200) quanto em caso de erro.
            String msg = resp.getString("mensagem");

            if ("200".equals(status)) {
                // Exibe a mensagem de sucesso (vinda do servidor)
                JOptionPane.showMessageDialog(this, msg, "Sucesso (" + status + ")", JOptionPane.INFORMATION_MESSAGE);
                carregarUsuarios(); // Recarrega a lista
            } else {
                // Exibe a mensagem de erro (vinda do servidor)
                // Protocolo: 401, 403, 404, 405, 422, 500
                JOptionPane.showMessageDialog(this, msg, "Erro (" + status + ")", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro de comunicação: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (JSONException e) {
            // Adicionado para tratar erros de parse do JSON ou falta das chaves "status"/"mensagem"
            JOptionPane.showMessageDialog(this, "Erro ao processar resposta do servidor: " + e.getMessage(), "Erro de Protocolo", JOptionPane.ERROR_MESSAGE);
        }
    }
}