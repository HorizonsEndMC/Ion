package net.horizonsend.ion.server.features.multiblock.type.fluid.collector

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.BasicFluidStoringEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.CategoryRestrictedInternalStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringEntity
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.world.ChunkMultiblockManager
import net.horizonsend.ion.server.features.transport.fluids.TransportedFluids.HYDROGEN
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
		return GasCollectorEntity(manager, data, x, y, z, world, structureDirection)
	}

	class GasCollectorEntity(
		manager: ChunkMultiblockManager,
		data: PersistentMultiblockData,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
	) : BasicFluidStoringEntity(manager, PipedGasCollectorMultiblock, data, x, y, z, world, structureDirection, CategoryRestrictedInternalStorage(500, FluidCategory.GAS)),
		AsyncTickingMultiblockEntity,
		FluidStoringEntity
	{
		private var lastTicked: Long = System.currentTimeMillis()

		override suspend fun tickAsync() {
			mainStorage.storage.addAmount(HYDROGEN, 1)
		}

		override fun onLoad() {

		}

		override fun onUnload() {

		}

		override fun handleRemoval() {

		}

		override fun toString(): String {
			return "Piped gas collector. Storage: $mainStorage"
		}
	}
}
