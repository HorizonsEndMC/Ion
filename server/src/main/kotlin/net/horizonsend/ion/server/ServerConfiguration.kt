package net.horizonsend.ion.server

import net.minecraft.core.BlockPos
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class ServerConfiguration(
	val serverName: String? = null,
	val ParticleColourChoosingMoneyRequirement: Double? = 5.0,
	val beacons: List<HyperspaceBeacon> = listOf(
		HyperspaceBeacon(
			"test", 100.0, Pos("space2", 100000, 128, 100000),
			Pos("space2", 0, 128, 0), "zero zero"
		)
	)
) {
	@ConfigSerializable
	data class HyperspaceBeacon(
		val name: String,
		val radius: Double,
		val spaceLocation: Pos,
		val destination: Pos,
		val destinationName: String? = null
	)

	@ConfigSerializable
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
