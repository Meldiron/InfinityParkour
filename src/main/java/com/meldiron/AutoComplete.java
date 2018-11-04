package com.meldiron;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AutoComplete implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if(sender instanceof Player) {
            Player p = (Player) sender;

            if(args.length >= 0) {

                List<String> list = new ArrayList<>();

                list.add("leave");
                list.add("play");
                list.add("reload");

                return list;
            }
        }

        return null;
    }
}
