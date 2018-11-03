package com.meldiron;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class Game {
    private Integer score;
    private Player p;

    private Location middleLoc;

    public Location block1;
    public Location block2;

    public Game(Player p, Location middleLoc) {
        this.p = p;
        this.score = 0;
        this.middleLoc = middleLoc;

        startGame();
    }

    public void checkIfFalled() {
        if(p.getLocation().getY() < middleLoc.getY() - 6) {
            Main.getInstance().getGm().leaveGame(p);
        }
    }

    public void endGame() {
        block1.getBlock().setType(Material.AIR);
        block2.getBlock().setType(Material.AIR);

        ScoreboardManager.getInstance().updateScore(p, score);

        if(Main.getInstance().getConfig().getBoolean("runFinishCommand") == true) {
            String commandToRun = Main.getInstance().getConfig().getString("finishCommand");

            String cmd = commandToRun.replace("{{playerName}}", p.getName()).replace("{{score}}", score.toString());

            if(cmd.startsWith("/")) {
                cmd = cmd.substring(1);
            }

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
    }

    public void addScore() {
        this.score++;

        Random rand = new Random();
        List<String> msgs = Main.getInstance().getLangConfig().getStringList("chat.scoreMsgs");
        String msg = msgs.get(rand.nextInt(msgs.size()));

        this.p.sendMessage(Main.formatedMsg(msg.replace("{{score}}", score.toString())));
    }

    public void startGame() {
        Location underPlayer = new Location(middleLoc.getWorld(), middleLoc.getX(), middleLoc.getY() - 1, middleLoc.getZ());
        Location locToTeleportTo =  new Location(middleLoc.getWorld(), middleLoc.getX() + 0.5f, middleLoc.getY() + 0.5f, middleLoc.getZ() + 0.5f);

        spawnAtPos(underPlayer);
        spawnAtRandomPos(underPlayer);
        p.teleport(locToTeleportTo);
        p.closeInventory();
        p.sendMessage(Main.formatedMsg(Main.getInstance().getLangConfig().getString("chat.arenaStart")));
    }

    public void spawnAtPos(Location loc) {
        if(block1 != null) {
            block1.getBlock().setType(Material.AIR);
        }

        block1 = block2;
        block2 = loc;

        loc.getBlock().setType(Material.getMaterial(Main.getInstance().getConfig().getString("parkourBlock")));
    }

    public void spawnAtRandomPos(Location currentLoc) {
        Location startingLoc = middleLoc;

        String dir = "";
        Random rand = new Random();

        if(startingLoc.getY() > currentLoc.getY()) {
            // som pod tym
            if(startingLoc.getY() - currentLoc.getY() >= 5) {

                // musi stay alebo up
                int randDir = rand.nextInt(2);
                if(randDir == 0) {
                    dir = "stay";
                } else {
                    dir = "up";
                }
            } else {
                // moze up, down, stay
                int randDir = rand.nextInt(3);
                if(randDir == 0) {
                    dir = "stay";
                } else if(randDir == 1) {
                    dir = "up";
                } else {
                    dir = "down";
                }
            }
        } else {
            //som nad tym
            if(currentLoc.getY() - startingLoc.getY() >= 5) {
                // som velmi vysoko, iba down alebo stay
                int randDir = rand.nextInt(2);
                if(randDir == 0) {
                    dir = "stay";
                } else {
                    dir = "down";
                }
            } else {
                // som OK, up/down/stay
                int randDir = rand.nextInt(3);
                if(randDir == 0) {
                    dir = "stay";
                } else if(randDir == 1) {
                    dir = "down";
                } else {
                    dir = "up";
                }
            }
        }

        Location newBlockLoc = new Location(middleLoc.getWorld(), 0,0,0);
        int howFar;
        int lookDir = 0;

        if(Math.abs(startingLoc.getX() - currentLoc.getX()) > 5) {
            if(startingLoc.getX() > currentLoc.getX()) {
                // ja mam mensie X.. ovela mensie.. cize ho chcem zvacsit..
                // x+,y+,y-
                int randNumber = rand.nextInt(3);
                if(randNumber == 1) {
                    randNumber = 3;
                }

                lookDir = randNumber;
            } else {
                // x-,y+,y-
                int randNumber = rand.nextInt(3);
                if(randNumber == 0) {
                    randNumber = 3;
                }

                lookDir = randNumber;
            }
        } else if(Math.abs(startingLoc.getZ() - currentLoc.getZ()) > 5) {
            if(startingLoc.getZ() > currentLoc.getZ()) {
                // current Y je ovela mensie. nemozem y-
                int randNumber = rand.nextInt(3);
                lookDir = randNumber;
            } else {
                // vsetko okrem y+
                int randNumber = rand.nextInt(3);
                if(randNumber == 2) {
                    randNumber = 3;
                }

                lookDir = randNumber;
            }
        } else {
            howFar = rand.nextInt(4);
        }


        if(dir == "up") {
            if(score < 20) {
                howFar = 2 + rand.nextInt(2);
            } else if(score < 50) {
                howFar = 2 + rand.nextInt(3);
            } else {
                howFar = 3 + rand.nextInt(2);
            }
        } else if(dir == "down") {
            if(score < 20) {
                howFar = 2 + rand.nextInt(3);
            } else if(score < 50) {
                howFar = 3 + rand.nextInt(2);
            } else {
                howFar = 3 + rand.nextInt(3);
            }
        } else {
            if(score < 100) {
                howFar = 2 + rand.nextInt(3);
            } else {
                howFar = 3 + rand.nextInt(3);
            }
        }

        if(lookDir == 0) {
            newBlockLoc.setX(howFar);
        } else if(lookDir == 1) {
            newBlockLoc.setX(-howFar);
        } else if(lookDir == 2) {
            newBlockLoc.setZ(howFar);
        } else {
            newBlockLoc.setZ(-howFar);
        }

        newBlockLoc.setX(newBlockLoc.getX() + currentLoc.getX());
        newBlockLoc.setY(newBlockLoc.getY() + currentLoc.getY());
        newBlockLoc.setZ(newBlockLoc.getZ() + currentLoc.getZ());

        if(dir == "up") {
            newBlockLoc.setY(newBlockLoc.getY() + 1);
        } else if(dir == "down") {
            newBlockLoc.setY(newBlockLoc.getY() - 1);
        }

        spawnAtPos(newBlockLoc);
    }
}
