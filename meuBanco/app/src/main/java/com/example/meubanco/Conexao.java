package com.example.meubanco;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * Classe responsavel pela interacao da aplicacao com o banco de dados
 */
public class Conexao {
    private String servidor = "com.mysql.cj.jdbc.Driver";
    // url que identifica o banco
    private String urlBanco = "jdbc:mysql://localhost:3306/teste";
    // login de usuario no banco
    private String usuarioBanco = "root";
    // senha do usuario no banco
    private String senhaBanco = "root";
    private Statement statement;
    Connection con = null;
    

    public boolean conectar() {
       boolean ret = false;
        try {
            Class.forName(servidor);
            con = DriverManager.getConnection(urlBanco, usuarioBanco, senhaBanco);
            ret = true;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();// se ClassNotFoundException o servidor (MySql) n�o foi encontrado
        } catch (SQLException e) {
            e.printStackTrace(); // se der outro erro, apresenta a descri��o do erro utilizando 
            // a classe SQLException que trata de erros scripts slq
        }
    return ret;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    public Connection getCon() {
        return con;
    }

    public void setCon(Connection con) {
        this.con = con;
    }
    
   
  

    // *** Metodo de teste de conexao
    public boolean testaConexao() {
        // Declaracao e inicializacao de uma variavel de retorno ("true" se conectou ou "false" se ocorreu erro)
        boolean ret = false;
		// Declaracao e inicializacao de uma variavel do tipo Connection que armazenara a conexao estabelecida
        //Connection con = null;

        // Tenta executar as instrucoes em try
        try {
            // Quando o m�todo est�tico Class.forName() � utilizado, o Class Loader tenta inicializar esta classe. 
            // Esta classe (que � o nosso driver jdbc) possui um bloco inicializador est�tico que ir� registrar 
            // essa classe como um driver JDBC, avisando o java.sql.DriverManager, pelo m�todo registerDriver.			
            Class.forName(servidor);

            // A classe DriverManager abre uma conexao com o banco de dados.
            // A classe Connection designa um objeto, no caso con, para receber a conexao estabelecida
            con = DriverManager.getConnection(urlBanco, usuarioBanco, senhaBanco);

            // Seta a variavel de retorno com true
            ret = true;

            // Se der erro no try verifica qual erro foi gerado
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            // se ClassNotFoundException o servidor (MySql) n�o foi encontrado, 	

        } catch (SQLException e) {
            e.printStackTrace();
            // se der outro erro, apresenta a descri��o do erro utilizando 
            // a classe SQLException que trata de erros scripts slq
        }

        return ret;
    }

    // *** Metodo para consulta geral de usuarios - listando em MessageDialog
    public void Consultas(String strSQL) {

        Connection con = null;

        try {
            Class.forName(servidor);
            con = DriverManager.getConnection(urlBanco, usuarioBanco, senhaBanco);

            Statement stmt = con.createStatement();
            String sql = strSQL;

            //A classe ResultSet permite colher os resultados da execucao de uma query no banco de dados. 
            //Esta classe apresenta uma serie de metodos para prover o acesso aos dados
            ResultSet rs = stmt.executeQuery(sql);

            String relacao = "Rela��o de usuarios cadastrados \n";
            while (rs.next()) {
                // A rs tem o formato de uma matriz onde o retorno de uma consulta pode ser tratado 
                // como muma tabela. O metodo next pula para a proxima linha.
                // Os metodos de acesso aos dados contidos na rs sao gets especificos para o tipo de dado armazenado. 
                // Identifica-se o campo desejado pelo seu referente indice (coluna) na linha acessada
                relacao = relacao + "\n Cod: " + rs.getString(1).toString() + " - Nome: " + rs.getString(2) + " - Email: " + rs.getString(3) + " - Tipo: " + rs.getString(4) + " - Login: " + rs.getString(5) + " - Senha: " + rs.getString(6);
                // Observe que rs.getString(1) equivale ao codigo que por sua vez eh int, mas, como a intencao 
                // eh montar uma String de mensagem o dados pode ser recuperado ja com seu tipo convertido
            }
            rs.close();
            // Apresenta o resultado da consulta
            //JOptionPane.showMessageDialog(null, relacao + "\n");			

        } catch (ClassNotFoundException e) {
            e.printStackTrace();

        } catch (SQLException e) {
            e.printStackTrace();

            /*	
             Eh importante liberar os recursos alocados pelo banco de dados para a execucao da manipulacao dos dados. 
             Podemos fazer isso fechando o Statement, que libera os recursos associados a execucao desta consulta 
             mas deixa a conexao aberta para a execucao de uma proxima consulta, ou fechando diretamente a conexao, 
             que encerra a comunicacao com o banco de dados. 
             Para termos certeza de que vamos encerrar esta conexao mesmo que uma excecao ocorra, 
             reservamos o fechamento para a clausula finally() do tratamento de excecoes.
             */
        } finally {

            try {
                con.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    // *** Metodo de inclusao de clientes
    public void Incluir(Cliente cliente) {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            Class.forName(servidor);
            con = DriverManager.getConnection(urlBanco, usuarioBanco, senhaBanco);

            // O campo usuCod na tabela usuarios eh do tipo int. 
            // Para inserir um novo usuario eh necessario obter o proximo codigo
            // A funcao max() retorna o maior valor da coluna especificada
            // (nesse caso o ultimo codigo armazenado)
            Statement stmt = con.createStatement();
            String sql = "select max(id) from cliente";
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            // Incrementando o ultimo codigo armazenado temos o codigo do novo usuario
            int proximoCodigo = rs.getInt(1) + 1;
            rs.close();

            // Aqui utilizamos a classe PreparedStatement que permite a insercao de 
            // parametros (?) na construcao da string de SQL
            String sqlInsert = "insert into cliente values(?, ?, ?, ?, ?);";

            ps = con.prepareStatement(sqlInsert);
            ps.setInt(1, proximoCodigo);
            ps.setString(2, cliente.getNome());
            ps.setString(3, cliente.getCpf());
            ps.setString(4, cliente.getEmail());
            ps.setString(5, cliente.getCelular());
            ps.executeUpdate();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {

            try {
                ps.close();
                con.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    // *** Metodo para pesquisa de um determinado usuario
    public Cliente Pesquisar(Cliente cli) {

        Connection con = null;
        try {
            Class.forName(servidor);
            con = DriverManager.getConnection(urlBanco, usuarioBanco, senhaBanco);

            Statement stmt = con.createStatement();
            String sql = "select * from usuario where id = " + cli.getIdCliente();

            ResultSet rs = stmt.executeQuery(sql);
            rs.next();

            //cli.setIdCliente(rs.getInt(1));
            cli.setNome(rs.getString(2));
            cli.setEmail(rs.getString(3));
            cli.setCelular(rs.getNString(4));
            cli.setCpf(rs.getString(5));



            //usu.setLogin(rs.getString(5));
            //usu.setSenha(rs.getString(6));	

            rs.close();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {

            try {
                con.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return cli;
    }

    // *** Metodo para alteracao de usuarios
    public void Alterar(Cliente cli) {

        Connection con = null;
        PreparedStatement ps = null;

        try {
            Class.forName(servidor);
            con = DriverManager.getConnection(urlBanco, usuarioBanco, senhaBanco);

            String sqlUpdate = "update usuarios set usuNome = ?, usuCpf = ?, usuRg = ? where usuCod = ?;";

            ps = con.prepareStatement(sqlUpdate);
            ps.setString(1, cli.getNome());
            ps.setString(2, cli.getCpf());
            ps.setString(3, cli.getEmail());
            ps.setString(4,cli.getCelular());
            //ps.setString(4, usu.getLogin());
            //ps.setString(5, usu.getSenha());
            //ps.setString(6, Integer.toString(usu.getCodigo()));
            ps.executeUpdate();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {

            try {
                ps.close();
                con.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    // *** Metodo para exclusao de usuarios	
    public void Excluir(Cliente cli) {

        Connection con = null;
        PreparedStatement ps = null;

        try {
            Class.forName(servidor);
            con = DriverManager.getConnection(urlBanco, usuarioBanco, senhaBanco);

            String sqlDelete = "delete from usuarios where cliCod = ?;";

            ps = con.prepareStatement(sqlDelete);
            //ps.setString(1, Integer.toString(cli.getCodigo()));				
            ps.executeUpdate();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {

            try {
                ps.close();
                con.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public void Consultar() {
        // TODO Auto-generated method stub

    }

}
