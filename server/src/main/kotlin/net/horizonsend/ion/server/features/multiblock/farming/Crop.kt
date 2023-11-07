package net.horizonsend.ion.server.features.multiblock.farming

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Ageable
import org.bukkit.inventory.ItemStack

enum class Crop(val material: Material, val seed: Material) {
	WHEAT(Material.WHEAT, Material.WHEAT_SEEDS),
	CARROTS(Material.CARROTS, Material.CARROT),
	POTATOES(Material.POTATOES, Material.POTATO),
	BEETROOTS(Material.BEETROOTS, Material.BEETROOT_SEEDS),
	NETHER_WART(Material.NETHER_WART, Material.NETHER_WART),
	SWEET_BERRIES(Material.SWEET_BERRY_BUSH, Material.SWEET_BERRIES) {
		private val harvestedBush = Material.SWEET_BERRY_BUSH.createBlockData {
			it as Ageable

			it.age = 1
		}

		override fun harvest(block: Block) {
			block.blockData = harvestedBush
		}

		override fun getDrops(block: Block): Collection<ItemStack> = listOf(ItemStack(Material.SWEET_BERRIES))

		private val plantable = listOf(
			Material.FARMLAND,
			Material.DIRT,
			Material.GRASS_BLOCK,
			Material.ROOTED_DIRT,
			Material.PODZOL,
			Material.MYCELIUM,
			Material.MOSS_BLOCK,
			Material.MUD,
			Material.MUDDY_MANGROVE_ROOTS,
		)

		override fun canBePlanted(block: Block): Boolean {
			return plantable.contains(block.getRelative(BlockFace.DOWN).type)
		}
	}

	;

	open fun harvest(block: Block) {
		block.type = Material.AIR
	}

	open fun getDrops(block: Block): Collection<ItemStack> = block.drops

	open fun plant(block: Block) {
		block.type = material
	}

	open fun canBePlanted(block: Block): Boolean {
		return block.getRelative(BlockFace.DOWN).type == Material.FARMLAND
	}

	companion object {
		operator fun get(material: Material) = values().firstOrNull { it.material == material }
		fun findBySeed(material: Material) = values().firstOrNull { it.seed == material }
	}
}
