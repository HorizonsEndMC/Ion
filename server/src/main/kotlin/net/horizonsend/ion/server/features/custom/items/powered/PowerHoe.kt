package net.horizonsend.ion.server.features.custom.items.powered

import net.horizonsend.ion.common.extensions.alertAction
import net.horizonsend.ion.server.features.custom.blocks.CustomBlockListeners
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.mods.ItemModRegistry.AUTO_REPLANT
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.drops.DropModifier
import net.horizonsend.ion.server.features.custom.items.mods.drops.DropSource
import net.horizonsend.ion.server.features.custom.items.mods.tool.BlockListModifier
import net.horizonsend.ion.server.features.custom.items.mods.tool.hoe.FertilizerDispenser
import net.horizonsend.ion.server.features.custom.items.objects.CustomModeledItem
import net.horizonsend.ion.server.features.custom.items.objects.LoreCustomItem
import net.horizonsend.ion.server.features.custom.items.objects.ModdedCustomItem
import net.horizonsend.ion.server.features.multiblock.farming.Crop
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.enumSetOf
import net.horizonsend.ion.server.miscellaneous.utils.toLocation
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.minecraft.core.BlockPos
import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.SoundCategory
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Ageable
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING
import java.util.EnumSet
import kotlin.math.roundToInt

