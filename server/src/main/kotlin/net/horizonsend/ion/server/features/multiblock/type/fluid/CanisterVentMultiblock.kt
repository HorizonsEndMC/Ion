package net.horizonsend.ion.server.features.multiblock.type.fluid

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.horizonsend.ion.server.features.custom.items.GasCanister
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.fluid.collector.CanisterGasCollectorMultiblock
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputsData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.inventory.FurnaceInventory

object CanisterVentMultiblock : Multiblock(), EntityMultiblock<CanisterVentMultiblock.CanisterVentMultiblockEntity> {
	override val name: String = "vent"

	override val signText: Array<Component?> = arrayOf(
		Component.text()
			.append(Component.text("Gas", NamedTextColor.RED))
			.append(Component.text(" Vent", NamedTextColor.GOLD))
			.build(),
		null,
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).lightningRod()
				x(+0).ironBlock()
				x(+1).lightningRod()
			}
			y(+0) {
				x(-1).lightningRod()
				x(+0).machineFurnace()
				x(+1).lightningRod()
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): CanisterVentMultiblockEntity {
		return CanisterVentMultiblockEntity(manager, x, y, z, world, structureDirection)
	}

	class CanisterVentMultiblockEntity(
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
	) : MultiblockEntity(manager, CanisterGasCollectorMultiblock, x, y, z, world, structureDirection), AsyncTickingMultiblockEntity {
		val configuration get() = IonServer.globalGassesConfiguration
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(20)

		override fun tickAsync() {
			val furnaceInventory = getInventory(0, 0, 0) as? FurnaceInventory ?: return

			val fuel = furnaceInventory.fuel ?: return
			val customItem = fuel.customItem ?: return
			if (customItem !is GasCanister) return

			furnaceInventory.fuel = null
			furnaceInventory.result = Gasses.EMPTY_CANISTER
		}

		override val inputsData: InputsData = none()
	}
}
