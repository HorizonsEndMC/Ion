package net.horizonsend.ion.server.features.custom.items.powered

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.customBlock
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.general.AutoReplantModifier
import net.horizonsend.ion.server.features.custom.items.mods.tool.chainsaw.ExtendedBar
import net.horizonsend.ion.server.features.custom.items.objects.CustomModeledItem
import net.horizonsend.ion.server.features.custom.items.objects.LoreCustomItem
import net.horizonsend.ion.server.features.custom.items.objects.ModdedCustomItem
import net.horizonsend.ion.server.features.multiblock.farming.Crop
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.CUSTOM_ITEM
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.isFence
import net.horizonsend.ion.server.miscellaneous.utils.isLeaves
import net.horizonsend.ion.server.miscellaneous.utils.isLog
import net.horizonsend.ion.server.miscellaneous.utils.isWood
import net.horizonsend.ion.server.miscellaneous.utils.toLocation
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.minecraft.core.BlockPos
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType.STRING
import org.bukkit.scheduler.BukkitRunnable
import java.util.ArrayDeque
import kotlin.math.roundToInt

class PowerChainsaw(
	identifier: String,
	override val displayName: Component,
	override val modLimit: Int,
	override val basePowerCapacity: Int,
	override val customModelData: Int,
	val initialBlocksBroken: Int,
) : CustomItem(identifier), ModdedPowerItem, CustomModeledItem {
	override val basePowerUsage: Int = 10
	override val displayDurability: Boolean = true

	override val material: Material = Material.DIAMOND_PICKAXE

	override fun getLoreManagers(): List<LoreCustomItem.CustomItemLoreManager> {
		return listOf(
			PoweredItem.PowerLoreManager,
			ModdedCustomItem.ModLoreManager,
		)
	}

	override fun constructItemStack(): ItemStack {
		val base = getModeledItem()

		base.updateMeta {
			it.displayName(displayName)
			it.persistentDataContainer.set(CUSTOM_ITEM, STRING, identifier)
			it.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
		}

		setPower(base, getPowerCapacity(base))
		setMods(base, arrayOf())

		rebuildLore(base, asTask = false)

		return base
	}

	override fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent?) {
		if (livingEntity is Player && livingEntity.isSneaking) openMenu(livingEntity, itemStack)
	}

	override fun handlePrimaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent) {
		if (event.player.gameMode != GameMode.SURVIVAL) return

		if (livingEntity !is Player) return

		val origin = event.clickedBlock ?: return

		val mods = getMods(itemStack)

		val maxDepth = if (mods.contains(ExtendedBar)) initialBlocksBroken + 100 else initialBlocksBroken

		PowerChainsawMineTask(
			player = livingEntity,
			chainsawItem = itemStack,
			chainsaw = this,
			mods = mods,
			origin = origin,
			maxDepth = maxDepth
		).runTaskTimer(IonServer, 0, 1)
	}

	class PowerChainsawMineTask(
		private val player: Player,
		private val chainsawItem: ItemStack,
		private val chainsaw: PowerChainsaw,
		private val mods: Array<ItemModification>,
		private val origin: Block,
		private val maxDepth: Int
	) : BukkitRunnable() {
		// blocks that are pending checking
		private val queue = ArrayDeque<Long>()

		// blocks that were already checked and should not be detected twice
		private val visited = mutableMapOf<Long, Block>()

		init {
			// Jumpstart the queue by adding the origin block
			val originKey = BlockPos.asLong(origin.x, origin.y, origin.z)
			visited[originKey] = origin
			queue.push(originKey)
		}

		override fun run() {
			if (visited.count() > maxDepth || queue.isEmpty() || !player.isOnline) {
				cancel()
				return
			}

			if (!player.inventory.contains(chainsawItem)) return

			val key = queue.removeFirst()
			val x = BlockPos.getX(key)
			val y = BlockPos.getY(key)
			val z = BlockPos.getZ(key)

			// Do not allow checking blocks larger than render distance
			val block = getBlockIfLoaded(origin.world, x, y, z) ?: return

			if (!canMine(block)) {
				// Immediately run again if it can't be mined
				run()
				return
			}

			visited[key] = block

			val powerUse = chainsaw.getPowerUse(chainsawItem)
			if (powerUse > chainsaw.getPower(chainsawItem)) {
				player.userError("Out of power!")
				cancel()
				return
			}

			var replacementType: Material? = null

			if (mods.contains(AutoReplantModifier) && (block.type.isWood || block.type.isLog)) {
				if (Crop.SWEET_BERRIES.canBePlanted(block)) {
					replacementType = saplingTypes[block.type]
				}
			}

			val drops = mutableMapOf<Long, Collection<ItemStack>>()

			val usage = PowerHoe.UsageReference()

			if (PowerDrill.tryBreakBlock(player, block, mods, drops, usage)) {
				chainsaw.removePower(chainsawItem, (powerUse * usage.multiplier).roundToInt())
			}

			for ((dropLocation, items) in drops) {
				val location = BlockPos.of(dropLocation).toLocation(origin.world)
				items.forEach { origin.world.dropItemNaturally(location, it) }
			}

			// Handle auto-replant
			replacementType?.let {
				block.type = it
			}

			for (offsetX in -1..1) for (offsetY in -1..1) for (offsetZ in -1..1) {
				val adjacentX = offsetX + x
				val adjacentY = offsetY + y
				val adjacentZ = offsetZ + z

				// Ensure it's a valid Y-level before adding it to the queue
				if (adjacentY < 0 || adjacentY > origin.world.maxHeight) {
					continue
				}

				val key1 = BlockPos.asLong(adjacentX, adjacentY, adjacentZ)

				if (key1 == BlockPos.asLong(x, y, z)) continue

				if (visited.containsKey(key1)) continue

				queue.addLast(key1)
			}
		}
	}

	companion object {
		fun canMine(block: Block): Boolean {
			if (block.customBlock != null) return false

			return block.type.isLeaves ||
				block.type.isWood ||
				block.type.isLog ||
				block.type.isFence ||
				block.type == Material.CRIMSON_STEM ||
				block.type == Material.CRIMSON_HYPHAE ||
				block.type == Material.WARPED_STEM ||
				block.type == Material.WARPED_HYPHAE ||
				block.type == Material.NETHER_WART_BLOCK ||
				block.type == Material.WARPED_WART_BLOCK ||
				block.type == Material.SHROOMLIGHT ||
				block.type == Material.VINE
		}

		val saplingTypes = mapOf(
			Material.OAK_WOOD to Material.OAK_SAPLING,
			Material.OAK_LOG to Material.OAK_SAPLING,
			Material.SPRUCE_WOOD to Material.SPRUCE_SAPLING,
			Material.SPRUCE_LOG to Material.SPRUCE_SAPLING,
			Material.BIRCH_WOOD to Material.BIRCH_SAPLING,
			Material.BIRCH_LOG to Material.BIRCH_SAPLING,
			Material.ACACIA_WOOD to Material.ACACIA_SAPLING,
			Material.ACACIA_LOG to Material.ACACIA_SAPLING,
			Material.DARK_OAK_WOOD to Material.DARK_OAK_SAPLING,
			Material.DARK_OAK_LOG to Material.DARK_OAK_SAPLING,
			Material.JUNGLE_WOOD to Material.JUNGLE_SAPLING,
			Material.JUNGLE_LOG to Material.JUNGLE_SAPLING,
			Material.CHERRY_WOOD to Material.CHERRY_SAPLING,
			Material.CHERRY_LOG to Material.CHERRY_SAPLING,
			Material.MANGROVE_WOOD to Material.MANGROVE_PROPAGULE,
			Material.MANGROVE_LOG to Material.MANGROVE_PROPAGULE,
			Material.CRIMSON_HYPHAE to Material.CRIMSON_FUNGUS,
			Material.CRIMSON_STEM to Material.CRIMSON_FUNGUS,
			Material.WARPED_HYPHAE to Material.WARPED_FUNGUS,
			Material.WARPED_STEM to Material.WARPED_FUNGUS
		)
	}
}
