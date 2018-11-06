package com.meldiron.infinityparkour.managers;

import com.meldiron.infinityparkour.Main;
import com.meldiron.infinityparkour.guis.InfinityParkourGUI;
import com.meldiron.infinityparkour.libs.SQL;
import com.meldiron.infinityparkour.utils;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreboardManager {
    private static ScoreboardManager ourInstance = new ScoreboardManager();
    public static ScoreboardManager getInstance() {
        return ourInstance;
    }

    private Main main;
    private SQL sql;
    
    private ScoreboardManager() {
        main = Main.getInstance();
        sql = SQL.getInstance();
    }

    public List<Map.Entry<String, Integer>> getTopFive() {
        HashMap<String, Integer> scoreboard = new HashMap<>();

        sql.exec(
                "SELECT score, username FROM scoreboard LIMIT ?",
                st -> {
                    st.setInt(1, 5);
                },
                res -> {
                    while (res.next()) {
                        Integer score = res.getInt("score");
                        String playerName = res.getString("username");

                        scoreboard.put(playerName, score);
                    }
                });

        List<Map.Entry<String, Integer>> sortedScoreboard = utils.entriesSortedByValues(scoreboard);
        return sortedScoreboard;
    }

    public void updateScore(Player p, Integer score) {
        sql.exec(
                "SELECT score FROM scoreboard WHERE uuid = ?",
                st -> {
                    st.setString(1, p.getUniqueId().toString());
                },
                res -> {
                    if(res.next()) {
                        // UPDATE

                        Integer oldScore = res.getInt("score");

                        if(score > oldScore) {
                            sql.run(
                                    "UPDATE scoreboard SET score = ?, username = ? WHERE uuid = ?",
                                    st -> {
                                        st.setInt(1, score);
                                        st.setString(2, p.getName());
                                        st.setString(3, p.getUniqueId().toString());
                                    }
                            );

                            InfinityParkourGUI.refresh();
                            p.sendMessage(main.color(true, main.lang.getString("chat.newRecord")));

                        }

                    } else {
                        // INSERT

                        sql.run(
                                "INSERT INTO scoreboard (score, username, uuid) VALUES (?,?,?)",
                                st -> {
                                    st.setInt(1, score);
                                    st.setString(2, p.getName());
                                    st.setString(3, p.getUniqueId().toString());
                                }
                        );

                        InfinityParkourGUI.refresh();
                        p.sendMessage(main.color(true, main.lang.getString("chat.newRecord")));

                    }
                }
        );
    }

    public HashMap<String, Integer> getStatsByPlayer(Player p) {
        try {
            HashMap<String, Integer> stats = new HashMap<>();

            final boolean[] isEmpty = {false};

            sql.exec(
                    "SELECT score FROM scoreboard WHERE uuid = ?",
                    st -> {
                        st.setString(1, p.getUniqueId().toString());
                    },
                    res -> {
                        if(!res.next()) {
                            isEmpty[0] = true;
                            return;
                        }

                        stats.put("score", res.getInt("score"));
                    });

            if(isEmpty[0] == true) {
                return null;
            }

            sql.exec(
                    "SELECT COUNT(*) AS place FROM scoreboard WHERE score >= (SELECT score FROM scoreboard WHERE uuid = ?)",
                    st -> {
                        st.setString(1, p.getUniqueId().toString());
                    },
                    res -> {
                        if(!res.next()) {
                            isEmpty[0] = true;
                            return;
                        }

                        stats.put("place", res.getInt("place"));
                    });

            if(isEmpty[0] == true) {
                return null;
            }

            sql.exec("SELECT COUNT(*) AS total FROM scoreboard", res -> {
                if(!res.next()) {
                    isEmpty[0] = true;
                    return;
                }

                stats.put("total", res.getInt("total"));
            });

            if(isEmpty[0] == true) {
                return null;
            }

            double percentile =  stats.get("place") * 100.0 / stats.get("total");

            stats.put("topPerc", (int) percentile);

            return stats;
        }
        catch (NullPointerException exp) {
            return null;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
