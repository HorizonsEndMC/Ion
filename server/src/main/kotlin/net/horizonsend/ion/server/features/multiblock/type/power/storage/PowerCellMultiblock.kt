package net.horizonsend.ion.server.features.multiblock.type.power.storage

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.NewPoweredMultiblock
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign

object PowerCellMultiblock : Multiblock(), NewPoweredMultiblock<PowerCellMultiblock.PowerCellEntity> {
	override val name = "powercell"

	override val signText = createSignText(
		line1 = "&6Power &8Cell",
		line2 = "------",
		line3 = null,
		line4 = "&cCompact Power"
	)

	override val maxPower = 50_000

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(+0) {
				x(-1).anyGlassPane()
				x(+0).wireInputComputer()
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
		return PowerCellEntity(
			data,
			manager,
			this,
			x,
			y,
			z,
			world,
			structureDirection
		)
	}

	class PowerCellEntity(
        data: PersistentMultiblockData,
        manager: MultiblockManager,
        override val poweredMultiblock: PowerCellMultiblock,
        x: Int,
        y: Int,
        z: Int,
        world: World,
        structureDirection: BlockFace
	) : SimplePoweredMultiblockEntity(data, manager, poweredMultiblock, x, y, z, world, structureDirection), LegacyMultiblockEntity {
		override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			PowerEntityDisplay(this, +0.0, +0.0, +0.0, 0.5f)
		).register()

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}
	}
}
