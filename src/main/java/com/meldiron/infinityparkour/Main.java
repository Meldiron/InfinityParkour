package com.meldiron.infinityparkour;

import com.meldiron.infinityparkour.commands.InfinityParkourCmd;
import com.meldiron.infinityparkour.events.HungerEvent;
import com.meldiron.infinityparkour.events.LeaveEvent;
import com.meldiron.infinityparkour.events.MoveEvent;
import com.meldiron.infinityparkour.guis.InfinityParkourGUI;
import com.meldiron.infinityparkour.libs.FileManager;
import com.meldiron.infinityparkour.libs.GUIManager;
import com.meldiron.infinityparkour.libs.SQL;
import com.meldiron.infinityparkour.managers.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private String pluginName = "Infinity Parkour";
    private static Main ourInstance;
    public static Main getInstance() {
        return ourInstance;
    }

    public FileManager fileManager;

    public YamlConfiguration config;
    public YamlConfiguration lang;

    @Override
    public void onEnable() {
        ourInstance = this;

        fileManager = new FileManager(this);

        FileManager.Config config = fileManager.getConfig("config.yml");
        config.copyDefaults(true).save();
        this.config = config.get();

        FileManager.Config lang = fileManager.getConfig("translations.yml");
        lang.copyDefaults(true).save();
        this.lang = lang.get();

        FixConfig.getInstance().FixFor102();
        FixConfig.getInstance().FixFor103();
        FixConfig.getInstance().FixFor105();
        fileManager.saveConfig("config.yml");

        this.getCommand("infinityparkour").setExecutor(InfinityParkourCmd.getInstance());
        this.getCommand("infinityparkour").setTabCompleter(InfinityParkourCmd.getInstance());

        getServer().getPluginManager().registerEvents(new GUIManager(), this);
        getServer().getPluginManager().registerEvents(new MoveEvent(), this);
        getServer().getPluginManager().registerEvents(new HungerEvent(), this);
        getServer().getPluginManager().registerEvents(new LeaveEvent(), this);

        ConfigurationSection mysqlConfig = this.config.getConfigurationSection("mysql");

        // SQL.getInstance().setTesting(true);

        if(mysqlConfig.getBoolean("enabled") == false) {
            SQL.getInstance().connectLite();
        } else {
            SQL.getInstance().connect(
                    mysqlConfig.getString("host"),
                    mysqlConfig.getString("database"),
                    mysqlConfig.getString("username"),
                    mysqlConfig.getString("password"),
                    mysqlConfig.getInt("port")
            );
        }

        Bukkit.getLogger().info("Plugin " + pluginName + " started");
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("Plugin " + pluginName + " stopped");
    }

    public void reloadConfigs() {
        this.config = fileManager.reloadConfig("config.yml").get();

        InfinityParkourGUI.getInstance().refresh();
        GameManager.getInstance().reloadFreePoses();
    }

    public String color(String msg) {
        return msg.replace("&", "§");
    }

    public String color(boolean withPrefix, String msg) {
        if(withPrefix == true) {
            return color(Main.getInstance().lang.getString("chat.prefix") + msg);
        }

        return color(msg);
    }
}
