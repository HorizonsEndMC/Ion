package net.horizonsend.ion.server.ores

import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.MultipleFacing

enum class Ore(
	val blockData: BlockData
) {
	Chetherite(
		Material.BROWN_MUSHROOM_BLOCK.createBlockData {
			it as MultipleFacing
			it.setFace(BlockFace.DOWN, false)
			it.setFace(BlockFace.SOUTH, false)
			it.setFace(BlockFace.WEST, false)
		}
	),
	Aluminium(
		Material.BROWN_MUSHROOM_BLOCK.createBlockData {
			it as MultipleFacing
			it.setFace(BlockFace.DOWN, false)
			it.setFace(BlockFace.EAST, false)
			it.setFace(BlockFace.SOUTH, false)
			it.setFace(BlockFace.WEST, false)
		}
	),
	Titanium(
		Material.BROWN_MUSHROOM_BLOCK.createBlockData {
			it as MultipleFacing
			it.setFace(BlockFace.DOWN, false)
			it.setFace(BlockFace.EAST, false)
			it.setFace(BlockFace.NORTH, false)
			it.setFace(BlockFace.SOUTH, false)
		}
	),
	Uranium(
		Material.BROWN_MUSHROOM_BLOCK.createBlockData {
			it as MultipleFacing
			it.setFace(BlockFace.DOWN, false)
			it.setFace(BlockFace.EAST, false)
			it.setFace(BlockFace.NORTH, false)
			it.setFace(BlockFace.SOUTH, false)
			it.setFace(BlockFace.WEST, false)
		}
	),
	Netherite(Material.ANCIENT_DEBRIS.createBlockData()),
	Quartz(Material.NETHER_QUARTZ_ORE.createBlockData()),
	Redstone(Material.REDSTONE_ORE.createBlockData()),
	Diamond(Material.DIAMOND_ORE.createBlockData()),
	Emerald(Material.EMERALD_ORE.createBlockData()),
	Copper(Material.COPPER_ORE.createBlockData()),
	Lapis(Material.LAPIS_ORE.createBlockData()),
	Coal(Material.COAL_ORE.createBlockData()),
	Gold(Material.GOLD_ORE.createBlockData()),
	Iron(Material.IRON_ORE.createBlockData())
}