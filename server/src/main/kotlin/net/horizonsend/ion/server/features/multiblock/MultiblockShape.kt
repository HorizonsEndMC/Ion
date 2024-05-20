package net.horizonsend.ion.server.features.multiblock

import net.horizonsend.ion.server.features.customblocks.CustomBlock
import net.horizonsend.ion.server.features.customblocks.CustomBlocks
import net.horizonsend.ion.server.features.customblocks.CustomBlocks.REACTOR_CORE
import net.horizonsend.ion.server.features.multiblock.areashield.AreaShield10.buildStructure
import net.horizonsend.ion.server.features.transport.Extractors
import net.horizonsend.ion.server.features.transport.Wires
import net.horizonsend.ion.server.features.transport.pipe.Pipes
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.CONCRETE_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.MATERIALS
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_TERRACOTTA_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.TERRACOTTA_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.blockFace
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.getNMSBlockData
import net.horizonsend.ion.server.miscellaneous.utils.getNMSBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.isButton
import net.horizonsend.ion.server.miscellaneous.utils.isConcrete
import net.horizonsend.ion.server.miscellaneous.utils.isDaylightSensor
import net.horizonsend.ion.server.miscellaneous.utils.isDoor
import net.horizonsend.ion.server.miscellaneous.utils.isFroglight
import net.horizonsend.ion.server.miscellaneous.utils.isGlass
import net.horizonsend.ion.server.miscellaneous.utils.isGlassPane
import net.horizonsend.ion.server.miscellaneous.utils.isRedstoneLamp
import net.horizonsend.ion.server.miscellaneous.utils.isSlab
import net.horizonsend.ion.server.miscellaneous.utils.isStainedGlass
import net.horizonsend.ion.server.miscellaneous.utils.isStainedGlassPane
import net.horizonsend.ion.server.miscellaneous.utils.isStairs
import net.horizonsend.ion.server.miscellaneous.utils.isTrapdoor
import net.horizonsend.ion.server.miscellaneous.utils.isWall
import net.horizonsend.ion.server.miscellaneous.utils.isWool
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.minecraft.world.level.block.AbstractFurnaceBlock
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Slab
import java.util.EnumSet
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/** Parameters: block, inward, load chunks */
private typealias BlockRequirement = (Block, BlockFace, Boolean) -> Boolean

class MultiblockShape {
	// Cache of direction to requirement, so it doesn't need to be calculated every time based on the direction
	private val requirements = mutableMapOf<BlockFace, MutableMap<Vec3i, Pair<BlockData, BlockRequirement>>>()

	var signCentered = false
	private var ignoreDirection = false // ignore whether inward direction matches

	fun signCentered() {
		signCentered = true
	}

	fun ignoreDirection() {
		ignoreDirection = true
	}

	/**
	 * Creates a requirement builder with the specified coordinates. Use another method after it.
	 *
	 * @param x The blocks right of the sign's attached block
	 * @param y The blocks above the sign's attached block
	 * @param z The blocks inwards of the sign's attached block
	 *
	 * Example:
	 * ```Kotlin
	 *     at(-1, -1, +0).extractor()
	 *     at(+0, -1, +0).wireInputComputer()
	 *     at(+1, -1, +0).extractor()
	 *
	 *     at(+0, +0, +0).machineFurnace()
	 * ```
	 */
	fun at(x: Int, y: Int, z: Int) =
		RequirementBuilder(
			shape = this,
			right = x,
			upward = y,
			inward = z
		)

	/**
	 * This is used for structuring it by y, z, x. See the area shield multiblocks for an example.
	 *
	 * @sample net.horizonsend.ion.server.features.multiblock.areashield.AreaShield10.buildStructure
	 */
	fun z(z: Int, block: Z.() -> Unit) = Z(
		this,
		z
	).block()

	fun z(zValues: IntProgression, block: Z.() -> Unit) {
		for (z in zValues) {
			z(z, block)
		}
	}

	class Z(private val shape: MultiblockShape, val z: Int) {
		fun y(y: Int, block: ZY.() -> Unit) = ZY(
			shape,
			z,
			y
		).block()

		fun y(yValues: IntProgression, block: ZY.() -> Unit) {
			for (y in yValues) {
				y(y, block)
			}
		}

