package com.meldiron.infinityparkour.events;

import com.meldiron.infinityparkour.managers.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveEvent implements Listener {
    @EventHandler
    public void onleave(PlayerQuitEvent e)
    {
        if(GameManager.getInstance().isInArena(e.getPlayer()) == true) {
            GameManager.getInstance().leaveGame(e.getPlayer());
        }
    }
}

