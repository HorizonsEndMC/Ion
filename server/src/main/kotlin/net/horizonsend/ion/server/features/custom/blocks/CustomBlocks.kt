package net.horizonsend.ion.server.features.custom.blocks

import com.google.common.collect.HashBasedTable
import io.papermc.paper.datacomponent.DataComponentTypes
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.horizonsend.ion.server.core.registries.IonRegistryKey
import net.horizonsend.ion.server.core.registries.keys.CustomItemKeys
import net.horizonsend.ion.common.utils.set
import net.horizonsend.ion.server.features.custom.blocks.extractor.AdvancedItemExtractorBlock
import net.horizonsend.ion.server.features.custom.blocks.filter.ItemFilterBlock
import net.horizonsend.ion.server.features.custom.blocks.misc.DirectionalCustomBlock
import net.horizonsend.ion.server.features.custom.blocks.misc.MultiblockWorkbench
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.POWER_DRILL_BASIC
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.type.CustomBlockItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.rotateBlockFace
import net.horizonsend.ion.server.miscellaneous.utils.getMatchingMaterials
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.BlockFace.EAST
import org.bukkit.block.BlockFace.NORTH
import org.bukkit.block.BlockFace.SOUTH
import org.bukkit.block.BlockFace.UP
import org.bukkit.block.BlockFace.WEST
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.MultipleFacing
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

object CustomBlocks {
    val ALL get() = customBlocks.values
    private val customBlocks: MutableMap<String, CustomBlock> = mutableMapOf()
    private val customBlocksData = Object2ObjectOpenHashMap<BlockState, CustomBlock>()
	private val directionalCustomBlocksData = HashBasedTable.create<BlockState, CustomBlock, BlockFace>()

    fun mushroomBlockData(faces: Set<BlockFace>) : BlockData {
        return Material.BROWN_MUSHROOM_BLOCK.createBlockData { data ->
            for (face in (data as MultipleFacing).allowedFaces) {
                data.setFace(face, faces.contains(face))
            }
        }
    }

    val ALUMINUM_ORE : CustomBlock = register(CustomBlock(
        identifier = "ALUMINUM_ORE",
        blockData = mushroomBlockData(setOf(NORTH, UP)),
		drops = BlockLoot(
			requiredTool = { BlockLoot.Tool.PICKAXE },
			drops = fortuneEnabledCustomItemDrop(CustomItemKeys.RAW_ALUMINUM)
		),
 		CustomItemKeys.ALUMINUM_ORE
 	))

    val ALUMINUM_BLOCK : CustomBlock = register(CustomBlock(
        identifier = "ALUMINUM_BLOCK",
        blockData = mushroomBlockData(setOf(SOUTH, UP, WEST)),
		drops = BlockLoot(
			requiredTool = { BlockLoot.Tool.PICKAXE },
			drops = customItemDrop(CustomItemKeys.ALUMINUM_BLOCK)
		),
 		CustomItemKeys.ALUMINUM_BLOCK
 	))

    val RAW_ALUMINUM_BLOCK : CustomBlock = register(CustomBlock(
        identifier = "RAW_ALUMINUM_BLOCK",
        blockData = mushroomBlockData(setOf(NORTH)),
		drops = BlockLoot(
			requiredTool = { BlockLoot.Tool.PICKAXE },
			drops = customItemDrop(CustomItemKeys.RAW_ALUMINUM_BLOCK)
		),
 		CustomItemKeys.RAW_ALUMINUM_BLOCK
 	))

    val CHETHERITE_ORE : CustomBlock = register(CustomBlock(
        identifier = "CHETHERITE_ORE",
        blockData = mushroomBlockData(setOf(EAST, NORTH, UP)),
		drops = BlockLoot(
			requiredTool = { BlockLoot.Tool.PICKAXE },
			drops = fortuneEnabledCustomItemDrop(CustomItemKeys.CHETHERITE)
		),
 		CustomItemKeys.CHETHERITE_ORE
 	))

