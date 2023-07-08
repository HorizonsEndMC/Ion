package net.starlegacy.feature.nations

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.nations.territories.AbstractTerritoryCompanion
import net.horizonsend.ion.server.database.schema.nations.territories.TerritoryInterface
import net.starlegacy.feature.nations.region.packTerritoryPolygon
import net.starlegacy.util.msg
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import java.awt.Point
import java.awt.Polygon
import java.io.File

object TerritoryImporter {
	private data class UpdateData<T: TerritoryInterface>(val id: Oid<T>, val polygonData: ByteArray)
	private data class ImportData(val label: String, val worldName: String, val polygonData: ByteArray)

	fun <T: TerritoryInterface>importOldTerritories(sender: CommandSender, companion: AbstractTerritoryCompanion<T>) {
		val territoryFolder = File(IonServer.dataFolder, "territories")
		if (!territoryFolder.exists()) {
			sender msg "${territoryFolder.absolutePath} doesn't exist"
			return
		}

		val update = mutableListOf<UpdateData<T>>()
		val import = mutableListOf<ImportData>()

		for (file in territoryFolder.listFiles()) {
			if (file.extension != "yml") continue
			val config = YamlConfiguration.loadConfiguration(file)
			val worldName = file.nameWithoutExtension
			sender msg "Importing $worldName"

			for (key in config.getKeys(false)) {
				val section = config.getConfigurationSection(key)!!
//                val territoryId = key.trim().replace("\n", "")
				val label = section.getString("label")!!
				val points = section.getStringList("points").asSequence()
					.map { it.split(" ").dropWhile { it.isEmpty() } }
					.map { Point(Math.round(it[0].toDouble()).toInt(), Math.round(it[1].toDouble()).toInt()) }

				val polygonData = buildPolygonData(points)

				val existing = companion.findByName(label)
				if (existing != null) {
					check(existing.world == worldName) { "Duplicate territory $label" }
					@Suppress("UNCHECKED_CAST")
					update.add(UpdateData<T>(existing._id as Oid<T>, polygonData))
				} else {
					import.add(ImportData(label, worldName, polygonData))
				}
			}
		}

		for ((existingId, polygonData) in update) {
			companion.setPolygonData(existingId, polygonData)
		}

		for ((label, worldName, polygonData) in import) {
			companion.create(label, worldName, polygonData)
		}

		sender msg "Imported ${import.size} and updated ${update.size} territories"
		territoryFolder.renameTo(File(territoryFolder.parentFile, "territories_imported"))
	}

	private fun buildPolygonData(points: Sequence<Point>): ByteArray {
		val polygon = Polygon()
		for (point in points) {
			polygon.addPoint(point.x, point.y)
		}
		return packTerritoryPolygon(polygon)
	}
}
