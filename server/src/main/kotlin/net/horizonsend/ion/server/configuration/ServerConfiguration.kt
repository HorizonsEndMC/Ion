package net.horizonsend.ion.server.configuration

import com.sk89q.worldedit.extent.clipboard.Clipboard
import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.IonServer
import net.minecraft.core.BlockPos
import net.starlegacy.util.readSchematic
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World

@Serializable
data class ServerConfiguration(
	val serverName: String? = null,
	val particleColourChoosingMoneyRequirement: Double? = 5.0,
	val beacons: List<HyperspaceBeacon>,
	val soldShips: List<Ship>
) {
	@Serializable
	data class HyperspaceBeacon(
		val name: String,
		val radius: Double,
		val spaceLocation: Pos,
		val destination: Pos,
		val destinationName: String? = null,
		val exits: ArrayList<Pos>? = null,
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

	/**
	 * @param cooldown ticks
	 **/
	@Serializable
	data class Ship(
		val price: Double,
		val displayName: String,
		val name: String,
		val guiMaterial: Material,
		val cooldown: Long,
		val teleportOffsetX: Double, // teleport offsets to teleport the player to after the ship is placed (away from schematic origin)
		val teleportOffsetY: Double,
		val teleportOffsetZ: Double,
		val lore: List<String>
	) {
		@kotlinx.serialization.Transient
		private val schematicFile = IonServer.dataFolder.resolve("sold_ships").resolve("$name.schem")

		fun schematic(): Clipboard = readSchematic(schematicFile)!!
	}
}
