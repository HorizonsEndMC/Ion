package net.starlegacy.feature.nations

import java.awt.Point
import java.awt.Polygon
import java.io.File
import net.starlegacy.PLUGIN
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.nations.Territory
import net.starlegacy.feature.nations.region.packTerritoryPolygon
import net.starlegacy.util.msg
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration

object TerritoryImporter {
	private data class UpdateData(val id: Oid<Territory>, val polygonData: ByteArray)
	private data class ImportData(val label: String, val worldName: String, val polygonData: ByteArray)

	fun importOldTerritories(sender: CommandSender) {
		val territoryFolder = File(PLUGIN.dataFolder, "territories")
		if (!territoryFolder.exists()) {
			sender msg "${territoryFolder.absolutePath} doesn't exist"
			return
		}

		val update = mutableListOf<UpdateData>()
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

				val existing = Territory.findByName(label)
				if (existing != null) {
					check(existing.world == worldName) { "Duplicate territory $label" }
					update.add(UpdateData(existing._id, polygonData))
				} else {
					import.add(ImportData(label, worldName, polygonData))
				}
			}
		}

		for ((existingId, polygonData) in update) {
			Territory.setPolygonData(existingId, polygonData)
		}

		for ((label, worldName, polygonData) in import) {
			Territory.create(label, worldName, polygonData)
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
