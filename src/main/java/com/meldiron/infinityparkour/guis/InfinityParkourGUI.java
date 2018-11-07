package com.meldiron.infinityparkour.guis;

import com.meldiron.infinityparkour.managers.GameManager;
import com.meldiron.infinityparkour.Main;
import com.meldiron.infinityparkour.managers.ScoreboardManager;
import com.meldiron.infinityparkour.libs.GUIManager;
import com.meldiron.infinityparkour.libs.XItemStack;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InfinityParkourGUI extends GUIManager {
    private static InfinityParkourGUI ourInstance;

    public static InfinityParkourGUI getInstance() {
        if(ourInstance == null) {
            ourInstance = new InfinityParkourGUI();
            return ourInstance;
        } else {
            return ourInstance;
        }
    }

    private Main main;
    private ScoreboardManager sm;
    private GameManager gm;
    private XItemStack is;
    
    public static void refresh() {
        if(InfinityParkourGUI.getInstance() != null) {
            InfinityParkourGUI.getInstance().delete();
        }
        InfinityParkourGUI.ourInstance = new InfinityParkourGUI();
    }


    public InfinityParkourGUI() {
        super(3,  Main.getInstance().color(Main.getInstance().lang.getString("mainGui.title")));

        main = Main.getInstance();
        sm = ScoreboardManager.getInstance();
        gm = GameManager.getInstance();
        is = XItemStack.getInstance();

        ConfigurationSection lang = main.lang.getConfigurationSection("mainGui");

        ItemStack glass = null;

        if(main.lang.getBoolean("mainGui.useFillItem") == true) {
           glass = is.createItem(
               main.lang.getString("mainGui.fillItem"),
               "",
               new ArrayList<>()
           );
        }

        for(int i = 0; i < 27; i++) {
            if(i == 10) {
                ConfigurationSection tutItemCfg = lang.getConfigurationSection("tutorialItem");

                setItem(i, is.createItem(
                        tutItemCfg.getString("item"),
                        tutItemCfg.getString("title"),
                        tutItemCfg.getStringList("lore")
                ));

                continue;
            } else if(i == 13) {
                ConfigurationSection playItemCfg = lang.getConfigurationSection("playItem");

                setItem(i, is.createItem(
                        playItemCfg.getString("item"),
                        playItemCfg.getString("title"),
                        playItemCfg.getStringList("lore")
                ), player -> {
                    String guiPermission = main.getConfig().getString("permissions.playGame");
                    if(!(player.hasPermission(guiPermission))) {
                        player.sendMessage(main.color(true, main.lang.getString("chat.noPermissionPlay").replace("{{permissionName}}", guiPermission)));
                        player.closeInventory();
                        return;
                    }

                    gm.startGame(player);
                });

                continue;
            } else if(i == 16) {
                ConfigurationSection scoreboardItemCfg = lang.getConfigurationSection("scoreboardItem");
                List<String> loreList = new ArrayList<>();

                for(String line : scoreboardItemCfg.getStringList("lore")) {
                    if(line.toLowerCase().contains("{{scoreboard}}")) {

                        String msgFormat = scoreboardItemCfg.getString("scoreboardRecord");

                        List<Map.Entry<String, Integer>> records = ScoreboardManager.getInstance().getTopFive();
                        Integer index = 1;
                        for(Map.Entry<String, Integer> record : records) {
                            String playerName = record.getKey();
                            Integer score = record.getValue();

                            String msg = msgFormat
                                    .replace("{{playerName}}", playerName)
                                    .replace("{{score}}", score.toString())
                                    .replace("{{index}}", index.toString());

                            loreList.add(msg);

                            index++;
                        }

                    } else {
                        loreList.add(line);
                    }
                }

                List<String> finalLore = loreList.stream().map(n -> main.color(n)).collect(Collectors.toList());

                setItem(i, is.createItem(
                        scoreboardItemCfg.getString("item"),
                        scoreboardItemCfg.getString("title"),
                        finalLore
                ), player -> {
                    sm.getStatsByPlayer(player, stats -> {
                        if(stats == null) {
                            player.sendMessage(main.color(true, main.lang.getString("chat.chatStatsError")));
                            player.closeInventory();
                            return;
                        }

                        player.sendMessage(main.color(true,
                                main.lang.getString("chat.chatStats")
                                        .replace("{{playerPlace}}", stats.get("place").toString())
                                        .replace("{{totalPlaces}}", stats.get("total").toString())
                                        .replace("{{percentile}}", stats.get("topPerc").toString())
                                        .replace("{{playerScore}}", stats.get("score").toString())
                        ));

                        player.closeInventory();
                    });
                });

                continue;
            }

            if(glass != null) {
                setItem(i, glass);
            }
        }
    }

}
