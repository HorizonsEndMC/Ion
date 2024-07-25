package net.horizonsend.ion.server.features.custom.items.mods.drops

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.horizonsend.ion.server.features.custom.items.minerals.Smeltable
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.ModificationItem
import net.horizonsend.ion.server.features.custom.items.mods.tool.PowerUsageIncrease
import net.horizonsend.ion.server.features.custom.items.objects.ModdedCustomItem
import net.horizonsend.ion.server.features.custom.items.powered.PowerChainsaw
import net.horizonsend.ion.server.features.custom.items.powered.PowerDrill
import net.horizonsend.ion.server.features.custom.items.powered.PowerHoe
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.world.SimpleContainer
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SmeltingRecipe
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import java.util.Optional
import java.util.function.Supplier
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass

object AutoSmeltModifier : ItemModification, DropModifier, PowerUsageIncrease {
	override val displayName: Component = "<gradient:red:yellow>Auto Smelt".miniMessage().decoration(TextDecoration.ITALIC, false)
	override val identifier: String = "AUTO_SMELT"

	override val crouchingDisables: Boolean = false

	override val applicableTo: Array<KClass<out ModdedCustomItem>> = arrayOf(PowerDrill::class, PowerChainsaw::class, PowerHoe::class)
	override val incompatibleWithMods: Array<KClass<out ItemModification>> = arrayOf(FortuneModifier::class, SilkTouchSource::class, AutoSmeltModifier::class)

	override val modItem: Supplier<ModificationItem?> = Supplier { CustomItems.AUTO_SMELT }

	override val usageMultiplier: Double = 2.0

	override val priority: Int = 1

	override fun modifyDrop(itemStack: ItemStack): Boolean {
		val customItem = itemStack.customItem
		if (customItem is Smeltable) return false

		// Replace with modified version
		smeltedItemCache[itemStack].getOrNull()?.let {
			itemStack.type = it.type
			itemStack.itemMeta = it.itemMeta
		}

		return true
	}

	private val level = Bukkit.getServer().worlds.first().minecraft

	private val smeltedItemCache: LoadingCache<ItemStack, Optional<ItemStack>> = CacheBuilder.newBuilder().build(CacheLoader.from { baseDrop ->
		val optional: Optional<RecipeHolder<SmeltingRecipe>> = level
			.recipeManager
			.getRecipeFor(RecipeType.SMELTING, SimpleContainer(CraftItemStack.asNMSCopy(baseDrop)), level)

		optional.map {
			val itemStack = optional.get().value().getResultItem(level.registryAccess())

			itemStack.copyWithCount(baseDrop.amount).asBukkitCopy()
		}
	})
}
