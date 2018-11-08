package com.meldiron.infinityparkour.events;

import com.meldiron.infinityparkour.managers.Game;
import com.meldiron.infinityparkour.managers.GameManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class MoveEvent implements Listener {
    @EventHandler
    public void onMove(PlayerMoveEvent e)
    {
        Location pLoc = e.getPlayer().getLocation();

        Location underPlayer = pLoc.clone();
        underPlayer.setX(Math.floor(underPlayer.getX()));
        underPlayer.setY(Math.floor(underPlayer.getY()) - 1);
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
