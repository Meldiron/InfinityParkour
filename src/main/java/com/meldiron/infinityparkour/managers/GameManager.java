package com.meldiron.infinityparkour.managers;

import com.meldiron.infinityparkour.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class GameManager {
    private static GameManager ourInstance = new GameManager();
    public static GameManager getInstance() {
        return ourInstance;
    }

    private Main main;

    private ArrayList<Location> freePoses;
    private HashMap<Location, Location> freePosToEndPos;
    private HashMap<Player, Location> usedPoses;
    private HashMap<Player, Location> playerPosBeforeTeleport;
    private HashMap<Player, Game> playerToGame;


    private HashMap<Player, Double> playerHealth;
    private HashMap<Player, Integer> playerHunge;


    public GameManager() {
        main = Main.getInstance();

        freePoses = new ArrayList<>();
        freePosToEndPos = new HashMap<>();
        usedPoses = new HashMap<>();
        playerToGame = new HashMap<>();
        playerPosBeforeTeleport = new HashMap<>();
        playerHunge = new HashMap<>();
        playerHealth = new HashMap<>();
        loadFreePoses();
    }

    public void reloadFreePoses() {
        for(Player p : playerToGame.keySet()) {
            leaveGame(p);
        }

        freePoses = new ArrayList<>();
        usedPoses = new HashMap<>();
        freePosToEndPos = new HashMap<>();
        loadFreePoses();
    }

    public Game getGameByLoc(Location loc) {
        Game g = null;
        for (Game game : playerToGame.values()) {
            if(game.block2.getBlockX() == loc.getBlockX() && game.block2.getBlockY() == loc.getBlockY() && game.block2.getBlockZ() == loc.getBlockZ()) {
                g = game;
                break;
            }
        }

        return g;
    }

    public boolean isBlockInArena(Location loc) {
        for(Game g : playerToGame.values()) {
            if(g.block1.equals(loc) || g.block2.equals(loc)) {
                return true;
            }
        }

        return false;
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
        YamlConfiguration config = main.config;

        for(Map<?,?> pos : config.getMapList("startPositions")) {
            float x = Float.parseFloat(pos.get("x").toString());
            float y = Float.parseFloat(pos.get("y").toString());
            float z = Float.parseFloat(pos.get("z").toString());
            String world = pos.get("world").toString();

            Location loc = new Location(Bukkit.getWorld(world), x,y,z);

            if(pos.get("pitch") != null) {
                loc.setPitch(Float.parseFloat(pos.get("pitch").toString()));
            }

            if(pos.get("yaw") != null) {
                loc.setYaw(Float.parseFloat(pos.get("yaw").toString()));
            }

            freePoses.add(loc);

            if(pos.get("useCustomEndPosition") != null && Boolean.parseBoolean(pos.get("useCustomEndPosition").toString()) == true) {


                Map<?, ?> endPosCfg = (Map<?, ?>) pos.get("endPos");

                float endX = Float.parseFloat(endPosCfg.get("x").toString());
                float endY = Float.parseFloat(endPosCfg.get("y").toString());
                float endZ = Float.parseFloat(endPosCfg.get("z").toString());
                String endWorld = endPosCfg.get("world").toString();

                Location endLoc = new Location(Bukkit.getWorld(endWorld), endX, endY, endZ);

                if(endPosCfg.get("pitch") != null) {
                    endLoc.setPitch(Float.parseFloat(endPosCfg.get("pitch").toString()));
            }

            if(endPosCfg.get("yaw") != null) {
                endLoc.setYaw(Float.parseFloat(endPosCfg.get("yaw").toString()));
            }
                freePosToEndPos.put(loc, endLoc);
            }
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
            p.sendMessage(main.color(true, main.lang.getString("chat.alreadyInGame")));
            p.closeInventory();
            return;
        }

        if(freePoses.size() == 0) {
            p.sendMessage(main.color(true, main.lang.getString("chat.allArenasUsed")));
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
            p.sendMessage(main.color(true, main.lang.getString("chat.meaninglessLeave")));
            return;
        }

        Location oldLoc = playerPosBeforeTeleport.get(p);

        if(oldLoc == null) {
            p.sendMessage(main.color(true, main.lang.getString("chat.cantTeleport")));
            return;
        }
        usedPoses.remove(p);
        playerPosBeforeTeleport.remove(p);
        freePoses.add(loc);
        p.setVelocity(new Vector(0,0,0));
        p.setFallDistance(0F);

        Location leaveLoc = freePosToEndPos.get(loc);

        if(leaveLoc != null) {
            p.teleport(leaveLoc);
        } else {
            p.teleport(oldLoc);
        }

        Game g = playerToGame.get(p);
        if(g != null) {
            g.endGame();
        }


        playerToGame.remove(p);

        p.sendMessage(main.color(true, main.lang.getString("chat.areaLeave")));

        p.setHealth(playerHealth.get(p));
        p.setFoodLevel(playerHunge.get(p));

        playerHealth.remove(p);
        playerHunge.remove(p);
    }

    public void runFinishCommands(Player p, Integer score) {
        if(Main.getInstance().config.getBoolean("runFinishCommands") == true) {
            List<Map<?, ?>> cmds = Main.getInstance().config.getMapList("finishCommands");

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

    public Material getRandomParkourBlock() {
        List<String> blocks = Main.getInstance().config.getStringList("parkourBlocks");
        Random rand = new Random();

        String block = blocks.get(rand.nextInt(blocks.size()));

        return Material.getMaterial(block);
    }
}
