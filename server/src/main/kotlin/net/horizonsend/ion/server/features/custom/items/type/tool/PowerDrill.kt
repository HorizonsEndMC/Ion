package net.horizonsend.ion.server.features.custom.items.type.tool

import net.horizonsend.ion.common.extensions.alertAction
import net.horizonsend.ion.server.features.custom.blocks.CustomBlockListeners
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.leftClickListener
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModification
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.drops.DropModifier
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.drops.DropSource
import net.horizonsend.ion.server.features.custom.items.util.serialization.SerializationManager
import net.horizonsend.ion.server.features.custom.items.util.serialization.token.IntegerToken
import net.horizonsend.ion.server.features.custom.items.util.serialization.token.ItemModificationToken
import net.horizonsend.ion.server.features.custom.items.util.serialization.token.ListToken
import net.horizonsend.ion.server.miscellaneous.utils.getNMSBlockData
import net.horizonsend.ion.server.miscellaneous.utils.isShulkerBox
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.toLocation
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
import org.bukkit.craftbukkit.block.CraftBlock
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.roundToInt

class PowerDrill(identifier: String, displayName: Component, modLimit: Int, basePowerCapacity: Int, model: String) : PowerTool(identifier, displayName, modLimit, basePowerCapacity, model) {
	override val serializationManager: SerializationManager = SerializationManager().apply {
		addSerializedData(
			"power",
			IntegerToken,
			{ customItem, itemStack -> customItem.getComponent(CustomComponentTypes.POWER_STORAGE).getPower(itemStack) },
			{ customItem: CustomItem, itemStack: ItemStack, data: Int -> customItem.getComponent(CustomComponentTypes.POWER_STORAGE).setPower(customItem, itemStack, data) }
		)
		addSerializedData(
			"mods",
			ListToken(ItemModificationToken()),
			{ customItem, itemStack -> customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getMods(itemStack).toList() },
			{ customItem: CustomItem, itemStack: ItemStack, data: List<ItemModification> -> customItem.getComponent(CustomComponentTypes.MOD_MANAGER).setMods(itemStack, customItem, data.toTypedArray()) }
		)
	}

	override val customComponents: CustomItemComponentManager = super.customComponents.apply {
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, leftClickListener(this@PowerDrill) { event, _, item ->
			handleClick(event.player, item, event)
		})
	}

	private fun handleClick(player: Player, itemStack: ItemStack, event: PlayerInteractEvent) {
		if (event.player.gameMode != GameMode.SURVIVAL) return

		val origin = event.clickedBlock ?: return

		val blockList = mutableListOf(origin)

		val modManger = getComponent(CustomComponentTypes.MOD_MANAGER)
		val powerManager = getComponent(CustomComponentTypes.POWER_STORAGE)
		val mods = modManger.getMods(itemStack)

		mods.filterNot { it.crouchingDisables && player.isSneaking }
			.filterIsInstance<net.horizonsend.ion.server.features.custom.items.type.tool.mods.tool.BlockListModifier>()
			.sortedBy { it.priority }
			.forEach {
				it.modifyBlockList(event.blockFace, origin, blockList)
			}

		var availablePower = powerManager.getPower(itemStack)
		val powerUse = powerManager.getPowerUse(itemStack, this)
		var broken = 0

		val drops = mutableMapOf<Long, Collection<ItemStack>>()

		for (block in blockList) {
			if (availablePower < powerUse) {
				player.alertAction("Out of power!")
				break
			}

			val usage = PowerHoe.UsageReference()

			if (tryBreakBlock(player, block, mods, drops, usage)) {
				availablePower -= (powerUse * usage.multiplier).roundToInt()
				broken++
			}
		}

		if (broken <= 0) return

		player.world.playSound(player.location, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.1f, 1.5f)

		powerManager.setPower(this, itemStack, availablePower)

		for ((key, items) in drops) {
			val location = BlockPos.of(key).toLocation(player.world)
			items.forEach { player.world.dropItemNaturally(location, it) }
		}

		return
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
					if (it.modifyDrop(drop) && it is net.horizonsend.ion.server.features.custom.items.type.tool.mods.tool.PowerUsageIncrease) {
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
