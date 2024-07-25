package net.horizonsend.ion.server.features.custom.items.mods.general

import net.horizonsend.ion.common.utils.miscellaneous.testRandom
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.ModificationItem
import net.horizonsend.ion.server.features.custom.items.mods.drops.AutoSmeltModifier
import net.horizonsend.ion.server.features.custom.items.mods.drops.DropModifier
import net.horizonsend.ion.server.features.custom.items.mods.drops.SilkTouchSource
import net.horizonsend.ion.server.features.custom.items.objects.ModdedCustomItem
import net.horizonsend.ion.server.features.custom.items.powered.PowerChainsaw
import net.horizonsend.ion.server.features.custom.items.powered.PowerDrill
import net.horizonsend.ion.server.features.custom.items.powered.PowerHoe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.minecraft.world.level.block.ComposterBlock
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier
import kotlin.reflect.KClass

object AutoCompostModifier : ItemModification, DropModifier {
	override val identifier: String = "AUTO_COMPOST"
	override val displayName: Component = text("Automatic Composter", HEColorScheme.HE_LIGHT_GRAY)
	override val applicableTo: Array<KClass<out ModdedCustomItem>> = arrayOf(PowerDrill::class, PowerChainsaw::class, PowerHoe::class)
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf(AutoSmeltModifier::class, SilkTouchSource::class)
	override val modItem: Supplier<ModificationItem?> = Supplier { CustomItems.AUTO_COMPOST }

	override val crouchingDisables: Boolean = false

	override val priority: Int = 1

	override fun modifyDrop(itemStack: ItemStack): Boolean {
		val nms = CraftItemStack.asNMSCopy(itemStack)
		val percentage = ComposterBlock.COMPOSTABLES.getFloat(nms.item)

		if (percentage == -1.0f) return false

		if (testRandom(percentage / 8.0f)) return false

		itemStack.type = Material.BONE_MEAL
		itemStack.itemMeta = null

		return true
	}
}
