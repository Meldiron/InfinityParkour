package com.meldiron.events;

import com.meldiron.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class HungerEvent implements Listener {
    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if(!(e.getEntity() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getEntity();

        if(Main.getInstance().getGm().isInArena(p) == true) {
            e.setCancelled(true);
        }
    }
}