	val STEEL_BLOCK : CustomBlock = register(CustomBlock(
		identifier = "STEEL_BLOCK",
		blockData = mushroomBlockData(setOf(SOUTH, UP, DOWN)),
		drops = BlockLoot(
			requiredTool = { BlockLoot.Tool.PICKAXE },
			drops = customItemDrop(CustomItemKeys.STEEL_BLOCK)
		),
 		CustomItemKeys.STEEL_BLOCK
 	))

    val CHETHERITE_BLOCK : CustomBlock = register(CustomBlock(
        identifier = "CHETHERITE_BLOCK",
        blockData = mushroomBlockData(setOf(SOUTH, UP)),
		drops = BlockLoot(
			requiredTool = { BlockLoot.Tool.PICKAXE },
			drops = customItemDrop(CustomItemKeys.CHETHERITE_BLOCK)
		),
 		CustomItemKeys.CHETHERITE_BLOCK
 	))

    val TITANIUM_ORE : CustomBlock = register(CustomBlock(
        identifier = "TITANIUM_ORE",
        blockData = mushroomBlockData(setOf(UP, WEST)),
		drops = BlockLoot(
			requiredTool = { BlockLoot.Tool.PICKAXE },
			drops = fortuneEnabledCustomItemDrop(CustomItemKeys.RAW_TITANIUM)
		),
 		CustomItemKeys.TITANIUM_ORE
 	))

    val TITANIUM_BLOCK : CustomBlock = register(CustomBlock(
        identifier = "TITANIUM_BLOCK",
        blockData = mushroomBlockData(setOf(EAST, SOUTH, UP)),
		drops = BlockLoot(
			requiredTool = { BlockLoot.Tool.PICKAXE },
			drops = customItemDrop(CustomItemKeys.TITANIUM_BLOCK)
		),
 		CustomItemKeys.TITANIUM_BLOCK
 	))

    val RAW_TITANIUM_BLOCK : CustomBlock = register(CustomBlock(
        identifier = "RAW_TITANIUM_BLOCK",
        blockData = mushroomBlockData(setOf(EAST)),
		drops = BlockLoot(
			requiredTool = { BlockLoot.Tool.PICKAXE },
			drops = customItemDrop(CustomItemKeys.RAW_TITANIUM_BLOCK)
		),
 		CustomItemKeys.RAW_TITANIUM_BLOCK
 	))

    val URANIUM_ORE : CustomBlock = register(CustomBlock(
        identifier = "URANIUM_ORE",
        blockData = mushroomBlockData(setOf(UP)),
		drops = BlockLoot(
			requiredTool = { BlockLoot.Tool.PICKAXE },
			drops = fortuneEnabledCustomItemDrop(CustomItemKeys.RAW_URANIUM)
		),
 		CustomItemKeys.URANIUM_ORE
 	))

    val URANIUM_BLOCK : CustomBlock = register(CustomBlock(
        identifier = "URANIUM_BLOCK",
        blockData = mushroomBlockData(setOf(EAST, NORTH, SOUTH, WEST)),
		drops = BlockLoot(
			requiredTool = { BlockLoot.Tool.PICKAXE },
			drops = customItemDrop(CustomItemKeys.URANIUM_BLOCK)
		),
 		CustomItemKeys.URANIUM_BLOCK
 	))

	val ENRICHED_URANIUM_BLOCK : CustomBlock = register(CustomBlock(
		identifier = "ENRICHED_URANIUM_BLOCK",
		blockData = mushroomBlockData(setOf(EAST, WEST)),
		drops = BlockLoot(
			requiredTool = { BlockLoot.Tool.PICKAXE },
			drops = customItemDrop(CustomItemKeys.ENRICHED_URANIUM_BLOCK)
		),
 		CustomItemKeys.ENRICHED_URANIUM_BLOCK
 	))

    val NETHERITE_CASING : CustomBlock = register(CustomBlock(
        identifier = "NETHERITE_CASING",
        blockData = mushroomBlockData(setOf(WEST,NORTH,DOWN,UP)),
		drops = BlockLoot(
			requiredTool = { BlockLoot.Tool.PICKAXE },
			drops = customItemDrop(CustomItemKeys.NETHERITE_CASING)
		),
 		CustomItemKeys.NETHERITE_CASING
 	))

