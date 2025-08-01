package net.horizonsend.ion.server.features.multiblock.shape

import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.ENRICHED_URANIUM_BLOCK
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.NETHERITE_CASING
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.type.CustomBlockItem
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager.Companion.STANDARD_EXTRACTOR_TYPE
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.CONCRETE_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.MATERIALS
import net.horizonsend.ion.server.miscellaneous.utils.TERRACOTTA_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.blockFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toAbsolute
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.getNMSBlockData
import net.horizonsend.ion.server.miscellaneous.utils.getNMSBlockSateSafe
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.isButton
import net.horizonsend.ion.server.miscellaneous.utils.isDaylightSensor
import net.horizonsend.ion.server.miscellaneous.utils.isDoor
import net.horizonsend.ion.server.miscellaneous.utils.isFroglight
import net.horizonsend.ion.server.miscellaneous.utils.isGlass
import net.horizonsend.ion.server.miscellaneous.utils.isGlassPane
import net.horizonsend.ion.server.miscellaneous.utils.isPipedInventory
import net.horizonsend.ion.server.miscellaneous.utils.isRedstoneLamp
import net.horizonsend.ion.server.miscellaneous.utils.isSlab
import net.horizonsend.ion.server.miscellaneous.utils.isStainedGlass
import net.horizonsend.ion.server.miscellaneous.utils.isStairs
import net.horizonsend.ion.server.miscellaneous.utils.isTerracotta
import net.horizonsend.ion.server.miscellaneous.utils.isTrapdoor
import net.horizonsend.ion.server.miscellaneous.utils.isWall
import net.horizonsend.ion.server.miscellaneous.utils.isWool
import net.minecraft.world.level.block.AbstractFurnaceBlock
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Furnace
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Slab.Type.DOUBLE
import org.bukkit.inventory.ItemStack
import java.util.EnumSet

class MultiblockShape {
	// Cache of direction to requirement, so it doesn't need to be calculated every time based on the direction
	private val requirements = mutableMapOf<BlockFace, MutableMap<Vec3i, BlockRequirement>>()

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
	 * This is used for structuring it by y, z, x. See the area shield multiblocks for an example
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

	fun getRequirementMap(inwardFace: BlockFace): MutableMap<Vec3i, BlockRequirement> {
		return requirements.getOrPut(inwardFace) {
			require(CARDINAL_BLOCK_FACES.contains(inwardFace)) { "Unsupported inward direction: $inwardFace" }
			return@getOrPut mutableMapOf()
		}
	}

	fun getLocations(face: BlockFace): Set<Vec3i> = requirements.getValue(face).keys

	fun checkRequirements(origin: Block, inward: BlockFace, loadChunks: Boolean, particles: Boolean = false): Boolean {
		// check all directions if ignoring direction
		for (face in if (ignoreDirection) CARDINAL_BLOCK_FACES else listOf(inward)) {
			if (checkRequirementsSpecific(origin, face, loadChunks, particles)) {
				return true
			}
		}
		return false
	}

