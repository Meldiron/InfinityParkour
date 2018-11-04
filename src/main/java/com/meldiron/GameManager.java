package com.meldiron;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.lang.reflect.Array;
import java.util.*;

public class GameManager {
    private ArrayList<Location> freePoses;
    private HashMap<Player, Location> usedPoses;
    private HashMap<Player, Location> playerPosBeforeTeleport;
    private HashMap<Player, Game> playerToGame;


    private HashMap<Player, Double> playerHealth;
    private HashMap<Player, Integer> playerHunge;


    public GameManager() {
        freePoses = new ArrayList<>();
        usedPoses = new HashMap<>();
        playerToGame = new HashMap<>();
        playerPosBeforeTeleport = new HashMap<>();
        playerHunge = new HashMap<>();
        playerHealth = new HashMap<>();
        loadFreePoses();
    }

    public Game getGameByLoc(Location loc) {
        Game g = null;
        for (Game game : playerToGame.values()) {
            if(game.block2.equals(loc)) {
                g = game;
                break;
            }
        }

        return g;
    }

    public Game getGameByPlayer(Player p) {
        Game g = null;

        for(Player player : playerToGame.keySet()) {
            if(player.equals(p)) {
                g = playerToGame.get(player);
                break;
            }
        }

        return g;
    }

    private void loadFreePoses() {
        Main main = Main.getInstance();
        YamlConfiguration config = main.getConfig();

        for(Map<?, ?> pos : config.getMapList("startPositions")) {
            float x = Integer.parseInt(pos.get("x").toString());
            float y = Integer.parseInt(pos.get("y").toString());
            float z = Integer.parseInt(pos.get("z").toString());
            String world = pos.get("world").toString();

            Location loc = new Location(Bukkit.getWorld(world), x,y,z);
            freePoses.add(loc);
        }
    }

    public boolean isInArena(Player p) {
        Location loc = usedPoses.get(p);

        if(loc == null) {
            return false;
        }

        return true;
    }

    public void startGame(Player p) {
        if(isInArena(p) == true) {
            p.sendMessage(Main.formatedMsg(Main.getInstance().getLangConfig().getString("chat.alreadyInGame")));
            p.closeInventory();
            return;
        }

        if(freePoses.size() == 0) {
            p.sendMessage(Main.formatedMsg(Main.getInstance().getLangConfig().getString("chat.allArenasUsed")));
            p.closeInventory();
            return;
        }

        Random rand = new Random();
        int index = rand.nextInt(freePoses.size());
        Location locToPlay = freePoses.get(index);
        freePoses.remove(index);
        usedPoses.put(p, locToPlay);

        playerPosBeforeTeleport.put(p, p.getLocation());

        Game newGame = new Game(p, locToPlay);
        playerToGame.put(p, newGame);

        playerHealth.put(p, p.getHealth());
        playerHunge.put(p, p.getFoodLevel());

        p.setHealth(20.0);
        p.setFoodLevel(20);
    }

    public void leaveGame(Player p) {
        Location loc = usedPoses.get(p);

        if(loc == null) {
            p.sendMessage(Main.formatedMsg(Main.getInstance().getLangConfig().getString("chat.meaninglessLeave")));
            return;
        }

        Location oldLoc = playerPosBeforeTeleport.get(p);

        if(oldLoc == null) {
            p.sendMessage(Main.formatedMsg(Main.getInstance().getLangConfig().getString("chat.cantTeleport")));
            return;
        }
        usedPoses.remove(p);
        playerPosBeforeTeleport.remove(p);
        freePoses.add(loc);
        p.setVelocity(new Vector(0,0,0));
        p.setFallDistance(0F);
        p.teleport(oldLoc);

        Game g = playerToGame.get(p);
        if(g != null) {
            g.endGame();
        }


        playerToGame.remove(p);

        p.sendMessage(Main.formatedMsg(Main.getInstance().getLangConfig().getString("chat.areaLeave")));

        p.setHealth(playerHealth.get(p));
        p.setFoodLevel(playerHunge.get(p));

        playerHealth.remove(p);
        playerHunge.remove(p);
    }

    public void runFinishCommands(Player p, Integer score) {
        if(Main.getInstance().getConfig().getBoolean("runFinishCommands") == true) {
            List<Map<?, ?>> cmds = Main.getInstance().getConfig().getMapList("finishCommands");

            for(Map<?, ?> cmdData : cmds) {
                Integer minScore = Integer.MIN_VALUE;
                Integer maxScore = Integer.MAX_VALUE;

                if(cmdData.get("minScore") != null) {
                    minScore = Integer.parseInt(cmdData.get("minScore").toString());
                }

                if(cmdData.get("maxScore") != null) {
                    maxScore = Integer.parseInt(cmdData.get("maxScore").toString());
                }

                if(score >= minScore && score <= maxScore) {
                    List<String> cmdsToRun = (List<String>) cmdData.get("commands");

                    for(String cmd : cmdsToRun) {
                        runCommand(p, score, cmd);
                    }
                }
            }
        }
    }

    public void runCommand(Player p, Integer score, String cmd) {
        cmd = cmd.replace("{{playerName}}", p.getName()).replace("{{score}}", score.toString());

        if(cmd.startsWith("/")) {
            cmd = cmd.substring(1);
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }
}
