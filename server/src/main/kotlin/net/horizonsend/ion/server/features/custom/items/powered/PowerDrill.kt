package net.horizonsend.ion.server.features.custom.items.powered

import net.horizonsend.ion.common.extensions.alertAction
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.blocks.CustomBlockListeners
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.mods.tool.BlockListModifier
import net.horizonsend.ion.server.features.custom.items.mods.tool.drops.DropModifier
import net.horizonsend.ion.server.features.custom.items.objects.CustomModeledItem
import net.horizonsend.ion.server.features.custom.items.objects.LoreCustomItem
import net.horizonsend.ion.server.features.custom.items.objects.ModdedCustomItem
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.getNMSBlockData
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.toLocation
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.TextDecoration
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

object PowerDrill : CustomItem("POWER_DRILL"), ModdedPowerItem, CustomModeledItem {
	val displayName: Component = ofChildren(text("Power ", GOLD), text("Drill", GRAY)).decoration(TextDecoration.ITALIC, false)
	override val basePowerCapacity: Int = 50_000
	override val basePowerUsage: Int = 10

	override val material: Material = Material.DIAMOND_PICKAXE
	override val customModelData: Int = 1

	override val displayDurability: Boolean = true

	override fun getLoreManagers(): List<LoreCustomItem.CustomItemLoreManager> {
		return listOf(
			PoweredItem.PowerLoreManager,
			ModdedCustomItem.ModLoreManager,
		)
	}

	override fun constructItemStack(): ItemStack {
		val base = getModeledItem()

		setPower(base, getPowerCapacity(base))

		return base.updateMeta {
			it.displayName(displayName)
			it.persistentDataContainer.set(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, identifier)
			it.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
		}
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

		for (block in blockList) {
			if (availablePower < powerUse) {
				livingEntity.alertAction("Out of power!")
				break
			}

			if (tryBreakBlock(livingEntity, block, mods, drops)) {
				availablePower -= powerUse
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

	override fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack) {
		if (livingEntity is Player && livingEntity.isSneaking) openMenu(livingEntity, itemStack)
	}

	fun tryBreakBlock(
		player: Player,
		block: Block,
		mods: Array<ItemModification>,
		drops: MutableMap<Long, Collection<ItemStack>>,
	): Boolean {
		val blockType = block.type
		val customBlock = CustomBlocks.getByBlock(block)

		if (blockType == Material.BEDROCK || blockType == Material.BARRIER) {
			return false
		}

		val event = BlockBreakEvent(block, player)
		CustomBlockListeners.noDropEvents.add(event)

		if (!event.callEvent()) {
			return false
		}

		val dropProvider = mods
			.filterNot { it.crouchingDisables && player.isSneaking }
			.filterIsInstance<DropModifier>()
			.firstOrNull() ?: DropModifier.DEFAULT_DROP_PROVIDER

		// customBlock turns to AIR due to BlockBreakEvent; play break sound and drop item
		if (customBlock != null) {
			block.world.playSound(block.location.toCenterLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 1.0f)
			block.world.playEffect(block.location, Effect.STEP_SOUND, Material.IRON_ORE)

			drops[BlockPos.asLong(block.x, block.y, block.z)] = dropProvider.getDrop(customBlock)
		} else {
			drops[BlockPos.asLong(block.x, block.y, block.z)] = dropProvider.getDrop(block)
			block.world.playEffect(block.location, Effect.STEP_SOUND, blockType)
		}

		block.type = Material.AIR
		breakNaturally(block, dropProvider.shouldDropXP)

		if (blockType == Material.END_PORTAL_FRAME) block.world.dropItem(block.location, ItemStack(Material.END_PORTAL_FRAME))

		return true
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
			net.minecraft.world.level.block.Block.dropResources(
				blockState,
				level,
				pos,
				level.getBlockEntity(block.position),
				null,
				nmsTool,
				false
			)

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
