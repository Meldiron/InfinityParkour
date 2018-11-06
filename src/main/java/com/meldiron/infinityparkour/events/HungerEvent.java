package com.meldiron.infinityparkour.events;

import com.meldiron.infinityparkour.managers.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class HungerEvent implements Listener {
    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if(!(e.getEntity() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getEntity();

        if(GameManager.getInstance().isInArena(p) == true) {
            e.setCancelled(true);
        }
    }
}