package net.starlegacy.feature.multiblock.dockingtube

import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.util.*
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Sign
import org.bukkit.entity.Player

object DisconnectedDockingTubeMultiblock : DockingTubeMultiblock("&c[Disconnected]".colorize()) {
    override fun MultiblockShape.RequirementBuilder.tubeStateExtension() = anyButton()

    override fun toggle(sign: Sign, player: Player) {
        if (ActiveStarships.findByBlock(sign.block) != null) {
            player msg "&cCannot toggle tube in an active ship"
            return
        }

        val direction = sign.getFacing().oppositeFace
        val doorTop = sign.block.getRelative(direction)

        val buttons = getButtons(sign.location, direction)

        for (distance in 1..100) {
            // checks if docking tube is too far
            if (distance == 100) {
                player msg "&cOther end not found!"
                return
            }

            // needs to be 2 so it's 1 block ahead of the buttons, since the door is already 1 block behind to start with
            val block = doorTop.getRelativeIfLoaded(direction, distance + 2) ?: break

            // if it's not air, it is either the destination or an obstruction
            val blockType = block.type
            if (blockType != Material.AIR) {
                // if it's not a door it's an obstruction
                if (!blockType.isDoor) {
                    player msg "&cDocking tube is blocked or the other end is missing/misaligned. Distance: $distance"
                    return
                }

                // get the location the other side would have a sign at
                // doesn't actually have to be a sign block
                // if it's unloaded then tell them too move closer
                val otherSignLocation = block.getRelativeIfLoaded(direction)?.location ?: run {
                    player msg "&cDoor on other end is too far."
                    return
                }

                // if the other side's sign is not a valid closed docking tube it can't connect
                if (!signMatchesStructure(otherSignLocation, direction.oppositeFace)) {
                    player msg "&cDocking tube on the other end is invalid or misaligned."
                    return
                }

                for (i in 0..distance) {
                    buttons.forEach { button ->
                        button.getRelative(direction, i).setType(Material.GLASS, false)
                    }
                }

                player action "&2Docking tube connected."
                sign.setLine(3, ConnectedDockingTubeMultiblock.stateText)
                sign.update(false, false)

                sign.world.playSound(sign.location, Sound.BLOCK_PISTON_EXTEND, 1.0f, 1.5f)
                return
            }

            val buttonRelatives = buttons.map { it.getRelative(direction, distance) }

            for (buttonRelative in buttonRelatives) {
                if (buttonRelative.type == Material.AIR) {
                    continue
                }

                player msg "&cBlocked at ${Vec3i(buttonRelative.location)}"
                return
            }
        }

        player msg "&4Other end not found!"
    }
}
