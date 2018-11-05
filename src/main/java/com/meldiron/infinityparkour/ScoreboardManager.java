package com.meldiron.infinityparkour;

import com.meldiron.infinityparkour.guis.InfinityParkourGUI;
import com.meldiron.infinityparkour.libs.MySQL;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreboardManager {
    private static ScoreboardManager ourInstance = new ScoreboardManager();
    public static ScoreboardManager getInstance() {
        return ourInstance;
    }

    private Main main;
    private MySQL sql;
    
    private ScoreboardManager() {
        main = Main.getInstance();
        sql = MySQL.getInstance();
    }

    public List<Map.Entry<String, Integer>> getLeatherboard() {
        HashMap<String, Integer> scoreboard = new HashMap<>();

        try {
            PreparedStatement ps = sql.getStatement("SELECT score, username FROM scoreboard");
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                Integer score = res.getInt("score");
                String playerName = res.getString("username");

                scoreboard.put(playerName, score);
            }

            res.close();
            ps.close();

            List<Map.Entry<String, Integer>> sortedScoreboard = utils.entriesSortedByValues(scoreboard);
            return sortedScoreboard;
        } catch (Exception exp) {
            exp.printStackTrace();
        }

        return null;
    }

    public List<Map.Entry<String, Integer>> getTopFive() {
        HashMap<String, Integer> scoreboard = new HashMap<>();

        try {
            PreparedStatement ps = sql.getConnection().prepareStatement("SELECT score, username FROM scoreboard LIMIT ?");
            ps.setInt(1, 5);

            ResultSet res = ps.executeQuery();
            while (res.next()) {
                Integer score = res.getInt("score");
                String playerName = res.getString("username");

                scoreboard.put(playerName, score);
            }

            res.close();
            ps.close();

            List<Map.Entry<String, Integer>> sortedScoreboard = utils.entriesSortedByValues(scoreboard);
            return sortedScoreboard;
        } catch (Exception exp) {
            exp.printStackTrace();
        }

        return null;
    }

    public void updateScore(Player p, Integer score) {
        try {
            PreparedStatement getScoreStatement = sql.getStatement("SELECT score FROM scoreboard WHERE uuid = ?");
            getScoreStatement.setString(1, p.getUniqueId().toString());
            ResultSet playerScoreSet = getScoreStatement.executeQuery();

            if (!playerScoreSet.next() ) {
                playerScoreSet.close();
                getScoreStatement.close();
                // NO DATA
                PreparedStatement insertScoreStatement = sql.getStatement("INSERT INTO SCOREBOARD (score, username, uuid) VALUES (?,?,?)");
                insertScoreStatement.setInt(1, score);
                insertScoreStatement.setString(2, p.getName());
                insertScoreStatement.setString(3, p.getUniqueId().toString());

                insertScoreStatement.executeUpdate();

                InfinityParkourGUI.getInstance().refresh();
                p.sendMessage(main.color(true, main.lang.getString("chat.newRecord")));

                insertScoreStatement.close();
            } else {
                Integer playerScore = playerScoreSet.getInt("score");

                playerScoreSet.close();
                getScoreStatement.close();

                if(score > playerScore) {
                    PreparedStatement updateScoreStatement = sql.getStatement("UPDATE scoreboard SET score = ?, username = ? WHERE uuid = ?");
                    updateScoreStatement.setInt(1, score);
                    updateScoreStatement.setString(2, p.getName());
                    updateScoreStatement.setString(3, p.getUniqueId().toString());
                    updateScoreStatement.executeUpdate();
                    updateScoreStatement.close();

                    InfinityParkourGUI.getInstance().refresh();
                    p.sendMessage(main.color(true, main.lang.getString("chat.newRecord")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, Object> getStatsByPlayer(Player p) {
        try {
            HashMap<String, Object> stats = new HashMap<>();

            PreparedStatement scoreStatement = sql.getStatement("SELECT score FROM scoreboard WHERE uuid = ?");
            scoreStatement.setString(1, p.getUniqueId().toString());
            ResultSet scoreRes = scoreStatement.executeQuery();

            if(!scoreRes.next()) {
                scoreRes.close();
                scoreStatement.close();
                return null;
            }

            Integer statsScore = scoreRes.getInt("score");
            scoreRes.close();
            scoreStatement.close();
            
            PreparedStatement placeStatement = sql.getStatement("SELECT COUNT(*) AS place FROM scoreboard WHERE score >= (SELECT score FROM scoreboard WHERE uuid = ?)");
            placeStatement.setString(1, p.getUniqueId().toString());
            ResultSet placeRes = placeStatement.executeQuery();

            if(!placeRes.next()) {
                placeRes.close();
                placeStatement.close();
                return null;
            }

            Integer statsPlace = placeRes.getInt("place");
            placeRes.close();
            placeStatement.close();

            PreparedStatement totalResStatement = sql.getStatement("SELECT COUNT(*) AS total FROM scoreboard");
            ResultSet totalRes = totalResStatement.executeQuery();

            if(!totalRes.next()) {
                totalRes.close();
                totalResStatement.close();
                return null;
            }

            Integer statsTotal = totalRes.getInt("total");
            double percentile =  statsPlace * 100.0 / statsTotal;

            totalRes.close();
            totalResStatement.close();

            stats.put("score", statsScore);
            stats.put("place", statsPlace);
            stats.put("total", statsTotal);
            stats.put("topPerc", (int) percentile);

            return stats;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
