package net.starlegacy.feature.multiblock.hyperdrive

import net.md_5.bungee.api.ChatColor
import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.util.CARDINAL_BLOCK_FACES
import net.starlegacy.util.Vec3i
import net.starlegacy.util.add
import net.starlegacy.util.getFacing
import net.starlegacy.util.rightFace
import org.bukkit.block.BlockFace
import org.bukkit.block.Hopper
import org.bukkit.block.Sign
import org.bukkit.entity.Player

abstract class HyperdriveMultiblock : Multiblock() {
    override val name = "hyperdrive"

    abstract val maxPower: Int
    abstract val hyperdriveClass: Int

    protected abstract fun buildHopperOffsets(): List<Vec3i>

    private val hopperOffsets: Map<BlockFace, List<Vec3i>> =
        CARDINAL_BLOCK_FACES.associate { inward ->
            val right = inward.rightFace
            val offsets: List<Vec3i> = buildHopperOffsets().map { (x, y, z) ->
                Vec3i(x = right.modX * x + inward.modX * z, y = y, z = right.modZ * x + inward.modZ * z)
            }
            return@associate inward to offsets
        }

    protected fun addHoppers(multiblockShape: MultiblockShape) = buildHopperOffsets().forEach { (x, y, z) ->
        multiblockShape.at(x, y, z).hopper()
    }

    fun getHoppers(sign: Sign): Set<Hopper> {
        val inwards = sign.getFacing().oppositeFace
        val offsets = hopperOffsets[inwards] ?: error("Unhandled sign direction $inwards")

        val origin = sign.location.add(inwards)

        return offsets.map { origin.clone().add(it).block.getState(false) as Hopper }.toSet()
    }

    override fun onTransformSign(player: Player, sign: Sign) {
        super.onTransformSign(player, sign)
        sign.setLine(3, ChatColor.RED.toString() + "Offline")
        sign.update()
    }
}
