package com.meldiron.infinityparkour.libs;

import com.meldiron.infinityparkour.Main;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

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

    public interface SQLAction {
        void callback(ResultSet res) throws SQLException;
    }

    public interface SQLStatementAction {
        void callback(PreparedStatement st) throws SQLException;
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

    public void execAsync(String sql, SQLAction action) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                st = getConnection().prepareStatement(sql);
                res = st.executeQuery();

                ResultSet finalRes = res;
                PreparedStatement finalSt = st;
                ResultSet finalRes1 = res;
                asyncExecFinal(action, finalSt, finalRes1, finalRes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void exec(String sql, SQLAction action) {
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

    public void execAsync(String sql, SQLStatementAction statementAction, SQLAction action) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                st = getConnection().prepareStatement(sql);

                statementAction.callback(st);

                res = st.executeQuery();

                ResultSet finalRes = res;
                asyncExecFinal(action, st, res, finalRes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void asyncExecFinal(SQLAction action, PreparedStatement st, ResultSet res, ResultSet finalRes) {
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            try {
                action.callback(finalRes);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                closeQuery(st, res);
            }
        });
    }

    public void exec(String sql, SQLStatementAction statementAction, SQLAction action) {
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = getConnection().prepareStatement(sql);

            statementAction.callback(st);
            res = st.executeQuery();

            action.callback(res);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuery(st, res);
        }
    }

    public void execAsyncTest(String sql, SQLStatementAction statementAction, SQLAction action) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            PreparedStatement stL = null;
            PreparedStatement stR = null;

            ResultSet resL = null;
            ResultSet resR = null;

            logQuery(sql);

            try {

                Connection lc = getLocalConnection();
                Connection rc = getRemoteConnection();

                ResultSet resData = null;

                if (rc != null) {
                    stR = rc.prepareStatement(sql);
                    statementAction.callback(stR);
                    resR = stR.executeQuery();
                    resData = resR;
                }

                if (lc != null) {
                    stL = lc.prepareStatement(sql);
                    statementAction.callback(stL);
                    resL = stL.executeQuery();
                    resData = resL;
                }

                ResultSet finalRes = resData;
                PreparedStatement finalStL = stL;
                PreparedStatement finalStR = stR;
                ResultSet finalResL = resL;
                ResultSet finalResR = resR;
                asyncExecFinalAdvanced(action, finalStL, finalStR, finalResL, finalResR, finalRes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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

            if (rc != null) {
                stR = rc.prepareStatement(sql);
                statementAction.callback(stR);
                resR = stR.executeQuery();
                resData = resR;
            }

            if (lc != null) {
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

    public void execAsyncTest(String sql, SQLAction action) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            PreparedStatement stL = null;
            PreparedStatement stR = null;

            ResultSet resL = null;
            ResultSet resR = null;

            logQuery(sql);

            try {

                Connection lc = getLocalConnection();
                Connection rc = getRemoteConnection();

                ResultSet resData = null;

                if (rc != null) {
                    stR = rc.prepareStatement(sql);
                    resR = stR.executeQuery();
                    resData = resR;
                }

                if (lc != null) {
                    stL = lc.prepareStatement(sql);
                    resL = stL.executeQuery();
                    resData = resL;
                }

                ResultSet finalRes = resData;
                asyncExecFinalAdvanced(action, stL, stR, resL, resR, finalRes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void asyncExecFinalAdvanced(SQLAction action, PreparedStatement stL, PreparedStatement stR, ResultSet resL, ResultSet resR, ResultSet finalRes) {
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            try {
                action.callback(finalRes);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                closeQuery(stL, stR, resL, resR);
            }
        });
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

            if (rc != null) {
                stR = rc.prepareStatement(sql);
                resR = stR.executeQuery();
                resData = resR;
            }

            if (lc != null) {
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

    public interface SQLBoolAction {
        void callback(boolean success);
    }

    private void runHelper(String sql, SQLBoolAction action) {
        PreparedStatement st = null;

        try {
            st = getConnection().prepareStatement(sql);

            Boolean wasSuccess = st.execute();

            if(action != null) {
                action.callback(wasSuccess);
            }
        } catch (Exception e) {
            if(action != null) {
                action.callback(false);
            }

            e.printStackTrace();
        } finally {
            closeQuery(st);
        }
    }

    private void runHelper(String sql) {
        runHelper(sql, null);
    }

    private void runAdvancedHelper(String sql, SQLStatementAction statementAction, SQLBoolAction action) {
        PreparedStatement st = null;

        try {
            st = getConnection().prepareStatement(sql);

            statementAction.callback(st);

            Boolean wasSuccess = st.execute();

            if(action != null) {
                action.callback(wasSuccess);
            }
        } catch (Exception e) {
            if(action != null) {
                action.callback(false);
            }

            e.printStackTrace();
        } finally {
            closeQuery(st);
        }
    }

    private void runAdvancedHelper(String sql, SQLStatementAction statementAction) {
        runAdvancedHelper(sql, statementAction, null);
    }

    public void runAsync(String sql) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            runHelper(sql);
        });
    }

    public void runAsync(String sql, SQLBoolAction action) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            runHelper(sql, action);
        });
    }

    public void run(String sql) {
        runHelper(sql);
    }

    public void runAsync(String sql, SQLStatementAction statementAction) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            runAdvancedHelper(sql, statementAction);
        });
    }

    public void runAsync(String sql, SQLStatementAction statementAction, SQLBoolAction action) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            runAdvancedHelper(sql, statementAction, action);
        });
    }

    public void run(String sql, SQLStatementAction statementAction) {
        runAdvancedHelper(sql, statementAction);
    }

    private void runAsyncTest(String sql) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            logQuery(sql);

            runTestHelper(sql);
        });
    }

    private void runTest(String sql) {
        logQuery(sql);
        runTestHelper(sql);
    }

    private void runTestHelper(String sql) {
        PreparedStatement stL = null;
        PreparedStatement stR = null;

        try {
            Connection lc = getLocalConnection();
            Connection rc = getRemoteConnection();

            if (rc != null) {
                stR = rc.prepareStatement(sql);
                stR.execute();
            }


            if (lc != null) {
                stL = lc.prepareStatement(sql);
                stL.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuery(stL, stR);
        }
    }

    private void runAsyncTest(String sql, SQLStatementAction statementAction) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {

            logQuery(sql);

            runTestAdvancedHelper(sql, statementAction);
        });
    }

    private void runTest(String sql, SQLStatementAction statementAction) {

        logQuery(sql);

        runTestAdvancedHelper(sql, statementAction);
    }

    private void runTestAdvancedHelper(String sql, SQLStatementAction statementAction) {
        PreparedStatement stL = null;
        PreparedStatement stR = null;

        try {
            Connection lc = getLocalConnection();
            Connection rc = getRemoteConnection();

            if (rc != null) {
                stR = rc.prepareStatement(sql);
                statementAction.callback(stR);
                stR.execute();
            }


            if (lc != null) {
                stL = lc.prepareStatement(sql);
                statementAction.callback(stL);
                stL.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuery(stL, stR);
        }
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
                String[] queries = query.split(";");

                for(String queryToExec : queries) {
                    run(queryToExec);
                }
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
