package net.horizonsend.ion.server.features.custom.items.mods.general

import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.items.ModificationItem
import net.horizonsend.ion.server.features.custom.items.mods.tool.drops.AutoSmeltModifier
import net.horizonsend.ion.server.features.custom.items.mods.tool.drops.DropModifier
import net.horizonsend.ion.server.features.custom.items.mods.tool.drops.SilkTouchModifier
import net.horizonsend.ion.server.features.custom.items.powered.PowerChainsaw
import net.horizonsend.ion.server.features.custom.items.powered.PowerDrill
import net.horizonsend.ion.server.features.custom.items.powered.PowerHoe
import net.kyori.adventure.text.Component
import net.minecraft.world.level.block.ComposterBlock
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier
import kotlin.random.Random
import kotlin.reflect.KClass

object AutoCompostModifier : ItemModification, DropModifier {
	override val identifier: String = "AUTO_COMPOST"
	override val displayName: Component = Component.text("AUTO_COMPOST")
	override val applicableTo: Array<KClass<out CustomItem>> = arrayOf(PowerDrill::class, PowerChainsaw::class, PowerHoe::class)
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf(AutoSmeltModifier::class, SilkTouchModifier::class)
	override val modItem: Supplier<ModificationItem?> = Supplier { null }

	override val crouchingDisables: Boolean = false
	override val shouldDropXP: Boolean = false
	override val usedTool: ItemStack? = null

	override fun getDrop(block: Block): Collection<ItemStack> {
		val drops = block.drops

		return drops.map {
			val nms = CraftItemStack.asNMSCopy(it)
			val percentage = ComposterBlock.COMPOSTABLES.getFloat(nms.item)

			if (percentage == -1.0f) return@map it

			val testChance = Random.nextDouble()

			if (percentage / 8.0f > testChance) ItemStack(Material.BONE_MEAL) else ItemStack(Material.AIR)
		}
	}

	override fun getDrop(block: CustomBlock): Collection<ItemStack> {
		// No compostable custom blocks currently
		return block.drops.getDrops(null, false)
	}
}
