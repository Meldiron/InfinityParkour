package com.meldiron.infinityparkour.commands;

import com.meldiron.infinityparkour.guis.InfinityParkourGUI;
import com.meldiron.infinityparkour.libs.GUIManager;
import com.meldiron.infinityparkour.libs.SQL;
import com.meldiron.infinityparkour.managers.GameManager;
import com.meldiron.infinityparkour.Main;
import com.meldiron.infinityparkour.managers.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InfinityParkourCmd implements CommandExecutor, TabCompleter {
    private static InfinityParkourCmd ourInstance = new InfinityParkourCmd();
    public static InfinityParkourCmd getInstance() {
        return ourInstance;
    }

    private Main main;
    private GameManager gm;
    private ScoreboardManager sm;

    public InfinityParkourCmd() {
        main = Main.getInstance();
        gm = GameManager.getInstance();
        sm = ScoreboardManager.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {

            if(args.length == 0) {
                sender.sendMessage(main.color("From console only thing you can do is: /infp <playerName> - open GUI to specific player"));
                return true;
            } else if(args.length == 1) {
                Player p = null;

                for(Player player : Bukkit.getOnlinePlayers()) {
                    if(player.getName().equalsIgnoreCase(args[0])) {
                        p = player;
                    }
                }

                if(p == null) {
                    sender.sendMessage(main.color("Player not found (player must be online)"));
                    return true;
                }

                InfinityParkourGUI.getInstance().open(p);
            } else {
                sender.sendMessage(main.color("From console only thing you can do is: /infp <playerName> - open GUI to specific player"));
                return true;
            }
        }

        Player p = (Player) sender;

        if(args.length == 0) {
            String guiPermission = main.config.getString("permissions.openGui");
            if(!(p.hasPermission(guiPermission))) {
                p.sendMessage(main.color(true, main.lang.getString("chat.noPermissionGui").replace("{{permissionName}}", guiPermission)));
                return true;
            }

            InfinityParkourGUI.getInstance().open(p);
        } else if(args.length == 1) {
            if(args[0].equalsIgnoreCase("leave")) {
                String leavePermission = main.config.getString("permissions.leaveArena");
                if(!(p.hasPermission(leavePermission))) {
                    p.sendMessage(main.color(true, main.lang.getString("chat.noPermissionLeave").replace("{{permissionName}}", leavePermission)));
                    return true;
                }

                gm.leaveGame(p);
            } else if(args[0].equalsIgnoreCase("reload")) {
                String reloadPermission = main.config.getString("permissions.reload");
                if(!(p.hasPermission(reloadPermission))) {
                    p.sendMessage(main.color(true, main.lang.getString("chat.noPermissionReload").replace("{{permissionName}}", reloadPermission)));
                    return true;
                }

                main.reloadConfigs();
                p.sendMessage(main.color(true, main.lang.getString("chat.reloadSuccess")));
            } else if(args[0].equalsIgnoreCase("help")) {
                for(String msg : main.lang.getStringList("helpCommand")) {
                    p.sendMessage(main.color(msg));
                }
            } else if(args[0].equalsIgnoreCase("play")) {
                String guiPermission = main.config.getString("permissions.playGame");
                if(!(p.hasPermission(guiPermission))) {
                    p.sendMessage(main.color(true, main.lang.getString("chat.noPermissionPlay").replace("{{permissionName}}", guiPermission)));
                    return true;
                }

                gm.startGame(p);
            } else if(args[0].equalsIgnoreCase("stats")) {
                String guiPermission = main.config.getString("permissions.statsCmd");
                if(!(p.hasPermission(guiPermission))) {
                    p.sendMessage(main.color(true, main.lang.getString("chat.noPermissionStats").replace("{{permissionName}}", guiPermission)));
                    return true;
                }


                sm.getStatsByPlayer(p, stats -> {
                    if(stats == null) {
                        p.sendMessage(main.color(true, main.lang.getString("chat.chatStatsError")));
                        p.closeInventory();
                        return;
                    }

                    p.sendMessage(main.color(true,
                            main.lang.getString("chat.chatStats")
                                    .replace("{{playerPlace}}", stats.get("place").toString())
                                    .replace("{{totalPlaces}}", stats.get("total").toString())
                                    .replace("{{percentile}}", stats.get("topPerc").toString())
                                    .replace("{{playerScore}}", stats.get("score").toString())
                    ));

                    p.closeInventory();
                });
            } else if(args[0].equalsIgnoreCase("top")) {
                String guiPermission = main.config.getString("permissions.topCmd");
                if(!(p.hasPermission(guiPermission))) {
                    p.sendMessage(main.color(true, main.lang.getString("chat.noPermissionTop").replace("{{permissionName}}", guiPermission)));
                    return true;
                }

                List<String> msgsList = new ArrayList<>();

                for(String msgPrefix : main.lang.getStringList("scoreboardRecordInChat.prefix")) {
                    msgsList.add(msgPrefix);
                }


                String msgFormat = main.lang.getString("scoreboardRecordInChat.record");
                List<Map.Entry<String, Integer>> records = ScoreboardManager.getInstance().getTopFive();
                Integer index = 1;
                for(Map.Entry<String, Integer> record : records) {
                    String playerName = record.getKey();
                    Integer score = record.getValue();

                    String msg = msgFormat
                            .replace("{{playerName}}", playerName)
                            .replace("{{score}}", score.toString())
                            .replace("{{index}}", index.toString());

                    msgsList.add(msg);

                    index++;
                }

                for(String msgSuffix : main.lang.getStringList("scoreboardRecordInChat.suffix")) {
                    msgsList.add(msgSuffix);
                }

                for(String msgPart : msgsList) {
                    p.sendMessage(main.color(msgPart));
                }

            } else {
                p.sendMessage(main.color(true, main.lang.getString("chat.wrongUsage")));
            }
        } else {
            p.sendMessage(main.color(true, main.lang.getString("chat.wrongUsage")));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if(sender instanceof Player) {
            Player p = (Player) sender;

            if(args.length >= 0) {
                List<String> list = new ArrayList<>();

                list.add("leave");
                list.add("play");
                list.add("reload");
                list.add("top");
                list.add("stats");

                return list;
            }
        }

        return null;
    }
}
