package org.voteflix.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBancoDados {
    // ATENÇÃO: Configure com os dados do seu banco de dados MySQL
    // O parâmetro 'serverTimezone=UTC' é importante para evitar erros de fuso horário.
    private static final String URL = "jdbc:mysql://localhost:3306/voteflix?serverTimezone=UTC";
    private static final String USUARIO_BD = "root"; // Ou o usuário que você configurou
    private static final String SENHA_BD = "1234";

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO_BD, SENHA_BD);
    }
}