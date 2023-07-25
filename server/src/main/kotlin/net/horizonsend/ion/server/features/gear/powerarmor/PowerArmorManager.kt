package net.horizonsend.ion.server.features.gear.powerarmor

import net.horizonsend.ion.server.features.misc.CustomItems
import net.horizonsend.ion.server.features.misc.getPower
import net.horizonsend.ion.server.features.misc.removePower
import net.horizonsend.ion.server.listener.gear.hasMovedInLastSecond
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.Material.LEATHER_BOOTS
import org.bukkit.Material.LEATHER_CHESTPLATE
import org.bukkit.Material.LEATHER_HELMET
import org.bukkit.Material.LEATHER_LEGGINGS
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.Locale
import java.util.UUID

object PowerArmorManager {

	private val glidingPlayers = mutableSetOf<UUID>()
	val glideDisabledPlayers = mutableMapOf<UUID, Long>() // UUID to end time of glide block

	fun init() {
		Tasks.syncRepeat(20, 20) {
			powerModuleTick()
		}

		Tasks.syncRepeat(1, 1) {
			tickRocketBoosters()
		}
	}

	private fun powerModuleTick() {
		for (player in Bukkit.getOnlinePlayers()) {
			for (item: ItemStack? in player.inventory.armorContents) {
				if (item == null || !isPowerArmor(item) || getPower(item) == 0) {
					continue
				}

				for (module in getModules(item)) {
					when (module) {
						PowerArmorModule.SPEED_BOOSTING -> {
							player.addPotionEffect(
								PotionEffect(PotionEffectType.SPEED, 20, 2, true, true)
							)

							if (hasMovedInLastSecond(player) && !player.world.name.lowercase(Locale.getDefault())
								.contains("arena")
							) {
								removePower(item, 1)
							}
						}

						PowerArmorModule.NIGHT_VISION -> {
							player.addPotionEffect(
								PotionEffect(PotionEffectType.NIGHT_VISION, 1000, 1, true, true)
							)
						}

						PowerArmorModule.ROCKET_BOOSTING -> {
							if (player.isGliding && !player.world.name.lowercase(Locale.getDefault())
								.contains("arena")
							) {
								removePower(item, 5)
							}
						}

						PowerArmorModule.ENVIRONMENT -> {
							player.addPotionEffect(
								PotionEffect(PotionEffectType.WATER_BREATHING, 20, 1, true, true)
							)
							removePower(item, 1)
						}

						else -> {
						}
					}
				}
			}
		}
	}

	private fun tickRocketBoosters() {
		loop@ for (uuid in glidingPlayers.toList()) {
			val player = Bukkit.getPlayer(uuid) ?: continue
			if ((glideDisabledPlayers[uuid] ?: 0) > System.currentTimeMillis()) continue
			glideDisabledPlayers[uuid]?.let { glideDisabledPlayers.remove(uuid) } // remove if not disabled

			if (player.isOnGround || !player.isSneaking) {
				toggleGliding(player)
				continue
			}

			for (item in player.inventory.armorContents) {
				if (!isPowerArmor(item) || getPower(item!!) == 0) {
					continue
				}

				for (module in getModules(item)) {
					if (module != PowerArmorModule.ROCKET_BOOSTING) {
						continue
					}

					player.isGliding = true
					player.velocity = player.velocity.midpoint(player.location.direction.multiply(0.6))
					player.world.spawnParticle(Particle.SMOKE_NORMAL, player.location, 5)

					if (!player.world.name.lowercase(Locale.getDefault()).contains("arena")) {
						removePower(item, 5)
					}

					player.world.playSound(player.location, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 2.0f)
					continue@loop
				}
			}

			// no rocket boosting module was found
			toggleGliding(player)
		}
	}

	fun isPowerArmor(item: ItemStack?): Boolean = CustomItems[item] is CustomItems.PowerArmorItem

	fun isModule(item: ItemStack?) = PowerArmorModule[item] != null

	fun getPowerArmorType(item: ItemStack?): PowerArmorType? {
		return if (item == null) {
			null
		} else {
			when (item.type) {
				LEATHER_HELMET -> PowerArmorType.HELMET
				LEATHER_CHESTPLATE -> PowerArmorType.CHESTPLATE
				LEATHER_LEGGINGS -> PowerArmorType.LEGGINGS
				LEATHER_BOOTS -> PowerArmorType.BOOTS
				else -> throw RuntimeException("Power armor can only be leather armor!")
			}
		}
	}

	fun getModules(item: ItemStack): Set<PowerArmorModule> {
		return item.lore
			?.filter { it.startsWith("Module: ") }
			?.mapNotNull { PowerArmorModule[it.split(" ")[1]] }
			?.toSet()
			?: setOf()
	}

	fun hasModule(item: ItemStack, module: PowerArmorModule): Boolean {
		return getModules(item).contains(module)
	}

	fun toggleGliding(player: Player) {
		if (glidingPlayers.contains(player.uniqueId)) {
			glidingPlayers.remove(player.uniqueId)
			player.isGliding = false
		} else {
			if (player.isOnGround) return
			glidingPlayers.add(player.uniqueId)
			player.isGliding = true
		}
	}
}
