package net.horizonsend.ion.server.features.starship.active

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.hyperspace.HyperspaceMovement
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.registerIcon
import net.kyori.adventure.text.format.TextColor
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
		registerIcons()

		Tasks.asyncRepeat(0L, 20L, ::updateStarships)
	}

	private fun registerIcons() {
		val icons = StarshipType.values().map { it.dynmapIcon }

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
				description
			)
		} else createOverworldMarker(starship, displayName, markerIcon, description)

		starshipsIcons[charIdentifier] = starshipIcon
	}

	private fun createDynmapPopupHTML(starship: ActiveStarship, hyperspace: Boolean): String {
		val starshipDisplayName = starship.getDisplayNamePlain()

		val pilotNamePlain = starship.controller.pilotName.plainText()

		val type = starship.type.component.plainText()
		val blockCount = starship.initialBlockCount
		val location = starship.centerOfMass

		val nation: Oid<Nation>? = (starship.controller as? PlayerController)?.player?.let { PlayerCache[it] }?.nationOid
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
	private fun clearInactive(markerSet: MarkerSet) {
		val iterator = starshipsIcons.iterator()

		while (iterator.hasNext()) {
			val (identifier, _) = iterator.next()

			if (ActiveStarships[identifier] != null) continue

			markerSet.findMarker(identifier)?.deleteMarker()
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
	}
}
