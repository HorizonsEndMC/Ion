package net.horizonsend.ion.server.features.custom.items.powered

import net.horizonsend.ion.common.extensions.alertAction
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.custom.blocks.CustomBlockListeners
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.drops.DropModifier
import net.horizonsend.ion.server.features.custom.items.mods.drops.DropSource
import net.horizonsend.ion.server.features.custom.items.mods.tool.BlockListModifier
import net.horizonsend.ion.server.features.custom.items.mods.tool.PowerUsageIncrease
import net.horizonsend.ion.server.features.custom.items.objects.CustomModeledItem
import net.horizonsend.ion.server.features.custom.items.objects.LoreCustomItem
import net.horizonsend.ion.server.features.custom.items.objects.ModdedCustomItem
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.getNMSBlockData
import net.horizonsend.ion.server.miscellaneous.utils.isShulkerBox
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.toLocation
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.BaseFireBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.IceBlock
import net.minecraft.world.level.block.LevelEvent
import net.minecraft.world.level.block.TurtleEggBlock
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Effect
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlock
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.math.roundToInt

class PowerDrill(
	identifier: String,
	override val displayName: Component,
	override val modLimit: Int,
	override val basePowerCapacity: Int,
	override val customModelData: Int
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
			it.persistentDataContainer.set(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, identifier)
			it.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
		}

		setPower(base, getPowerCapacity(base))
		setMods(base, arrayOf())

		rebuildLore(base, asTask = false)

		return base
	}

	override fun handlePrimaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent) {
		if (event.player.gameMode != GameMode.SURVIVAL) return

		if (livingEntity !is Player) return

		val origin = event.clickedBlock ?: return

		val blockList = mutableListOf(origin)

		val mods = getMods(itemStack)

		mods.filterNot { it.crouchingDisables && livingEntity.isSneaking }
			.filterIsInstance<BlockListModifier>()
			.sortedBy { it.priority }
			.forEach {
				it.modifyBlockList(event.blockFace, origin, blockList)
			}

		var availablePower = getPower(itemStack)
		val powerUse = getPowerUse(itemStack)
		var broken = 0

		val drops = mutableMapOf<Long, Collection<ItemStack>>()

		if (CombatTimer.isPvpCombatTagged(livingEntity)) {
			livingEntity.userError("Cannot use Power Drills while in combat")
			return
		}

		for (block in blockList) {
			if (availablePower < powerUse) {
				livingEntity.alertAction("Out of power!")
				break
			}

			val usage = PowerHoe.UsageReference()

			if (tryBreakBlock(livingEntity, block, mods, drops, usage)) {
				availablePower -= (powerUse * usage.multiplier).roundToInt()
				broken++
			}
		}

		if (broken <= 0) return

		livingEntity.world.playSound(livingEntity.location, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.1f, 1.5f)

		setPower(itemStack, availablePower)

		for ((key, items) in drops) {
			val location = BlockPos.of(key).toLocation(livingEntity.world)
			items.forEach { livingEntity.world.dropItemNaturally(location, it) }
		}

		return
	}

	override fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent?) {
		if (livingEntity is Player && livingEntity.isSneaking) openMenu(livingEntity, itemStack)
	}

	companion object {
		fun tryBreakBlock(
			player: Player,
			block: Block,
			mods: Array<ItemModification>,
			drops: MutableMap<Long, Collection<ItemStack>>,
			usage: PowerHoe.UsageReference
		): Boolean {
			val blockType = block.type
			val customBlock = CustomBlocks.getByBlock(block)

			if (blockType == Material.BEDROCK || blockType == Material.BARRIER || blockType.isShulkerBox) {
				return false
			}

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

			// customBlock turns to AIR due to BlockBreakEvent; play break sound and drop item
			if (customBlock != null) {
				block.world.playSound(block.location.toCenterLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 1.0f)
				block.world.playEffect(block.location, Effect.STEP_SOUND, Material.IRON_ORE)

				val baseDrops = dropSource.getDrop(customBlock)
				usage.multiplier = handleModifiers(baseDrops, dropModifiers)

				drops[BlockPos.asLong(block.x, block.y, block.z)] = baseDrops
			} else {
				val baseDrops = dropSource.getDrop(block)
				usage.multiplier = handleModifiers(baseDrops, dropModifiers)

				drops[BlockPos.asLong(block.x, block.y, block.z)] = baseDrops
				block.world.playEffect(block.location, Effect.STEP_SOUND, blockType)
			}

			breakNaturally(block, dropSource.shouldDropXP)

			if (blockType == Material.END_PORTAL_FRAME) block.world.dropItem(block.location, ItemStack(Material.END_PORTAL_FRAME))

			return true
		}

		fun handleModifiers(drops: Collection<ItemStack>, dropModifiers: Collection<DropModifier>): Double {
			var multiplier = 1.0

			for (drop in drops) {
				dropModifiers.forEach {
					if (it.modifyDrop(drop) && it is PowerUsageIncrease) {
						multiplier *= it.usageMultiplier
					}
				}
			}

			return multiplier
		}

		private val defaultPickaxe = ItemStack(Material.DIAMOND_PICKAXE)

		/**
		 * A modified version of the Spigot API Block#breakNaturally
		 *
		 * This will break a block as normal but not drop any items. XP Will be dropped as normal, if desired
		 **/
		fun breakNaturally(block: Block, dropExperience: Boolean, tool: ItemStack? = null) {
			block as CraftBlock
			val nmsTool = CraftItemStack.asNMSCopy(tool ?: defaultPickaxe)

			val level = block.world.minecraft
			val pos = block.position

			val blockState: BlockState = block.getNMSBlockData()
			val nmsBlock = blockState.block

			// Modelled off EntityHuman#hasBlock
			if (nmsBlock !== Blocks.AIR && (!blockState.requiresCorrectToolForDrops() || nmsTool.isCorrectToolForDrops(blockState))) {
//				net.minecraft.world.level.block.Block.dropResources(
//					blockState,
//					level,
//					pos,
//					level.getBlockEntity(block.position),
//					null,
//					nmsTool,
//					false
//				)

				if (blockState.block is BaseFireBlock) level.levelEvent(LevelEvent.SOUND_EXTINGUISH_FIRE, pos, 0)
				if (dropExperience) nmsBlock.popExperience(level, pos, nmsBlock.getExpDrop(blockState, level, pos, nmsTool, true))
			}

			val destroyed: Boolean = level.removeBlock(pos, false)
			if (destroyed) nmsBlock.destroy(level, pos, blockState)

			// special cases
			when (nmsBlock) {
				is IceBlock -> nmsBlock.afterDestroy(level, pos, nmsTool)
				is TurtleEggBlock -> nmsBlock.decreaseEggs(level, pos, blockState)
			}
		}
	}
}
