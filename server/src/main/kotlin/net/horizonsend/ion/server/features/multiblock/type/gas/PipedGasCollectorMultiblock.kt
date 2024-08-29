package net.horizonsend.ion.server.features.multiblock.type.gas

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.CategoryRestrictedInternalStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.InternalStorage
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.world.ChunkMultiblockManager
import net.horizonsend.ion.server.features.transport.fluids.TransportedFluids
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

object PipedGasCollectorMultiblock : Multiblock(),
	EntityMultiblock<PipedGasCollectorMultiblock.GasCollectorEntity>,
	InteractableMultiblock {
	override val name: String = "gascollector"

	override val signText: Array<Component?> = arrayOf(
		ofChildren(text("Gas ", RED), text("Collector", GOLD)),
		null,
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(0) {
				x(-1).lightningRod()
				x(0).anyCopperBlock()
				x(+1).lightningRod()
			}
			y(-1) {
				x(-1).anyStairs()
				x(0).fluidInput()
				x(+1).anyStairs()
			}
		}
		z(+1) {
			y(0) {
				x(-1).titaniumBlock()
				x(0).copperGrate()
				x(+1).titaniumBlock()
			}
			y(-1) {
				x(-1).titaniumBlock()
				x(0).copperGrate()
				x(+1).titaniumBlock()
			}

		}
		z(+2) {
			y(0) {
				x(-1).lightningRod()
				x(0).anyCopperBlock()
				x(+1).lightningRod()
			}
			y(-1) {
				x(-1).anyStairs()
				x(0).anyCopperBlock()
				x(+1).anyStairs()
			}

		}
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val entity = getMultiblockEntity(sign)

		player.information("Entity: $entity")
	}

	override fun createEntity(manager: ChunkMultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): GasCollectorEntity {
		return GasCollectorEntity(manager, x, y, z, world, structureDirection)
	}

	class GasCollectorEntity(
		manager: ChunkMultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
	) : MultiblockEntity(manager, PipedGasCollectorMultiblock, x, y, z, world, structureDirection),
		AsyncTickingMultiblockEntity,
		FluidStoringEntity
	{
		override val capacities: Array<InternalStorage> = arrayOf(CategoryRestrictedInternalStorage(500, FluidCategory.GAS))

		override suspend fun tickAsync() {
			firstCasStore(TransportedFluids.HYDROGEN, 1.0)?.addAmount(1)
		}

		override fun toString(): String {
			return """
				Piped gas collector :)
				Storage: ${getStoredResources()}
				""".trimIndent()
		}
	}
}
