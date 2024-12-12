package net.horizonsend.ion.server.features.custom.items.type.tool.mods.drops

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.attribute.AdditionalPowerConsumption
import net.horizonsend.ion.server.features.custom.items.attribute.CustomItemAttribute
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerChainsaw
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerDrill
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerHoe
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ModificationItem
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.item.crafting.SmeltingRecipe
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import java.util.Optional
import java.util.function.Supplier
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass

object AutoSmeltModifier : ItemModification, DropModifier {
	override val displayName: Component = "<gradient:red:yellow>Auto Smelt".miniMessage().decoration(TextDecoration.ITALIC, false)
	override val identifier: String = "AUTO_SMELT"

	override val crouchingDisables: Boolean = false

	override val applicableTo: Array<KClass<out CustomItem>> = arrayOf(PowerDrill::class, PowerChainsaw::class, PowerHoe::class)
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf(AutoSmeltModifier::class)

	override val modItem: Supplier<ModificationItem?> = Supplier { CustomItemRegistry.AUTO_SMELT }

	override val priority: Int = 1

	override fun modifyDrop(itemStack: ItemStack): Boolean {
		val customItem = itemStack.customItem
		if (customItem?.hasComponent(CustomComponentTypes.SMELTABLE) == true) return false

		// Replace with modified version
		smeltedItemCache[itemStack].getOrNull()?.let {
			itemStack.type = it.type
			itemStack.itemMeta = it.itemMeta
		}

		return true
	}

	private val level get() = Bukkit.getServer().worlds.first().minecraft

	private val smeltedItemCache: LoadingCache<ItemStack, Optional<ItemStack>> = CacheBuilder.newBuilder().build(CacheLoader.from { baseDrop ->
		val input = SingleRecipeInput(CraftItemStack.asNMSCopy(baseDrop))
		val optional: Optional<RecipeHolder<SmeltingRecipe>> = level
			.recipeAccess()
			.getRecipeFor(RecipeType.SMELTING, input, level)

		optional.map {
			val itemStack = optional.get().value().assemble(input, level.registryAccess())

			itemStack.copyWithCount(baseDrop.amount).asBukkitCopy()
		}
	})

	override fun getAttributes(): List<CustomItemAttribute> = listOf(AdditionalPowerConsumption(2.0))
}
