package net.horizonsend.ion.server.features.world.environment

import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.ItemModKeys
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.miscellaneous.utils.PerPlayerCooldown
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.concurrent.TimeUnit

fun isWearingSpaceSuit(player: Player): Boolean {
	val inventory = player.inventory

	return inventory.helmet?.type == Material.CHAINMAIL_HELMET &&
		inventory.chestplate?.type == Material.CHAINMAIL_CHESTPLATE &&
		inventory.leggings?.type == Material.CHAINMAIL_LEGGINGS &&
		inventory.boots?.type == Material.CHAINMAIL_BOOTS
}

private val directionArray = arrayOf(
	BlockFace.EAST,
	BlockFace.WEST,
	BlockFace.SOUTH,
	BlockFace.NORTH,
	BlockFace.UP,
	BlockFace.DOWN
)

fun isInside(location: Location, extraChecks: Int): Boolean {
	fun getRelative(location: Location, direction: BlockFace, i: Int): Block? {
		val x = (direction.modX * i).d()
		val y = (direction.modY * i).d()
		val z = (direction.modZ * i).d()
		val newLocation = location.clone().add(x, y, z)

		return when {
			location.world.isChunkLoaded(newLocation.blockX shr 4, newLocation.blockZ shr 4) -> newLocation.block
			else -> null
		}
	}

	if (location.isChunkLoaded && !location.block.type.isAir) {
		return true
	}

	val airBlocks = HashSet<Block>()

	quickLoop@
	for (direction in directionArray) {
		if (direction.oppositeFace == direction) {
			continue
		}

		var block: Block?

		for (i in 1..189) {
			block = getRelative(location, direction, i)

			if (block == null) {
				continue@quickLoop
			}

			if (block.type != Material.AIR) {
				val relative = getRelative(location, direction, i - 1)

				if (relative != null) {
					airBlocks.add(relative)
				}

				continue@quickLoop
			}
		}
		return false
	}

	var check = 0

	while (check < extraChecks && airBlocks.isNotEmpty()) {
		edgeLoop@ for (airBlock in airBlocks.toList()) {
			for (direction in directionArray) {
				if (direction.oppositeFace == direction) {
					continue
				}

				var block: Block?

				for (i in 0..189) {
					block = getRelative(airBlock.location, direction, i)

					if (block == null) {
						break
					}

					if (block.type != Material.AIR) {
						if (i != 0) {
							airBlocks.add(airBlock.getRelative(direction, i))
						}

						airBlocks.remove(airBlock)
						continue@edgeLoop
					}
				}

				return false
			}
		}
		check++
	}

	return true
}

val pressureFieldPowerCooldown = PerPlayerCooldown(1, TimeUnit.SECONDS)
val environmentModuleCooldown = PerPlayerCooldown(1, TimeUnit.SECONDS)

/**
 * Returns the item stack of power armor in the provided slot if it contains the specified mod
 **/
private fun Player.getModdedPowerArmorItem(mod: IonRegistryKey<ItemModification, out ItemModification>, slot: EquipmentSlot): ItemStack? {
	val item = equipment.getItem(slot)
	if (item.isEmpty) return null

	val customItem = item.customItem ?: return null

	if (!customItem.hasComponent(CustomComponentTypes.MOD_MANAGER)) return null
	val mods = customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getModKeys(item)
	if (!mods.contains(mod)) return null

	return item
}

/**
 * Consumes the provided power
 *
 * Returns whether the power could be consumed
 **/
private fun Player.consumeModPower(mod: IonRegistryKey<ItemModification, out ItemModification>, slot: EquipmentSlot, powerUsage: Int, cooldown: PerPlayerCooldown): Boolean {
	val item = getModdedPowerArmorItem(mod, slot) ?: return false
	val customItem = item.customItem ?: return false

	val power = customItem.getComponent(CustomComponentTypes.POWER_STORAGE)
	if (power.getPower(item) < powerUsage) return false

	cooldown.tryExec(this) {
		power.removePower(item, customItem, powerUsage)
	}

	return true
}

fun tickPressureFieldModule(player: Player, powerUsage: Int): Boolean {
	return player.consumeModPower(ItemModKeys.PRESSURE_FIELD, EquipmentSlot.HEAD, powerUsage, pressureFieldPowerCooldown)
}

fun tickEnvironmentModule(player: Player, powerUsage: Int): Boolean {
	return player.consumeModPower(ItemModKeys.ENVIRONMENT, EquipmentSlot.HEAD, powerUsage, pressureFieldPowerCooldown)
}

