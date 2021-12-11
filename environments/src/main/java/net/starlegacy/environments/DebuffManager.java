package net.starlegacy.environments;

import kotlin.Unit;
import net.starlegacy.feature.space.Space;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static net.starlegacy.util.CoordinatesKt.isInside;
import static net.starlegacy.util.ItemsKt.updateMeta;

class DebuffManager {
    static void init() {
        Bukkit.getScheduler().runTaskTimer(Environments.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                World world = player.getWorld();

                if (Space.INSTANCE.getPlanet(world) == null || player.getGameMode() != GameMode.SURVIVAL) {
                    continue;
                }

                boolean inside = isInside(player.getLocation(), 2);
                boolean wearingSuit = Environments.isWearingEnvironmentSuit(player);

                int slowness = 0;
                int slowDigging = 0;
                int hunger = 0;

                if (wearingSuit && player.getInventory().getHelmet().getType() == Material.CHAINMAIL_HELMET) {
                    slowness = 1;
                }

                int temperature = Environments.getTemperature(world);

                if (!Environments.isBreathable(world) && !wearingSuit && !inside) {
                    player.damage(1);
                }

                if (Environments.isCold(world) && !inside) {
                    slowness = Math.abs(temperature) - 1;
                    slowDigging = Math.abs(temperature) - 1;

                    if (temperature < -1) {
                        if (wearingSuit) {
                            if (Math.random() < 0.05 * Math.abs(temperature)) {
                                for (ItemStack item : player.getInventory().getArmorContents()) {
                                    if (item == null || !item.getType().name().contains("CHAIN")) {
                                        continue;
                                    }

                                    item.setDurability((short) (item.getDurability() + 1));
                                }
                            }
                        } else {
                            player.damage(0.25 * Math.abs(temperature));
                        }
                    }
                }

                if (Environments.isHot(world) && !inside && ((world.getTime() < 12300 || world.getTime() > 23850) ||
                        world.getEnvironment() != World.Environment.NORMAL)) {
                    slowness = temperature - 1;
                    if (temperature > 1) {
                        if (temperature > 2) {
                            if (wearingSuit) {
                                if (Math.random() < 0.05 * temperature) {
                                    for (ItemStack item : player.getInventory().getArmorContents()) {
                                        if (item != null && item.getType().name().contains("CHAIN")) {
                                            updateMeta(item, meta -> {
                                                Damageable damageable = (Damageable) meta;
                                                damageable.setDamage(damageable.getDamage() + 1);
                                                return Unit.INSTANCE;
                                            });
                                        }
                                    }
                                }
                            } else {
                                player.damage(0.2 * Math.abs(temperature));
                            }
                        }

                        slowDigging = temperature - 2;
                        hunger = temperature - 2;
                    }
                }

                if (Environments.isRadioactive(world)) {
                    if (wearingSuit) {
                        if (Math.random() < 0.1) {
                            for (ItemStack i : player.getInventory().getArmorContents()) {
                                if (i != null && i.getType().name().contains("CHAIN")) {
                                    i.setDurability((short) (i.getDurability() + 1));
                                }
                            }
                        }
                    } else {
                        player.damage(1);
                    }

                    player.playSound(player.getLocation(), Sound.ENTITY_SNOW_GOLEM_HURT, 0.5f, 0.5f);
                }

                double gravity = Environments.getGravity(world);

                if (gravity < 1) {
                    player.removePotionEffect(PotionEffectType.JUMP);
                    player.removePotionEffect(PotionEffectType.LEVITATION);

                    if (player.hasGravity() && !inside) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 30, 2));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 30, -3));
                    }
                }

                player.removePotionEffect(PotionEffectType.SLOW);
                player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
                player.removePotionEffect(PotionEffectType.HUNGER);

                if (slowness != 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, slowness - 1));
                }

                if (slowDigging != 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, slowDigging - 1));
                }

                if (hunger != 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, hunger - 1));
                }

                for (ItemStack item : player.getInventory().getArmorContents()) {
                    if (item != null && item.getDurability() < 0) {
                        item.setAmount(0);
                    }
                }
            }
        }, 20, 20);
        Bukkit.getPluginManager().registerEvents(new DebuffListener(), Environments.getInstance());
    }

    private static class DebuffListener implements Listener {
        @EventHandler
        public void onStructureGrow(StructureGrowEvent event) {
            if (Space.INSTANCE.getPlanet(event.getWorld()) == null) {
                return;
            }

            if ((Environments.isBreathable(event.getWorld()) && event.getWorld().getEnvironment() == World.Environment.NORMAL)) {
                return;
            }

            if (isInside(event.getLocation(), 2)) {
                return;
            }

            event.setCancelled(true);
        }
    }
}
