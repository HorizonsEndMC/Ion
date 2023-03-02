package net.starlegacy.feature.multiblock

import net.minecraft.world.level.block.AbstractFurnaceBlock
import net.starlegacy.feature.misc.CustomBlock
import net.starlegacy.feature.misc.CustomBlocks
import net.starlegacy.feature.multiblock.areashield.AreaShield10.buildStructure
import net.starlegacy.feature.transport.Extractors
import net.starlegacy.feature.transport.Wires
import net.starlegacy.feature.transport.pipe.Pipes
import net.starlegacy.util.CARDINAL_BLOCK_FACES
import net.starlegacy.util.CONCRETE_TYPES
import net.starlegacy.util.MATERIALS
import net.starlegacy.util.STAINED_TERRACOTTA_TYPES
import net.starlegacy.util.Vec3i
import net.starlegacy.util.blockFace
import net.starlegacy.util.getNMSBlockData
import net.starlegacy.util.getRelativeIfLoaded
import net.starlegacy.util.getTypeSafe
import net.starlegacy.util.isButton
import net.starlegacy.util.isConcrete
import net.starlegacy.util.isDaylightSensor
import net.starlegacy.util.isDoor
import net.starlegacy.util.isGlass
import net.starlegacy.util.isGlassPane
import net.starlegacy.util.isRedstoneLamp
import net.starlegacy.util.isSlab
import net.starlegacy.util.isStainedGlass
import net.starlegacy.util.isStainedGlassPane
import net.starlegacy.util.isStairs
import net.starlegacy.util.isWall
import net.starlegacy.util.isWool
import net.starlegacy.util.rightFace
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Slab
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/** Parameters: block, inward */
private typealias BlockRequirement = (Block, BlockFace) -> Boolean

class LegacyMultiblockShape {
	// Cache of direction to requirement, so it doesn't need to be calculated every time based on the direction
	private val requirements = mutableMapOf<BlockFace, MutableMap<Vec3i, BlockRequirement>>()

	private var signCentered = false
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
	fun at(x: Int, y: Int, z: Int) = RequirementBuilder(shape = this, right = x, upward = y, inward = z)

	/**
	 * This is used for structuring it by y, z, x. See the area shield multiblocks for an example.
	 *
	 * @sample net.starlegacy.feature.multiblock.areashield.AreaShield10.buildStructure
	 */
	fun z(z: Int, block: Z.() -> Unit) = Z(this, z).block()

	fun z(zValues: IntProgression, block: Z.() -> Unit) {
		for (z in zValues) {
			z(z, block)
		}
	}

	class Z(private val shape: LegacyMultiblockShape, val z: Int) {
		fun y(y: Int, block: ZY.() -> Unit) = ZY(shape, z, y).block()

		fun y(yValues: IntProgression, block: ZY.() -> Unit) {
			for (y in yValues) {
				y(y, block)
			}
		}

		class ZY(private val shape: LegacyMultiblockShape, val z: Int, val y: Int) {
			fun x(x: Int) = shape.at(x, y, z)

			fun x(xValues: IntProgression, block: RequirementBuilder.() -> Unit) {
				for (x in xValues) {
					block(x(x))
				}
			}
		}
	}

	fun y(y: Int, block: Y.() -> Unit) = Y(this, y).block()

	fun y(yValues: IntProgression, block: Y.() -> Unit) {
		for (y in yValues) {
			y(y, block)
		}
	}

	class Y(private val shape: LegacyMultiblockShape, val y: Int) {
		fun z(z: Int, block: YZ.() -> Unit) = YZ(shape, y, z).block()

		fun z(zValues: IntProgression, block: YZ.() -> Unit) {
			for (z in zValues) {
				z(z, block)
			}
		}

		class YZ(private val shape: LegacyMultiblockShape, val y: Int, val z: Int) {
			fun x(x: Int) = shape.at(x, y, z)

			fun x(xValues: IntProgression, block: RequirementBuilder.() -> Unit) {
				for (x in xValues) {
					block(x(x))
				}
			}
		}
	}

