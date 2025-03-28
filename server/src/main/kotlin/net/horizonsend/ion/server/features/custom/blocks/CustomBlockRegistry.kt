package net.horizonsend.ion.server.features.custom.blocks

import com.google.common.collect.HashBasedTable
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.horizonsend.ion.server.core.registries.IonRegistries
import net.horizonsend.ion.server.core.registries.IonRegistryKey
import net.horizonsend.ion.server.core.registries.Registry
import net.horizonsend.ion.server.core.registries.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registries.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registries.keys.KeyRegistry
import net.horizonsend.ion.server.features.custom.blocks.extractor.AdvancedItemExtractorBlock
import net.horizonsend.ion.server.features.custom.blocks.filter.ItemFilterBlock
import net.horizonsend.ion.server.features.custom.blocks.misc.DirectionalCustomBlock
import net.horizonsend.ion.server.features.custom.blocks.misc.MultiblockWorkbench
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.miscellaneous.utils.map
import net.horizonsend.ion.server.miscellaneous.utils.nms
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
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

class CustomBlockRegistry : Registry<CustomBlock>("CUSTOM_BLOCKS") {
	override val keySet: KeyRegistry<CustomBlock> = CustomBlockKeys
	private val directionalCustomBlocksData = HashBasedTable.create<BlockState, CustomBlock, BlockFace>()
	private val customBlocksData = Object2ObjectOpenHashMap<BlockState, CustomBlock>()

