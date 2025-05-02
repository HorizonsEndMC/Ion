package net.horizonsend.ion.server.features.starship.destruction

import net.horizonsend.ion.common.utils.miscellaneous.testRandom
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.customBlock
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.AIShipDamager
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData

class RemoveBlockSink(starship: ActiveStarship, val checkRemove: (Block) -> RemovalState) : AdvancedSinkProvider(starship) {
	override fun setup() {
		val pointSum = starship.damagers.entries.sumOf { it.value.points.get() }
		val aiSinkTotal = starship.damagers.entries.filter { it.key is AIShipDamager }.sumOf { it.value.points.get() }
		val isPlayerSink = if (pointSum > 0) { aiSinkTotal.toDouble() / pointSum.toDouble() > 0.90 } else false

		starship.iterateBlocks { x, y, z ->
			val removalState = checkRemove(starship.world.getBlockAt(x, y, z))

			when (removalState) {
				RemovalState.Never -> return@iterateBlocks
				RemovalState.IfPlayerSink -> if (isPlayerSink) return@iterateBlocks
				RemovalState.Always -> {}
			}

			starship.world.setType(x, y, z, Material.AIR)
		}

		super.setup()
	}

	companion object {
		private const val MAX_PARTICLE_RADIUS = 100.0

		fun withChance(starship: Starship, chances: Map<BlockWrapper, Pair<Double, RemovalState>>): SinkProvider {
			return RemoveBlockSink(starship) { block ->
				val wrapped = BlockWrapper.getWrapper(block.blockData)
				val chance = chances[wrapped] ?: return@RemoveBlockSink RemovalState.Never
				val succeess = testRandom(chance.first)

				if (succeess) chance.second else RemovalState.Never
			}
		}

		sealed interface RemovalState {
			data object Never : RemovalState
			data object Always : RemovalState
			data object IfPlayerSink : RemovalState
		}

		sealed interface BlockWrapper {
			fun matches(block: Block): Boolean

			class CustomBlockWrapper(val customBlock: CustomBlock) : BlockWrapper {
				override fun matches(block: Block): Boolean {
					return block.customBlock?.identifier == customBlock.identifier
				}

				override fun equals(other: Any?): Boolean {
					if (this === other) return true
					if (javaClass != other?.javaClass) return false

					other as CustomBlockWrapper

					return customBlock == other.customBlock
				}

				override fun hashCode(): Int {
					return customBlock.hashCode()
				}
			}

			class MaterialWrapper(val material: Material) : BlockWrapper {
				override fun matches(block: Block): Boolean {
					return block.type == material
				}

				override fun equals(other: Any?): Boolean {
					if (this === other) return true
					if (javaClass != other?.javaClass) return false

					other as MaterialWrapper

					return material == other.material
				}

				override fun hashCode(): Int {
					return material.hashCode()
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
