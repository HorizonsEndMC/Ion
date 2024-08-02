package net.horizonsend.ion.server.features.custom.blocks

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.custom.items.CustomItems.POWER_DRILL_BASIC
import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.horizonsend.ion.server.miscellaneous.utils.getMatchingMaterials
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

object CustomBlocks {
    val ALL get() = customBlocks.values
    private val customBlocks: MutableMap<String, CustomBlock> = mutableMapOf()
    private val customBlocksData = Object2ObjectOpenHashMap<BlockState, CustomBlock>()

    fun mushroomBlockData(faces: Set<BlockFace>) : BlockData {
        return Material.BROWN_MUSHROOM_BLOCK.createBlockData { data ->
            for (face in (data as MultipleFacing).allowedFaces) {
                data.setFace(face, faces.contains(face))
            }
        }
    }

    val ALUMINUM_ORE = register(CustomBlock(
        identifier = "ALUMINUM_ORE",
        blockData = mushroomBlockData(setOf(NORTH, UP)),
		drops = BlockLoot(
			requiredTool = BlockLoot.Tool.PICKAXE,
        	drops = customItemDrop("RAW_ALUMINUM")
		)
    ))

    val ALUMINUM_BLOCK = register(CustomBlock(
        identifier = "ALUMINUM_BLOCK",
        blockData = mushroomBlockData(setOf(SOUTH, UP, WEST)),
		drops = BlockLoot(
			requiredTool = BlockLoot.Tool.PICKAXE,
        	drops = customItemDrop("ALUMINUM_BLOCK")
		)
    ))

    val RAW_ALUMINUM_BLOCK = register(CustomBlock(
        identifier = "RAW_ALUMINUM_BLOCK",
        blockData = mushroomBlockData(setOf(NORTH)),
		drops = BlockLoot(
			requiredTool = BlockLoot.Tool.PICKAXE,
        	drops = customItemDrop("RAW_ALUMINUM_BLOCK")
		)
    ))

    val CHETHERITE_ORE = register(CustomBlock(
        identifier = "CHETHERITE_ORE",
        blockData = mushroomBlockData(setOf(EAST, NORTH, UP)),
		drops = BlockLoot(
			requiredTool = BlockLoot.Tool.PICKAXE,
        	drops = customItemDrop("CHETHERITE")
		)
    ))

	val STEEL_BLOCK = register(CustomBlock(
		identifier = "STEEL_BLOCK",
		blockData = mushroomBlockData(setOf(SOUTH, UP, DOWN)),
		drops = BlockLoot(
			requiredTool = BlockLoot.Tool.PICKAXE,
			drops = customItemDrop("STEEL_BLOCK")
		)
	))

    val CHETHERITE_BLOCK = register(CustomBlock(
        identifier = "CHETHERITE_BLOCK",
        blockData = mushroomBlockData(setOf(SOUTH, UP)),
		drops = BlockLoot(
			requiredTool = BlockLoot.Tool.PICKAXE,
        	drops = customItemDrop("CHETHERITE_BLOCK")
		)
    ))

    val TITANIUM_ORE = register(CustomBlock(
        identifier = "TITANIUM_ORE",
        blockData = mushroomBlockData(setOf(UP, WEST)),
		drops = BlockLoot(
			requiredTool = BlockLoot.Tool.PICKAXE,
        	drops = customItemDrop("RAW_TITANIUM")
		)
    ))

    val TITANIUM_BLOCK = register(CustomBlock(
        identifier = "TITANIUM_BLOCK",
        blockData = mushroomBlockData(setOf(EAST, SOUTH, UP)),
		drops = BlockLoot(
			requiredTool = BlockLoot.Tool.PICKAXE,
        	drops = customItemDrop("TITANIUM_BLOCK")
		)
    ))

    val RAW_TITANIUM_BLOCK = register(CustomBlock(
        identifier = "RAW_TITANIUM_BLOCK",
        blockData = mushroomBlockData(setOf(EAST)),
		drops = BlockLoot(
			requiredTool = BlockLoot.Tool.PICKAXE,
        	drops = customItemDrop("RAW_TITANIUM_BLOCK")
		)
    ))

    val URANIUM_ORE = register(CustomBlock(
        identifier = "URANIUM_ORE",
        blockData = mushroomBlockData(setOf(UP)),
		drops = BlockLoot(
			requiredTool = BlockLoot.Tool.PICKAXE,
        	drops = customItemDrop("RAW_URANIUM")
		)
    ))

    val URANIUM_BLOCK = register(CustomBlock(
        identifier = "URANIUM_BLOCK",
        blockData = mushroomBlockData(setOf(EAST, NORTH, SOUTH, WEST)),
		drops = BlockLoot(
			requiredTool = BlockLoot.Tool.PICKAXE,
        	drops = customItemDrop("URANIUM_BLOCK")
		)
    ))

	val ENRICHED_URANIUM_BLOCK = register(CustomBlock(
		identifier = "ENRICHED_URANIUM_BLOCK",
		blockData = mushroomBlockData(setOf(EAST, WEST)),
		drops = BlockLoot(
			requiredTool = BlockLoot.Tool.PICKAXE,
			drops = customItemDrop("ENRICHED_URANIUM_BLOCK")
		)
	))

