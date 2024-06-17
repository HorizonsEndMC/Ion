package net.horizonsend.ion.server.features.multiblock.type.misc

import org.bukkit.Sound as SoundType
import com.destroystokyo.paper.event.player.PlayerJumpEvent
import io.papermc.paper.entity.TeleportFlag
import net.horizonsend.ion.common.utils.text.colors.Colors
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.isSlab
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
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.event.player.PlayerToggleSneakEvent

object TractorBeamMultiblock : Multiblock(), InteractableMultiblock, Listener {
	override val name = "tractorbeam"

	override val signText = createSignText(
		line1 = "&7Tractor",
		line2 = "&7Beam",
		line3 = "[-?::]",
		line4 = "[:->:]"
	)

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).anySlab()
		at(-1, +0, +1).anySlab()
		at(+1, +0, +1).anySlab()
		at(+0, +0, +2).anySlab()

		at(+0, +0, +1).anyGlass()
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

			val newLocation = block.location.add(0.5, 1.5, 0.5)

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

			val newLocation = block.location.add(0.5, 1.5, 0.5)

			finishTeleport(player, newLocation, event, SoundType.BLOCK_PISTON_EXTEND, "Ascending")
			break
		}
	}

	private fun finishTeleport(player: Player, location: Location, event: Cancellable?, soundType: SoundType, verb: String) {
		location.pitch = player.location.pitch
		location.yaw = player.location.yaw

		@Suppress("UnstableApiUsage")
		player.teleport(
			location,
			TeleportCause.PLUGIN,
			TeleportFlag.Relative.PITCH,
			TeleportFlag.Relative.YAW,
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
		for (face in CARDINAL_BLOCK_FACES) {
			val slabEdge = block.getRelative(face, 1)

			if (!slabEdge.type.isSlab) continue
			if (!TractorBeamMultiblock.blockMatchesStructure(slabEdge, face.oppositeFace)) continue

			return true
		}

		return false
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		if (event.item?.type != Material.CLOCK) return
		if (event.action != Action.RIGHT_CLICK_BLOCK) return
		if (event.clickedBlock?.type?.isWallSign != true) return

		tryDescend(player, event)
	}

	// Bring the player up if they right-click while facing up with a clock
	// and there's a tractor beam above them
	@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerInteractEventC(event: PlayerInteractEvent) {
		if (event.item?.type != Material.CLOCK) return
		if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
		if (event.player.location.pitch > -60) return

		tryAscend(event.player, event)
	}

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
}