	fun checkRequirementsSpecific(origin: Block, face: BlockFace, loadChunks: Boolean, particles: Boolean): Boolean {
		return getRequirementMap(face).all { (offset, requirement) ->
			val x = offset.x
			val y = offset.y
			val z = offset.z

			if ((y + origin.y) !in origin.world.minHeight ..< origin.world.maxHeight) return@all false

			val relative: Block = if (loadChunks) {
				origin.getRelative(x, y, z)
			} else {
				origin.getRelativeIfLoaded(x, y, z) ?: return@all false
			}

			val requirementMet = requirement(relative, face, loadChunks)

			if (!requirementMet && particles) {
				val location = relative.location.toCenterLocation().add(0.0, 0.4, 0.0)
				relative.world.spawnParticle(Particle.WITCH, location, 1)
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
			val vec = toAbsolute(inwardFace, right, upward, inward)

			val requirementMap = getRequirementMap(inwardFace)

			requirementMap[vec] = requirement
		}
	}

	@Suppress("unused")
	class RequirementBuilder(val shape: MultiblockShape, val right: Int, val upward: Int, val inward: Int) {
		private fun complete(requirement: BlockRequirement) = shape.addRequirement(right, upward, inward, requirement)

		fun type(type: Material, edit: BlockRequirement.() -> Unit = {}) {
			val requirement = BlockRequirement(
				alias = type.toString(),
				example = type.createBlockData(),
				syncCheck = { block, _, loadChunks -> if (loadChunks) block.type == type else block.getTypeSafe() == type },
				itemRequirement = BlockRequirement.ItemRequirement(
					itemCheck = { type == it.type },
					amountConsumed = { 1 },
					toBlock = { item -> item.type.createBlockData() },
					toItemStack = { block -> ItemStack(block.material) }
				)
			)

			complete(requirement)

			edit(requirement)
		}

		fun anyType(vararg types: Material, alias: String, edit: BlockRequirement.() -> Unit = {}) {
			val typeSet = EnumSet.copyOf(types.toList())

			val requirement = BlockRequirement(
				alias = alias,
				example = types.first().createBlockData(),
				syncCheck = { block, _, loadChunks ->
					typeSet.contains(if (loadChunks) block.type else block.getTypeSafe() ?: return@BlockRequirement false)
				},
				itemRequirement = BlockRequirement.ItemRequirement(
					itemCheck = { typeSet.contains(it.type) },
					amountConsumed = { 1 },
					toBlock = { item -> item.type.createBlockData() },
					toItemStack = { block -> ItemStack(block.material) }
				)
			)

			complete(requirement)

			edit(requirement)
		}

		fun customBlock(customBlock: CustomBlock) {
			val requirement = BlockRequirement(
				alias = customBlock.identifier,
				example = customBlock.blockData,
				syncCheck = { block, _, loadChunks ->
					if (loadChunks) CustomBlocks.getByBlock(block) else {
						getBlockDataSafe(block.world, block.x, block.y, block.z)?.let { CustomBlocks.getByBlockData(it) }
					} === customBlock
				},
				itemRequirement = BlockRequirement.ItemRequirement(
					itemCheck = { val customItem = it.customItem; customItem is CustomBlockItem && customItem.getCustomBlock() == customBlock },
					amountConsumed = { 1 },
					toBlock = { _ -> customBlock.blockData },
					toItemStack = { block -> CustomBlocks.getByBlockData(block)?.customItem?.constructItemStack() ?: ItemStack(Material.AIR) }
				)
			)

			complete(requirement)
		}

		fun anyType(alias: String, types: Iterable<Material>, edit: BlockRequirement.() -> Unit = {}) = anyType(
			*types.toList().toTypedArray(),
			alias = alias,
			edit = edit
		)

		fun filteredTypes(alias: String, edit: BlockRequirement.() -> Unit = {}, filter: (Material) -> Boolean) = anyType(alias, MATERIALS.filter(filter), edit = edit)

		// Start presets
		fun anyConcrete() = anyType("any concrete block", CONCRETE_TYPES) { setExample(Material.GRAY_CONCRETE.createBlockData()) }
		fun anyTerracotta() = anyType("any terracotta", TERRACOTTA_TYPES) { setExample(Material.CYAN_TERRACOTTA.createBlockData()) }
		fun anyGlass() = filteredTypes("any glass block", { setExample(Material.BLACK_STAINED_GLASS.createBlockData()) }) { it.isGlass }
		fun stainedGlass() = filteredTypes("any stained glass block", { setExample(Material.BLACK_STAINED_GLASS.createBlockData()) }) { it.isStainedGlass }
		fun anyGlassPane(edit: BlockRequirement.() -> Unit = { setExample(Material.BLACK_STAINED_GLASS_PANE.createBlockData()) }) = filteredTypes("any stained glass pane", edit = edit) { it.isGlassPane }

		fun stoneBrick() = type(Material.STONE_BRICKS)

		fun seaLantern() = type(Material.SEA_LANTERN)

		fun anyStairs(edit: BlockRequirement.() -> Unit = { setExample(Material.STONE_BRICK_STAIRS.createBlockData()) }) =
			filteredTypes("any stair block", edit) { it.isStairs }

		fun anyWall(edit: BlockRequirement.() -> Unit = { setExample(Material.STONE_BRICK_WALL.createBlockData()) }) =
			filteredTypes("any wall block", edit) { it.isWall }

		fun anyWool() = filteredTypes("any wool block") { it.isWool }

		fun anySlab(edit: BlockRequirement.() -> Unit = { setExample(Material.STONE_BRICK_SLAB.createBlockData()) }) =
			filteredTypes("any slab block", edit) { it.isSlab }

		fun anyDoubleSlab() = complete(
			BlockRequirement(
				alias = "any double slab block",
				example = (Material.STONE_BRICK_SLAB.createBlockData() as Slab).apply { this.type = DOUBLE },
				syncCheck = { block, _, loadChunks ->
					val blockData: BlockData? = if (loadChunks) block.blockData else getBlockDataSafe(block.world, block.x, block.y, block.z)
					blockData is Slab && blockData.type == DOUBLE
				},
				itemRequirement = BlockRequirement.ItemRequirement(
					itemCheck = { (it.type.isSlab && it.amount >= 2) },
					amountConsumed = { 2 },
					toBlock = { item -> (item.type.createBlockData() as Slab).apply { this.type = DOUBLE } },
					toItemStack = { ItemStack(it.material, 2) }
				),
			)
		)

		fun anySlabOrStairs() = filteredTypes("any slab or stairs", { setExample(Material.STONE_BRICK_SLAB) }) { it.isSlab || it.isStairs }

		fun terracottaOrDoubleSlab() = complete(BlockRequirement(
			alias = "any double slab or terracotta block",
			example = Material.CYAN_TERRACOTTA.createBlockData(),
			syncCheck = { block, _, loadChunks ->
				val blockData: BlockData? = if (loadChunks) block.blockData else getBlockDataSafe(block.world, block.x, block.y, block.z)
				val blockType = if (loadChunks) block.type else block.getTypeSafe()

				(blockData is Slab && blockData.type == DOUBLE) || TERRACOTTA_TYPES.contains(blockType)
			},
			itemRequirement = BlockRequirement.ItemRequirement(
				itemCheck = { (it.type.isSlab && it.amount >= 2) || it.type.isTerracotta  },
				amountConsumed = { if (it.type.isSlab) 2 else 1 },
				toBlock = { item ->
					val type = item.type
					if (type.isSlab) {
						(type.createBlockData() as Slab).apply { this.type = DOUBLE }
					} else type.createBlockData()
				},
				{ block ->
					val type = block.material
					if (type.isSlab) ItemStack(type, 2) else ItemStack(type)
				}
			)
		))

		fun sculkCatalyst() = type(Material.SCULK_CATALYST)

		fun ironBlock() = type(Material.IRON_BLOCK)
		fun goldBlock() = type(Material.GOLD_BLOCK)
		fun diamondBlock() = type(Material.DIAMOND_BLOCK)
		fun netheriteBlock() = type(Material.NETHERITE_BLOCK)
		fun emeraldBlock() = type(Material.EMERALD_BLOCK)
		fun redstoneBlock() = type(Material.REDSTONE_BLOCK)
		fun lapisBlock() = type(Material.LAPIS_BLOCK)

		fun titaniumBlock() = customBlock(CustomBlocks.TITANIUM_BLOCK)
		fun aluminumBlock() = customBlock(CustomBlocks.ALUMINUM_BLOCK)
		fun chetheriteBlock() = customBlock(CustomBlocks.CHETHERITE_BLOCK)
		fun steelBlock() = customBlock(CustomBlocks.STEEL_BLOCK)
		fun enrichedUraniumBlock() = customBlock(ENRICHED_URANIUM_BLOCK)

		fun anyCopperVariant() = anyType(
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
			Material.WAXED_OXIDIZED_CUT_COPPER,
			alias = "any copper variant",
		)

		fun anySolidCopperBlock() = anyType(
			Material.COPPER_BLOCK,
			Material.EXPOSED_COPPER,
			Material.WEATHERED_COPPER,
			Material.OXIDIZED_COPPER,
			Material.WAXED_COPPER_BLOCK,
			Material.WAXED_EXPOSED_COPPER,
			Material.WAXED_WEATHERED_COPPER,
			Material.WAXED_OXIDIZED_COPPER,
			alias = "any solid copper block"
		)

		fun anyWaxedCopperBlock() = anyType(
			Material.WAXED_COPPER_BLOCK,
			Material.WAXED_EXPOSED_COPPER,
			Material.WAXED_WEATHERED_COPPER,
			Material.WAXED_OXIDIZED_COPPER,
			alias = "any waxed solid copper block"
		)

		fun anyUnwaxedCopperBlock() = anyType(
			Material.COPPER_BLOCK,
			Material.EXPOSED_COPPER,
			Material.WEATHERED_COPPER,
			Material.OXIDIZED_COPPER,
			alias = "any unwaxed solid copper block"
		)

		fun anyCopperGrate() = anyType(
			Material.COPPER_GRATE,
			Material.EXPOSED_COPPER_GRATE,
			Material.WEATHERED_COPPER_GRATE,
			Material.OXIDIZED_COPPER_GRATE,
			Material.WAXED_COPPER_GRATE,
			Material.WAXED_EXPOSED_COPPER_GRATE,
			Material.WAXED_WEATHERED_COPPER_GRATE,
			Material.WAXED_OXIDIZED_COPPER_GRATE,
			alias = "any copper grate"
		)

		fun anyUnwaxedCopperGrate() = anyType(
			Material.COPPER_GRATE,
			Material.EXPOSED_COPPER_GRATE,
			Material.WEATHERED_COPPER_GRATE,
			Material.OXIDIZED_COPPER_GRATE,
			alias = "any unwaxed copper grate"
		)

		fun anyWaxedCopperGrate() = anyType(
			Material.WAXED_COPPER_GRATE,
			Material.WAXED_EXPOSED_COPPER_GRATE,
			Material.WAXED_WEATHERED_COPPER_GRATE,
			Material.WAXED_OXIDIZED_COPPER_GRATE,
			alias = "any waxed copper grate"
		)

		fun anyCopperBulb() = anyType(
			Material.COPPER_BULB,
			Material.EXPOSED_COPPER_BULB,
			Material.WEATHERED_COPPER_BULB,
			Material.OXIDIZED_COPPER_BULB,
			Material.WAXED_COPPER_BULB,
			Material.WAXED_EXPOSED_COPPER_BULB,
			Material.WAXED_WEATHERED_COPPER_BULB,
			Material.WAXED_OXIDIZED_COPPER_BULB,
			alias = "any copper bulb"
		)

		fun anyWaxedCopperBulb() = anyType(
			Material.WAXED_COPPER_BULB,
			Material.WAXED_EXPOSED_COPPER_BULB,
			Material.WAXED_WEATHERED_COPPER_BULB,
			Material.WAXED_OXIDIZED_COPPER_BULB,
			alias = "any waxed copper bulb"
		)

		fun anyUnwaxedCopperBulb() = anyType(
			Material.COPPER_BULB,
			Material.EXPOSED_COPPER_BULB,
			Material.WEATHERED_COPPER_BULB,
			Material.OXIDIZED_COPPER_BULB,
			alias = "any unwaxed copper bulb"
		)

		fun fluidInput() = type(Material.FLETCHING_TABLE)
		fun powerInput() = type(Material.NOTE_BLOCK)
		fun extractor() = type(STANDARD_EXTRACTOR_TYPE)

		fun sponge() = anyType(Material.SPONGE, Material.WET_SPONGE, alias = "sponge")
		fun endRod(edit: BlockRequirement.() -> Unit = {}) = type(Material.END_ROD, edit)
		fun lightningRod(edit: BlockRequirement.() -> Unit = {}) = type(Material.LIGHTNING_ROD, edit)

		fun hopper() = type(Material.HOPPER)
		fun anyPipedInventory() = filteredTypes("any container block", edit = { setExample(Material.CHEST.createBlockData()) }) { it.isPipedInventory }
		fun dispenser() = type(Material.DISPENSER)

		fun netheriteCasing() = customBlock(NETHERITE_CASING)

		fun redstoneLamp() = filteredTypes("redstone lamp") { it.isRedstoneLamp }
		fun daylightSensor() = filteredTypes("daylight sensor") { it.isDaylightSensor }

		fun grindstone(edit: BlockRequirement.() -> Unit = {}) = type(Material.GRINDSTONE, edit)

		fun anyDoor() = filteredTypes("any door", edit = { setExample(Material.OAK_DOOR.createBlockData()) }) { it.isDoor }
		fun anyButton() = filteredTypes("any button") { it.isButton }

		fun pistonBase() = type(Material.PISTON)
		fun pistonHead() = type(Material.PISTON_HEAD)

		fun furnace() = type(Material.FURNACE)
		fun lodestone() = type(Material.LODESTONE)
		fun anyTrapdoor(edit: BlockRequirement.() -> Unit = {}) = filteredTypes("any trapdoor", edit) { it.isTrapdoor }
		fun anyFroglight() = filteredTypes("any froglight") { it.isFroglight }

		fun thrusterBlock() = anyType(
			Material.OCHRE_FROGLIGHT,
			Material.VERDANT_FROGLIGHT,
			Material.PEARLESCENT_FROGLIGHT,
			Material.GLOWSTONE,
			Material.REDSTONE_LAMP,
			Material.MAGMA_BLOCK,
			Material.SEA_LANTERN,
			Material.SHROOMLIGHT,
			alias = "any light block"
		)

		fun machineFurnace() = complete(BlockRequirement(
			alias = "furnace",
			example = Material.FURNACE.createBlockData(),
			syncCheck = check@{ block, inward, loadChunks ->
				val blockData = if (loadChunks) block.getNMSBlockData() else getNMSBlockSateSafe(block.world, block.x, block.y, block.z) ?: return@check false

				if (blockData.bukkitMaterial != Material.FURNACE) return@check false
				val facing = blockData.getValue(AbstractFurnaceBlock.FACING).blockFace
				return@check facing == inward.oppositeFace
			},
			itemRequirement = BlockRequirement.ItemRequirement(
				itemCheck = { it.type == Material.FURNACE },
				amountConsumed = { 1 },
				toBlock = { Material.FURNACE.createBlockData() },
				toItemStack = { ItemStack(Material.FURNACE) }
			)
		).addPlacementModification { direction, data ->
			data as Furnace
			data.facing = direction.oppositeFace
		})

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
			alias = "stone bricks"
		)
	}
}
