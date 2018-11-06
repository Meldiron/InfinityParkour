package com.meldiron.infinityparkour;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.HashMap;

public class FixConfig {
    private static FixConfig ourInstance = new FixConfig();
    public static FixConfig getInstance() {
        return ourInstance;
    }

    private Main main;

    public FixConfig() {
        main = Main.getInstance();
    }


    public void FixFor105() {
        YamlConfiguration config = main.config;

        if(config.get("particle.amount") != null) {
            config.set("particle.amount", null);
        }
    }

    public void FixFor103() {
        YamlConfiguration config = main.config;

        if(config.getString("parkourBlock") != null) {
            config.set("parkourBlocks", new ArrayList<String>(){{
                add(config.getString("parkourBlock"));
            }});
        }

        config.set("parkourBlock", null);
    }

    public void FixFor102() {
        YamlConfiguration config = main.config;

        if(config.get("runFinishCommand") != null) {
            config.set("runFinishCommands", config.getBoolean("runFinishCommand"));
        }


        if(config.getString("finishCommand") != null) {
            config.set("finishCommands", new ArrayList<HashMap<String, Object>>(){{
                add(new HashMap<String, Object>(){{
                    put("commands", new ArrayList<String>() {{
                        add(config.getString("finishCommand"));
                    }});
                }});
            }});
        }

        config.set("runFinishCommand", null);
        config.set("finishCommand", null);
    }
}
