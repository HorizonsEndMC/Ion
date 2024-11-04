package net.horizonsend.ion.server.features.multiblock.type.industry

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.PoweredMultiblock
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign

object GasFurnaceMultiblock : Multiblock(), PoweredMultiblock<GasFurnaceMultiblock.GasFurnaceMultiblockEntity> {
	override val maxPower: Int = 250_000
	override val name = "gasfurnace"

	override val signText = createSignText(
		line1 = "&2Gas",
		line2 = "&8Furnace",
		line3 = null,
		line4 = null
	)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).ironBlock()
				x(+0).wireInputComputer()
				x(+1).ironBlock()
			}
			y(+0) {
				x(-1).anyGlassPane()
				x(+0).machineFurnace()
				x(+1).anyGlassPane()
			}
		}
		z(+1) {
			y(-1) {
				x(-1).sponge()
				x(+0).anyGlass()
				x(+1).sponge()
			}
			y(+0) {
				x(-1).emeraldBlock()
				x(+0).anyGlass()
				x(+1).emeraldBlock()
			}
		}
		z(+2) {
			y(-1) {
				x(-1).sponge()
				x(+0).aluminumBlock()
				x(+1).sponge()
			}
			y(+0) {
				x(-1).anyStairs()
				x(+0).aluminumBlock()
				x(+1).anyStairs()
			}
		}
		z(+3) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).craftingTable()
				x(+1).anyStairs()
			}
			y(+0) {
				x(-1).anyStairs()
				x(+0).anyPipedInventory()
				x(+1).anyStairs()
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): GasFurnaceMultiblockEntity {
		return GasFurnaceMultiblockEntity(data, manager, x, y, z, world, structureDirection)
	}

	class GasFurnaceMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace
	) : MultiblockEntity(manager, GasFurnaceMultiblock, x, y, z, world, structureDirection), LegacyMultiblockEntity, PoweredMultiblockEntity {
		override val powerStorage: PowerStorage = loadStoredPower(data)
		override val multiblock: GasFurnaceMultiblock = GasFurnaceMultiblock

		val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			PowerEntityDisplay(this, +0.0, +0.0, +0.0, 0.5f)
		).register()

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}

		override fun onLoad() {
			displayHandler.update()
		}

		override fun onUnload() {
			displayHandler.remove()
		}

		override fun handleRemoval() {
			displayHandler.remove()
		}

		override fun displaceAdditional(movement: StarshipMovement) {
			displayHandler.displace(movement)
		}
	}
}
