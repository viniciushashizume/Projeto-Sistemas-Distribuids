package org.voteflix.cliente;
import org.voteflix.cliente.gui.TelaConexao;
import javax.swing.SwingUtilities;
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                TelaConexao telaConexao = new TelaConexao();
                telaConexao.setVisible(true);
            });
        }
}