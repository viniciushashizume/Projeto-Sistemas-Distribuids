package org.voteflix;
import org.voteflix.gui.TelaConexao;
import javax.swing.SwingUtilities;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                TelaConexao telaConexao = new TelaConexao();
                telaConexao.setVisible(true);
            });
        }
}