	override fun boostrap() {
		register(CustomBlockKeys.ALUMINUM_ORE, CustomBlock(
			key = CustomBlockKeys.ALUMINUM_ORE,
			blockData = mushroomBlockData(setOf(NORTH, UP)),
			drops = BlockLoot(
				requiredTool = { BlockLoot.Tool.PICKAXE },
				drops = customItemDrop(CustomItemKeys.RAW_ALUMINUM)
			),
			CustomItemKeys.ALUMINUM_ORE
		))
		register(CustomBlockKeys.ALUMINUM_BLOCK, CustomBlock(
			key = CustomBlockKeys.ALUMINUM_BLOCK,
			blockData = mushroomBlockData(setOf(SOUTH, UP, WEST)),
			drops = BlockLoot(
				requiredTool = { BlockLoot.Tool.PICKAXE },
				drops = customItemDrop(CustomItemKeys.ALUMINUM_BLOCK)
			),
			CustomItemKeys.ALUMINUM_BLOCK
		))
		register(CustomBlockKeys.RAW_ALUMINUM_BLOCK, CustomBlock(
			key = CustomBlockKeys.RAW_ALUMINUM_BLOCK,
			blockData = mushroomBlockData(setOf(NORTH)),
			drops = BlockLoot(
				requiredTool = { BlockLoot.Tool.PICKAXE },
				drops = customItemDrop(CustomItemKeys.RAW_ALUMINUM_BLOCK)
			),
			CustomItemKeys.RAW_ALUMINUM_BLOCK
		))

		register(CustomBlockKeys.CHETHERITE_ORE, CustomBlock(
			key = CustomBlockKeys.CHETHERITE_ORE,
			blockData = mushroomBlockData(setOf(EAST, NORTH, UP)),
			drops = BlockLoot(
				requiredTool = { BlockLoot.Tool.PICKAXE },
				drops = customItemDrop(CustomItemKeys.CHETHERITE)
			),
			CustomItemKeys.CHETHERITE_ORE
		))
		register(CustomBlockKeys.CHETHERITE_BLOCK, CustomBlock(
			key = CustomBlockKeys.CHETHERITE_BLOCK,
			blockData = mushroomBlockData(setOf(SOUTH, UP)),
			drops = BlockLoot(
				requiredTool = { BlockLoot.Tool.PICKAXE },
				drops = customItemDrop(CustomItemKeys.CHETHERITE_BLOCK)
			),
			CustomItemKeys.CHETHERITE_BLOCK
		))

		register(CustomBlockKeys.TITANIUM_ORE, CustomBlock(
			key = CustomBlockKeys.TITANIUM_ORE,
			blockData = mushroomBlockData(setOf(UP, WEST)),
			drops = BlockLoot(
				requiredTool = { BlockLoot.Tool.PICKAXE },
				drops = customItemDrop(CustomItemKeys.RAW_TITANIUM)
			),
			CustomItemKeys.TITANIUM_ORE
		))
		register(CustomBlockKeys.TITANIUM_BLOCK, CustomBlock(
			key = CustomBlockKeys.TITANIUM_BLOCK,
			blockData = mushroomBlockData(setOf(EAST, SOUTH, UP)),
			drops = BlockLoot(
				requiredTool = { BlockLoot.Tool.PICKAXE },
				drops = customItemDrop(CustomItemKeys.TITANIUM_BLOCK)
			),
			CustomItemKeys.TITANIUM_BLOCK
		))
		register(CustomBlockKeys.RAW_TITANIUM_BLOCK, CustomBlock(
			key = CustomBlockKeys.RAW_TITANIUM_BLOCK,
			blockData = mushroomBlockData(setOf(EAST)),
			drops = BlockLoot(
				requiredTool = { BlockLoot.Tool.PICKAXE },
				drops = customItemDrop(CustomItemKeys.RAW_TITANIUM_BLOCK)
			),
			CustomItemKeys.RAW_TITANIUM_BLOCK
		))

		register(CustomBlockKeys.URANIUM_ORE, CustomBlock(
			key = CustomBlockKeys.URANIUM_ORE,
			blockData = mushroomBlockData(setOf(UP)),
			drops = BlockLoot(
				requiredTool = { BlockLoot.Tool.PICKAXE },
				drops = customItemDrop(CustomItemKeys.RAW_URANIUM)
			),
			CustomItemKeys.URANIUM_ORE
		))
		register(CustomBlockKeys.URANIUM_BLOCK, CustomBlock(
			key = CustomBlockKeys.URANIUM_BLOCK,
			blockData = mushroomBlockData(setOf(EAST, NORTH, SOUTH, WEST)),
			drops = BlockLoot(
				requiredTool = { BlockLoot.Tool.PICKAXE },
				drops = customItemDrop(CustomItemKeys.URANIUM_BLOCK)
			),
			CustomItemKeys.URANIUM_BLOCK
		))
		register(CustomBlockKeys.RAW_URANIUM_BLOCK, CustomBlock(
			key = CustomBlockKeys.RAW_URANIUM_BLOCK,
			blockData = mushroomBlockData(setOf(SOUTH)),
			drops = BlockLoot(
				requiredTool = { BlockLoot.Tool.PICKAXE },
				drops = customItemDrop(CustomItemKeys.RAW_URANIUM_BLOCK)
			),
			CustomItemKeys.RAW_ALUMINUM_BLOCK
		))

		register(CustomBlockKeys.ENRICHED_URANIUM_BLOCK, CustomBlock(
			key = CustomBlockKeys.ENRICHED_URANIUM_BLOCK,
			blockData = mushroomBlockData(setOf(EAST, WEST)),
			drops = BlockLoot(
				requiredTool = { BlockLoot.Tool.PICKAXE },
				drops = customItemDrop(CustomItemKeys.ENRICHED_URANIUM_BLOCK)
			),
			CustomItemKeys.ENRICHED_URANIUM_BLOCK
		))
		register(CustomBlockKeys.NETHERITE_CASING, CustomBlock(
			key = CustomBlockKeys.NETHERITE_CASING,
			blockData = mushroomBlockData(setOf(WEST,NORTH,DOWN,UP)),
			drops = BlockLoot(
				requiredTool = { BlockLoot.Tool.PICKAXE },
				drops = customItemDrop(CustomItemKeys.NETHERITE_CASING)
			),
			CustomItemKeys.NETHERITE_CASING
		))
		register(CustomBlockKeys.STEEL_BLOCK, CustomBlock(
			key = CustomBlockKeys.STEEL_BLOCK,
			blockData = mushroomBlockData(setOf(SOUTH, UP, DOWN)),
			drops = BlockLoot(
				requiredTool = { BlockLoot.Tool.PICKAXE },
				drops = customItemDrop(CustomItemKeys.STEEL_BLOCK)
			),
			CustomItemKeys.STEEL_BLOCK
		))
		register(CustomBlockKeys.SUPERCONDUCTOR_BLOCK, CustomBlock(
			key = CustomBlockKeys.SUPERCONDUCTOR_BLOCK,
			blockData = mushroomBlockData(setOf(SOUTH, DOWN)),
			drops = BlockLoot(
				requiredTool = { BlockLoot.Tool.PICKAXE },
				drops = customItemDrop(CustomItemKeys.SUPERCONDUCTOR_BLOCK)
			),
			CustomItemKeys.SUPERCONDUCTOR_BLOCK
		))

		register(CustomBlockKeys.BATTLECRUISER_REACTOR_CORE, CustomBlock(
			key = CustomBlockKeys.BATTLECRUISER_REACTOR_CORE,
			blockData = mushroomBlockData(setOf(NORTH, UP, WEST)),
			drops = BlockLoot(
				requiredTool = null,
				drops = customItemDrop(CustomItemKeys.BATTLECRUISER_REACTOR_CORE)
			),
			CustomItemKeys.BATTLECRUISER_REACTOR_CORE
		))
		register(CustomBlockKeys.BARGE_REACTOR_CORE, CustomBlock(
			key = CustomBlockKeys.BARGE_REACTOR_CORE,
			blockData = mushroomBlockData(setOf(NORTH, EAST, WEST)),
			drops = BlockLoot(
				requiredTool = null,
				drops = customItemDrop(CustomItemKeys.BARGE_REACTOR_CORE)
			),
			CustomItemKeys.BARGE_REACTOR_CORE
		))
		register(CustomBlockKeys.CRUISER_REACTOR_CORE, CustomBlock(
			key = CustomBlockKeys.CRUISER_REACTOR_CORE,
			blockData = mushroomBlockData(setOf(NORTH, DOWN, WEST)),
			drops = BlockLoot(
				requiredTool = null,
				drops = customItemDrop(CustomItemKeys.CRUISER_REACTOR_CORE)
			),
			CustomItemKeys.CRUISER_REACTOR_CORE
		))

		register(CustomBlockKeys.MULTIBLOCK_WORKBENCH, MultiblockWorkbench)
		register(CustomBlockKeys.ADVANCED_ITEM_EXTRACTOR, AdvancedItemExtractorBlock)
		register(CustomBlockKeys.ITEM_FILTER, ItemFilterBlock)
	}

