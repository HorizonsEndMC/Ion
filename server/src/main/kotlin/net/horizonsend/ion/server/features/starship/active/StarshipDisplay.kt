package net.horizonsend.ion.server.features.starship.active

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.hyperspace.HyperspaceMovement
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.plainText
import org.bukkit.Bukkit
import org.bukkit.World
import org.dynmap.bukkit.DynmapPlugin
import org.dynmap.markers.Marker
import org.dynmap.markers.MarkerAPI
import org.dynmap.markers.MarkerIcon
import org.dynmap.markers.MarkerSet

object StarshipDisplay : IonServerComponent(true) {
	private lateinit var starshipMarkers: MarkerSet
	private lateinit var walk: MarkerIcon
	private val markerAPI: MarkerAPI get() = DynmapPlugin.plugin.markerAPI
	private val starshipsIcons: MutableMap<String, StarshipIcon> = mutableMapOf()

	override fun onEnable() {
		if (!Bukkit.getPluginManager().isPluginEnabled("dynmap")) return

		starshipMarkers = markerAPI.createMarkerSet("starship-icons", "Starships", null, false)
		walk = markerAPI.getMarkerIcon("walk")

		Tasks.asyncRepeat(0L, 20L, ::updateStarships)
	}

	fun updateStarships() {
		val markerSet = starshipMarkers
//		markerSet.markers.clear()

		clearInactive(markerSet)
		ActiveStarships.all().forEach(::createMarker)
		updateMap(markerSet)
	}

	/** Stores the StarshipIcon for use in populating the map */
	fun createMarker(starship: ActiveStarship, marker: MarkerIcon? = null) {
		val charIdentifier = starship.charIdentifier
		val displayName = starship.controller.getDisplayName().plainText()

		val markerIcon = marker ?: markerAPI.getMarkerIcon(starship.type.dynmapMarker)

		val isInHyperspace = Hyperspace.isMoving(starship)

		val description = "<h3>Marker Description"

		val starshipIcon = if (isInHyperspace) {
			if (starship !is ActiveControlledStarship) return
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
				"Hyperspace\n$description"
			)
		} else createOverworldMarker(starship, displayName, markerIcon, description)

		starshipsIcons[charIdentifier] = starshipIcon
	}

	/** Create a normal marker for a ship not in hyperspace **/
	private fun createOverworldMarker(
		starship: ActiveStarship,
		displayName: String,
		markerIcon: MarkerIcon,
		description: String
	) = StarshipIcon(
		starship.charIdentifier,
		displayName,
		markerIcon,
		starship.world,
		starship.centerOfMass,
		description
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
		description
	)

	/**
	 * Clears all inactive starships from the icon set
	 * The identifier is unique to every piloted ship, if its been released and re-piloted, its old icon will be removed.
	 **/
	fun clearInactive(markerSet: MarkerSet) {
		val iterator = starshipsIcons.iterator()

		while (iterator.hasNext()) {
			val (identifier, _) = iterator.next()

			if (ActiveStarships[identifier] != null) continue

			markerSet.findMarker(identifier)?.deleteMarker()
			iterator.remove()
		}
	}

	/** Populates the map with markers */
	fun updateMap(markerSet: MarkerSet) {
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
	) {
		fun update(markerSet: MarkerSet) {
			val marker: Marker? = markerSet.findMarker(charIdentifier)
			marker?.deleteMarker()

			createMarker(markerSet)
		}

		fun createMarker(markerSet: MarkerSet): Marker? {
			val (x, y, z) = position

			val marker: Marker? = markerSet.createMarker(
				charIdentifier,
				displayName,
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
	}
}