    val RAW_URANIUM_BLOCK : CustomBlock = register(CustomBlock(
        identifier = "RAW_URANIUM_BLOCK",
        blockData = mushroomBlockData(setOf(SOUTH)),
		drops = BlockLoot(
			requiredTool = { BlockLoot.Tool.PICKAXE },
			drops = customItemDrop(CustomItemKeys.RAW_URANIUM_BLOCK)
		),
 		CustomItemKeys.RAW_ALUMINUM_BLOCK
 	))

	val SUPERCONDUCTOR_BLOCK : CustomBlock = register(CustomBlock(
		identifier = "SUPERCONDUCTOR_BLOCK",
		blockData = mushroomBlockData(setOf(SOUTH, DOWN)),
		drops = BlockLoot(
			requiredTool = { BlockLoot.Tool.PICKAXE },
			drops = customItemDrop(CustomItemKeys.SUPERCONDUCTOR_BLOCK)
		),
 		CustomItemKeys.SUPERCONDUCTOR_BLOCK
 	))

	val BATTLECRUISER_REACTOR_CORE : CustomBlock = register(CustomBlock(
		identifier = "BATTLECRUISER_REACTOR_CORE",
		blockData = mushroomBlockData(setOf(NORTH, UP, WEST)),
		drops = BlockLoot(
			requiredTool = null,
			drops = customItemDrop(CustomItemKeys.BATTLECRUISER_REACTOR_CORE)
		),
 		CustomItemKeys.BATTLECRUISER_REACTOR_CORE
 	))

    val BARGE_REACTOR_CORE : CustomBlock = register(CustomBlock(
        identifier = "BARGE_REACTOR_CORE",
        blockData = mushroomBlockData(setOf(NORTH, EAST, WEST)),
		drops = BlockLoot(
			requiredTool = null,
			drops = customItemDrop(CustomItemKeys.BARGE_REACTOR_CORE)
		),
 		CustomItemKeys.BARGE_REACTOR_CORE
 	))

	val CRUISER_REACTOR_CORE : CustomBlock = register(CustomBlock(
        identifier = "CRUISER_REACTOR_CORE",
        blockData = mushroomBlockData(setOf(NORTH, DOWN, WEST)),
		drops = BlockLoot(
			requiredTool = null,
			drops = customItemDrop(CustomItemKeys.CRUISER_REACTOR_CORE)
		),
 		CustomItemKeys.CRUISER_REACTOR_CORE
 	))

	fun customItemDrop(customItem: Supplier<CustomItem>, amount: Int = 1): (ItemStack?) -> Collection<ItemStack> {
		return { _ -> listOf(customItem.get().constructItemStack(amount)) }
	}

	fun fortuneEnabledCustomItemDrop(customItem: Supplier<CustomItem>, amount: Int = 1): (ItemStack?) -> Collection<ItemStack> {
		return resultSupplier@{ usedTool ->
			val fallback = listOf(customItem.get().constructItemStack(amount))
			if (usedTool == null) return@resultSupplier fallback
			val enchantments = usedTool.getDataOrDefault(DataComponentTypes.ENCHANTMENTS, null) ?: return@resultSupplier fallback

			val fortuneLevel = enchantments.enchantments()[Enchantment.FORTUNE] ?: return@resultSupplier fallback
			if (fortuneLevel == 0) return@resultSupplier fallback

			val newAmount = if (fortuneLevel > 0) {
				var i: Int = Random.nextInt(fortuneLevel + 2) - 1
				if (i < 0) {
					i = 0
				}

				amount * (i + 1)
			} else {
				amount
			}

			listOf(customItem.get().constructItemStack(newAmount))
		}
	}

	val MULTIBLOCK_WORKBENCH = register(MultiblockWorkbench)

	val ADVANCED_ITEM_EXTRACTOR = register(AdvancedItemExtractorBlock)
	val ITEM_FILTER = registerDirectional(ItemFilterBlock)

