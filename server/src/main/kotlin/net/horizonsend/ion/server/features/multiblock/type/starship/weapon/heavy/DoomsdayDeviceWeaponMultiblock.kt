package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.DoomsdayDeviceWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.Material
import org.bukkit.block.BlockFace

object DoomsdayDeviceWeaponMultiblock : SignlessStarshipWeaponMultiblock<DoomsdayDeviceWeaponSubsystem>() {
	override val key: String = "doomsday_device"
    override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): DoomsdayDeviceWeaponSubsystem {
        return DoomsdayDeviceWeaponSubsystem(starship, pos, face)
    }

    override fun MultiblockShape.buildStructure() {
        z(0) {
            y(0) {
                x(2).ironBlock()
                x(1).assemblyCore()
                x(0).uraniumBlock()
                x(-1).assemblyCore()
                x(-2).ironBlock()
            }
            y(-1) {
                x(0).assemblyCore()
            }
            y(1) {
                x(0).assemblyCore()
            }
        }
        z(1) {
            y(0) {
                x(2).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
                x(1).anyGlass()
                x(0).uraniumBlock()
                x(-1).anyGlass()
                x(-2).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
            }
            y(-1) {
                x(0).anyGlass()
            }
            y(1) {
                x(0).anyGlass()
            }
        }
        z(2) {
            y(0) {
                x(2).ironBlock()
                x(1).assemblyCore()
                x(0).uraniumBlock()
                x(-1).assemblyCore()
                x(-2).ironBlock()
            }
            y(-1) {
                x(0).assemblyCore()
            }
            y(1) {
                x(0).assemblyCore()
            }
        }
        z(3) {
            y(0) {
                x(2).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
                x(1).anyGlass()
                x(0).uraniumBlock()
                x(-1).anyGlass()
                x(-2).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
            }
            y(-1) {
                x(0).anyGlass()
            }
            y(1) {
                x(0).anyGlass()
            }
        }
        z(4) {
            y(0) {
                x(2).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
                x(1).anyGlass()
                x(0).uraniumBlock()
                x(-1).anyGlass()
                x(-2).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
            }
            y(-1) {
                x(0).anyGlass()
            }
            y(1) {
                x(0).anyGlass()
            }
        }
        z(5) {
            y(0) {
                x(2).anyCopperVariant()
                x(1).anyCopperVariant()
                x(0).uraniumBlock()
                x(-1).anyCopperVariant()
                x(-2).anyCopperVariant()
            }
            y(-1) {
                x(0).anyCopperVariant()
            }
            y(1) {
                x(0).anyCopperVariant()
            }
        }
        z(6) {
            y(0) {
                x(2).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
                x(1).anyGlass()
                x(0).uraniumBlock()
                x(-1).anyGlass()
                x(-2).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
            }
            y(-1) {
                x(0).anyGlass()
            }
            y(1) {
                x(0).anyGlass()
            }
        }
        z(7) {
            y(0) {
                x(2).anyCopperVariant()
                x(1).anyCopperVariant()
                x(0).uraniumBlock()
                x(-1).anyCopperVariant()
                x(-2).anyCopperVariant()
            }
            y(-1) {
                x(0).anyCopperVariant()
            }
            y(1) {
                x(0).anyCopperVariant()
            }
        }
        z(8) {
            y(0) {
                x(1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
                x(0).uraniumBlock()
                x(-1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
            }
            y(-1) {
                x(0).anyGlass()
            }
            y(1) {
                x(0).anyGlass()
            }
        }
        z(9) {
            y(0) {
                x(1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
                x(0).uraniumBlock()
                x(-1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
            }
            y(-1) {
                x(0).anyGlass()
            }
            y(1) {
                x(0).anyGlass()
            }
        }
        z(10) {
            y(0) {
                x(1).ironBlock()
                x(0).anyGlassPane(PrepackagedPreset.pane(RelativeFace.BACKWARD, RelativeFace.LEFT, RelativeFace.RIGHT))
                x(-1).ironBlock()
            }
            y(-1) {
                x(0).ironBlock()
            }
            y(1) {
                x(0).ironBlock()
            }
        }
        z(11) {
            y(0) {
                x(1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
                x(-1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
            }
        }
        z(12) {
            y(0) {
                x(1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
                x(-1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
            }
        }
        z(13) {
            y(0) {
                x(1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
                x(-1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
            }
        }
        z(14) {
            y(0) {
                x(1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
                x(-1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
            }
        }
    }}