class PowerHoe(
	identifier: String,
	override val displayName: Component,
	override val modLimit: Int,
	override val basePowerCapacity: Int,
	override val customModelData: Int
) : CustomItem(identifier), ModdedPowerItem, CustomModeledItem {
	override val basePowerUsage: Int = 10
	override val displayDurability: Boolean = true

	override val material: Material = Material.DIAMOND_PICKAXE

	override fun constructItemStack(): ItemStack {
		val base = getModeledItem()

		setPower(base, getPowerCapacity(base))

		return base.updateMeta {
			it.displayName(displayName)
			it.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
			it.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
		}
	}

	override fun getLoreManagers(): List<LoreCustomItem.CustomItemLoreManager> {
		return listOf(
			PoweredItem.PowerLoreManager,
			ModdedCustomItem.ModLoreManager,
		)
	}

	/**
	 * Block types that may erroneously trigger the open menu, instead of the intended use of the blocks
	 **/
	private val ignoreMenuOpen: EnumSet<Material> = enumSetOf(
		Material.WHEAT,
		Material.POTATOES,
		Material.CARROTS,
		Material.BEETROOTS,
		Material.NETHER_WART,
		Material.DIRT,
	)

	override fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent?) {
		if (livingEntity !is Player) return

		val type = livingEntity.getTargetBlockExact(4, FluidCollisionMode.NEVER)?.type

		if (!ignoreMenuOpen.contains(type) && livingEntity.isSneaking) return openMenu(livingEntity, itemStack)

		val block = event?.clickedBlock ?: return

		val mods = getMods(itemStack)

		// If targeting a crop, harvest it
		if (Crop[block.type] != null) return tryHarvest(livingEntity, mods, itemStack, block)

		// Else try to use the hoe
		tryTill(livingEntity, itemStack, block)
	}

	override fun handlePrimaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent) {
		if (livingEntity !is Player) return
		val block = event.clickedBlock ?: return

		val mods = getMods(itemStack)

		tryHarvest(livingEntity, mods, itemStack, block)
	}

	private fun tryHarvest(player: Player, mods: Array<ItemModification>, itemStack: ItemStack, origin: Block) {
		val blockList = compileBlockList(player, origin, itemStack)

		var availablePower = getPower(itemStack)
		val powerUse = getPowerUse(itemStack)
		var broken = 0

		val drops = mutableMapOf<Long, Collection<ItemStack>>()

		for (block in blockList) {
			if (availablePower < powerUse) {
				player.alertAction("Out of power!")
				break
			}

			val usage = UsageReference()

			if (handleHarvest(player, mods, block, drops, usage)) {
				availablePower -= (powerUse * usage.multiplier).roundToInt()
				broken++
			}
		}

		for ((key, items) in drops) {
			val location = BlockPos.of(key).toLocation(origin.world)
			items.forEach { origin.world.dropItemNaturally(location, it) }
		}

		if (broken <= 0) return

		setPower(itemStack, availablePower)
	}

	data class UsageReference(var multiplier: Double = 1.0)

	private fun handleHarvest(
		player: Player,
		mods: Array<ItemModification>,
		block: Block,
		drops: MutableMap<Long, Collection<ItemStack>>,
		usage: UsageReference
	): Boolean {
		val data = block.blockData

		if (data !is Ageable) return false

		if (data.age != data.maximumAge) {
			if (!mods.contains(FertilizerDispenser)) return false

			return FertilizerDispenser.fertilizeCrop(player, block)
		}

		val crop = Crop[block.type] ?: return false

		val event = BlockBreakEvent(block, player)
		CustomBlockListeners.noDropEvents.add(event)

		if (!event.callEvent()) {
			return false
		}

		val dropSource = mods
			.filterNot { it.crouchingDisables && player.isSneaking }
			.filterIsInstance<DropSource>()
			.firstOrNull() ?: DropSource.DEFAULT_DROP_PROVIDER

		val dropModifiers = mods
			.filterIsInstance<DropModifier>()
			.sortedByDescending { it.priority }

		val dropList = dropSource.getDrop(block)

		usage.multiplier = PowerDrill.handleModifiers(dropList, dropModifiers)
		drops[BlockPos.asLong(block.x, block.y, block.z)] = dropList

		var replacement = Material.AIR.createBlockData()

		if (mods.contains(AUTO_REPLANT)) {
			replacement = crop.material.createBlockData()
		}

		PowerDrill.breakNaturally(block, true)
		block.setBlockData(replacement, true)

		return true
	}

	private fun tryTill(player: Player, itemStack: ItemStack, origin: Block) {
		val blockList = compileBlockList(player, origin, itemStack)

		var availablePower = getPower(itemStack)
		val powerUse = getPowerUse(itemStack)
		var broken = 0

		for (block in blockList) {
			if (availablePower < powerUse) {
				player.alertAction("Out of power!")
				break
			}

			if (processTill(player, block)) {
				availablePower -= powerUse
				broken++

				origin.world.playSound(block.location, "minecraft:item.hoe.till", SoundCategory.BLOCKS, 1.0f, 1.0f)
			}
		}

		if (broken <= 0) return
		setPower(itemStack, availablePower)
	}

	private fun processTill(player: Player, block: Block): Boolean {
		val type = block.type

		val event = BlockBreakEvent(block, player)
		CustomBlockListeners.noDropEvents.add(event)

		if (!event.callEvent()) {
			return false
		}

		if (TILL_ABLE.contains(type) && block.getRelative(BlockFace.UP).type.isAir) {
			block.setType(Material.FARMLAND, true)

			return true
		}

		if (DIRT_CONVERTIBLE.contains(type)) {
			block.setType(Material.DIRT, true)

			return true
		}

		return false
	}

	private fun compileBlockList(player: Player, origin: Block, itemStack: ItemStack) : List<Block> {
		val blockList = mutableListOf(origin)

		val mods = getMods(itemStack)

		mods.filterNot { it.crouchingDisables && player.isSneaking }
			.filterIsInstance<BlockListModifier>()
			.sortedBy { it.priority }
			.forEach {
				it.modifyBlockList(BlockFace.DOWN, origin, blockList)
			}

		return blockList
	}

	companion object {
		private val TILL_ABLE = enumSetOf(Material.DIRT, Material.DIRT_PATH, Material.DIRT, Material.GRASS_BLOCK)
		private val DIRT_CONVERTIBLE = enumSetOf(Material.ROOTED_DIRT, Material.COARSE_DIRT, Material.DIRT)
	}
}
