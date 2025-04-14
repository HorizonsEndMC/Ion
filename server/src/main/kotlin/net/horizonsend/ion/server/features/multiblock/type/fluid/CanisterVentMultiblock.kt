package net.horizonsend.ion.server.features.multiblock.type.fluid

import net.horizonsend.ion.server.configuration.ConfigurationFiles.globalGassesConfiguration
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.type.GasCanister
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.FurnaceBasedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputsData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.block.BlockFace

object CanisterVentMultiblock : Multiblock(), EntityMultiblock<CanisterVentMultiblock.CanisterVentMultiblockEntity>, DisplayNameMultilblock {
	override val name: String = "vent"

	override val signText: Array<Component?> = arrayOf(
		text()
			.append(text("Gas", NamedTextColor.RED))
			.append(text(" Vent", NamedTextColor.GOLD))
			.build(),
		null,
		null,
		null
	)

	override val displayName: Component
		get() = text("Canister Gas Vent")
	override val description: Component
		get() = text("Removes gas from a Gas Canister.")

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
	) : MultiblockEntity(manager, CanisterVentMultiblock, world, x, y, z, structureDirection), SyncTickingMultiblockEntity, FurnaceBasedMultiblockEntity {
		val configuration get() = globalGassesConfiguration()
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(20)

		override fun tick() {
			val furnaceInventory = getFurnaceInventory() ?: return

			val result = furnaceInventory.result
			if (result != null && !result.isEmpty) return

			val fuel = furnaceInventory.fuel ?: return
			val customItem = fuel.customItem ?: return
			if (customItem !is GasCanister) return

			furnaceInventory.fuel = null
			furnaceInventory.result = Gasses.EMPTY_CANISTER
		}

		override val inputsData: InputsData = none()
	}
}
