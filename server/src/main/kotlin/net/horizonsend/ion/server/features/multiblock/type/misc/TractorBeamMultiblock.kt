package net.horizonsend.ion.server.features.multiblock.type.misc

import org.bukkit.Sound as SoundType
import com.destroystokyo.paper.event.player.PlayerJumpEvent
import io.papermc.paper.entity.TeleportFlag
import net.horizonsend.ion.common.utils.text.colors.Colors
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.PerPlayerCooldown
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.isGlass
import net.horizonsend.ion.server.miscellaneous.utils.isSlab
import net.horizonsend.ion.server.miscellaneous.utils.isStairs
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.format.TextColor.color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.event.player.PlayerToggleSneakEvent
import java.util.concurrent.TimeUnit

abstract class AbstractTractorBeam : Multiblock(), InteractableMultiblock {
	override val name = "tractorbeam"

	override val signText = createSignText(
		line1 = "&7Tractor",
		line2 = "&7Beam",
		line3 = "[-?::]",
		line4 = "[:->:]"
	)

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		if (event.item?.type != Material.CLOCK) return
		if (event.action != Action.RIGHT_CLICK_BLOCK) return
		if (event.clickedBlock?.type?.isWallSign != true) return

		tryDescend(player, event)
	}

	abstract fun isInteriorBlock(block: Block): Boolean

	companion object : SLEventListener() {
		// Bring the player up if they right-click while facing up with a clock
		// and there's a tractor beam above them
		@EventHandler(priority = EventPriority.MONITOR)
		fun onPlayerInteractEventC(event: PlayerInteractEvent) {
			if (event.item?.type != Material.CLOCK) return
			if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
			if (event.player.location.pitch > -60) return

			cooldown.tryExec(event.player) { tryAscend(event.player, event) }
		}

		// try to address the double execution from right clicking air
		private val cooldown = PerPlayerCooldown(1L, TimeUnit.MILLISECONDS)

		@EventHandler(priority = EventPriority.MONITOR)
		fun onPlayerJumpEvent(event: PlayerJumpEvent) {
			if (event.player.inventory.itemInMainHand.type != Material.CLOCK) return

			tryAscend(event.player, null)
		}

		@EventHandler(priority = EventPriority.MONITOR)
		fun onPlayerToggleSneakEvent(event: PlayerToggleSneakEvent) {
			if (!event.isSneaking) return event.player.debug("sneak cancelled 1")
			if (ActiveStarships.findByPilot(event.player) != null) return event.player.debug("sneak cancelled 1")
			if (event.player.inventory.itemInMainHand.type != Material.CLOCK) return event.player.debug("sneak cancelled 1")

			tryDescend(event.player, event)
		}

		private fun tryDescend(
			player: Player,
			event: Cancellable?,
		) {
			val (x, originY, z) = Vec3i(player.location)
			val world = player.world

			val standingOn = getBlockIfLoaded(player.world, x, originY - 1, z) ?: return

			if (!checkMultiblock(standingOn)) return

			for (y in originY - 2 downTo player.world.minHeight) {
				val block = getBlockIfLoaded(world, x, y, z) ?: return
				val type = block.getTypeSafe() ?: return

				if (type.isAir) continue
				if (!type.isCollidable) continue

				val newLocation = Location(player.world, player.location.x, block.y + 1.0, player.location.z)

				finishTeleport(player, newLocation, event, SoundType.BLOCK_PISTON_CONTRACT, "Descending")
				break
			}
		}

		private fun tryAscend(player: Player, event: Cancellable?) {
			val (x, originY, z) = Vec3i(player.location)
			val world = player.world

			for (y in originY + 1..player.world.maxHeight) {
				val block = getBlockIfLoaded(world, x, y, z) ?: return player.debug("Block not loaded, cancelled")

				if (block.getTypeSafe()?.isAir == true) {
					continue
				}

				if (!checkMultiblock(block)) {
					if (block.getTypeSafe()?.isAir == false) break // obstructed

					continue
				}

				val newLocation = Location(player.world, player.location.x, block.y + 1.0, player.location.z)

				finishTeleport(player, newLocation, event, SoundType.BLOCK_PISTON_EXTEND, "Ascending")
				break
			}
		}

		private fun finishTeleport(player: Player, location: Location, event: Cancellable?, soundType: SoundType, verb: String) {
			location.pitch = player.location.pitch
			location.yaw = player.location.yaw

			player.teleport(
				location,
				TeleportCause.PLUGIN,
				TeleportFlag.Relative.VELOCITY_ROTATION
			)

			event?.isCancelled = true

			val sound = Sound.sound(
				soundType,
				Sound.Source.BLOCK,
				0.5f,
				1.0f
			)

			player.playSound(sound)
			player.sendActionBar(verb.toComponent(color(Colors.HINT)))
		}

		/** From the glass block, check if it is part of a valid tractor beam **/
		private fun checkMultiblock(block: Block): Boolean {
			if (!block.type.isGlass) return false

			return TractorBeamMultiblock.isInteriorBlock(block) || LargeTractorBeamMultiblock.isInteriorBlock(block)
		}
	}
}

object TractorBeamMultiblock : AbstractTractorBeam() {
	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).anySlabOrStairs()
		at(-1, +0, +1).anySlabOrStairs()
		at(+1, +0, +1).anySlabOrStairs()
		at(+0, +0, +2).anySlabOrStairs()

		at(+0, +0, +1).anyGlass()
	}

	override fun isInteriorBlock(block: Block): Boolean {
		for (face in CARDINAL_BLOCK_FACES) {
			val slabEdge = block.getRelative(face, 1)

			if (!(slabEdge.type.isSlab || slabEdge.type.isStairs)) continue
			if (!blockMatchesStructure(slabEdge, face.oppositeFace)) continue

			return true
		}

		return false
	}
}

object LargeTractorBeamMultiblock : AbstractTractorBeam() {
	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(0) {
				x(-1).anySlabOrStairs()
				x(+0).anySlabOrStairs()
				x(+1).anySlabOrStairs()
			}
		}
		z(1) {
			y(0) {
				x(-2).anySlabOrStairs()
				x(-1).anyGlass()
				x(+0).anyGlass()
				x(+1).anyGlass()
				x(+2).anySlabOrStairs()
			}
		}
		z(2) {
			y(0) {
				x(-2).anySlabOrStairs()
				x(-1).anyGlass()
				x(+0).anyGlass()
				x(+1).anyGlass()
				x(+2).anySlabOrStairs()
			}
		}
		z(3) {
			y(0) {
				x(-2).anySlabOrStairs()
				x(-1).anyGlass()
				x(+0).anyGlass()
				x(+1).anyGlass()
				x(+2).anySlabOrStairs()
			}
		}
		z(4) {
			y(0) {
				x(-1).anySlabOrStairs()
				x(+0).anySlabOrStairs()
				x(+1).anySlabOrStairs()
			}
		}
	}

	override fun isInteriorBlock(block: Block): Boolean {
		for (x in -2..+2) for (z in -2..+2) {
			val edgeBlock = block.getRelativeIfLoaded(x, 0, z) ?: continue
			debugAudience.highlightBlock(Vec3i(edgeBlock.location), 10L)
			if (!(edgeBlock.type.isSlab || edgeBlock.type.isStairs)) continue

			for (face in CARDINAL_BLOCK_FACES) {
				if (!blockMatchesStructure(edgeBlock, face)) continue
				return true
			}
		}

		return false
	}
}
