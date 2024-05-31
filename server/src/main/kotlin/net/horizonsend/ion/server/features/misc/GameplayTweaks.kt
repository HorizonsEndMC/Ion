package net.horizonsend.ion.server.features.misc

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.FLYABLE_BLOCKS
import net.horizonsend.ion.server.features.starship.Mass
import net.horizonsend.ion.server.miscellaneous.utils.ANVIL_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.GLAZED_TERRACOTTA_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.SLAB_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_GLASS_PANE_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_GLASS_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_TERRACOTTA_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.STAIR_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.TRAPDOOR_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.WALL_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.world.PortalCreateEvent

object GameplayTweaks : IonServerComponent() {
	override fun onEnable() {
		// important: don't use anything from the materials.kt utility class here,
		// in order to avoid this being initialized after that, and that giving wrong blast resistance
		modifyBlastResistance()

		listen<BlockPhysicsEvent> { event -> if (physicsDisabled) event.isCancelled = true }
		listen<ItemSpawnEvent> { event -> event.entity.isInvulnerable = true }
	}

	@EventHandler
	fun onSnowLayerExist(event: BlockFormEvent) {
		if (event.newState.type == Material.SNOW &&
			FLYABLE_BLOCKS.contains(event.block.getRelative(BlockFace.DOWN).type)
		) {
			event.isCancelled = true
		}
	}

	@EventHandler
	fun onMushroomianPhysics(event: BlockPhysicsEvent) {
		if (event.block.type != Material.BROWN_MUSHROOM_BLOCK) {
			return
		}

		event.isCancelled = true
	}

	@EventHandler
	fun onEatGoldApple(event: PlayerItemConsumeEvent) {
		val item = event.item

		when (item.type) {
			Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE -> item.type = Material.APPLE
			Material.GOLDEN_CARROT -> item.type = Material.CARROT
			else -> return
		}

		event.setItem(item)
	}

	@EventHandler
	fun onPortalCreate(event: PortalCreateEvent) {
		event.isCancelled = true
	}

	@EventHandler
	fun onFireworkBoost(event: PlayerElytraBoostEvent) {
		event.isCancelled = true
	}

	private var physicsDisabled = false

	/** Runs the code with all physics events cancelled during its execution. Must be on the main thread. */
	fun withPhysicsDisabled(block: () -> Unit) {
		require(Bukkit.isPrimaryThread())

		physicsDisabled = true

		try {
			block()
		} finally {
			physicsDisabled = false
		}
	}

	private fun modifyBlastResistance() {
		setBlastResistance(Material.OBSIDIAN, 8.0f)
		setBlastResistance(Material.CRYING_OBSIDIAN, 8.0f)
		setBlastResistance(Material.NETHERITE_BLOCK, 8.0f)
		setBlastResistance(Material.BROWN_MUSHROOM_BLOCK, 6.0f)
		setBlastResistance(Material.LAPIS_BLOCK, 6.0f)
		setBlastResistance(Material.FURNACE, 6.0f)
		setBlastResistance(Material.DISPENSER, 6.0f)
		setBlastResistance(Material.IRON_TRAPDOOR, 6.0f)
		STAINED_GLASS_TYPES.forEach { setBlastResistance(it, 5.0f) }
		setBlastResistance(Material.END_STONE, 5.0f)
		setBlastResistance(Material.END_PORTAL_FRAME, 5.0f)
		setBlastResistance(Material.END_STONE_BRICKS, 6.0f)
		setBlastResistance(Material.END_STONE_BRICK_STAIRS, 6.0f)
		setBlastResistance(Material.END_STONE_BRICK_SLAB, 6.0f)
		setBlastResistance(Material.QUARTZ_STAIRS, 6.0f)
		setBlastResistance(Material.SANDSTONE_STAIRS, 6.0f)
		setBlastResistance(Material.RED_SANDSTONE_STAIRS, 6.0f)
		STAINED_TERRACOTTA_TYPES.forEach { setBlastResistance(it, 6.0f) }
		GLAZED_TERRACOTTA_TYPES.forEach { setBlastResistance(it, 6.0f) }
		SLAB_TYPES.forEach { setBlastResistance(it, 6.0f) }
		STAIR_TYPES.forEach { setBlastResistance(it, 6.0f) }
		STAINED_GLASS_PANE_TYPES.forEach { setBlastResistance(it, 5.0f) }
		TRAPDOOR_TYPES.forEach { setBlastResistance(it, 6.0f)}
		WALL_TYPES.forEach { setBlastResistance(it, 6.0f)}
		ANVIL_TYPES.forEach { setBlastResistance(it, 6.0f)}
		setBlastResistance(Material.ANCIENT_DEBRIS, 6.0f)
		setBlastResistance(Material.NETHERITE_BLOCK, 6.0f)
		setBlastResistance(Material.RESPAWN_ANCHOR, 6.0f)
		setBlastResistance(Material.ENCHANTING_TABLE, 6.0f)

		// allow underwater explosions, cancel the liquid from actually exploding
		setBlastResistance(Material.WATER, 0.0f)
		setBlastResistance(Material.LAVA, 0.0f)
		listen<EntityExplodeEvent>(priority = EventPriority.LOWEST) { event ->
			event.blockList().removeIf { it.isLiquid }
		}
		listen<BlockExplodeEvent>(priority = EventPriority.LOWEST) { event ->
			event.blockList().removeIf { it.isLiquid }
		}
	}

	private fun setBlastResistance(material: Material, resistance: Float) {
		require(material.isBlock)

		val block = CraftMagicNumbers.getBlock(material)
		val field = BlockBehaviour::class.java.getDeclaredField("aH") // obfuscation for explosionResistance
		field.isAccessible = true
		field.set(block, resistance)

		// For some reason, stairs, and stairs only have a parent block, from which the blast resistance is referenced from.
		if (block is StairBlock) {
			val baseField = StairBlock::class.java.getDeclaredField("J") // Parent block
			baseField.isAccessible = true
			val baseBlock = baseField.get(block)

			field.set(baseBlock, resistance)
		}

		// ignore if overridden
		if (Mass[material] != material.blastResistance * Mass.BLAST_RESIST_MASS_MULTIPLIER) {
			return
		}

		Mass[material] = resistance * Mass.BLAST_RESIST_MASS_MULTIPLIER
	}
}
