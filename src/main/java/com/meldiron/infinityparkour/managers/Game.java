package com.meldiron.infinityparkour.managers;

import com.meldiron.infinityparkour.Main;
import com.meldiron.infinityparkour.managers.GameManager;
import com.meldiron.infinityparkour.managers.ScoreboardManager;
import jdk.nashorn.internal.ir.Block;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;

public class Game {
    private GameManager gm;
    private Main main;

    private Integer score;
    private Player p;

    private Location middleLoc;

    public Location block1;
    public Location block2;

    private BlockState loc1OldBlock;
    private BlockState loc2OldBlock;

    public Game(Player p, Location middleLoc) {
        gm = GameManager.getInstance();
        main = Main.getInstance();

        this.p = p;
        this.score = 0;
        this.middleLoc = middleLoc;

        startGame();
    }

    public void checkIfFalled() {
        if(p.getLocation().getY() < middleLoc.getY() - 6) {
            gm.leaveGame(p);
        }
    }

    public void endGame() {

        setBlock(loc1OldBlock, block1);

        setBlock(loc2OldBlock, block2);

        ScoreboardManager.getInstance().updateScore(p, score);

        gm.runFinishCommands(p, score);
    }

    private void setBlock(BlockState loc2OldBlock, Location block2) {
        if(loc2OldBlock != null) {
            BlockState bs = block2.getBlock().getState();
            bs.setType(loc2OldBlock.getType());
            bs.setData(loc2OldBlock.getData());
            bs.update(true);
        } else {
            block2.getBlock().setType(Material.AIR);
        }
    }

    public void addScore() {
        this.score++;

        Random rand = new Random();
        List<String> msgs = main.lang.getStringList("chat.scoreMsgs");
        String msg = msgs.get(rand.nextInt(msgs.size()));

        p.sendMessage(main.color(true, msg.replace("{{score}}", score.toString())));
    }

    public void startGame() {
        Location underPlayer = middleLoc.clone().subtract(new Vector(0,1,0));
        Location locToTeleportTo =  middleLoc.clone().add(new Vector(0.5, 0.5, 0.5));

        spawnAtPos(underPlayer);
        spawnAtRandomPos(underPlayer);
        p.teleport(locToTeleportTo);
        p.closeInventory();
        p.sendMessage(main.color(true, main.lang.getString("chat.arenaStart")));
    }

    public void spawnAtPos(Location loc) {
        if(block1 != null) {
            setBlock(loc1OldBlock, block1);
        }

        if(block2 != null) {
            block1 = block2.clone();
            loc1OldBlock = loc2OldBlock;
        } else {
            block1 = null;
            loc1OldBlock = null;
        }

        block2 = loc.clone();

        loc2OldBlock = loc.getBlock().getState();

        loc.getBlock().setType(gm.getRandomParkourBlock());

        Location particleLoc = loc.clone();
        particleLoc.add(0,1,0);

       if(Main.getInstance().config.getBoolean("particle.show") == true) {
            loc.getWorld().playEffect(particleLoc, Effect.SMOKE, Integer.MAX_VALUE);
        }
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

        Location newBlockLoc = middleLoc.clone();
        newBlockLoc.setX(0);
        newBlockLoc.setY(0);
        newBlockLoc.setZ(0);

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
