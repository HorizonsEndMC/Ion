package net.horizonsend.ion.server.features.multiblock.areashield

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.machine.AreaShields
import net.horizonsend.ion.server.features.multiblock.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getSphereBlocks
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import java.util.concurrent.TimeUnit

abstract class AreaShield(val radius: Int) : Multiblock(), PowerStoringMultiblock, InteractableMultiblock {
	override fun onTransformSign(player: Player, sign: Sign) {
		AreaShields.register(sign.location, radius)
		player.sendMessage(ChatColor.GREEN.toString() + "Area Shield created.")
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val blocks: List<Vec3i> = getSphereBlocks(radius)

		val world = sign.world
		val (x0, y0, z0) = Vec3i(sign.location)

		val start = System.nanoTime()
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
					Material.BARRIER.createBlockData()
				)
			}

			if (System.nanoTime() - start > TimeUnit.SECONDS.toNanos(10L)) {
				cancel()
			}
		}.runTaskTimer(IonServer, 20, 20)
	}

	override val name get() = "areashield"

	override val maxPower = 100_000

	override val signText = createSignText(
		"&6Area",
		"&bParticle Shield",
		null,
		"&8Radius: &a$radius"
	)
}
