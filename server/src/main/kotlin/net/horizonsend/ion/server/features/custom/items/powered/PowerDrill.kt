package net.horizonsend.ion.server.features.custom.items.powered

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object PowerDrill : CustomItem("POWER_DRILL"), ModdedPowerItem {
	override val basePowerCapacity: Int = 100_000
	override val displayName: Component = ofChildren(text("Power ", GOLD), text("Drill", GRAY))
		.decoration(TextDecoration.ITALIC, false)

	override val material: Material = Material.DIAMOND_PICKAXE
	override val customModelData: Int = 1

	override fun constructItemStack(): ItemStack {
		val base = getModeledItem()

		setPower(base, getPowerCapacity(base))

		return base.updateMeta {
			it.displayName(displayName)
			it.persistentDataContainer.set(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, identifier)
		}
	}

	override fun handlePrimaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent) {
		if (livingEntity !is Player) return

		val block = event.clickedBlock ?: return
		val blockType = block.type
		val customBlock = CustomBlocks.getByBlock(block)

		if (blockType == Material.BEDROCK || blockType == Material.BARRIER) {
			return
		}

		if (!BlockBreakEvent(block, livingEntity).callEvent()) {
			return
		}

		if (getPower(itemStack) < 20) {
			livingEntity.userError("Out of power!")
			return
		}

		removePower(itemStack, 10)

		livingEntity.world.playSound(livingEntity.location, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.1f, 1.5f)
		block.world.playEffect(block.location, Effect.STEP_SOUND, blockType)

		block.breakNaturally(PICKAXE)

		// customBlock turns to AIR due to BlockBreakEvent; play break sound and drop item
		if (customBlock != null) {
			block.world.playSound(block.location.toCenterLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 1.0f)
			Tasks.sync {
				for (drop in customBlock.getDrops(itemStack)) {
					livingEntity.world.dropItem(block.location.toCenterLocation(), drop)
				}
			}
		}
		if (blockType == Material.END_PORTAL_FRAME) {
			livingEntity.world.dropItem(block.location, ItemStack(Material.END_PORTAL_FRAME))
		}

		return
	}


	private val PICKAXE = ItemStack(Material.DIAMOND_PICKAXE, 1)
}
