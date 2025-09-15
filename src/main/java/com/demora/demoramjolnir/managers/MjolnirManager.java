package com.demora.demoramjolnir.managers;

import com.demora.demoramjolnir.DemoraMjolnir;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MjolnirManager {
    
    private final DemoraMjolnir plugin;
    private final Map<String, Long> cooldowns;
    private final NamespacedKey mjolnirKey;
    
    public MjolnirManager(DemoraMjolnir plugin) {
        this.plugin = plugin;
        this.cooldowns = new HashMap<>();
        this.mjolnirKey = new NamespacedKey(plugin, "mjolnir");
    }
    
    public void giveMjolnir(Player player) {
        ItemStack mjolnir = createMjolnir();
        player.getInventory().addItem(mjolnir);
    }
    
    public ItemStack createMjolnir() {
        Material material = Material.STONE_AXE;
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6§lМьёльнир");
            meta.setCustomModelData(1);
            meta.addEnchant(Enchantment.SHARPNESS, 5, true);
            meta.addEnchant(Enchantment.UNBREAKING, 255, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            meta.getPersistentDataContainer().set(mjolnirKey, PersistentDataType.STRING, "mjolnir");
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public boolean isMjolnir(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        
        return meta.getPersistentDataContainer().has(mjolnirKey, PersistentDataType.STRING);
    }
    
    public boolean isOnCooldown(Player player) {
        return isOnThrowCooldown(player);
    }
    
    public boolean isOnThrowCooldown(Player player) {
        String playerId = player.getUniqueId().toString();
        if (!cooldowns.containsKey(playerId + "_throw")) {
            return false;
        }
        
        long lastUse = cooldowns.get(playerId + "_throw");
        int cooldownDuration = getThrowCooldownDuration();
        long currentTime = System.currentTimeMillis();
        
        return (currentTime - lastUse) < (cooldownDuration * 1000L);
    }
    
    public boolean isOnMeleeCooldown(Player player) {
        String playerId = player.getUniqueId().toString();
        if (!cooldowns.containsKey(playerId + "_melee")) {
            return false;
        }
        
        long lastUse = cooldowns.get(playerId + "_melee");
        int cooldownDuration = getMeleeCooldownDuration();
        long currentTime = System.currentTimeMillis();
        
        return (currentTime - lastUse) < (cooldownDuration * 1000L);
    }
    
    public void setThrowCooldown(Player player) {
        cooldowns.put(player.getUniqueId().toString() + "_throw", System.currentTimeMillis());
    }
    
    public void setMeleeCooldown(Player player) {
        cooldowns.put(player.getUniqueId().toString() + "_melee", System.currentTimeMillis());
    }
    
    public void setCooldown(Player player) {
        setThrowCooldown(player);
    }
    
    public long getRemainingThrowCooldown(Player player) {
        String playerId = player.getUniqueId().toString();
        if (!cooldowns.containsKey(playerId + "_throw")) {
            return 0;
        }
        
        long lastUse = cooldowns.get(playerId + "_throw");
        int cooldownDuration = getThrowCooldownDuration();
        long currentTime = System.currentTimeMillis();
        long remaining = (cooldownDuration * 1000L) - (currentTime - lastUse);
        
        return Math.max(0, remaining / 1000L);
    }
    
    public long getRemainingMeleeCooldown(Player player) {
        String playerId = player.getUniqueId().toString();
        if (!cooldowns.containsKey(playerId + "_melee")) {
            return 0;
        }
        
        long lastUse = cooldowns.get(playerId + "_melee");
        int cooldownDuration = getMeleeCooldownDuration();
        long currentTime = System.currentTimeMillis();
        long remaining = (cooldownDuration * 1000L) - (currentTime - lastUse);
        
        return Math.max(0, remaining / 1000L);
    }
    
    public long getRemainingCooldown(Player player) {
        return getRemainingThrowCooldown(player);
    }
    
    

    public double getAnimationFlightSpeed() {
        return plugin.getConfig().getDouble("mjolnir.animation.flight_speed", 0.3);
    }
    
    public double getAnimationReturnSpeed() {
        return plugin.getConfig().getDouble("mjolnir.animation.return_speed", 0.4);
    }
    
    public float getAnimationRotationSpeed() {
        return (float) plugin.getConfig().getDouble("mjolnir.animation.rotation_speed", 25.0);
    }
    
    public float getAnimationReturnRotationMultiplier() {
        return (float) plugin.getConfig().getDouble("mjolnir.animation.return_rotation_multiplier", 1.5);
    }
    
    public double getMeleeDamage() {
        return plugin.getConfig().getDouble("mjolnir.damage.melee_damage", 20.0);
    }
    
    public double getThrowDamage() {
        return plugin.getConfig().getDouble("mjolnir.damage.throw_damage", 12.0);
    }
    
    public double getAreaDamage() {
        return plugin.getConfig().getDouble("mjolnir.damage.area_damage", 12.0);
    }
    
    public int getThrowCooldownDuration() {
        return plugin.getConfig().getInt("mjolnir.cooldown.throw_duration", 10);
    }
    
    public int getMeleeCooldownDuration() {
        return plugin.getConfig().getInt("mjolnir.cooldown.melee_duration", 10);
    }
    

    public void clearCooldowns() {
        cooldowns.clear();
    }
    

}
