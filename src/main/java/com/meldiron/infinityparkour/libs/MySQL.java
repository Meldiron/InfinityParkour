package com.meldiron.infinityparkour.libs;

import com.meldiron.infinityparkour.Main;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Scanner;

public class MySQL {
    private static final MySQL mysql = new MySQL();

    private MySQL() {
    }

    public static MySQL getInstance() {
        return mysql;
    }

    public static boolean usingRemote = false;

    private static Connection connection;
    private static String host, database, username, password;
    private static int port;

    public Connection getConnection() throws Exception {
        if(connection == null || connection.isClosed()) {
            connect();
            return connection;
        }

        return connection;
    }

    public void connectLite() {
        usingRemote = false;
        Bukkit.getLogger().info("MySQL disabled in config.yml! Using SQLite file");

        try {
            openSqliteConnection();
            Bukkit.getLogger().info("SQLite connection success");
        } catch (Exception eLite) {
            Bukkit.getLogger().info("Cant use SQLite file. Stopping plugin.");
            eLite.printStackTrace();
            Bukkit.getServer().getPluginManager().disablePlugin(Main.getInstance());
        } finally {
            initDatabase();
        }
    }

    public void connect() {
        try {
            openMysqlConnection();
            usingRemote = true;
        } catch (Exception EMySql) {
            usingRemote = false;
            Bukkit.getLogger().info("MySQL could not be connected ! Check your config.yml! Using fallback SQLite file");

            try {
                openSqliteConnection();

                Bukkit.getLogger().info("Fallback to SQLite success");
            } catch (Exception eLite) {
                Bukkit.getLogger().info("Cant fallback to SQLite file. Stopping plugin.");
                eLite.printStackTrace();
                Bukkit.getServer().getPluginManager().disablePlugin(Main.getInstance());
            }

        } finally {
            initDatabase();
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

    private void openSqliteConnection() throws SQLException, ClassNotFoundException {
        synchronized (this) {
            Class.forName("org.sqlite.JDBC");
            File folder = new File(Main.getInstance().getDataFolder(), "database.db");
            connection = (Connection) DriverManager.getConnection("jdbc:sqlite:" + folder);
        }
    }

    private void openMysqlConnection() throws SQLException, ClassNotFoundException {
        synchronized (this) {
            Class.forName("org.sqlite.JDBC");
            connection = (Connection) DriverManager.getConnection(
                    "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
        }
    }

    private void initDatabase() {
        String query = getInitQuery();
        if(query != null) {
            run(query);
        }
    }

    private String getInitQuery() {
        try {
            InputStreamReader readStream = new InputStreamReader(Main.getInstance().getResource("init.sql"), "UTF8");

            Scanner s = new Scanner(readStream).useDelimiter("\\A");
            String query = s.hasNext() ? s.next() : "";

            if(usingRemote == true) {
                query = query.replace("AUTOINCREMENT", "AUTO_INCREMENT");
                query = query.replace("INTEGER", "INT");
            }

           return query;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public ResultSet exec(String sql) {
        try {
            return getConnection().prepareStatement(sql).executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean run(String sql) {
        try {
            getConnection().prepareStatement(sql).execute();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public PreparedStatement getStatement(String sql) {
        try {
            return getConnection().prepareStatement(sql);
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
