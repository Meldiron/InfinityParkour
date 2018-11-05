package com.meldiron.infinityparkour.events;

import com.meldiron.infinityparkour.Game;
import com.meldiron.infinityparkour.GameManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveEvent implements Listener {
    @EventHandler
    public void onMove(PlayerMoveEvent e)
    {
        Location pLoc = e.getPlayer().getLocation();

        Location underPlayer = new Location(pLoc.getWorld(), pLoc.getX(), pLoc.getY() - 1, pLoc.getZ());
        underPlayer.setX(Math.floor(underPlayer.getX()));
        underPlayer.setY(Math.floor(underPlayer.getY()));
        underPlayer.setZ(Math.floor(underPlayer.getZ()));

        GameManager gm = GameManager.getInstance();

        if(gm.isInArena(e.getPlayer()) == true) {
            Game game = gm.getGameByLoc(underPlayer);
            if(game != null) {
                game.spawnAtRandomPos(underPlayer);
                game.addScore();
            }
        }

        Game g = gm.getGameByPlayer(e.getPlayer());
        if(g != null) {
            g.checkIfFalled();
        }
    }
}
