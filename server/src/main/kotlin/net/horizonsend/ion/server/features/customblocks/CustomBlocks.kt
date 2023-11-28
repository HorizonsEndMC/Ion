package net.horizonsend.ion.server.features.customblocks

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.horizonsend.ion.server.features.customitems.CustomItems
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockFace.EAST
import org.bukkit.block.BlockFace.NORTH
import org.bukkit.block.BlockFace.SOUTH
import org.bukkit.block.BlockFace.UP
import org.bukkit.block.BlockFace.WEST
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.MultipleFacing
import org.bukkit.inventory.ItemStack

object CustomBlocks {
    val ALL get() = customBlocks.values
    private val customBlocks: MutableMap<String, CustomBlock> = mutableMapOf()
    private val customBlocksData = Object2ObjectOpenHashMap<BlockState, CustomBlock>()

    private fun mushroomBlockData(faces: Set<BlockFace>) : BlockData {
        return Material.BROWN_MUSHROOM_BLOCK.createBlockData { data ->
            for (face in (data as MultipleFacing).allowedFaces) {
                data.setFace(face, faces.contains(face))
            }
        }
    }

    val ALUMINUM_ORE = register(CustomBlock(
        identifier = "ALUMINUM_ORE",
        blockData = mushroomBlockData(setOf(NORTH, UP)),
        tool = "pickaxe",
        drops = customItemDrop("RAW_ALUMINUM")
    ))

    val ALUMINUM_BLOCK = register(CustomBlock(
        identifier = "ALUMINUM_BLOCK",
        blockData = mushroomBlockData(setOf(SOUTH, UP, WEST)),
        tool = "pickaxe",
        drops = customItemDrop("ALUMINUM_BLOCK")
    ))

    val CHETHERITE_ORE = register(CustomBlock(
        identifier = "CHETHERITE_ORE",
        blockData = mushroomBlockData(setOf(EAST, NORTH, UP)),
        tool = "pickaxe",
        drops = customItemDrop("CHETHERITE")
    ))

    val CHETHERITE_BLOCK = register(CustomBlock(
        identifier = "CHETHERITE_BLOCK",
        blockData = mushroomBlockData(setOf(SOUTH, UP)),
        tool = "pickaxe",
        drops = customItemDrop("CHETHERITE_BLOCK")
    ))

    val TITANIUM_ORE = register(CustomBlock(
        identifier = "TITANIUM_ORE",
        blockData = mushroomBlockData(setOf(UP, WEST)),
        tool = "pickaxe",
        drops = customItemDrop("RAW_TITANIUM")
    ))

    val TITANIUM_BLOCK = register(CustomBlock(
        identifier = "TITANIUM_BLOCK",
        blockData = mushroomBlockData(setOf(EAST, SOUTH, UP)),
        tool = "pickaxe",
        drops = customItemDrop("TITANIUM_BLOCK")
    ))

    val URANIUM_ORE = register(CustomBlock(
        identifier = "URANIUM_ORE",
        blockData = mushroomBlockData(setOf(UP)),
        tool = "pickaxe",
        drops = customItemDrop("RAW_URANIUM")
    ))

    val URANIUM_BLOCK = register(CustomBlock(
        identifier = "URANIUM_BLOCK",
        blockData = mushroomBlockData(setOf(EAST, NORTH, SOUTH, WEST)),
        tool = "pickaxe",
        drops = customItemDrop("URANIUM_BLOCK")
    ))

    private fun customItemDrop(identifier: String, amount: Int = 1): List<ItemStack> {
        val customItem = CustomItems.getByIdentifier(identifier)?.constructItemStack() ?: return listOf()
        customItem.amount = 1
        return listOf(customItem)
    }

    fun <T : CustomBlock> register(customBlock: T): T {
        customBlocks[customBlock.identifier] = customBlock
        customBlocksData[customBlock.blockData.nms] = customBlock
        return customBlock
    }

    val identifiers = customBlocks.keys

    fun getByIdentifier(identifier: String): CustomBlock? = customBlocks[identifier]

    fun getByBlock(block: Block): CustomBlock? = this.getByBlockData(block.blockData)

    fun getByBlockData(blockData: BlockData): CustomBlock? = customBlocksData[blockData.nms]

    fun getByBlockState(blockState: BlockState): CustomBlock? = customBlocksData[blockState]
}

open class CustomBlock(
    val identifier: String,
    val blockData: BlockData,
    private val tool: String,
    private val drops: List<ItemStack>
) {
    private fun cloneDrops() = drops.map { it.clone() }.toList()

    fun getDrops(itemUsed: ItemStack? = null): List<ItemStack> {
        if (itemUsed == null) {
            return cloneDrops()
        }

        val customItem = itemUsed.customItem
        val isTool = customItem == null && itemUsed.type.name.contains(tool, ignoreCase = true)
        val isSpecialItem = customItem != null && customItem.identifier.replace("drill", "pickaxe").contains(tool, ignoreCase = true)

        return if (isTool || isSpecialItem) cloneDrops() else listOf()
    }
}