    fun customItemDrop(key: IonRegistryKey<CustomItem>, amount: Int = 1): Supplier<Collection<ItemStack>> {
        return Supplier {
			val itemStack = key.getValue().constructItemStack()
			itemStack.amount = amount
			listOf(itemStack)
		}
    }

    fun <T : CustomBlock> register(customBlock: T): T {
        customBlocks[customBlock.identifier] = customBlock
        customBlocksData[customBlock.blockData.nms] = customBlock
        return customBlock
    }

    fun <T : DirectionalCustomBlock> registerDirectional(customBlock: T): T {
        customBlocks[customBlock.identifier] = customBlock

		for ((data, face) in customBlock.bukkitFaceLookup) {
			directionalCustomBlocksData[data.nms, customBlock] = face
			customBlocksData[data.nms] = customBlock
		}

        return customBlock
    }

    val identifiers = customBlocks.keys

	val Block.customBlock get(): CustomBlock? = getByBlock(this)

    fun getByIdentifier(identifier: String): CustomBlock? = customBlocks[identifier]

    fun getByBlock(block: Block): CustomBlock? = getByBlockData(block.blockData)

    fun getByBlockData(blockData: BlockData): CustomBlock? = customBlocksData[blockData.nms]

    fun getByBlockState(blockState: BlockState): CustomBlock? = customBlocksData[blockState]

	companion object {
		fun getRotated(customBlock: CustomBlock, blockState: BlockState, rotation: Rotation): BlockState {
			return when (customBlock) {
				is DirectionalCustomBlock -> {
					val face = customBlock.getFace(blockState)
					val newFace = rotateBlockFace(face, rotation)
					customBlock.faceData[newFace]!!.nms
				}
				else -> blockState
			}
		}
	}
}

open class CustomBlock(
	val identifier: String,
	val blockData: BlockData,
	val drops: BlockLoot,
	private val customBlockItem: IonRegistryKey<CustomItem>,
) {
	val customItem get() = customBlockItem.getValue()

	open fun placeCallback(placedItem: ItemStack, block: Block) {}
	open fun removeCallback(block: Block) {}
}

data class BlockLoot(
	val requiredTool: Supplier<Tool>? = Supplier { Tool.PICKAXE },
	val drops: (ItemStack?) -> Collection<ItemStack>,
	val silkTouchDrops: (ItemStack?) -> Collection<ItemStack> = drops,
) {
	fun getDrops(tool: ItemStack?, silkTouch: Boolean): Collection<ItemStack> {
		if (tool != null && requiredTool != null) {
			if (!requiredTool.get().matches(tool)) return listOf()
		}

		if (silkTouch) return silkTouchDrops.invoke(tool).map { it.clone() }

		return drops.invoke(tool).map { it.clone() }
	}

	companion object ToolPredicate {
		fun matchMaterial(material: Material): (ItemStack) -> Boolean {
			return { it.type == material }
		}

		fun matchAnyMaterial(materials: Iterable<Material>): (ItemStack) -> Boolean {
			return { materials.contains(it.type) }
		}

		fun customItem(customItem: IonRegistryKey<CustomItem>): (ItemStack) -> Boolean {
			return { it.customItem?.key == customItem }
		}
	}

	enum class Tool(vararg val checks: (ItemStack) -> Boolean) {
		PICKAXE(
			customItem(CustomItemKeys.POWER_DRILL_BASIC),
			matchAnyMaterial(getMatchingMaterials { it.name.endsWith("PICKAXE") })
		),
		SHOVEL(
			customItem(CustomItemKeys.POWER_DRILL_BASIC),
			matchAnyMaterial(getMatchingMaterials { it.name.endsWith("SHOVEL") })

		),
		AXE(
			customItem(CustomItemKeys.POWER_DRILL_BASIC),
			matchAnyMaterial(getMatchingMaterials { it.name.endsWith("AXE") })

		),
		SHEARS(
			customItem(CustomItemKeys.POWER_DRILL_BASIC),
			matchMaterial(Material.SHEARS)
		);

		fun matches(itemStack: ItemStack): Boolean = checks.any { it.invoke(itemStack) }
	}
}
