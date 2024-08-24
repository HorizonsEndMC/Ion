package net.horizonsend.ion.server.features.multiblock.type.particleshield

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.AbstractCooldown
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.blockKey
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import java.util.UUID
import java.util.concurrent.TimeUnit

abstract class ShieldMultiblock : Multiblock(), InteractableMultiblock {
	open val isReinforced: Boolean = false

	override val name: String = "shield"

	// let people use [particleshield] if they want
	override fun matchesUndetectedSign(sign: Sign): Boolean {
		return super.matchesUndetectedSign(sign) || sign.line(0).plainText().equals("[particleshield]", ignoreCase = true)
	}

	override fun onTransformSign(player: Player, sign: Sign) {
		sign.line(2, sign.line(1))
	}

	override fun setupSign(player: Player, sign: Sign) {
		if (sign.line(1).plainText().isEmpty()) {
			player.userError("The second line must be the shield's name.")
			return
		}

		super.setupSign(player, sign)
	}

	abstract fun getShieldBlocks(sign: Sign): List<Vec3i>

	companion object {
		val cooldown = object : AbstractCooldown<Pair<UUID, Long>>(5L, TimeUnit.SECONDS) {
			override fun cooldownRejected(player: Pair<UUID, Long>) {
				val (uuid, _) = player
				Bukkit.getPlayer(uuid)?.userError("You're doing that too often!")
			}
		}
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val (signX, signY, signZ) = Vec3i(sign.location)
		val key = blockKey(signX, signY, signZ)

		cooldown.tryExec(player.uniqueId to key) {
			val blocks: List<Vec3i> = getShieldBlocks(sign)

			val world = sign.world
			val (x0, y0, z0) = Vec3i(sign.location)

			val start = System.nanoTime()

			val barrier = Material.BARRIER.createBlockData()

			Tasks.bukkitRunnable {
				for ((dx, dy, dz) in blocks) {
					val x = x0 + dx + 0.5
					val y = y0 + dy + 0.5
					val z = z0 + dz + 0.5
					world.spawnParticle(
						Particle.BLOCK_MARKER,
						x,
						y,
						z,
						1,
						0.0,
						0.0,
						0.0,
						0.0,
						barrier
					)
				}

				if (System.nanoTime() - start > TimeUnit.SECONDS.toNanos(10L)) {
					cancel()
				}
			}.runTaskTimer(IonServer, 20, 20)
		}
	}
}
