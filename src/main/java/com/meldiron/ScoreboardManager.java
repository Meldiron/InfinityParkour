package com.meldiron;

import com.meldiron.guis.InfinityParkourGUI;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

public class ScoreboardManager {
    private static ScoreboardManager ourInstance = new ScoreboardManager();

    public static ScoreboardManager getInstance() {
        return ourInstance;
    }

    private ScoreboardManager() {
    }

    public List<Map.Entry<String, Integer>> getLeatherboard() {
        HashMap<String, Integer> scoreboard = new HashMap<>();

        YamlConfiguration scoreboardData =  Main.getInstance().getScoreboard();

        for(String playerName : scoreboardData.getConfigurationSection("players").getKeys(false)) {
            Integer score = scoreboardData.getInt("players." + playerName + ".score");

            scoreboard.put(playerName, score);
        }

        List<Map.Entry<String, Integer>> sortedScoreboard = utils.entriesSortedByValues(scoreboard);

        return sortedScoreboard;
    }

    public void updateScore(Player p, Integer score) {
        Integer oldScore = Main.getInstance().getScoreboard().getInt("players." + p.getName() + ".score");

        if(oldScore == null || score > oldScore) {
            Main.sendMessage(p, Main.formatedMsg(Main.getInstance().getLangConfig().getString("chat.newRecord")));
            Main.getInstance().getScoreboard().set("players." + p.getName() + ".score", score);
            Main.getInstance().getFm().saveConfig("scoreboard.yml");

            InfinityParkourGUI.refresh();
        }
    }

    public List<Map.Entry<String, Integer>> GetTopFive() {
        List<Map.Entry<String, Integer>> sortedScoreboardAll = getLeatherboard();
        List<Map.Entry<String, Integer>> sortedScoreboard = new ArrayList<>();

        Integer index = 0;
        for(Map.Entry<String, Integer> record : sortedScoreboardAll) {
            if(index < 5) {
                sortedScoreboard.add(record);
            } else {
                break;
            }

            index++;
        }

        return sortedScoreboard;
    }

    public HashMap<String, Object> getStatsByPlayer(Player p) {
        boolean found = false;
        Integer foundScore = -1;
        Integer foundPlace = -1;

        int index = 1;
        for(Map.Entry<String, Integer> stat : getLeatherboard()) {
            if(found == true) {
                index++;
                continue;
            }
            String playerName = stat.getKey();

            if(playerName.equals(p.getName())) {
                found = true;
                foundScore = stat.getValue();
                foundPlace = index;
            }

            index++;
        }

        if(found == false) {
            return null;
        }

        Integer total = Main.getInstance().getScoreboard().getConfigurationSection("players").getKeys(false).size();
        double percentile =  foundPlace * 100.0 / total;

        HashMap<String, Object> stats = new HashMap<>();

        stats.put("score", foundScore);
        stats.put("place", foundPlace);
        stats.put("total", total);
        stats.put("topPerc", (int) percentile);

        return stats;
    }
}
