package net.horizonsend.ion.server.features.multiblock.type.starship

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.nations.utils.toPlayersInRadius
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.playDirectionalStarshipSound
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import org.bukkit.ChatColor
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import kotlin.math.cbrt

object HornMultiblock : Multiblock(), InteractableMultiblock, DisplayNameMultilblock {

	override val name: String = "horn"

	override val displayName: Component get() = text("Ship Horn")
	override val description: Component get() = text("Sing the song of your people from a great distance")

	override val signText: Array<Component?> = arrayOf(
		text("Ship Horn", AQUA),
		null,
		null,
		null
	)

	override fun MultiblockShape.buildStructure() {
		at(-1, 0, 0).anyWall()
		at(0, 0, 0).anyGlass()
		at(1, 0, 0).anyWall()
	}


	override fun matchesUndetectedSign(sign: Sign): Boolean {
		return super.matchesUndetectedSign(sign) || sign.getSide(Side.FRONT).line(0).plainText().equals("[horn]", ignoreCase = true)
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val starship = ActiveStarships.findByPassenger(player) ?: return player.userError("You're not riding the starship")
		if (!starship.contains(sign.x, sign.y, sign.z)) return

		honk(Vec3i(sign.x, sign.y, sign.z), event.player, starship, 4.0)

	}

	val BASERADIUS = 500
	val BASESIZE = 2000.0

	fun honk(pos : Vec3i, player : Player, ship : Starship, multiplier: Double) {

		val sizemultiplier = cbrt(ship.initialBlockCount / BASESIZE)
		val radius = BASERADIUS * multiplier * sizemultiplier

		toPlayersInRadius(pos.toLocation(player.world), radius) { player ->
			playDirectionalStarshipSound(
				pos.toLocation(player.world),
				player,
				ship.balancing.shipSounds.horn,
				ship.balancing.shipSounds.horn,
				radius / 2,
				pitchMod = 1 / sizemultiplier.toFloat()
			)
		}

	}
}
