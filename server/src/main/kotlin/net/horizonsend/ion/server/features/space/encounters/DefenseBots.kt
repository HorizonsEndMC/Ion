package net.horizonsend.ion.server.features.space.encounters

import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.hint
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.features.customitems.CustomItems
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.INACTIVE
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.LOCKED
import net.horizonsend.ion.server.miscellaneous.utils.castSpawnEntity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.nbt.CompoundTag
import net.starlegacy.util.toBlockPos
import net.horizonsend.ion.server.miscellaneous.registrations.updateMeta
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.EntityType
import org.bukkit.entity.Skeleton
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

object DefenseBots : Encounter(identifier = "defense_bots") {
	override fun onChestInteract(event: PlayerInteractEvent) {
		val targetedBlock = event.clickedBlock!!
		val chest = (targetedBlock.state as? Chest) ?: return

		val keyCode = targetedBlock.location.toBlockPos().hashCode()
			.toString().toList().chunked(4).map { chars ->
				chars.joinToString().filter { it.isDigit() }.toInt().toChar()
			}.joinToString()

		if ((event.item?.lore()?.get(0) as? TextComponent)?.content() == keyCode) {
			event.player.success("The key card unlocked the chest!")

			Encounters.setChestFlag(chest, LOCKED, "false")
			Encounters.setChestFlag(chest, INACTIVE, "true")
			return
		}

		if (Encounters.getChestFlag(chest, LOCKED) == "true") {
			event.isCancelled = true
			event.player.alert("The chest was still locked! More security droids have appeared!")
			event.isCancelled = true
		} else {
			// Not a success condition, just if it hasn't been set yet
			Encounters.setChestFlag(chest, LOCKED, "true")

			event.player.alert("The disturbance you caused has activated ancient security droids!")
			event.player.hint("Maybe one of them still has a card to open this chest...")
			event.isCancelled = true
		}

		val blocks = Encounters.getBlocks(
			chest.world,
			chest.location.toBlockPos(),
			10.0
		) { Encounters.checkAir(it) && it.isSolid }
		val firstFour = blocks.shuffled().subList(0, 3)

		for (block in firstFour) {
			val blockAboveLoc = block.location.add(Location(chest.world, 0.0, 1.0, 0.0)).toCenterLocation()

			chest.world.castSpawnEntity<Skeleton>(blockAboveLoc, EntityType.SKELETON).apply {
				this.equipment.itemInMainHandDropChance = 0.0f
				this.equipment.itemInOffHandDropChance = 1.0f

				val weirdPistol = CustomItems.PISTOL.constructItemStack()
				weirdPistol.type = Material.BOW
				weirdPistol.updateMeta {
					it.displayName(
						Component.text("Rusty Blaster Pistol").color(TextColor.fromHexString("#802716"))
							.decoration(TextDecoration.ITALIC, false)
							.decoration(TextDecoration.BOLD, false)
					)
				}

				val keyCard = ItemStack(Material.PAPER).updateMeta {
					it.displayName(
						Component.text("Key Card").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE)
					)
					it.lore(
						listOf(Component.text(keyCode).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA))
					)
				}

				this.equipment.setItemInOffHand(keyCard)
				this.equipment.setItemInMainHand(weirdPistol)
				this.customName(Component.text("Security Droid").color(NamedTextColor.RED).decorate(TextDecoration.BOLD))
				this.isCustomNameVisible = true
				this.isSilent = true
				this.isPersistent = false
			}
		}
	}

	override fun constructChestNBT(): CompoundTag {
		return Encounters.createLootChest("horizonsend:chests/gun_parts")
	}
}

