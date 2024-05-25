package net.horizonsend.ion.server.features.ores

import net.horizonsend.ion.server.features.ores.storage.Ore
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.MultipleFacing

/*
TODO: Enums should have full caps names, however simply renaming it will break things as these names are used in ore
	data storage.
*/

enum class OldOreData(
	val blockData: BlockData,
	val new: Ore
) {
	Chetherite(
		Material.BROWN_MUSHROOM_BLOCK.createBlockData {
			it as MultipleFacing
			it.setFace(BlockFace.DOWN, false)
			it.setFace(BlockFace.SOUTH, false)
			it.setFace(BlockFace.WEST, false)
		},
		Ore.CHETHERITE
	),
	Aluminium(
		Material.BROWN_MUSHROOM_BLOCK.createBlockData {
			it as MultipleFacing
			it.setFace(BlockFace.DOWN, false)
			it.setFace(BlockFace.EAST, false)
			it.setFace(BlockFace.SOUTH, false)
			it.setFace(BlockFace.WEST, false)
		},
		Ore.ALUMINIUM
	),
	Titanium(
		Material.BROWN_MUSHROOM_BLOCK.createBlockData {
			it as MultipleFacing
			it.setFace(BlockFace.DOWN, false)
			it.setFace(BlockFace.EAST, false)
			it.setFace(BlockFace.NORTH, false)
			it.setFace(BlockFace.SOUTH, false)
		},
		Ore.TITANIUM
	),
	Uranium(
		Material.BROWN_MUSHROOM_BLOCK.createBlockData {
			it as MultipleFacing
			it.setFace(BlockFace.DOWN, false)
			it.setFace(BlockFace.EAST, false)
			it.setFace(BlockFace.NORTH, false)
			it.setFace(BlockFace.SOUTH, false)
			it.setFace(BlockFace.WEST, false)
		},
		Ore.URANIUM
	),
	Netherite(Material.ANCIENT_DEBRIS.createBlockData(), Ore.NETHERITE),
	Quartz(Material.NETHER_QUARTZ_ORE.createBlockData(), Ore.QUARTZ),
	Redstone(Material.REDSTONE_ORE.createBlockData(), Ore.REDSTONE),
	Diamond(Material.DIAMOND_ORE.createBlockData(), Ore.DIAMOND),
	Emerald(Material.EMERALD_ORE.createBlockData(), Ore.EMERALD),
	Copper(Material.COPPER_ORE.createBlockData(), Ore.COPPER),
	Lapis(Material.LAPIS_ORE.createBlockData(), Ore.LAPIS),
	Coal(Material.COAL_ORE.createBlockData(), Ore.COAL),
	Gold(Material.GOLD_ORE.createBlockData(), Ore.GOLD),
	Iron(Material.IRON_ORE.createBlockData(), Ore.IRON)
}
