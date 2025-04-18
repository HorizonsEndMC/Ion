package net.horizonsend.ion.server.features.multiblock.type.industry

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.crafting.input.FurnaceEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.input.GasFurnaceEnviornment
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.power.IndustryEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.inventory.FurnaceInventory

object GasFurnaceMultiblock : Multiblock(), EntityMultiblock<GasFurnaceMultiblock.GasFurnaceMultiblockEntity>, DisplayNameMultilblock {
	override val name = "gasfurnace"

	override val signText = createSignText(
		line1 = "&2Gas",
		line2 = "&8Furnace",
		line3 = null,
		line4 = null
	)

	override val displayName: Component get() = text("Gas Furnace")
	override val description: Component get() = text("Heats material in the presence of gas to produce refined materials.")

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).ironBlock()
				x(+0).powerInput()
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
				x(+0).extractor()
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
		structureFace: BlockFace
	) : IndustryEntity(data, GasFurnaceMultiblock, manager, x, y, z, world, structureFace, 250_000) {
		override fun buildRecipeEnviornment(): FurnaceEnviornment {
			return GasFurnaceEnviornment(
				this,
				getInventory(0, 0, 0) as FurnaceInventory,
				getInventory(0, 0, 3)!!,
				powerStorage,
				tickingManager,
				progressManager
			)
		}
	}
}
