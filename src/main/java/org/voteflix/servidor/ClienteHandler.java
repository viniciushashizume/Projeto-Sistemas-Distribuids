package org.voteflix.servidor;

import org.json.JSONObject;
import org.voteflix.servidor.servico.FilmeServico; // NOVO IMPORT
import org.voteflix.servidor.servico.UsuarioServico;
import org.voteflix.servidor.gui.TelaServidor;
import org.voteflix.util.ProtocoloMensagem; // Importar o Enum
import org.voteflix.servidor.servico.ReviewServico; // NOVO IMPORT

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ClienteHandler extends Thread {

    private final Socket clienteSocket;
    private final UsuarioServico usuarioServico;
    private final TelaServidor telaServidor;
    private final FilmeServico filmeServico;
    private final ReviewServico reviewServico; // NOVO
    private String nomeUsuarioLogado = null;

    public ClienteHandler(Socket socket, TelaServidor tela) {
        this.clienteSocket = socket;
        this.usuarioServico = new UsuarioServico();
        this.telaServidor = tela;
        this.filmeServico = new FilmeServico();
        this.reviewServico = new ReviewServico();
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clienteSocket.getOutputStream(), true)
        ) {
            String requisicaoJson;
            while ((requisicaoJson = in.readLine()) != null) {
                log("Requisição recebida de " + clienteSocket.getInetAddress() + ": " + requisicaoJson);
                JSONObject resposta = processarRequisicao(new JSONObject(requisicaoJson));
                out.println(resposta.toString());
                log("Resposta enviada para " + clienteSocket.getInetAddress() + ": " + resposta.toString());

                JSONObject requisicao = new JSONObject(requisicaoJson);
                if ("LOGOUT".equals(requisicao.optString("operacao"))) {
                    break;
                }
            }
        } catch (SocketException e) {
            log("Conexão com o cliente " + clienteSocket.getInetAddress() + " foi perdida ou fechada abruptamente.");
        } catch (IOException e) {
            log("Erro de comunicação com o cliente " + clienteSocket.getInetAddress() + ": " + e.getMessage());
        } finally {
            if (nomeUsuarioLogado != null) {
                Servidor.removerUsuarioAtivo(nomeUsuarioLogado);
            }
            try {
                clienteSocket.close();
                log("Conexão com o cliente " + clienteSocket.getInetAddress() + " fechada.");
            } catch (IOException e) {
                log("Erro ao fechar o socket do cliente " + clienteSocket.getInetAddress() + ": " + e.getMessage());
            }
        }
    }

    private JSONObject processarRequisicao(JSONObject requisicao) {
        try {
            String operacao = requisicao.getString("operacao");
            JSONObject resposta;

            switch (operacao) {
                case "LOGIN":
                    resposta = usuarioServico.realizarLogin(requisicao);
                    if ("200".equals(resposta.optString("status").trim())) {
                        this.nomeUsuarioLogado = requisicao.getString("usuario");
                    }
                    return resposta;
                case "LOGOUT":
                    resposta = usuarioServico.realizarLogout(requisicao);
                    this.nomeUsuarioLogado = null;
                    return resposta;
                case "CRIAR_USUARIO":
                    return usuarioServico.criarUsuario(requisicao);
                case "EDITAR_PROPRIO_USUARIO":
                    return usuarioServico.editarProprioUsuario(requisicao);
                case "EXCLUIR_PROPRIO_USUARIO":
                    this.nomeUsuarioLogado = null;
                    return usuarioServico.excluirProprioUsuario(requisicao);
                case "LISTAR_PROPRIO_USUARIO":
                    return usuarioServico.listarProprioUsuario(requisicao);
                // --- NOVAS Operações de Admin (Usuário) ---
                case "LISTAR_USUARIOS":
                    return usuarioServico.listarTodosUsuarios(requisicao);
                case "ADMIN_EDITAR_USUARIO":
                    return usuarioServico.adminEditarUsuario(requisicao);
                case "ADMIN_EXCLUIR_USUARIO":
                    return usuarioServico.adminExcluirUsuario(requisicao);
                // --- NOVAS Operações de Filme (Admin e Comum) ---
                case "CRIAR_FILME":
                    return filmeServico.criarFilme(requisicao);
                case "LISTAR_FILMES":
                    return filmeServico.listarFilmes(requisicao);
                case "EDITAR_FILME":
                    return filmeServico.editarFilme(requisicao);
                case "EXCLUIR_FILME":
                    return filmeServico.excluirFilme(requisicao);
                // --- REVIEWS (NOVO) ---
                case "CRIAR_REVIEW":
                    return reviewServico.criarReview(requisicao);
                case "EDITAR_REVIEW":
                    return reviewServico.editarReview(requisicao);
                case "EXCLUIR_REVIEW":
                    return reviewServico.excluirReview(requisicao);
                case "BUSCAR_FILME_ID":
                    return filmeServico.buscarFilmePorId(requisicao);
                default:
                    resposta = new JSONObject();
                    ProtocoloMensagem.ERRO_OPERACAO_INVALIDA.aplicar(resposta); // 400
                    return resposta;
            }
        } catch (Exception e) {
            log("Erro interno ao processar requisição: " + e.getMessage());
            JSONObject resposta = new JSONObject();
            ProtocoloMensagem.ERRO_FALHA_INTERNA.aplicar(resposta); // 500
            return resposta;
        }
    }

    private void log(String mensagem) {
        if (telaServidor != null) {
            telaServidor.adicionarLog(mensagem);
        }
        System.out.println(mensagem);
    }
}