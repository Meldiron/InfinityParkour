package com.meldiron.events;

import com.meldiron.Game;
import com.meldiron.GameManager;
import com.meldiron.Main;
import org.bukkit.Location;
import org.bukkit.Material;
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

        if(underPlayer.getBlock().getType() == Material.getMaterial(Main.getInstance().getConfig().getString("parkourBlock"))) {
            Game game = Main.getInstance().getGm().getGameByLoc(underPlayer);
            if(game != null) {
                game.spawnAtRandomPos(underPlayer);
                game.addScore();
            }
        }

        Game g = Main.getInstance().getGm().getGameByPlayer(e.getPlayer());
        if(g != null) {
            g.checkIfFalled();
        }
    }
}