	fun getRequirementMap(inwardFace: BlockFace): MutableMap<Vec3i, BlockRequirement> {
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
		return getRequirementMap(face).all { (coords, requirement) ->
			val x = coords.x
			val y = coords.y
			val z = coords.z
			val relative: Block = if (loadChunks) {
				origin.getRelative(x, y, z)
			} else {
				origin.getRelativeIfLoaded(x, y, z) ?: return@all false
			}

			val requirementMet = requirement(relative, face)

			if (!requirementMet && particles) {
				val location = relative.location.toCenterLocation().add(0.0, 0.4, 0.0)
				relative.world.spawnParticle(Particle.SPELL_WITCH, location, 1)
			}

			return@all requirementMet
		}
	}

	private val allLocations = mutableSetOf<Vec3i>() // used for checking for duplicates

	private fun addRequirement(right: Int, upward: Int, inward: Int, requirement: BlockRequirement) {
		if (!allLocations.add(Vec3i(right, upward, inward))) {
			Exception("Multiblock ${javaClass.simpleName} has more than one block at $right, $upward, $inward!").printStackTrace()
		}

		CARDINAL_BLOCK_FACES.forEach { inwardFace ->
			val rightFace = inwardFace.rightFace
			val x = rightFace.modX * right + inwardFace.modX * inward
			val z = rightFace.modZ * right + inwardFace.modZ * inward
			val intTrio = Vec3i(x, upward, z)
			val requirementMap = getRequirementMap(inwardFace)

			requirementMap[intTrio] = requirement
		}
	}

	@Suppress("unused")
	class RequirementBuilder(val shape: LegacyMultiblockShape, val right: Int, val upward: Int, val inward: Int) {
		private fun complete(requirement: BlockRequirement) = shape.addRequirement(right, upward, inward, requirement)

		fun type(type: Material) {
			complete { block, _ -> block.getTypeSafe() == type }
		}

		fun anyType(vararg types: Material) {
			val typeSet = EnumSet.copyOf(types.toList())
			complete { block, _ -> typeSet.contains(block.getTypeSafe()) }
		}

		fun customBlock(customBlock: CustomBlock) {
			complete { block, _ -> CustomBlocks[block] === customBlock }
		}

		fun anyType(types: Iterable<Material>) = anyType(*types.toList().toTypedArray())

		fun filteredTypes(filter: (Material) -> Boolean) = anyType(MATERIALS.filter(filter))

		fun carbyne() = anyType(CONCRETE_TYPES)
		fun stainedTerracotta() = anyType(STAINED_TERRACOTTA_TYPES)

		fun glass() = type(Material.GLASS)
		fun stainedGlass() = filteredTypes { it.isStainedGlass }
		fun anyGlass() = filteredTypes { it.isGlass }

		fun glassPane() = type(Material.GLASS_PANE)
		fun stainedGlassPane() = filteredTypes { it.isStainedGlassPane }
		fun anyGlassPane() = filteredTypes { it.isGlassPane }

		fun anyStairs() = filteredTypes { it.isStairs }

		fun anyWall() = filteredTypes { it.isWall }

		fun anyWool() = filteredTypes { it.isWool }

		fun anySlab() = filteredTypes { it.isSlab }
		fun anyDoubleSlab() = complete { block, _ ->
			val blockData = block.blockData
			return@complete blockData is Slab && blockData.type == Slab.Type.DOUBLE
		}

		fun concrete() = filteredTypes { it.isConcrete }

		fun stoneBrick() = type(Material.STONE_BRICKS)

		fun ironBlock() = type(Material.IRON_BLOCK)
		fun goldBlock() = type(Material.GOLD_BLOCK)
		fun diamondBlock() = type(Material.DIAMOND_BLOCK)
		fun netheriteBlock() = type(Material.NETHERITE_BLOCK)
		fun emeraldBlock() = type(Material.EMERALD_BLOCK)
		fun redstoneBlock() = type(Material.REDSTONE_BLOCK)
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

		fun titaniumBlock() = customBlock(CustomBlocks.MINERAL_TITANIUM.block)
		fun aluminumBlock() = customBlock(CustomBlocks.MINERAL_ALUMINUM.block)
		fun chetheriteBlock() = customBlock(CustomBlocks.MINERAL_CHETHERITE.block)
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

		fun machineFurnace() = complete { block, inward ->
			val blockData = block.getNMSBlockData()
			if (blockData.bukkitMaterial != Material.FURNACE) return@complete false
			val facing = blockData.getValue(AbstractFurnaceBlock.FACING).blockFace
			return@complete facing == inward.oppositeFace
		}
	}
}
