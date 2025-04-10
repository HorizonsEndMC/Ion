package net.horizonsend.ion.server.features.starship.destruction

import net.horizonsend.ion.common.utils.miscellaneous.testRandom
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.customBlock
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData

class RemoveBlockSink(starship: ActiveStarship, val checkRemove: (Block) -> Boolean) : AdvancedSinkProvider(starship) {
	override fun setup() {
		starship.iterateBlocks { x, y, z ->
			if (checkRemove(starship.world.getBlockAt(x, y, z))) {
				starship.world.setType(x, y, z, Material.AIR)
			}
		}

		super.setup()
	}

	companion object {
		private const val MAX_PARTICLE_RADIUS = 100.0

		fun withChance(chances: Map<BlockWrapper, Double>): (Starship) -> SinkProvider {
			return { RemoveBlockSink(it, { block ->
				val wrapped = BlockWrapper.getWrapper(block.blockData)
				val chance = chances[wrapped] ?: return@RemoveBlockSink false
				return@RemoveBlockSink testRandom(chance)
			}) }
		}

		sealed interface BlockWrapper {
			fun matches(block: Block): Boolean

			class CustomBlockWrapper(val customBlock: CustomBlock) : BlockWrapper {
				override fun matches(block: Block): Boolean {
					return block.customBlock?.identifier == customBlock.identifier
				}
			}

			class MaterialWrapper(val material: Material) : BlockWrapper {
				override fun matches(block: Block): Boolean {
					return block.type == material
				}
			}

			companion object {
				fun getWrapper(blockData: BlockData): BlockWrapper {
					val customBlock = CustomBlocks.getByBlockData(blockData)
					if (customBlock != null) return CustomBlockWrapper(customBlock)

					return MaterialWrapper(blockData.material)
				}
			}
		}
	}
}
