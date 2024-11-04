package net.horizonsend.ion.server.features.multiblock.type.industry

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.RecipeEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign

object PlatePressMultiblock : Multiblock(), EntityMultiblock<PlatePressMultiblock.PlatePressMultiblockEntity> {
	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).ironBlock()
				x(+0).wireInputComputer()
				x(+1).ironBlock()
			}
			y(+0) {
				x(-1).anyStairs()
				x(+0).machineFurnace()
				x(+1).anyStairs()
			}
		}
		z(+1) {
			y(-1) {
				x(-1).goldBlock()
				x(+0).endRod()
				x(+1).goldBlock()
			}
			y(+0) {
				x(-1).ironBlock()
				x(+0).craftingTable()
				x(+1).ironBlock()
			}
		}
		z(+2) {
			y(-1) {
				x(-1).goldBlock()
				x(+0).endRod()
				x(+1).goldBlock()
			}
			y(+0) {
				x(-1).anyGlassPane()
				x(+0).pistonBase()
				x(+1).anyGlassPane()
			}
		}
		z(+3) {
			y(-1) {
				x(-1).anyGlass()
				x(+0).sponge()
				x(+1).anyGlass()
			}
			y(+0) {
				x(-1).anyGlass()
				x(+0).anvil()
				x(+1).anyGlass()
			}
		}
		z(+4) {
			y(-1) {
				x(-1).goldBlock()
				x(+0).endRod()
				x(+1).goldBlock()
			}
			y(+0) {
				x(-1).anyGlassPane()
				x(+0).pistonBase()
				x(+1).anyGlassPane()
			}
		}
		z(+5) {
			y(-1) {
				x(-1).goldBlock()
				x(+0).endRod()
				x(+1).goldBlock()
			}
			y(+0) {
				x(-1).ironBlock()
				x(+0).ironBlock()
				x(+1).ironBlock()
			}
		}
		z(+6) {
			y(-1) {
				x(-1).ironBlock()
				x(+0).sponge()
				x(+1).ironBlock()
			}
			y(+0) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
		}
	}

	override val name = "platepress"

	override val signText = createSignText(
		line1 = "&5Plate Press",
		line2 = null,
		line3 = null,
		line4 = null
	)

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): PlatePressMultiblockEntity {
		return PlatePressMultiblockEntity(data, manager, x, y, z, world, structureDirection)
	}

	class PlatePressMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureFace: BlockFace
	) : SimplePoweredEntity(data, PlatePressMultiblock, manager, x, y, z, world, structureFace), LegacyMultiblockEntity, PoweredMultiblockEntity, RecipeEntity {
		override val maxPower = 300_000
		override val displayHandler = standardPowerDisplay(this)
		override val progressManager: ProgressMultiblock.ProgressManager = ProgressMultiblock.ProgressManager(data)
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(20)

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}
	}
}
