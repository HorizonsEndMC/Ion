package net.horizonsend.ion.server.features.custom.items.mods.tool.hoe

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.ModificationItem
import net.horizonsend.ion.server.features.custom.items.powered.PowerHoe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier
import kotlin.reflect.KClass

object FertilizerDispenser : ItemModification {
	override val identifier: String = "FERTILIZER_DISPENSER"
	override val displayName: Component = text("Fertilizer Dispenser")

	override val applicableTo: Array<KClass<out CustomItem>> = arrayOf(PowerHoe::class)
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf()

	override val modItem: Supplier<ModificationItem?> = Supplier { null }

	override val crouchingDisables: Boolean = true

	fun fertilizeCrop(player: Player, block: Block, hoe: ItemStack): Boolean {
		if (!player.inventory.contains(Material.BONE_MEAL)) return false

		val result = block.applyBoneMeal(BlockFace.DOWN)

		if (result) {
			player.inventory.removeItemAnySlot(ItemStack(Material.BONE_MEAL))
		}

		return result
	}
}
