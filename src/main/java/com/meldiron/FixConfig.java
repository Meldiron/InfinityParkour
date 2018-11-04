package com.meldiron;

import org.bukkit.configuration.file.YamlConfiguration;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class FixConfig {
    private static FixConfig ourInstance = new FixConfig();

    public static FixConfig getInstance() {
        return ourInstance;
    }

    public void FixFor103() {
        YamlConfiguration config = Main.getInstance().getConfig();

        if(config.getString("parkourBlock") != null) {
            config.set("parkourBlocks", new ArrayList<String>(){{
                add(config.getString("parkourBlock"));
            }});
        }

        config.set("parkourBlock", null);
        Main.getInstance().getFm().saveConfig("config.yml");
    }

    public void FixFor102() {
        YamlConfiguration config = Main.getInstance().getConfig();

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
        Main.getInstance().getFm().saveConfig("config.yml");
    }
}
