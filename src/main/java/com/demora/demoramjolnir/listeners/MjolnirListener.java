package com.demora.demoramjolnir.listeners;

import com.demora.demoramjolnir.DemoraMjolnir;
import com.demora.demoramjolnir.managers.MjolnirManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

public class MjolnirListener implements Listener {
    
    private final DemoraMjolnir plugin;
    private final MjolnirManager mjolnirManager;
    private final java.util.Set<java.util.UUID> processingMjolnirDamage = new java.util.HashSet<>();
    
    public MjolnirListener(DemoraMjolnir plugin) {
        this.plugin = plugin;
        this.mjolnirManager = plugin.getMjolnirManager();
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !mjolnirManager.isMjolnir(item)) {
            return;
        }
        
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        event.setCancelled(true);
        
        if (mjolnirManager.isOnThrowCooldown(player)) {
            long remaining = mjolnirManager.getRemainingThrowCooldown(player);
            player.sendMessage("§cМьёльнир перезаряжается! Осталось: §e" + remaining + " сек");
            return;
        }
        
        mjolnirManager.setThrowCooldown(player);
        
        Location loc = player.getEyeLocation();
        ItemDisplay mjolnirDisplay = (ItemDisplay) player.getWorld().spawnEntity(loc, EntityType.ITEM_DISPLAY);
        
        ItemStack displayItem = new ItemStack(Material.STONE_AXE);
        ItemMeta displayMeta = displayItem.getItemMeta();
        displayMeta.setCustomModelData(1);
        displayItem.setItemMeta(displayMeta);
        mjolnirDisplay.setItemStack(displayItem);
        
        Transformation transformation = mjolnirDisplay.getTransformation();
        transformation.getLeftRotation().rotationX((float) Math.toRadians(90));
        mjolnirDisplay.setTransformation(transformation);
        mjolnirDisplay.setRotation(loc.getYaw(), loc.getPitch());
        
        player.getInventory().setItemInMainHand(null);
        
        Vector direction = loc.getDirection().normalize().multiply(1.5);
        
        player.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.0f, 1.0f);
        
        new BukkitRunnable() {
            private final Location startLoc = loc.clone();
            private Location currentLoc = startLoc.clone();
            private boolean isReturning = false;
            private int ticks = 0;
            private float rotationAngle = 0;
            private float targetRotationAngle = 0;
            private float rotationSpeed = mjolnirManager.getAnimationRotationSpeed();
            private Vector currentVelocity = direction.clone().normalize().multiply(mjolnirManager.getAnimationFlightSpeed());
            
            @Override
            public void run() {
                if (!isReturning) {
                    currentLoc.add(currentVelocity);
                    
                    double wobble = Math.sin(ticks * 0.3) * 0.05;
                    Location displayLoc = currentLoc.clone().add(0, wobble, 0);
                    mjolnirDisplay.teleport(displayLoc);
                    
                    targetRotationAngle += rotationSpeed;
                    rotationAngle += (targetRotationAngle - rotationAngle) * 0.15f;
                    Transformation transformation = mjolnirDisplay.getTransformation();
                    transformation.getLeftRotation().rotationX((float) Math.toRadians(90));
                    transformation.getLeftRotation().rotateY((float) Math.toRadians(rotationAngle));
                    mjolnirDisplay.setTransformation(transformation);
                    
                    Block targetBlock = currentLoc.getBlock();
                    if (!targetBlock.isPassable() || ticks >= 80) {
                        currentLoc.getWorld().strikeLightning(currentLoc);
                        
                        double areaDamage = mjolnirManager.getAreaDamage();
                        for (Entity entity : currentLoc.getWorld().getNearbyEntities(currentLoc, 2, 2, 2)) {
                            if (entity instanceof LivingEntity && entity != player) {
                                ((LivingEntity) entity).damage(areaDamage, player);
                            }
                        }
                        isReturning = true;
                        return;
                    }
                    
                    for (Entity entity : currentLoc.getWorld().getNearbyEntities(currentLoc, 1, 1, 1)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            if (entity instanceof Player) {
                                Player targetPlayer = (Player) entity;
                                targetPlayer.getWorld().strikeLightning(targetPlayer.getLocation());
                                targetPlayer.damage(mjolnirManager.getThrowDamage(), player);
                            } else {
                                currentLoc.getWorld().strikeLightning(currentLoc);
                                double areaDamage = mjolnirManager.getAreaDamage();
                                for (Entity nearbyEntity : currentLoc.getWorld().getNearbyEntities(currentLoc, 2, 2, 2)) {
                                    if (nearbyEntity instanceof LivingEntity && nearbyEntity != player) {
                                        ((LivingEntity) nearbyEntity).damage(areaDamage, player);
                                    }
                                }
                            }
                            isReturning = true;
                            return;
                        }
                    }
                } else {
                    Location playerTarget = player.getLocation().add(0, 1, 0);
                    Vector returnVector = playerTarget.subtract(currentLoc).toVector().normalize().multiply(mjolnirManager.getAnimationReturnSpeed());
                    currentLoc.add(returnVector);
                    mjolnirDisplay.teleport(currentLoc);
                    
                    targetRotationAngle += rotationSpeed * mjolnirManager.getAnimationReturnRotationMultiplier();
                    rotationAngle += (targetRotationAngle - rotationAngle) * 0.2f;
                    Transformation transformation = mjolnirDisplay.getTransformation();
                    transformation.getLeftRotation().rotationX((float) Math.toRadians(90));
                    transformation.getLeftRotation().rotateY((float) Math.toRadians(rotationAngle));
                    mjolnirDisplay.setTransformation(transformation);
                    
                    if (currentLoc.distance(player.getLocation()) < 1.5) {
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.0f, 0.5f);
                        player.getInventory().addItem(mjolnirManager.createMjolnir());
                        mjolnirDisplay.remove();
                        this.cancel();
                        return;
                    }
                }
                
                currentLoc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, currentLoc, 5, 0.1, 0.1, 0.1, 0.05);
                
                if (ticks % 3 == 0) {
                    currentLoc.getWorld().spawnParticle(Particle.CLOUD, currentLoc, 2, 0.2, 0.2, 0.2, 0.01);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player attacker = (Player) event.getDamager();
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        
        if (!mjolnirManager.isMjolnir(weapon)) {
            return;
        }
        
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player target = (Player) event.getEntity();
        
        if (mjolnirManager.isOnMeleeCooldown(attacker)) {
            long remaining = mjolnirManager.getRemainingMeleeCooldown(attacker);
            attacker.sendMessage("§cМьёльнир перезаряжается! Осталось: §e" + remaining + " сек");
            event.setCancelled(true);
            return;
        }
        
        if (processingMjolnirDamage.contains(target.getUniqueId())) {
            return;
        }
        
        mjolnirManager.setMeleeCooldown(attacker);
        
        processingMjolnirDamage.add(target.getUniqueId());
        
        try {
            event.setDamage(mjolnirManager.getMeleeDamage());
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location lightningLoc = target.getLocation().add(0, 1, 0);
                    
                    LightningStrike lightning = target.getWorld().strikeLightningEffect(lightningLoc);
                    
                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                    
                    target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, lightningLoc, 15, 0.3, 0.3, 0.3, 0.05);
                }
            }.runTaskLater(plugin, 1L);
        } finally {
            processingMjolnirDamage.remove(target.getUniqueId());
        }
    }

}
