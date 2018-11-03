package com.meldiron.guis;

import com.meldiron.GameManager;
import com.meldiron.Main;
import com.meldiron.ScoreboardManager;
import com.meldiron.libs.GUIManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class InfinityParkourGUI extends GUIManager {
    public static InfinityParkourGUI instance = null;

    public static InfinityParkourGUI getInstance() {
        if(InfinityParkourGUI.instance == null) {
            InfinityParkourGUI.instance = new InfinityParkourGUI();
            return InfinityParkourGUI.instance;
        } else {
            return InfinityParkourGUI.instance;
        }
    }

    public static void refresh() {
        if(InfinityParkourGUI.instance != null) {
            InfinityParkourGUI.instance.delete();
        }
        InfinityParkourGUI.instance = new InfinityParkourGUI();
    }


    public InfinityParkourGUI() {
        super(3,  Main.getInstance().getLangConfig().getConfigurationSection("mainGui").getString("title"));

        ConfigurationSection lang = Main.getInstance().getLangConfig().getConfigurationSection("mainGui");

        ItemStack glass = new ItemStack(Material.getMaterial(Main.getInstance().getLangConfig().getString("mainGui.fillItem")));
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassMeta.setLore(new ArrayList<>());
        glass.setItemMeta(glassMeta);

        for(int i = 0; i < 27; i++) {
            if(i == 10) {
                ConfigurationSection tutItemCfg = lang.getConfigurationSection("tutorialItem");

                ItemStack tutItem = new ItemStack(Material.getMaterial(tutItemCfg.getString("item")));
                ItemMeta tutItemmeta = tutItem.getItemMeta();
                tutItemmeta.setDisplayName(tutItemCfg.getString("title"));
                tutItemmeta.setLore(tutItemCfg.getStringList("lore"));
                tutItem.setItemMeta(tutItemmeta);

                setItem(i, tutItem);

                continue;
            } else if(i == 13) {
                ConfigurationSection playItemCfg = lang.getConfigurationSection("playItem");

                ItemStack playItem = new ItemStack(Material.getMaterial(playItemCfg.getString("item")));
                ItemMeta playItemMeta = playItem.getItemMeta();
                playItemMeta.setDisplayName(playItemCfg.getString("title"));
                playItemMeta.setLore(playItemCfg.getStringList("lore"));
                playItemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                playItem.setItemMeta(playItemMeta);

                setItem(i, playItem, player -> {
                    String guiPermission = Main.getInstance().getConfig().getString("permissions.playGame");
                    if(!(player.hasPermission(guiPermission))) {
                        player.sendMessage(Main.formatedMsg(Main.getInstance().getLangConfig().getString("chat.noPermissionPlay").replace("{{permissionName}}", guiPermission)));
                        player.closeInventory();
                        return;
                    }

                    Main.getInstance().getGm().startGame(player);
                });
                continue;
            } else if(i == 16) {
                ConfigurationSection scoreboardItemCfg = lang.getConfigurationSection("scoreboardItem");

                ItemStack scoreboardItem = new ItemStack(Material.getMaterial(scoreboardItemCfg.getString("item")));
                ItemMeta scoreboardItemMeta = scoreboardItem.getItemMeta();
                scoreboardItemMeta.setDisplayName(scoreboardItemCfg.getString("title"));

                List<String> loreList = new ArrayList<>();

                for(String line : scoreboardItemCfg.getStringList("lore")) {
                    if(line.toLowerCase().contains("{{scoreboard}}")) {

                        String msgFormat = scoreboardItemCfg.getString("scoreboardRecord");

                        List<Map.Entry<String, Integer>> records = ScoreboardManager.getInstance().GetTopFive();
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

                scoreboardItemMeta.setLore(loreList);
                scoreboardItem.setItemMeta(scoreboardItemMeta);

                setItem(i, scoreboardItem, player -> {
                    HashMap<String, Object> stats = ScoreboardManager.getInstance().getStatsByPlayer(player);

                    if(stats == null) {
                        player.sendMessage(Main.formatedMsg(Main.getInstance().getLangConfig().getString("chat.chatStatsError")));
                        player.closeInventory();
                        return;
                    }

                    player.sendMessage(Main.formatedMsg(Main.getInstance().getLangConfig().getString("chat.chatStats")
                        .replace("{{playerPlace}}", stats.get("place").toString())
                            .replace("{{totalPlaces}}", stats.get("total").toString())
                            .replace("{{percentile}}", stats.get("topPerc").toString())
                            .replace("{{playerScore}}", stats.get("score").toString())
                    ));

                    player.closeInventory();

                });

                continue;
            }

            setItem(i, glass);
        }
    }

}
