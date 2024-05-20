package net.horizonsend.ion.server.features.ores.storage

import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.MultipleFacing

/**
 * A placed ore.
 **/
enum class Ore(val blockData: BlockData) {
	CHETHERITE(Material.BROWN_MUSHROOM_BLOCK.createBlockData {
		it as MultipleFacing
		it.setFace(BlockFace.DOWN, false)
		it.setFace(BlockFace.SOUTH, false)
		it.setFace(BlockFace.WEST, false)
	}),
	ALUMINIUM(Material.BROWN_MUSHROOM_BLOCK.createBlockData {
		it as MultipleFacing
		it.setFace(BlockFace.DOWN, false)
		it.setFace(BlockFace.EAST, false)
		it.setFace(BlockFace.SOUTH, false)
		it.setFace(BlockFace.WEST, false)
	}),
	TITANIUM(Material.BROWN_MUSHROOM_BLOCK.createBlockData {
		it as MultipleFacing
		it.setFace(BlockFace.DOWN, false)
		it.setFace(BlockFace.EAST, false)
		it.setFace(BlockFace.NORTH, false)
		it.setFace(BlockFace.SOUTH, false)
	}),
	URANIUM(Material.BROWN_MUSHROOM_BLOCK.createBlockData {
		it as MultipleFacing
		it.setFace(BlockFace.DOWN, false)
		it.setFace(BlockFace.EAST, false)
		it.setFace(BlockFace.NORTH, false)
		it.setFace(BlockFace.SOUTH, false)
		it.setFace(BlockFace.WEST, false)
	}),
	NETHERITE(Material.ANCIENT_DEBRIS.createBlockData()),
	QUARTZ(Material.NETHER_QUARTZ_ORE.createBlockData()),
	REDSTONE(Material.REDSTONE_ORE.createBlockData()),
	DIAMOND(Material.DIAMOND_ORE.createBlockData()),
	EMERALD(Material.EMERALD_ORE.createBlockData()),
	COPPER(Material.COPPER_ORE.createBlockData()),
	LAPIS(Material.LAPIS_ORE.createBlockData()),
	COAL(Material.COAL_ORE.createBlockData()),
	GOLD(Material.GOLD_ORE.createBlockData()),
	IRON(Material.IRON_ORE.createBlockData())
}
