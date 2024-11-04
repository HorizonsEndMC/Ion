package net.horizonsend.ion.server.features.multiblock.type.ammo

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
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign

object MissileLoaderMultiblock : Multiblock(), PoweredMultiblock<MissileLoaderMultiblock.MissileLoaderMultiblockEntity> {

    override fun MultiblockShape.buildStructure() {
        z(+0) {
            y(-1) {
                x(-2).anyStairs()
                x(-1).ironBlock()
                x(+0).wireInputComputer()
                x(+1).ironBlock()
                x(+2).anyStairs()
            }

            y(0) {
                x(-1).anyStairs()
                x(+0).machineFurnace()
                x(+1).anyStairs()
            }
        }
        z(+1) {
            y(-1) {
                x(-2).netheriteBlock()
                x(-1).type(Material.GRINDSTONE)
                x(+0).type(Material.OBSERVER)
                x(+1).type(Material.GRINDSTONE)
                x(+2).netheriteBlock()
            }
            y(0) {
                x(-2).anyGlassPane()
                x(-1).anyGlass()
                x(+0).anyGlass()
                x(+1).anyGlass()
                x(+2).anyGlassPane()
            }
        }
        z(+2) {
            y(-1) {
                x(-2).netheriteBlock()
                x(-1).type(Material.GRINDSTONE)
                x(+0).type(Material.OBSERVER)
                x(+1).type(Material.GRINDSTONE)
                x(+2).netheriteBlock()
            }
            y(0) {
                x(-2).anyGlass()
                x(-1).endRod()
                x(+0).anyGlass()
                x(+1).endRod()
                x(+2).anyGlass()
            }
        }
        z(+3) {
            y(-1) {
                x(-2).anyGlass()
                x(-1).type(Material.SMITHING_TABLE)
                x(+0).lodestone()
                x(+1).type(Material.SMITHING_TABLE)
                x(+2).anyGlass()
            }
            y(0) {
                x(-2).anyGlass()
                x(-1).type(Material.DROPPER)
                x(+0).anyGlass()
                x(+1).type(Material.DROPPER)
                x(+2).anyGlass()
            }
        }
        z(+4) {
            y(-1) {
                x(-2).netheriteBlock()
                x(-1).type(Material.GRINDSTONE)
                x(+0).type(Material.OBSERVER)
                x(+1).type(Material.GRINDSTONE)
                x(+2).netheriteBlock()
            }
            y(0) {
                x(-2).anyGlass()
                x(-1).endRod()
                x(+0).anyGlass()
                x(+1).endRod()
                x(+2).anyGlass()
            }
        }
        z(+5) {
            y(-1) {
                x(-2).netheriteBlock()
                x(-1).type(Material.GRINDSTONE)
                x(+0).type(Material.OBSERVER)
                x(+1).type(Material.GRINDSTONE)
                x(+2).netheriteBlock()
            }
            y(0) {
                x(-2).anyGlassPane()
                x(-1).anyGlass()
                x(+0).anyGlass()
                x(+1).anyGlass()
                x(+2).anyGlassPane()
            }
        }
        z(+6) {
            y(-1) {
                x(-2).anyStairs()
                x(-1).ironBlock()
                x(+0).ironBlock()
                x(+1).ironBlock()
                x(+2).anyStairs()
            }

            y(0) {
                x(-1).anyStairs()
                x(+0).anyGlass()
                x(+1).anyStairs()
            }
        }
    }

    override val name: String
        = "missileloader"
    override val signText = createSignText(
        line1 = "&6Missile",
        line2 = "&8Loader",
        line3 = null,
        line4 = null
    )

    override val maxPower = 250_000

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): MissileLoaderMultiblockEntity {
		return MissileLoaderMultiblockEntity(data, manager, x, y, z, world, structureDirection)
	}

	class MissileLoaderMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
	) : MultiblockEntity(manager, MissileLoaderMultiblock, x, y, z, world, structureDirection), LegacyMultiblockEntity, PoweredMultiblockEntity {
		override val multiblock: MissileLoaderMultiblock = MissileLoaderMultiblock
		override val powerStorage: PowerStorage = loadStoredPower(data)

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
