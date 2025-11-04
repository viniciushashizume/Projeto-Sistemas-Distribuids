Bibliotecas Necessárias (Dependências)

O projeto utiliza Maven para gerenciar as dependências. Todas as bibliotecas necessárias estão listadas no arquivo pom.xml e serão baixadas automaticamente pelo Maven.

As principais dependências são:

    org.json:json: Para manipulação de objetos JSON.

    io.jsonwebtoken (jjwt-api, jjwt-impl, jjwt-jackson): Para criação e validação de Tokens JWT.

    mysql:mysql-connector-java: Driver JDBC para conexão com o banco de dados MySQL.

Pré-requisitos para Execução

    Java JDK 23 ou superior.

    Apache Maven (para compilação e gerenciamento de dependências).

    Servidor MySQL.

Orientações de Uso (Configuração e Execução)

Siga estes passos para a correta execução do programa.

Passo 1: Configurar o Banco de Dados

    Inicie o servidor MySQL.

    Crie um novo banco de dados (schema) chamado voteflix.
    SQL
        comando: CREATE DATABASE voteflix;

Use o banco de dados voteflix e crie a tabela usuarios executando o script abaixo. A tabela usuarios é necessária para armazenar as credenciais.
SQL

    USE voteflix;

    CREATE TABLE usuarios (
        id INT AUTO_INCREMENT PRIMARY KEY,
        nome VARCHAR(255) NOT NULL UNIQUE,
        senha VARCHAR(255) NOT NULL
    );

Passo 2: Configurar Credenciais do Banco

    Abra o arquivo de configuração de conexão do banco no projeto: src/main/java/org/voteflix/util/ConexaoBancoDados.java

    Altere as constantes USUARIO_BD e SENHA_BD para corresponderem às credenciais do seu servidor MySQL. (O padrão no arquivo é root e 1234).
    Java

    // ...
    private static final String USUARIO_BD = "seu_usuario_mysql";
    private static final String SENHA_BD = "sua_senha_mysql";
    // ...

Passo 3: Compilar o Projeto

Navegue até o diretório raiz do projeto (onde o pom.xml está localizado) e execute o comando Maven para compilar o projeto e baixar as dependências:
Bash

mvn clean install

Passo 4: Executar o Servidor

O servidor deve ser iniciado antes de qualquer cliente.

Via Maven:
    Bash

    mvn exec:java -Dexec.mainClass="org.voteflix.servidor.Servidor"

Via IDE (IntelliJ, Eclipse, etc.):

    Localize a classe org.voteflix.servidor.Servidor e execute seu método main().

Ao iniciar, uma pequena janela (TelaConfiguracaoServidor) solicitará a porta que o servidor deve usar. O valor padrão é 20001. Clique em "Iniciar Servidor". Após isso, o painel principal do servidor aparecerá, pronto para receber conexões.

Passo 5: Executar o Cliente

Com o servidor rodando, você pode iniciar uma ou mais instâncias do cliente.

Via Maven (em um novo terminal):
Bash

mvn exec:java -Dexec.mainClass="org.voteflix.cliente.Main"

(Nota: O pom.xml não define uma classe principal. A classe org.voteflix.cliente.Main é o ponto de entrada do cliente, conforme visto nos arquivos-fonte. Se o comando acima falhar, use o org.voteflix.Main que aponta para o cliente).

Via IDE (IntelliJ, Eclipse, etc.):

    Localize a classe org.voteflix.cliente.Main (ou org.voteflix.Main) e execute seu método main().

Uso do Cliente:

    Tela de Conexão: A primeira tela pedirá o IP e a Porta do servidor.

        IP: Se estiver rodando na mesma máquina, use 127.0.0.1 ou localhost.

        Porta: Use a porta que você configurou no servidor (ex: 20001).

    Tela de Login: Após conectar, você pode "Logar" (se já tiver uma conta) ou "Cadastrar" (para criar uma nova conta).

    Tela Principal: Após o login bem-sucedido, você terá acesso às operações "Minha Conta", "Editar Minha Conta" e "Excluir Minha Conta".