		class ZY(private val shape: MultiblockShape, val z: Int, val y: Int) {
			fun x(x: Int) = shape.at(x, y, z)

			fun x(xValues: IntProgression, block: RequirementBuilder.() -> Unit) {
				for (x in xValues) {
					block(x(x))
				}
			}
		}
	}

	fun y(y: Int, block: Y.() -> Unit) = Y(
		this,
		y
	).block()

	fun y(yValues: IntProgression, block: Y.() -> Unit) {
		for (y in yValues) {
			y(y, block)
		}
	}

	class Y(private val shape: MultiblockShape, val y: Int) {
		fun z(z: Int, block: YZ.() -> Unit) = YZ(
			shape,
			y,
			z
		).block()

		fun z(zValues: IntProgression, block: YZ.() -> Unit) {
			for (z in zValues) {
				z(z, block)
			}
		}

		class YZ(private val shape: MultiblockShape, val y: Int, val z: Int) {
			fun x(x: Int) = shape.at(x, y, z)

			fun x(xValues: IntProgression, block: RequirementBuilder.() -> Unit) {
				for (x in xValues) {
					block(x(x))
				}
			}
		}
	}

	fun getRequirementMap(inwardFace: BlockFace): MutableMap<Vec3i, Pair<BlockData, BlockRequirement>> {
		return requirements.getOrPut(inwardFace) {
			require(CARDINAL_BLOCK_FACES.contains(inwardFace)) { "Unsupported inward direction: $inwardFace" }
			return@getOrPut mutableMapOf()
		}
	}

	fun getLocations(face: BlockFace): Set<Vec3i> = requirements.getValue(face).keys

	fun checkRequirements(origin: Block, inward: BlockFace, loadChunks: Boolean, particles: Boolean = false): Boolean {
		val realOrigin = if (signCentered) origin.getRelative(inward.oppositeFace) else origin

		// check all directions if ignoring direction
		for (face in if (ignoreDirection) CARDINAL_BLOCK_FACES else listOf(inward)) {
			if (checkRequirementsSpecific(realOrigin, face, loadChunks, particles)) {
				return true
			}
		}
		return false
	}

	fun checkRequirementsSpecific(origin: Block, face: BlockFace, loadChunks: Boolean, particles: Boolean): Boolean {
		return getRequirementMap(face).all { (offset, requirementMap) ->
			val (_, requirement) = requirementMap
			val x = offset.x
			val y = offset.y
			val z = offset.z
			val relative: Block = if (loadChunks) {
				origin.getRelative(x, y, z)
			} else {
				origin.getRelativeIfLoaded(x, y, z) ?: return@all false
			}

			val requirementMet = requirement(relative, face, loadChunks)

			if (!requirementMet && particles) {
				val location = relative.location.toCenterLocation().add(0.0, 0.4, 0.0)
				relative.world.spawnParticle(Particle.SPELL_WITCH, location, 1)
			}

			return@all requirementMet
		}
	}

	private val allLocations = mutableSetOf<Vec3i>() // used for checking for duplicates

	private fun addRequirement(right: Int, upward: Int, inward: Int, type: BlockData, requirement: BlockRequirement) {
		if (!allLocations.add(Vec3i(right, upward, inward))) {
			Exception("Multiblock ${javaClass.simpleName} has more than one block at $right, $upward, $inward!").printStackTrace()
		}

		CARDINAL_BLOCK_FACES.forEach { inwardFace ->
			val rightFace = inwardFace.rightFace
			val x = rightFace.modX * right + inwardFace.modX * inward
			val z = rightFace.modZ * right + inwardFace.modZ * inward
			val intTrio = Vec3i(x, upward, z)
			val requirementMap = getRequirementMap(inwardFace)

			requirementMap[intTrio] = type to requirement
		}
	}

	@Suppress("unused")
	class RequirementBuilder(val shape: MultiblockShape, val right: Int, val upward: Int, val inward: Int) {
		private fun complete(type: BlockData, requirement: BlockRequirement) = shape.addRequirement(right, upward, inward, type, requirement)

		fun type(type: Material) {
			complete(type.createBlockData()) { block, _, _ -> block.getTypeSafe() == type }
		}

