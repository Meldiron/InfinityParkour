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
        sql.execAsync(
                "SELECT score FROM scoreboard WHERE uuid = ?",
                st -> {
                    st.setString(1, p.getUniqueId().toString());
                },
                res -> {
                    if(res.next()) {
                        // UPDATE

                        Integer oldScore = res.getInt("score");

                        if(score > oldScore) {
                            sql.runAsync(
                                    "UPDATE scoreboard SET score = ?, username = ? WHERE uuid = ?",
                                    st -> {
                                        st.setInt(1, score);
                                        st.setString(2, p.getName());
                                        st.setString(3, p.getUniqueId().toString());
                                    },
                                    resU -> {
                                        InfinityParkourGUI.refresh();
                                        p.sendMessage(main.color(true, main.lang.getString("chat.newRecord")));
                                    }
                            );
                        }
                    } else {
                        // INSERT

                        sql.runAsync(
                                "INSERT INTO scoreboard (score, username, uuid) VALUES (?,?,?)",
                                st -> {
                                    st.setInt(1, score);
                                    st.setString(2, p.getName());
                                    st.setString(3, p.getUniqueId().toString());
                                },
                                resI -> {
                                    InfinityParkourGUI.refresh();
                                    p.sendMessage(main.color(true, main.lang.getString("chat.newRecord")));
                                }
                        );
                    }
                }
        );
    }

    public interface PlayerStatsCb {
        void callback(HashMap<String, Integer> res);
    }

    public void getStatsByPlayer(Player p, PlayerStatsCb cb) {
        try {
            HashMap<String, Integer> stats = new HashMap<>();

            sql.execAsync(
                    "SELECT score FROM scoreboard WHERE uuid = ?",
                    st -> {
                        st.setString(1, p.getUniqueId().toString());
                    },
                    res -> {
                        if (!res.next()) {
                            cb.callback(null);
                            return;
                        }

                        stats.put("score", res.getInt("score"));

                        sql.execAsync(
                                "SELECT COUNT(*) AS place FROM scoreboard WHERE score >= (SELECT score FROM scoreboard WHERE uuid = ?)",
                                st2 -> {
                                    st2.setString(1, p.getUniqueId().toString());
                                },
                                res2 -> {
                                    if (!res2.next()) {
                                        cb.callback(null);
                                        return;
                                    }

                                    stats.put("place", res2.getInt("place"));

                                    sql.execAsync("SELECT COUNT(*) AS total FROM scoreboard", res3 -> {
                                        if (!res3.next()) {
                                            cb.callback(null);
                                            return;
                                        }

                                        stats.put("total", res3.getInt("total"));

                                        double percentile = stats.get("place") * 100.0 / stats.get("total");

                                        stats.put("topPerc", (int) percentile);

                                        cb.callback(stats);
                                    });

                                });
                    });
        } catch (NullPointerException exp) {
            cb.callback(null);
            return;
        } catch (Exception e) {
            cb.callback(null);
            e.printStackTrace();
            return;
        }
    }
}
