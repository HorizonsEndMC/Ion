package net.horizonsend.ion.server.features.ores.storage

import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.MultipleFacing
import java.util.EnumSet

/**
 * A placed ore.
 **/
enum class Ore(val blockData: BlockData, private val deepslateVariant: BlockData?) {
	CHETHERITE(Material.BROWN_MUSHROOM_BLOCK.createBlockData {
		it as MultipleFacing
		it.setFace(BlockFace.DOWN, false)
		it.setFace(BlockFace.SOUTH, false)
		it.setFace(BlockFace.WEST, false)
	}, null),
	ALUMINIUM(Material.BROWN_MUSHROOM_BLOCK.createBlockData {
		it as MultipleFacing
		it.setFace(BlockFace.DOWN, false)
		it.setFace(BlockFace.EAST, false)
		it.setFace(BlockFace.SOUTH, false)
		it.setFace(BlockFace.WEST, false)
	}, null),
	TITANIUM(Material.BROWN_MUSHROOM_BLOCK.createBlockData {
		it as MultipleFacing
		it.setFace(BlockFace.DOWN, false)
		it.setFace(BlockFace.EAST, false)
		it.setFace(BlockFace.NORTH, false)
		it.setFace(BlockFace.SOUTH, false)
	}, null),
	URANIUM(Material.BROWN_MUSHROOM_BLOCK.createBlockData {
		it as MultipleFacing
		it.setFace(BlockFace.DOWN, false)
		it.setFace(BlockFace.EAST, false)
		it.setFace(BlockFace.NORTH, false)
		it.setFace(BlockFace.SOUTH, false)
		it.setFace(BlockFace.WEST, false)
	}, null),
	NETHERITE(Material.ANCIENT_DEBRIS.createBlockData(), null),
	QUARTZ(Material.NETHER_QUARTZ_ORE.createBlockData(), null),
	REDSTONE(Material.REDSTONE_ORE.createBlockData(), Material.DEEPSLATE_REDSTONE_ORE.createBlockData()),
	DIAMOND(Material.DIAMOND_ORE.createBlockData(), Material.DEEPSLATE_DIAMOND_ORE.createBlockData()),
	EMERALD(Material.EMERALD_ORE.createBlockData(), Material.DEEPSLATE_EMERALD_ORE.createBlockData()),
	COPPER(Material.COPPER_ORE.createBlockData(), Material.DEEPSLATE_COPPER_ORE.createBlockData()),
	LAPIS(Material.LAPIS_ORE.createBlockData(), Material.DEEPSLATE_LAPIS_ORE.createBlockData()),
	COAL(Material.COAL_ORE.createBlockData(), Material.DEEPSLATE_COAL_ORE.createBlockData()),
	GOLD(Material.GOLD_ORE.createBlockData(), Material.DEEPSLATE_GOLD_ORE.createBlockData()),
	IRON(Material.IRON_ORE.createBlockData(), Material.DEEPSLATE_IRON_ORE.createBlockData());

	/**
	 * Given a block that will be replaced with an ore, return the block data that it should be replaced with
	 **/
	fun getReplacementType(replacingBlockData: BlockData): BlockData {
		val type = replacingBlockData.material

		return if (deepslateVariant != null && deepslateTypes.contains(type)) deepslateVariant else blockData
	}

	companion object {
		private val deepslateTypes = EnumSet.of(Material.DEEPSLATE, Material.COBBLED_DEEPSLATE, Material.POLISHED_DEEPSLATE)
	}
}
