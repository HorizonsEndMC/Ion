package net.starlegacy.feature.misc

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.starlegacy.util.NMSBlockData
import net.starlegacy.util.nms
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

open class CustomBlock(
    val id: String,
    val blockData: BlockData,
    private val tool: String,
    private val drops: Array<ItemStack>
) {
    private fun cloneDrops() = drops.map { it.clone() }.toTypedArray()

    @JvmOverloads
    fun getDrops(itemUsed: ItemStack? = null): Array<ItemStack> {
        if (itemUsed == null) {
            return cloneDrops()
        }
        val customItem = CustomItems[itemUsed]

        val isTool = customItem == null && itemUsed.type.name.toLowerCase().contains(tool)
        val isSpecialItem = customItem != null && customItem.id.toLowerCase().replace("drill", "pickaxe").contains(tool)

        return if (isTool || isSpecialItem) cloneDrops() else arrayOf()
    }
}

@Suppress("unused")
object CustomBlocks {
    private val idMap = mutableMapOf<String, CustomBlock>()
    private val blockDataMap = Object2ObjectOpenHashMap<NMSBlockData, CustomBlock>()

    private fun <T : CustomBlock> register(block: T): T {
        idMap[block.id] = block
        blockDataMap[block.blockData.nms] = block
        return block
    }

    private fun makeBlock(id: String, blockData: BlockData, tool: String, drops: Array<ItemStack>): CustomBlock {
        val block = CustomBlock(id, blockData, tool, drops)
        register(block)
        return block
    }

    private fun customItemDrop(id: String, amount: Int): Array<ItemStack> {
        return arrayOf(CustomItems[id]?.itemStack(amount) ?: error("No item for block $id"))
    }

    //region Minerals
    data class MineralBlockHolder(val ore: CustomBlock, val block: CustomBlock)

    private fun mineral(
        id: String,
        oreData: Set<BlockFace>,
        blockData: Set<BlockFace>,
        ore: String = "${id}_ore",
        oreDrops: Int = 1,
        block: String = "${id}_block",
        blockDrops: Int = 1
    ): MineralBlockHolder {
        val oreBlockData = Material.BROWN_MUSHROOM_BLOCK.createBlockData { data ->
            for (face in (data as MultipleFacing).allowedFaces) {
                data.setFace(face, oreData.contains(face))
            }
        }

        val blockBlockData = Material.BROWN_MUSHROOM_BLOCK.createBlockData { data ->
            for (face in (data as MultipleFacing).allowedFaces) {
                data.setFace(face, blockData.contains(face))
            }
        }

        val oreBlock = makeBlock(
            id = "${id}_ore",
            blockData = oreBlockData,
            tool = "pickaxe",
            drops = customItemDrop(ore, oreDrops)
        )

        val blockBlock = makeBlock(
            id = "${id}_block",
            blockData = blockBlockData,
            tool = "pickaxe",
            drops = customItemDrop(block, blockDrops)
        )

        return MineralBlockHolder(oreBlock, blockBlock)
    }

    val MINERAL_ALUMINUM = mineral("aluminum", setOf(NORTH, UP), setOf(SOUTH, UP, WEST))
    val MINERAL_CHETHERITE = mineral("chetherite", setOf(EAST, NORTH, UP), setOf(SOUTH, UP), "chetherite", 3)
    val MINERAL_COPPER = mineral("copper", setOf(NORTH, UP, WEST), setOf(EAST, UP))
    val MINERAL_TITANIUM = mineral("titanium", setOf(UP, WEST), setOf(EAST, SOUTH, UP))
    val MINERAL_URANIUM = mineral("uranium", setOf(UP), setOf(EAST, NORTH, SOUTH, WEST))
    val MINERAL_ORIOMIUM = mineral("oriomium", setOf(EAST, NORTH, SOUTH, WEST, UP, DOWN), setOf(DOWN), "oriomium", 6)
    //endregion Minerals

    operator fun get(blockId: String?): CustomBlock? = idMap[blockId]

    operator fun get(block: Block): CustomBlock? = this[block.blockData]

    operator fun get(blockData: BlockData): CustomBlock? = this[blockData.nms]

    operator fun get(blockData: NMSBlockData): CustomBlock? = blockDataMap[blockData]
}
