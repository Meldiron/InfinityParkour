package com.meldiron.commands;

import com.meldiron.Main;
import com.meldiron.guis.InfinityParkourGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfinityParkourCmd implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(Main.formatedMsg("This command is only for players :("));
            return true;
        }

        Player p = (Player) sender;

        if(args.length == 0) {
            String guiPermission = Main.getInstance().getConfig().getString("permissions.openGui");
            if(!(p.hasPermission(guiPermission))) {
                p.sendMessage(Main.formatedMsg(Main.getInstance().getLangConfig().getString("chat.noPermissionGui").replace("{{permissionName}}", guiPermission)));
                return true;
            }

            InfinityParkourGUI.getInstance().open(p);

        } else if(args.length == 1) {
            if(args[0].equalsIgnoreCase("leave")) {
                String leavePermission = Main.getInstance().getConfig().getString("permissions.leaveArena");
                if(!(p.hasPermission(leavePermission))) {
                    p.sendMessage(Main.formatedMsg(Main.getInstance().getLangConfig().getString("chat.noPermissionLeave").replace("{{permissionName}}", leavePermission)));
                    return true;
                }

                Main.getInstance().getGm().leaveGame(p);
            } else if(args[0].equalsIgnoreCase("reload")) {
                String reloadPermission = Main.getInstance().getConfig().getString("permissions.reload");
                if(!(p.hasPermission(reloadPermission))) {
                    p.sendMessage(Main.formatedMsg(Main.getInstance().getLangConfig().getString("chat.noPermissionReload").replace("{{permissionName}}", reloadPermission)));
                    return true;
                }

                Main.getInstance().reloadConfigs();
                p.sendMessage(Main.formatedMsg(Main.getInstance().getLangConfig().getString("chat.reloadSuccess")));
            } else if(args[0].equalsIgnoreCase("help")) {
                p.sendMessage("§7--------------- §6§lInfinity Parkour §7---------------");
                p.sendMessage(" §6/infp §7- §fOpen Infinity parkoru GUI §7(infinityparkour.opengui)");;
                p.sendMessage(" §6/infp play §7- §fAttempt to join arena §7(infinityparkour.play)");
                p.sendMessage(" §6/infp help §7- §fShow this help");
                p.sendMessage(" §6/infp reload§7- §fReload Infinity parkour plugin (do this when you edit config files) §7(infinityparkour.reload)");
                p.sendMessage(" §6/infp leave §7- §fLeave arena §7(infinityparkour.leave)"); //
                p.sendMessage("§7------------------------------------------------");
            } else if(args[0].equalsIgnoreCase("play")) {
                String guiPermission = Main.getInstance().getConfig().getString("permissions.playGame");
                if(!(p.hasPermission(guiPermission))) {
                    p.sendMessage(Main.formatedMsg(Main.getInstance().getLangConfig().getString("chat.noPermissionPlay").replace("{{permissionName}}", guiPermission)));
                    return true;
                }

                Main.getInstance().getGm().startGame(p);
            } else {
                p.sendMessage(Main.formatedMsg(Main.getInstance().getLangConfig().getString("chat.wrongUsage")));
            }
        } else {
            p.sendMessage(Main.formatedMsg(Main.getInstance().getLangConfig().getString("chat.wrongUsage")));
        }

        return true;
    }
}