		fun anyType(vararg types: Material) {
			val typeSet = EnumSet.copyOf(types.toList())
			complete(types.first().createBlockData()) { block, _, loadChunks ->
				typeSet.contains(if (loadChunks) block.type else block.getTypeSafe())
			}
		}

		fun customBlock(customBlock: CustomBlock) {
			complete(customBlock.blockData) { block, _, _ -> CustomBlocks.getByBlock(block) === customBlock }
		}

		fun anyType(types: Iterable<Material>) = anyType(*types.toList().toTypedArray())

		fun filteredTypes(filter: (Material) -> Boolean) = anyType(MATERIALS.filter(filter))

		fun carbyne() = anyType(CONCRETE_TYPES)

		fun terracotta() = anyType(TERRACOTTA_TYPES)
		fun stainedTerracotta() = anyType(STAINED_TERRACOTTA_TYPES)

		fun glass() = type(Material.GLASS)
		fun anvil() = type(Material.ANVIL)
		fun reactorCore() = customBlock(REACTOR_CORE)
		fun stainedGlass() = filteredTypes { it.isStainedGlass }
		fun anyGlass() = filteredTypes { it.isGlass }
		fun seaLantern() = type(Material.SEA_LANTERN)
		fun glassPane() = type(Material.GLASS_PANE)
		fun stainedGlassPane() = filteredTypes { it.isStainedGlassPane }
		fun anyGlassPane() = filteredTypes { it.isGlassPane }

		fun anyStairs() = filteredTypes { it.isStairs }

		fun anyWall() = filteredTypes { it.isWall }

		fun anyWool() = filteredTypes { it.isWool }

		fun anySlab() = filteredTypes { it.isSlab }
		fun anyDoubleSlab() = complete(
			Material.STONE_BRICK_SLAB.createBlockData().apply {
				this as Slab
				this.type = Slab.Type.DOUBLE
			}
		) { block, _, loadChunks ->
			val blockData: BlockData? = if (loadChunks) block.blockData else getBlockDataSafe(block.world, block.x, block.y, block.z)
			return@complete blockData is Slab && blockData.type == Slab.Type.DOUBLE
		}

		fun terracottaOrDoubleslab() {
			complete(
				Material.TERRACOTTA.createBlockData()
			) { block, _, loadChunks ->
				val blockData: BlockData? = if (loadChunks) block.blockData else getBlockDataSafe(block.world, block.x, block.y, block.z)
				val blockType = if (loadChunks) block.type else block.getTypeSafe()

				return@complete (blockData is Slab && blockData.type == Slab.Type.DOUBLE)
					|| TERRACOTTA_TYPES.contains(blockType)
			}
		}

		fun concrete() = filteredTypes { it.isConcrete }

		fun sculkCatalyst() = type(Material.SCULK_CATALYST)
		fun stoneBrick() = type(Material.STONE_BRICKS)

		fun ironBlock() = type(Material.IRON_BLOCK)
		fun goldBlock() = type(Material.GOLD_BLOCK)
		fun diamondBlock() = type(Material.DIAMOND_BLOCK)
		fun netheriteBlock() = type(Material.NETHERITE_BLOCK)
		fun emeraldBlock() = type(Material.EMERALD_BLOCK)
		fun redstoneBlock() = type(Material.REDSTONE_BLOCK)
		fun lapisBlock() = type(Material.LAPIS_BLOCK)
		fun copperBlock() = anyType(
			Material.COPPER_BLOCK,
			Material.EXPOSED_COPPER,
			Material.WEATHERED_COPPER,
			Material.OXIDIZED_COPPER,
			Material.WAXED_COPPER_BLOCK,
			Material.WAXED_EXPOSED_COPPER,
			Material.WAXED_WEATHERED_COPPER,
			Material.WAXED_OXIDIZED_COPPER,
			Material.CUT_COPPER,
			Material.EXPOSED_CUT_COPPER,
			Material.WEATHERED_CUT_COPPER,
			Material.OXIDIZED_CUT_COPPER,
			Material.WAXED_CUT_COPPER,
			Material.WAXED_EXPOSED_CUT_COPPER,
			Material.WAXED_WEATHERED_CUT_COPPER,
			Material.WAXED_OXIDIZED_CUT_COPPER
		)

