package com.demora.demoramjolnir.commands;

import com.demora.demoramjolnir.DemoraMjolnir;
import com.demora.demoramjolnir.managers.MjolnirManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MjolnirCommand implements CommandExecutor {
    
    private final DemoraMjolnir plugin;
    private final MjolnirManager mjolnirManager;
    
    public MjolnirCommand(DemoraMjolnir plugin) {
        this.plugin = plugin;
        this.mjolnirManager = plugin.getMjolnirManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cИспользование:");
            sender.sendMessage("§c/demoramjolnir give <игрок> - выдать Мьёльнир игроку");
            sender.sendMessage("§c/demoramjolnir reload - перезагрузить конфигурацию плагина");
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "give":
                return handleGiveCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender);
            default:
                sender.sendMessage("§cНеизвестная команда. Используйте /demoramjolnir для справки.");
                return true;
        }
    }
    
    private boolean handleGiveCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("demoramjolnir.give")) {
            sender.sendMessage("§cУ вас нет прав для использования этой команды!");
            return true;
        }
        
        if (args.length != 2) {
            sender.sendMessage("§cИспользование: /demoramjolnir give <игрок>");
            return true;
        }
        
        String targetPlayerName = args[1];
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        
        if (targetPlayer == null) {
            sender.sendMessage("§cИгрок " + targetPlayerName + " не найден!");
            return true;
        }
        
        mjolnirManager.giveMjolnir(targetPlayer);
        
        targetPlayer.sendMessage("§aВы получили Мьёльнир!");
        sender.sendMessage("§aМьёльнир выдан игроку " + targetPlayer.getName() + "!");
        
        return true;
    }
    
    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("demoramjolnir.reload")) {
            sender.sendMessage("§cУ вас нет прав для использования этой команды!");
            return true;
        }
        
        try {
            plugin.reloadConfig();
            mjolnirManager.clearCooldowns();
            sender.sendMessage("§aКонфигурация плагина DemoraMjolnir успешно перезагружена!");
            return true;
        } catch (Exception e) {
            sender.sendMessage("§cОшибка при перезагрузке конфигурации: " + e.getMessage());
            plugin.getLogger().severe("Ошибка при перезагрузке конфигурации: " + e.getMessage());
            return true;
        }
    }
}
