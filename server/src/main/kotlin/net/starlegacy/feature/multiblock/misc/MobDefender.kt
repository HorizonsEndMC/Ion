package net.starlegacy.feature.multiblock.misc

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.util.Tasks
import net.starlegacy.util.getBlockIfLoaded
import net.starlegacy.util.msg
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Sign
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException
import kotlin.math.abs

object MobDefender : Multiblock() {
	override val name = "mobdefender"

	override val signText = createSignText(
		line1 = "&cHostile",
		line2 = "&cCreature",
		line3 = "&7Control",
		line4 = "MobDefender Co"
	)

	override fun LegacyMultiblockShape.buildStructure() {
		z(-1) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).stoneBrick()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+1).anyGlassPane()
			}
		}

		z(+0) {
			y(-1) {
				x(-1).stoneBrick()
				x(+0).diamondBlock()
				x(+1).stoneBrick()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).anyGlass()
				x(+1).anyGlassPane()
			}
		}

		z(+1) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).stoneBrick()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+1).anyGlassPane()
			}
		}
	}

	override fun onTransformSign(player: Player, sign: Sign) {
		mobDefenders.add(sign.location)
		player msg "Created mob defender."
		save() // TODO: don't do this on the main thread >_>
	}

	// TODO: come up with something less retarded for this
	private val mobDefenders = ArrayList<Location>()
	private var config = YamlConfiguration()
	private val file = File(IonServer.dataFolder, "mobdefenders.yml")

	init {
		Tasks.sync {
			file.createNewFile()
			config = YamlConfiguration.loadConfiguration(file)
			for (worldName in config.getKeys(false)) {
				val world = Bukkit.getWorld(worldName) ?: continue
				val keys = config.getConfigurationSection(worldName)?.getKeys(false) ?: continue
				for (key in keys) {
					val coords = key.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
					val x = coords[0].toDoubleOrNull()?.toInt() ?: continue
					val y = coords[1].toDoubleOrNull()?.toInt() ?: continue
					val z = coords[2].toDoubleOrNull()?.toInt() ?: continue
					mobDefenders.add(Location(world, x.toDouble(), y.toDouble(), z.toDouble()))
				}
			}
		}
	}

	private fun save() {
		config = YamlConfiguration()
		for (location in mobDefenders)
			config.set(
				String.format(
					"%s.%d-%d-%d",
					location.world.name, location.blockX, location.blockY,
					location.blockZ
				),
				location.hashCode()
			)
		try {
			config.save(file)
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	fun cancelSpawn(location: Location): Boolean {
		if (location.world.name.lowercase().contains("eden")) return false

		return mobDefenders.asSequence()
			.filter { it.world == location.world }
			.filter { abs(location.x - it.x) < 50 }
			.filter { abs(location.y - it.y) < 50 }
			.filter { abs(location.z - it.z) < 50 }
			.mapNotNull { getBlockIfLoaded(it.world, it.blockX, it.blockY, it.blockZ) }
			.mapNotNull { it.getState(false) as? Sign }
			.any { signMatchesStructure(it, loadChunks = false) }
	}

	fun removeDefender(location: Location) {
		if (!mobDefenders.contains(location)) return
		mobDefenders.remove(location)
		save()
	}
}
