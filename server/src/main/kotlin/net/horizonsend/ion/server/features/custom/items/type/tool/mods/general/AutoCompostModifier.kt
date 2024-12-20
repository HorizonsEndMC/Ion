package net.horizonsend.ion.server.features.custom.items.type.tool.mods.general

import net.horizonsend.ion.common.utils.miscellaneous.testRandom
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerChainsaw
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerDrill
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerHoe
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ApplicationPredicate
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ModificationItem
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.drops.DropModifier
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.minecraft.world.level.block.ComposterBlock
import org.bukkit.Material
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier
import kotlin.reflect.KClass

object AutoCompostModifier : ItemModification, DropModifier {
	override val identifier: String = "AUTO_COMPOST"
	override val displayName: Component = text("Auto Composter", HEColorScheme.HE_LIGHT_GRAY)
	override val applicationPredicates: Array<ApplicationPredicate> = arrayOf(
		ApplicationPredicate.ClassPredicate(PowerDrill::class),
		ApplicationPredicate.ClassPredicate(PowerHoe::class),
		ApplicationPredicate.ClassPredicate(PowerChainsaw::class)
	)
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf()
	override val modItem: Supplier<ModificationItem?> = Supplier { CustomItemRegistry.AUTO_COMPOST }

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

	override fun getAttributes(): List<CustomItemAttribute> = listOf()
}
