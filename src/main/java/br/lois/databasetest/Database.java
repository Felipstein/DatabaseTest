package br.lois.databasetest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

	private Connection connection;

	public Database(String host, int port, String database, String user, String password, boolean useSSL) {
		Main.info("Iniciando conexão com o banco de dados...");
		Main.info("host: " + host + ", port: " + port + ", database: " + database + ", user: " + user + ", password: " + password + ", SSL: " + (useSSL ? "sim" : "não"));
		String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + String.valueOf(useSSL);
		Main.info("Acesso remoto ao URL \"" + url + "\".");
		try {
			this.connection = DriverManager.getConnection(url, user, password);
			Main.info("Conexão efetuada com êxito.");
			this.setupTables();
		} catch (SQLException e) {
			Main.error(e);
		}
	}

	private void setupTables() throws SQLException{
		Main.info("Inicializando tabelas (caso não existam)...");
		try(Statement stat = createStatement()) {
			stat.execute("create table if not exists registers(id int not null auto_increment, uuid char(36) not null unique, name varchar(16) not null unique, coins decimal not null default 0, tag varchar(24), msgcolor char(1), address char(15) not null, primary key(id))");
			Main.info("Tabela registros criada com êxito");
		}
	}

	public boolean execute(String sql) {
		Main.info("Executando SQL \"" + sql + "\".");
		try {
			return this.createStatement().execute(sql);
		} catch (SQLException e) {
			Main.error(e);
		}
		return false;
	}

	public ResultSet executeQuery(String sql) {
		Main.info("Executando um pedido SQL \"" + sql + "\".");
		try {
			return this.createStatement().executeQuery(sql);
		} catch (SQLException e) {
			Main.error(e);
		}
		return null;
	}

	public int executeUpdate(String sql) {
		Main.info("Executando uma atualização SQL \"" + sql + "\".");
		try {
			return this.createStatementUpdatable().executeUpdate(sql);
		} catch (SQLException e) {
			Main.error(e);
		}
		return -1;
	}

	public Statement createStatementUpdatable() throws SQLException{
		return connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}

	public Statement createStatement() throws SQLException {
		return connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	}

	/**
	 * Isso irá apagar a tabela registros e você perderá todos seus dados.
	 */
	public void deleteTable() throws SQLException {
		try(Statement stat = createStatement()) {
			stat.execute("drop table registers");
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public static Database getInstance() {
		return Main.getInstance().getDatabase();
	}

}