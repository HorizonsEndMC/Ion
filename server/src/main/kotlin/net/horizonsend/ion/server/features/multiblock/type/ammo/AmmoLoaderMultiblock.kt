package net.horizonsend.ion.server.features.multiblock.type.ammo

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.power.IndustryEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace

object AmmoLoaderMultiblock	: Multiblock(), EntityMultiblock<AmmoLoaderMultiblock.AmmoLoaderMultiblockEntity>, DisplayNameMultilblock {
	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).powerInput()
				x(+1).anyStairs()
			}

			y(0) {
				x(+0).machineFurnace()
			}
		}

		z(+1) {
			y(-1) {
				x(-2).anyWall()
				x(-1).ironBlock()
				x(+0).ironBlock()
				x(+1).ironBlock()
				x(+2).anyWall()
			}

			y(+0) {
				x(-1).anySlab()
				x(+0).grindstone()
				x(+1).anySlab()
			}
		}

		z(+2) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).anyCopperVariant()
				x(+0).sponge()
				x(+1).anyCopperVariant()
				x(+2).ironBlock()
			}

			y(+0) {
				x(-2).anyStairs()
				x(-1).anyGlass()
				x(+0).endRod()
				x(+1).anyGlass()
				x(+2).anyStairs()
			}
		}

		z(+3) {
			y(-1) {
				x(-2).anyGlassPane()
				x(-1).anyCopperVariant()
				x(+0).aluminumBlock()
				x(+1).anyCopperVariant()
				x(+2).anyGlassPane()
			}

			y(+0) {
				x(-2).anyGlassPane()
				x(-1).anyGlass()
				x(+0).type(Material.ANVIL)
				x(+1).anyGlass()
				x(+2).anyGlassPane()
			}
		}

		z(+4) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).anyCopperVariant()
				x(+0).sponge()
				x(+1).anyCopperVariant()
				x(+2).ironBlock()
			}

			y(+0) {
				x(-2).anyStairs()
				x(-1).anyGlass()
				x(+0).endRod()
				x(+1).anyGlass()
				x(+2).anyStairs()
			}
		}

		z(+5) {
			y(-1) {
				x(-2).anyWall()
				x(-1).ironBlock()
				x(+0).ironBlock()
				x(+1).ironBlock()
				x(+2).anyWall()
			}

			y(+0) {
				x(-1).anySlab()
				x(+0).grindstone()
				x(+1).anySlab()
			}
		}
		z(+6) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(0) {
				x(+0).ironBlock()
			}
		}
	}

	override val name = "ammoloader"

	override val signText = createSignText(
			line1 = "&6Ammo",
			line2 = "&8Loader",
			line3 = null,
			line4 = null
	)

	override val displayName: Component get() = text("Ammo Loader")
	override val description: Component get() = text("Activates heavy starship ammunition.")

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): AmmoLoaderMultiblockEntity {
		return AmmoLoaderMultiblockEntity(data, manager, x, y, z, world, structureDirection)
	}

	class AmmoLoaderMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureFace: BlockFace
	) : IndustryEntity(data, AmmoLoaderMultiblock, manager, x, y, z, world, structureFace, 300_000)
}
