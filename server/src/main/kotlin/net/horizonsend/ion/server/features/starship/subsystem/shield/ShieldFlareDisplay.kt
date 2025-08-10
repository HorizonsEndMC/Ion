package net.horizonsend.ion.server.features.starship.subsystem.shield

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.client.display.modular.ItemDisplayContainer
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.PI
import kotlin.math.sin

class ShieldFlareDisplay(
	private val starship: Starship,
	private val local: Vec3i,          // ship-local integer block position
	private val colorItem: Material,
	private val lifetime: Long
) : BukkitRunnable() {

	private var t = 0L
	private var container = newContainer()


	private fun currentCenter(): Vector {
		val g = starship.getGlobalCoordinate(local)            // local -> global block coords
		val loc = g.toLocation(starship.world).toCenterLocation()
		return Vector(loc.x, loc.y, loc.z)
	}

	private fun newContainer(): ItemDisplayContainer {
		val c = ItemDisplayContainer(
			world = starship.world,
			initScale = 1.1f, // slightly large for readability
			initPosition = currentCenter(),
			initHeading = Vector(0.0, 1.0, 0.0),
			item = ItemStack(colorItem)
		)
		return c
	}

	override fun run() {
		t++

		// World hop (e.g., teleports): rebuild in the new world
		if (container.world !== starship.world) {
			container.remove()
			container = newContainer()
		}

		// Follow the ship: re-derive position from the ship transform every tick
		container.position = currentCenter()

		// Pulse scale a bit so it reads from distance
		val phase = (t.toDouble() / lifetime).coerceIn(0.0, 1.0)
		val pulse = 0.5 * (1.0 + sin(phase * PI)) // 0..1..0
		val s = 1.1 + 0.5 * pulse
		container.scale = Vector(s, s, s)

		container.update()

		if (t >= lifetime) cancel()
	}

	override fun cancel() {
		super.cancel()
		container.remove()
	}

	fun schedule() = runTaskTimer(IonServer, 1L, 1L)
}

