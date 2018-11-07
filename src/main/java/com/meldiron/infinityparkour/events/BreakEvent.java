package com.meldiron.infinityparkour.events;

import com.meldiron.infinityparkour.Main;
import com.meldiron.infinityparkour.managers.GameManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BreakEvent implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Location loc = e.getBlock().getLocation();

        GameManager gm = GameManager.getInstance();
        Main main = Main.getInstance();

        if(gm.isBlockInArena(loc) == true) {
            e.setCancelled(true);
            p.sendMessage(main.color(true, main.lang.getString("chat.blockInArena")));
        }
    }
}
