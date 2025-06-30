package net.horizonsend.ion.server.features.multiblock.type.power

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.transport.inputs.InputsData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace

object CreativePowerSourceMultiblock : Multiblock(), EntityMultiblock<CreativePowerSourceMultiblock.CreativePowerSourceEntity> {
	override val name: String = "creativesource"
	override val signText: Array<Component?> = createSignText(
		Component.text("Creative Power", NamedTextColor.DARK_PURPLE),
		Component.text("--Source--", NamedTextColor.RED),
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(+0) {
				x(-1).extractor()
				x(+0).powerInput()
				x(+1).extractor()
			}
		}

		z(+1) {
			y(+0) {
				x(-1).type(Material.COMMAND_BLOCK)
				x(+0).type(Material.COMMAND_BLOCK)
				x(+1).type(Material.COMMAND_BLOCK)
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): CreativePowerSourceEntity {
		return CreativePowerSourceEntity(manager, x, y, z, world, structureDirection)
	}

	class CreativePowerSourceEntity(
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureFace: BlockFace
	) : MultiblockEntity(manager, CreativePowerSourceMultiblock, world, x, y, z, structureFace), PoweredMultiblockEntity, AsyncTickingMultiblockEntity {
		override val powerStorage: PowerStorage = PowerStorage(this, Integer.MAX_VALUE, Integer.MAX_VALUE)
		override val maxPower: Int = Integer.MAX_VALUE
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(1)

		override val inputsData: InputsData = InputsData.builder(this)
			.addPowerInput(0, 0, 0)
			.registerSignInputs()
			.build()

		override fun tickAsync() {
			powerStorage.setPower(Integer.MAX_VALUE)
		}
	}
}
