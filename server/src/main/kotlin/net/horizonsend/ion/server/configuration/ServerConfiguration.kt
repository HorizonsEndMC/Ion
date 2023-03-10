package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable
import net.minecraft.core.BlockPos
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World

@Serializable
data class ServerConfiguration(
	val serverName: String? = null,
	val beacons: List<HyperspaceBeacon> = listOf()
) {
	@Serializable
	data class HyperspaceBeacon(
		val name: String,
		val radius: Double,
		val spaceLocation: Pos,
		val destination: Pos,
		val destinationName: String? = null,
		val prompt: String? = null
	)

	@Serializable
	data class Pos(
		val world: String,
		val x: Int,
		val y: Int,
		val z: Int
	) {
		fun bukkitWorld(): World = Bukkit.getWorld(world) ?: throw
		java.lang.NullPointerException("Hyperspace Beacons | Could not find world $world")

		fun toBlockPos(): BlockPos = BlockPos(x, y, z)

		fun toLocation(): Location = Location(bukkitWorld(), x.toDouble(), y.toDouble(), z.toDouble())
	}
}
