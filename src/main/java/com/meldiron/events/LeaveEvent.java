package com.meldiron.events;

import com.meldiron.Game;
import com.meldiron.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveEvent implements Listener {
    @EventHandler
    public void onleave(PlayerQuitEvent e)
    {
        if(Main.getInstance().getGm().isInArena(e.getPlayer()) == true) {
            Main.getInstance().getGm().leaveGame(e.getPlayer());
        }
    }
}

