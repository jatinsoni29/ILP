package uk.ac.ed.inf;

import java.io.IOException;
import java.sql.*;

/**
 * Set up the connection to the given server
 *
 *
 */

public class SQLConnection {

    // Private variables
    private String ip;
    private String port;
    private Statement statement;
    private Connection conn;

    /**
     * HttpConnection constructor
     *
     * @param ip   - Server IP: localhost
     * @param port - User given port
     */
    public SQLConnection(String ip, String port) {
        this.ip = ip;
        this.port = port;
    }

    // Getters
    public String getServer() {
        return "jdbc:derby://" + this.ip + ":" + this.port;
    }

    public Statement getStatement() {
        return statement;
    }

    public Connection getConn() {
        return conn;
    }

    // Methods
    /**
     * build a connection with SQL database by connect jdbc string.
     *
     * @param jdbcString - The URL to connect to
     * @throws IOException If unable to connect to server, then exits system
     */
    public void connToSQL(String jdbcString) {
        try {
            this.conn = DriverManager.getConnection(jdbcString);
            this.statement = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param query
     * @return a ResultSet of the statement of the SQL query
     * @throws SQLException
     */
    public ResultSet executeQuery(String query) throws SQLException {
            return this.statement.executeQuery(query);
    }
}
