package net.horizonsend.ion.server.features.multiblock.type.fluid.collector

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.CategoryRestrictedInternalStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.StorageContainer
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.world.ChunkMultiblockManager
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory.GAS
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.TANK_1
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.TANK_2
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.TANK_3
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.math.roundToInt

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
				x(0).copperBlock()
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
				x(0).copperBlock()
				x(+1).lightningRod()
			}
			y(-1) {
				x(-1).anyStairs()
				x(0).copperBlock()
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
	) : MultiblockEntity(
		manager,
		PipedGasCollectorMultiblock,
		x,
		y,
		z,
		world,
		structureDirection
	), AsyncTickingMultiblockEntity, FluidStoringEntity {
		override val capacities: Array<StorageContainer> = arrayOf(
			loadStoredResource(data, "tank_1", text("Tank 1"), TANK_1, CategoryRestrictedInternalStorage(500, GAS)),
			loadStoredResource(data, "tank_2", text("Tank 2"), TANK_2, CategoryRestrictedInternalStorage(500, GAS)),
			loadStoredResource(data, "tank_3", text("Tank 3"), TANK_3, CategoryRestrictedInternalStorage(500, GAS)),
		)

		private val worldConfig get() = world.ion.configuration.gasConfiguration

		override suspend fun tickAsync() {
			val amounts = worldConfig.gasses.associate { it.gas to it.factorStack.getAmount(location) }

			val deltaT = deltaTMS / 1000.0

			amounts.forEach { (gas, amount) ->
				val fluid = gas.fluid
				val adjusted = (amount * deltaT).roundToInt()

				addFirstAvailable(fluid, adjusted)
			}
		}

		override fun storeAdditionalData(store: PersistentMultiblockData) {
			val rawStorage = store.getAdditionalDataRaw()
			storeFluidData(rawStorage, rawStorage.adapterContext)
		}

		override fun toString(): String {
			return "Piped gas collector. Storages: ${capacities.toList()}"
		}
	}
}