	override fun registerAdditional(key: IonRegistryKey<CustomBlock, *>, value: CustomBlock) {
		customBlocksData[value.blockData.nms] = value

		if (value is DirectionalCustomBlock) {
			for ((data, face) in value.faceLookup) {
				directionalCustomBlocksData[data.nms, value] = face
				customBlocksData[data.nms] = value
			}
		}
	}

	/** Gets the custom block present at this block. CAUTION: May load chunks! */
	operator fun get(block: Block): CustomBlock? = get(block.blockData)

	/** Gets the custom block that holds this block data */
	operator fun get(blockData: BlockData): CustomBlock? = customBlocksData[blockData.nms]

	/** Gets the custom block that holds this block data */
	operator fun get(blockState: BlockState): CustomBlock? = customBlocksData[blockState]

	companion object {
		val Block.customBlock get(): CustomBlock? = IonRegistries.CUSTOM_BLOCKS[this]
		val BlockData.customBlock get(): CustomBlock? = IonRegistries.CUSTOM_BLOCKS[this]

		fun mushroomBlockData(faces: Set<BlockFace>) : BlockData {
			return Material.BROWN_MUSHROOM_BLOCK.createBlockData { data ->
				for (face in (data as MultipleFacing).allowedFaces) {
					data.setFace(face, faces.contains(face))
				}
			}
		}

		fun customItemDrop(customItem: Supplier<CustomItem>, amount: Int = 1): Supplier<Collection<ItemStack>> {
			return customItem.map { item -> listOf(item.constructItemStack(amount)) }
		}

		fun customItemDrop(key: IonRegistryKey<CustomItem, out CustomItem>, amount: Int = 1): Supplier<Collection<ItemStack>> {
			return Supplier {
				val itemStack = key.getValue().constructItemStack()
				itemStack.amount = amount
				listOf(itemStack)
			}
		}
	}
}

