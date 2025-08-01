package net.horizonsend.ion.server.features.starship.active

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.starship.Interdiction
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.hyperspace.HyperspaceMovement
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.registerIcon
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.World
import org.dynmap.bukkit.DynmapPlugin
import org.dynmap.markers.CircleMarker
import org.dynmap.markers.Marker
import org.dynmap.markers.MarkerAPI
import org.dynmap.markers.MarkerIcon
import org.dynmap.markers.MarkerSet

object StarshipDisplay : IonServerComponent(true) {
	private lateinit var walk: MarkerIcon
	private lateinit var starshipMarkers: MarkerSet
	private val markerAPI: MarkerAPI get() = DynmapPlugin.plugin.markerAPI
	private val starshipsIcons: MutableMap<String, StarshipIcon> = mutableMapOf()

	override fun onEnable() {
		if (!Bukkit.getPluginManager().isPluginEnabled("dynmap")) return
		starshipMarkers = markerAPI.createMarkerSet("starship-icons", "Starships", null, false)
		walk = markerAPI.getMarkerIcon("walk")
		registerIcons()

		Tasks.asyncRepeat(0L, 20L, ::updateStarships)
	}

	private fun registerIcons() {
		val icons = StarshipType.entries.map { it.dynmapIcon }

		for (iconName in icons) {
			registerIcon(iconName)
		}
	}

	private fun updateStarships() {
		val markerSet = starshipMarkers

		clearInactive(markerSet)
		ActiveStarships.all().forEach(::createMarker)
		updateMap(markerSet)
	}

	/** Stores the StarshipIcon for use in populating the map */
	private fun createMarker(starship: ActiveStarship, marker: MarkerIcon? = null) {
		val charIdentifier = starship.charIdentifier
		val displayName = starship.identifier

		val markerIcon = marker
			?: markerAPI.getMarkerIcon(starship.type.dynmapIcon)
			?: markerAPI.getMarkerIcon("anchor")

		val isInHyperspace = Hyperspace.isMoving(starship)

		val description = createDynmapPopupHTML(starship, isInHyperspace)

		val circles = mutableListOf<CircleInfo>()

		if (starship.isInterdicting) {
			circles.add(CircleInfo(
				"gravity_well",
				Interdiction.starshipInterdictionRangeEquation(starship).toInt(),
				Color.fromRGB(128, 128, 128)
			))
		}

		val starshipIcon = if (isInHyperspace) {
			val movement = Hyperspace.getHyperspaceMovement(starship)!!

			if (movement.originWorld != movement.dest.world) {
				starshipsIcons.remove(charIdentifier)
				return
			}

			createHyperspaceIcon(
				starship,
				displayName,
				walk,
				movement,
				description
			)
		} else createOverworldMarker(starship, displayName, markerIcon, description, circles)

		starshipsIcons[charIdentifier] = starshipIcon
	}

	private fun createDynmapPopupHTML(starship: ActiveStarship, hyperspace: Boolean): String {
		val starshipDisplayName = starship.getDisplayNamePlain()

		val pilotNamePlain = starship.controller.getPilotName().plainText()

		val type = starship.type.displayNameComponent.plainText()
		val blockCount = starship.initialBlockCount
		val location = starship.centerOfMass

		val nation: Oid<Nation>? = (starship.controller as? PlayerController)?.player?.let { if (it.isOnline) PlayerCache[it] else null }?.nationOid
		val cachedNation = nation?.let { NationCache[it] }
		val colorCSS = cachedNation?.color?.let { "color:${TextColor.color(it).asHexString()};" }

		val hyperspaceMessage = if (hyperspace) "<h2 style=\"text-align:center;\">Hyperspace Echo</h2>\n" else ""

		return """
			<h1 style="$colorCSS;text-align:center;">$starshipDisplayName</h1>
			$hyperspaceMessage
			<h3><b>Type:</b> $type</h3>
			<h3><b>Pilot:</b> $pilotNamePlain</h3>
			<h3><b>Size:</b> $blockCount</h3>
			<h3><b>Location:</b> $location</h3>
		""".trimIndent()
	}

