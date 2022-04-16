package net.horizonsend.ion.ores

import org.bukkit.Material.ANCIENT_DEBRIS
import org.bukkit.Material.BROWN_MUSHROOM_BLOCK
import org.bukkit.Material.COAL_ORE
import org.bukkit.Material.COPPER_ORE
import org.bukkit.Material.DIAMOND_ORE
import org.bukkit.Material.EMERALD_ORE
import org.bukkit.Material.GOLD_ORE
import org.bukkit.Material.IRON_ORE
import org.bukkit.Material.LAPIS_ORE
import org.bukkit.Material.REDSTONE_ORE
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.BlockFace.EAST
import org.bukkit.block.BlockFace.NORTH
import org.bukkit.block.BlockFace.SOUTH
import org.bukkit.block.BlockFace.WEST
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.MultipleFacing

internal enum class Ore(
	internal val blockData: BlockData
) {
	Chetherite(
		BROWN_MUSHROOM_BLOCK.createBlockData {
			it as MultipleFacing
			it.setFace(DOWN, false)
			it.setFace(SOUTH, false)
			it.setFace(WEST, false)
		}
	),
	Aluminium(
		BROWN_MUSHROOM_BLOCK.createBlockData {
			it as MultipleFacing
			it.setFace(DOWN, false)
			it.setFace(EAST, false)
			it.setFace(SOUTH, false)
			it.setFace(WEST, false)
		}
	),
	Titanium(
		BROWN_MUSHROOM_BLOCK.createBlockData {
			it as MultipleFacing
			it.setFace(DOWN, false)
			it.setFace(EAST, false)
			it.setFace(NORTH, false)
			it.setFace(SOUTH, false)
		}
	),
	Uranium(
		BROWN_MUSHROOM_BLOCK.createBlockData {
			it as MultipleFacing
			it.setFace(DOWN, false)
			it.setFace(EAST, false)
			it.setFace(NORTH, false)
			it.setFace(SOUTH, false)
			it.setFace(WEST, false)
		}
	),
	Netherite(ANCIENT_DEBRIS.createBlockData()),
	Redstone(REDSTONE_ORE.createBlockData()),
	Diamond(DIAMOND_ORE.createBlockData()),
	Emerald(EMERALD_ORE.createBlockData()),
	Copper(COPPER_ORE.createBlockData()),
	Lapis(LAPIS_ORE.createBlockData()),
	Coal(COAL_ORE.createBlockData()),
	Gold(GOLD_ORE.createBlockData()),
	Iron(IRON_ORE.createBlockData()),
}