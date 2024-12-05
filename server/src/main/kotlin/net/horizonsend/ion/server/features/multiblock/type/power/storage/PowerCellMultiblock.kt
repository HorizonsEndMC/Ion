package net.horizonsend.ion.server.features.multiblock.type.power.storage

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputsData
import net.kyori.adventure.text.Component
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign

object PowerCellMultiblock : Multiblock(), EntityMultiblock<PowerCellMultiblock.PowerCellEntity>, DisplayNameMultilblock {
	override val name = "powercell"

	override val signText = createSignText(
		line1 = "&6Power &8Cell",
		line2 = "------",
		line3 = null,
		line4 = "&cCompact Power"
	)

	override val displayName: Component = Component.text("Power Cell")

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(+0) {
				x(-1).anyGlassPane()
				x(+0).powerInput()
				x(+1).anyGlassPane()
			}
		}

		z(+1) {
			y(+0) {
				x(-1).anyGlassPane()
				x(+0).redstoneBlock()
				x(+1).anyGlassPane()
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): PowerCellEntity {
		return PowerCellEntity(data, manager, x, y, z, world, structureDirection)
	}

	class PowerCellEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureFace: BlockFace
	) : SimplePoweredEntity(data, PowerCellMultiblock, manager, x, y, z, world, structureFace, 50_000), LegacyMultiblockEntity, PoweredMultiblockEntity {
		override val displayHandler = standardPowerDisplay(this)

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}

		override val inputsData: InputsData = InputsData.builder(this).addPowerInput(0, 0, 0).build()
	}
}