	/** Create a normal marker for a ship not in hyperspace **/
	private fun createOverworldMarker(
		starship: ActiveStarship,
		displayName: String,
		markerIcon: MarkerIcon,
		description: String,
		circles: List<CircleInfo>
	) = StarshipIcon(
		starship.charIdentifier,
		displayName,
		markerIcon,
		starship.world,
		starship.centerOfMass,
		description,
		circles
	)

	/** Creates a hyperspace icon **/
	private fun createHyperspaceIcon(
		starship: ActiveStarship,
		displayName: String,
		markerIcon: MarkerIcon,
		movement: HyperspaceMovement,
		description: String
	): StarshipIcon = StarshipIcon(
		starship.charIdentifier,
		displayName,
		markerIcon,
		movement.originWorld,
		Vec3i(
			movement.x.toInt(),
			starship.centerOfMass.y,
			movement.z.toInt()
		),
		description,
		listOf() // hyperspace icons have no circles
	)

	/**
	 * Clears all inactive starships from the icon set
	 * The identifier is unique to every piloted ship, if its been released and re-piloted, its old icon will be removed.
	 **/
	private fun clearInactive(markerSet: MarkerSet) {
		val iterator = starshipsIcons.iterator()

		while (iterator.hasNext()) {
			val (identifier, icon) = iterator.next()

			if (ActiveStarships[identifier] != null) continue

			val gravityWellCircleMarker: CircleMarker? = markerSet.findCircleMarker("${identifier}_gravity_well")
			gravityWellCircleMarker?.deleteMarker()

			markerSet.findMarker(identifier)?.deleteMarker()
			for (circle in icon.circles) {
				markerSet.findMarker("${icon.charIdentifier}_${circle.charIdentifier}")?.deleteMarker()
			}

			iterator.remove()
		}
	}

	/** Populates the map with markers */
	private fun updateMap(markerSet: MarkerSet) {
		for ((_, starshipIcon) in starshipsIcons) {
			starshipIcon.update(markerSet)
		}
	}

	/** Store the location, icon, and other details of a starship on the map */
	data class StarshipIcon(
        val charIdentifier: String,
        val displayName: String,
        val icon: MarkerIcon,
        val world: World,
        var position: Vec3i,
        val description: String,
		val circles: List<CircleInfo>
	) {
		fun update(markerSet: MarkerSet) {
			val marker: Marker? = markerSet.findMarker(charIdentifier)
			val oldX = marker?.x?.toInt()
			val oldY = marker?.y?.toInt()
			val oldZ = marker?.z?.toInt()

			val (x, y, z) = position

			marker?.description = description

			if (oldX == x && oldY == y && oldZ == z) return

			marker?.deleteMarker()
			createMarker(markerSet)

			val gravityWellCircleMarker: CircleMarker? = markerSet.findCircleMarker("${charIdentifier}_gravity_well")
			gravityWellCircleMarker?.deleteMarker()

			for (circle in circles) {
				val circleMarker: CircleMarker? = markerSet.findCircleMarker("${charIdentifier}_${circle.charIdentifier}")
				circleMarker?.deleteMarker()
				createCircleMarker(markerSet, circle)
			}
		}

		private fun createMarker(markerSet: MarkerSet): Marker? {
			val (x, y, z) = position

			val marker: Marker? = markerSet.createMarker(
				charIdentifier,
				"<p>$displayName</p>",
				true, // HTML label support
				world.name,
				x.toDouble(),
				y.toDouble(),
				z.toDouble(),
				icon,
				false
			)

			marker?.description = description

			return marker
		}

		private fun createCircleMarker(markerSet: MarkerSet, info: CircleInfo): CircleMarker? {
			val (x, y, z) = position

			val circleMarker: CircleMarker? = markerSet.createCircleMarker(
				"${charIdentifier}_${info.charIdentifier}",
				"<p>$displayName's ${info.charIdentifier}</p>",
				true,
				world.name,
				x.toDouble(),
				y.toDouble(),
				z.toDouble(),
				info.radius.toDouble(),
				info.radius.toDouble(),
				false
			)

			circleMarker?.setFillStyle(0.25, info.color.asRGB())
			circleMarker?.setLineStyle(0, 0.0, 0)

			return circleMarker
		}
	}

	data class CircleInfo(
		val charIdentifier: String,
		val radius: Int,
		val color: Color
	)
}
