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
            String leavePermission = Main.getInstance().getConfig().getString("permissions.leaveArena");
            if(!(p.hasPermission(leavePermission))) {
                p.sendMessage(Main.formatedMsg(Main.getInstance().getLangConfig().getString("chat.noPermissionLeave").replace("{{permissionName}}", leavePermission)));
                return true;
            }

            if(args[0].equalsIgnoreCase("leave")) {
                Main.getInstance().getGm().leaveGame(p);
            } else {
                p.sendMessage(Main.formatedMsg(Main.getInstance().getLangConfig().getString("chat.wrongUsage")));
            }
        } else {
            p.sendMessage(Main.formatedMsg(Main.getInstance().getLangConfig().getString("chat.wrongUsage")));
        }

        return true;
    }
}