		fun titaniumBlock() = customBlock(CustomBlocks.TITANIUM_BLOCK)
		fun aluminumBlock() = customBlock(CustomBlocks.ALUMINUM_BLOCK)
		fun chetheriteBlock() = customBlock(CustomBlocks.CHETHERITE_BLOCK)
		fun steelBlock() = customBlock(CustomBlocks.STEEL_BLOCK)
		fun wireInputComputer() = type(Wires.INPUT_COMPUTER_BLOCK)

		fun redstoneLamp() = filteredTypes { it.isRedstoneLamp }

		fun daylightSensor() = filteredTypes { it.isDaylightSensor }
		fun craftingTable() = type(Material.CRAFTING_TABLE)

		fun extractor() = type(Extractors.EXTRACTOR_BLOCK)

		fun glowstone() = type(Material.GLOWSTONE)

		fun sponge() = type(Material.SPONGE)
		fun endRod() = type(Material.END_ROD)

		fun hopper() = type(Material.HOPPER)

		fun anyDoor() = filteredTypes { it.isDoor }
		fun anyButton() = filteredTypes { it.isButton }

		fun anyPipedInventory() = filteredTypes { Pipes.isPipedInventory(it) }

		fun pistonBase() = type(Material.PISTON)
		fun pistonHead() = type(Material.PISTON_HEAD)

		fun furnace() = type(Material.FURNACE)
		fun dispenser() = type(Material.DISPENSER)
		fun lodestone() = type(Material.LODESTONE)
		fun noteBlock() = type(Material.NOTE_BLOCK)
		fun anyTrapdoor() = filteredTypes { it.isTrapdoor }
		fun anyFroglight() = filteredTypes { it.isFroglight }

		fun thrusterBlock() = anyType(
			Material.OCHRE_FROGLIGHT,
			Material.VERDANT_FROGLIGHT,
			Material.PEARLESCENT_FROGLIGHT,
			Material.GLOWSTONE,
			Material.REDSTONE_LAMP,
			Material.MAGMA_BLOCK,
			Material.SEA_LANTERN
		)

		fun lightningRod() = type(Material.LIGHTNING_ROD)

		fun machineFurnace() = complete(Material.FURNACE.createBlockData()) { block, inward, loadChunks ->
			val blockData = if (loadChunks) block.getNMSBlockData() else
				getNMSBlockDataSafe(block.world, block.x, block.y, block.z) ?: return@complete false

			if (blockData.bukkitMaterial != Material.FURNACE) return@complete false
			val facing = blockData.getValue(AbstractFurnaceBlock.FACING).blockFace
			return@complete facing == inward.oppositeFace
		}

		fun solidBlock() = anyType(
			Material.STONE_BRICKS,
			Material.CHISELED_STONE_BRICKS,
			Material.SMOOTH_STONE,
			Material.POLISHED_GRANITE,
			Material.POLISHED_DIORITE,
			Material.POLISHED_ANDESITE,
			Material.POLISHED_DEEPSLATE,
			Material.DEEPSLATE_BRICKS,
			Material.CHISELED_DEEPSLATE,
			Material.DEEPSLATE_TILES,
			Material.BRICKS,
			Material.MUD_BRICKS,
			Material.CHISELED_SANDSTONE,
			Material.SMOOTH_SANDSTONE,
			Material.CUT_SANDSTONE,
			Material.CHISELED_RED_SANDSTONE,
			Material.SMOOTH_RED_SANDSTONE,
			Material.CUT_RED_SANDSTONE,
			Material.PRISMARINE_BRICKS,
			Material.DARK_PRISMARINE,
			Material.NETHER_BRICKS,
			Material.CHISELED_NETHER_BRICKS,
			Material.RED_NETHER_BRICKS,
			Material.POLISHED_BLACKSTONE,
			Material.POLISHED_BLACKSTONE_BRICKS,
			Material.CHISELED_POLISHED_BLACKSTONE,
			Material.END_STONE_BRICKS,
			Material.PURPUR_BLOCK,
			Material.SMOOTH_QUARTZ,
			Material.QUARTZ_BRICKS,
			Material.QUARTZ_BLOCK,
		)
	}
}
