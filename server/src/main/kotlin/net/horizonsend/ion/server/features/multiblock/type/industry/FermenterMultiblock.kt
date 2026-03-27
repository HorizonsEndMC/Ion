package net.horizonsend.ion.server.features.multiblock.type.industry

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.power.IndustryEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs

object FermenterMultiblock : Multiblock(), EntityMultiblock<FermenterMultiblock.FermenterMultiblockEntity>, DisplayNameMultilblock {
    override val name = "fermenter"

    override val signText = createSignText(
        line1 = "&6Fermenter",
        line2 = null,
        line3 = null,
        line4 = null
    )

    override fun MultiblockShape.buildStructure() {
        z(2) {
            y(-2) {
                x(-1).anyWall()
                x(1).anyWall()
            }
            y(-1) {
                x(-1).anyWall()
                x(1).anyWall()
            }
            y(0) {
                x(-1).anyWall()
                x(0).aluminumBlock()
                x(1).anyWall()
            }
            y(1) {
                x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
                x(0).aluminumBlock()
                x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
            }
            y(2) {
                x(0).aluminumBlock()
            }
            y(3) {
                x(0).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
            }
        }
        z(0) {
            y(-2) {
                x(-1).anyWall()
                x(1).anyWall()
            }
            y(-1) {
                x(-1).anyWall()
                x(1).anyWall()
            }
            y(0) {
                x(-1).anyWall()
                x(0).machineFurnace()
                x(1).anyWall()
            }
            y(1) {
                x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
                x(0).aluminumBlock()
                x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
            }
            y(2) {
                x(0).aluminumBlock()
            }
            y(3) {
                x(0).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
            }
        }
        z(1) {
            y(-1) {
                x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
                x(0).anyGlass()
                x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
            }
            y(0) {
                x(-1).aluminumBlock()
                x(0).extractor()
                x(1).aluminumBlock()
            }
            y(1) {
                x(-1).aluminumBlock()
                x(1).aluminumBlock()
            }
            y(2) {
                x(-1).aluminumBlock()
                x(1).aluminumBlock()
            }
            y(3) {
                x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
                x(0).aluminumBlock()
                x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
            }
        }
    }

    override fun createEntity(
        manager: MultiblockManager,
        data: PersistentMultiblockData,
        world: World,
        x: Int,
        y: Int,
        z: Int,
        structureDirection: BlockFace
    ): FermenterMultiblockEntity {
        return FermenterMultiblockEntity(data, manager, x, y, z, world, structureDirection)
    }

    override val displayName: Component = text("Fermenter")
    override val description: Component = text("Harnesses microbes to break down organic matter into simpler and more flavorful products.")

    class FermenterMultiblockEntity(
        data: PersistentMultiblockData,
        manager: MultiblockManager,
        x: Int,
        y: Int,
        z: Int,
        world: World,
        structureFace: BlockFace
    ) : IndustryEntity(data, FermenterMultiblock, manager, x, y, z, world, structureFace, 100_000)
}