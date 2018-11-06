package com.meldiron.infinityparkour.libs;

import com.meldiron.infinityparkour.Main;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Scanner;

public class SQL {
    private static final SQL mysql = new SQL();

    private SQL() {
    }

    public static SQL getInstance() {
        return mysql;
    }


    private static Connection connectionLocal;
    private static Connection connectionRemote;

    private static String host, database, username, password;
    private static int port;
    private static boolean isTesting = false; // Will use both MySQL and SQLite

    public interface SQLAction {
        void callback(ResultSet res) throws SQLException;
    }

    public interface SQLStatementAction {
        void callback(PreparedStatement st) throws SQLException;
    }

    public void setTesting(boolean isTesting) {
        SQL.isTesting = isTesting;
    }

    public Connection getLocalConnection() throws Exception {
        if(connectionLocal != null && !connectionLocal.isClosed()) {
            return connectionLocal;
        }

        return null;
    }

    public Connection getRemoteConnection() throws Exception {
        if(connectionLocal != null && !connectionLocal.isClosed()) {
            return connectionLocal;
        }

        return null;
    }

    public Connection getConnection() throws Exception {
        if(connectionLocal != null && !connectionLocal.isClosed()) {
            return connectionLocal;
        } else if(connectionRemote != null && !connectionRemote.isClosed()) {
            return connectionRemote;
        } else {
            connect();
            return getConnection(0);
        }
    }

    public Connection getConnection(Integer tryNumber) throws Exception {
        if(tryNumber >= 5) {
            return null;
        }

        if(connectionLocal != null && !connectionLocal.isClosed()) {
            return connectionLocal;
        } else if(connectionRemote != null && !connectionRemote.isClosed()) {
            return connectionRemote;
        } else {
            connect();
            return getConnection(tryNumber + 1);
        }
    }

    public void connectLite() {
        Bukkit.getLogger().info("SQL disabled in config.yml! Using SQLite file");

        try {
            openSqliteConnection();
            initDatabase(false);

            Bukkit.getLogger().info("SQLite connection success");
        } catch (Exception eLite) {
            Bukkit.getLogger().info("Cant use SQLite file. Stopping plugin.");
            eLite.printStackTrace();
            Bukkit.getServer().getPluginManager().disablePlugin(Main.getInstance());
        }
    }

    public void connect() {
        try {
            openMysqlConnection();

            initDatabase(true);

            if(SQL.isTesting == true) {
                connectLite();
            }
        } catch (Exception EMySql) {
            Bukkit.getLogger().info("SQL could not be connected ! Check your config.yml! Using fallback SQLite file");

            try {
                openSqliteConnection();

                initDatabase(false);

                Bukkit.getLogger().info("Fallback to SQLite success");
            } catch (Exception eLite) {
                Bukkit.getLogger().info("Cant fallback to SQLite file. Stopping plugin.");
                eLite.printStackTrace();
                Bukkit.getServer().getPluginManager().disablePlugin(Main.getInstance());
            }

        }
    }

    public void connect(String host, String database, String username, String password, int port) {
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;
        this.port = port;

        connect();
    }