    val NETHERITE_CASING = register(CustomBlock(
        identifier = "NETHERITE_CASING",
        blockData = mushroomBlockData(setOf(WEST,NORTH,DOWN,UP)),
		drops = BlockLoot(
			requiredTool = BlockLoot.Tool.PICKAXE,
       	 drops = customItemDrop("NETHERITE_CASING")
		)
    ))

    val RAW_URANIUM_BLOCK = register(CustomBlock(
        identifier = "RAW_URANIUM_BLOCK",
        blockData = mushroomBlockData(setOf(SOUTH)),
		drops = BlockLoot(
			requiredTool = BlockLoot.Tool.PICKAXE,
			drops = customItemDrop("RAW_URANIUM_BLOCK")
		)
    ))

	val SUPERCONDUCTOR_BLOCK = register(CustomBlock(
		identifier = "SUPERCONDUCTOR_BLOCK",
		blockData = mushroomBlockData(setOf(SOUTH, DOWN)),
		drops = BlockLoot(
			requiredTool = BlockLoot.Tool.PICKAXE,
			drops = customItemDrop("SUPERCONDUCTOR_BLOCK")
		)
	))

	val BATTLECRUISER_REACTOR_CORE = register(CustomBlock(
		identifier = "BATTLECRUISER_REACTOR_CORE",
		blockData = mushroomBlockData(setOf(NORTH, UP, WEST)),
		drops = BlockLoot(
			requiredTool = null,
			drops = customItemDrop("BATTLECRUISER_REACTOR_CORE")
		)
	))

    val BARGE_REACTOR_CORE = register(CustomBlock(
        identifier = "BARGE_REACTOR_CORE",
        blockData = mushroomBlockData(setOf(NORTH, EAST, WEST)),
		drops = BlockLoot(
			requiredTool = null,
			drops = customItemDrop("BARGE_REACTOR_CORE")
		)
    ))

	val CRUISER_REACTOR_CORE = register(CustomBlock(
        identifier = "CRUISER_REACTOR_CORE",
        blockData = mushroomBlockData(setOf(NORTH, DOWN, WEST)),
		drops = BlockLoot(
			requiredTool = null,
			drops = customItemDrop("CRUISER_REACTOR_CORE")
		)
	))

    private fun customItemDrop(identifier: String, amount: Int = 1): Supplier<Collection<ItemStack>> {
        val customItem = CustomItems.getByIdentifier(identifier)?.constructItemStack() ?: return Supplier { listOf() }
        customItem.amount = amount

        return Supplier { listOf(customItem) }
    }

    fun customItemDrop(customItem: Supplier<CustomItem>, amount: Int = 1): Supplier<Collection<ItemStack>> {
        val itemStack = customItem.get().constructItemStack().asQuantity(amount)

        return Supplier { listOf(itemStack) }
    }

    fun <T : CustomBlock> register(customBlock: T): T {
        customBlocks[customBlock.identifier] = customBlock
        customBlocksData[customBlock.blockData.nms] = customBlock
        return customBlock
    }

    val identifiers = customBlocks.keys

	val Block.customBlock get(): CustomBlock? = getByBlock(this)

    fun getByIdentifier(identifier: String): CustomBlock? = customBlocks[identifier]

    fun getByBlock(block: Block): CustomBlock? = getByBlockData(block.blockData)

    fun getByBlockData(blockData: BlockData): CustomBlock? = customBlocksData[blockData.nms]

    fun getByBlockState(blockState: BlockState): CustomBlock? = customBlocksData[blockState]
}

open class CustomBlock(
    val identifier: String,
    val blockData: BlockData,
    val drops: BlockLoot
)

data class BlockLoot(
	val requiredTool: Tool? = Tool.PICKAXE,
	val drops: Supplier<Collection<ItemStack>>,
	val silkTouchDrops: Supplier<Collection<ItemStack>> = drops
) {
	fun getDrops(tool: ItemStack?, silkTouch: Boolean): Collection<ItemStack> {
		if (tool != null && requiredTool != null) {
			if (!requiredTool.matches(tool)) return listOf()
		}

		if (silkTouch) return silkTouchDrops.get().map { it.clone() }

		return drops.get().map { it.clone() }
	}

	companion object ToolPredicate {
		fun matchMaterial(material: Material): (ItemStack) -> Boolean {
			return { it.type == material }
		}

		fun matchAnyMaterial(materials: Iterable<Material>): (ItemStack) -> Boolean {
			return { materials.contains(it.type) }
		}

		fun customItem(customItem: CustomItem): (ItemStack) -> Boolean {
			return { it.customItem == customItem }
		}
	}

	enum class Tool(vararg val checks: (ItemStack) -> Boolean) {
		PICKAXE(
			customItem(POWER_DRILL_BASIC),
			matchAnyMaterial(getMatchingMaterials { it.name.endsWith("PICKAXE") })
		),
		SHOVEL(
			customItem(POWER_DRILL_BASIC),
			matchAnyMaterial(getMatchingMaterials { it.name.endsWith("SHOVEL") })

		),
		AXE(
			customItem(POWER_DRILL_BASIC),
			matchAnyMaterial(getMatchingMaterials { it.name.endsWith("AXE") })

		),
		SHEARS(
			customItem(POWER_DRILL_BASIC),
			matchMaterial(Material.SHEARS)
		);

		fun matches(itemStack: ItemStack): Boolean = checks.any { it.invoke(itemStack) }
	}
}
