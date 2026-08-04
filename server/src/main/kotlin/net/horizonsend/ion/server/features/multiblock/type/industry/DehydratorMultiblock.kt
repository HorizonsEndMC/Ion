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
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs

object DehydratorMultiblock : Multiblock(), EntityMultiblock<DehydratorMultiblock.DehydratorMultiblockEntity>, DisplayNameMultilblock {
    override val name = "dehydrator"

    override val signText = createSignText(
        line1 = "&4Dehydrator",
        line2 = null,
        line3 = null,
        line4 = null
    )

    override fun MultiblockShape.buildStructure() {
        z(2) {
            y(-1) {
                x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
                x(0).aluminumBlock()
                x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
            }
            y(0) {
                x(-1).type(Material.IRON_BARS)
                x(0).type(Material.IRON_BARS)
                x(1).type(Material.IRON_BARS)
            }
            y(1) {
                x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
                x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
                x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
            }
        }
        z(1) {
            y(-1) {
                x(-1).aluminumBlock()
                x(0).anyGlass()
                x(1).aluminumBlock()
            }
            y(0) {
                x(-1).type(Material.IRON_BARS)
                x(0).anyGlass()
                x(1).type(Material.IRON_BARS)
            }
            y(1) {
                x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
                x(0).ironBlock()
                x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
            }
        }
        z(0) {
            y(-1) {
                x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
                x(0).powerInput()
                x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
            }
            y(0) {
                x(-1).type(Material.IRON_BARS)
                x(0).machineFurnace()
                x(1).type(Material.IRON_BARS)
            }
            y(1) {
                x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
                x(0).extractor()
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
    ): DehydratorMultiblockEntity {
        return DehydratorMultiblockEntity(data, manager, x, y, z, world, structureDirection)
    }

    override val displayName: Component = text("Dehydrator")
    override val description: Component = text("Reduces the water content of objects placed inside.")

    class DehydratorMultiblockEntity(
        data: PersistentMultiblockData,
        manager: MultiblockManager,
        x: Int,
        y: Int,
        z: Int,
        world: World,
        structureFace: BlockFace
    ) : IndustryEntity(data, DehydratorMultiblock, manager, x, y, z, world, structureFace, 100_000)
}