package com.demora.demoramjolnir;

import com.demora.demoramjolnir.commands.MjolnirCommand;
import com.demora.demoramjolnir.listeners.MjolnirListener;
import com.demora.demoramjolnir.managers.MjolnirManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DemoraMjolnir extends JavaPlugin {
    
    private static DemoraMjolnir instance;
    private MjolnirManager mjolnirManager;
    
    @Override
    public void onEnable() {
        instance = this;
        

        saveDefaultConfig();
        

        mjolnirManager = new MjolnirManager(this);
        

        getCommand("demoramjolnir").setExecutor(new MjolnirCommand(this));
        

        getServer().getPluginManager().registerEvents(new MjolnirListener(this), this);
        
        getLogger().info("Плагин DemoraMjolnir успешно загружен!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Плагин DemoraMjolnir выгружен!");
    }
    
    public static DemoraMjolnir getInstance() {
        return instance;
    }
    
    public MjolnirManager getMjolnirManager() {
        return mjolnirManager;
    }
}