    public void exec(String sql, SQLAction action) {
        if(SQL.isTesting == true) {
            execTest(sql, action);
            return;
        }

        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = getConnection().prepareStatement(sql);
            res = st.executeQuery();

            action.callback(res);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuery(st, res);
        }
    }

    public void exec(String sql, SQLStatementAction statementAction, SQLAction action) {
        if(SQL.isTesting == true) {
            execTest(sql, statementAction, action);
            return;
        }

        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = getConnection().prepareStatement(sql);

            statementAction.callback(st);

            if(SQL.isTesting == true) {
                Bukkit.getLogger().info(st.toString());
            }

            res = st.executeQuery();

            action.callback(res);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuery(st, res);
        }
    }

    public void execTest(String sql, SQLStatementAction statementAction, SQLAction action) {
        PreparedStatement stL = null;
        PreparedStatement stR = null;

        ResultSet resL = null;
        ResultSet resR = null;

        logQuery(sql);

        try {

            Connection lc = getLocalConnection();
            Connection rc = getRemoteConnection();

            ResultSet resData = null;

            if(rc != null) {
                stR = rc.prepareStatement(sql);
                statementAction.callback(stR);
                resR = stR.executeQuery();
                resData = resR;
            }

            if(lc != null) {
                stL = lc.prepareStatement(sql);
                statementAction.callback(stL);
                resL = stL.executeQuery();
                resData = resL;
            }

            action.callback(resData);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuery(stL, stR, resL, resR);
        }
    }

    public void execTest(String sql, SQLAction action) {
        PreparedStatement stL = null;
        PreparedStatement stR = null;

        ResultSet resL = null;
        ResultSet resR = null;

        logQuery(sql);

        try {

            Connection lc = getLocalConnection();
            Connection rc = getRemoteConnection();

            ResultSet resData = null;

            if(rc != null) {
                stR = rc.prepareStatement(sql);
                resR = stR.executeQuery();
                resData = resR;
            }

            if(lc != null) {
                stL = lc.prepareStatement(sql);
                resL = stL.executeQuery();
                resData = resL;
            }

            action.callback(resData);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuery(stL, stR, resL, resR);
        }
    }

    public boolean run(String sql) {
        if(SQL.isTesting == true) {
            return runTest(sql);
        }

        PreparedStatement st = null;

        try {
            st = getConnection().prepareStatement(sql);

            return st.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuery(st);
        }

        return false;
    }

    public boolean run(String sql, SQLStatementAction statementAction) {
        if(SQL.isTesting == true) {
            return runTest(sql, statementAction);
        }

        PreparedStatement st = null;

        try {
            st = getConnection().prepareStatement(sql);

            statementAction.callback(st);

            return st.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuery(st);
        }

        return false;
    }

    private boolean runTest(String sql) {
        PreparedStatement stL = null;
        PreparedStatement stR = null;

        logQuery(sql);

        try {
            Connection lc = getLocalConnection();
            Connection rc = getRemoteConnection();

            boolean wasSuccess = false;

            if(rc != null) {
                stR = rc.prepareStatement(sql);
                wasSuccess = stR.execute();
            }


            if(lc != null) {
                stL = lc.prepareStatement(sql);
                wasSuccess = stL.execute();
            }

            return wasSuccess;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuery(stL, stR);
        }

        return false;
    }

    private boolean runTest(String sql, SQLStatementAction statementAction) {
        PreparedStatement stL = null;
        PreparedStatement stR = null;

        logQuery(sql);

        try {
            Connection lc = getLocalConnection();
            Connection rc = getRemoteConnection();

            boolean wasSuccess = false;

            if(rc != null) {
                stR = rc.prepareStatement(sql);
                statementAction.callback(stR);
                wasSuccess = stR.execute();
            }


            if(lc != null) {
                stL = lc.prepareStatement(sql);
                statementAction.callback(stL);
                wasSuccess = stL.execute();
            }

            return wasSuccess;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuery(stL, stR);
        }

        return false;
    }

    private void logQuery(String q) {
        Bukkit.getLogger().info("[SQL]  " + q);
    }

    private void closeQuery(PreparedStatement st) {
        try {
            if(st != null) {
                st.close();
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    private void closeQuery(PreparedStatement st, ResultSet res) {
        try {
            if(st != null) {
                st.close();
            }

            if(res != null) {
                res.close();
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    private void closeQuery(PreparedStatement stL, PreparedStatement stR) {
        try {
            if(stL != null) {
                stL.close();
            }
            if(stR != null) {
                stR.close();
            }

        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    private void closeQuery(PreparedStatement stL, PreparedStatement stR, ResultSet resL, ResultSet resR) {
        try {
            if(stL != null) {
                stL.close();
            }

            if(stR != null) {
                stR.close();
            }

            if(resL != null) {
                resL.close();
            }

            if(resR != null) {
                resR.close();
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    private void openSqliteConnection() throws SQLException, ClassNotFoundException {
        synchronized (this) {
            Class.forName("org.sqlite.JDBC");
            File folder = new File(Main.getInstance().getDataFolder(), "database.db");
            connectionLocal = (Connection) DriverManager.getConnection("jdbc:sqlite:" + folder);
        }
    }

    private void openMysqlConnection() throws SQLException, ClassNotFoundException {
        synchronized (this) {
            Class.forName("com.mysql.jdbc.Driver");
            connectionRemote = (Connection) DriverManager.getConnection(
                    "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
        }
    }

    private void initDatabase(boolean usingRemote) {
        String query = getInitQuery(usingRemote);
        if(query != null) {
            try {
                run(query);
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        }
    }

    private String getInitQuery(boolean usingRemote) {
        try {
            InputStreamReader readStream = new InputStreamReader(Main.getInstance().getResource("init.sql"), "UTF8");

            Scanner s = new Scanner(readStream).useDelimiter("\\A");
            String query = s.hasNext() ? s.next() : "";

            if(usingRemote == false) {
                query = query.replace("AUTO_INCREMENT", "AUTOINCREMENT");
                query = query.replace("INT", "INTEGER");
            }

            return query;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
