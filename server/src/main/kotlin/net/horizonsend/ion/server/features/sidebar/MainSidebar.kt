package net.horizonsend.ion.server.features.sidebar

import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.sidebar.component.ContactsHeaderSidebarComponent
import net.horizonsend.ion.server.features.sidebar.component.ContactsSidebarComponent
import net.horizonsend.ion.server.features.sidebar.component.LocationSidebarComponent
import net.horizonsend.ion.server.features.sidebar.component.StarshipsHeaderSidebarComponent
import net.horizonsend.ion.server.features.sidebar.component.StarshipsSidebarComponent1
import net.horizonsend.ion.server.features.sidebar.component.StarshipsSidebarComponent2
import net.horizonsend.ion.server.features.sidebar.component.StarshipsSidebarComponent3
import net.horizonsend.ion.server.features.sidebar.component.StarshipsSidebarComponent4
import net.horizonsend.ion.server.features.sidebar.component.WaypointsHeaderSidebarComponent
import net.horizonsend.ion.server.features.sidebar.component.WaypointsNameSidebarComponent
import net.horizonsend.ion.server.features.sidebar.component.WaypointsSidebarComponent
import net.horizonsend.ion.server.features.sidebar.tasks.ContactsJammingSidebar
import net.horizonsend.ion.server.features.sidebar.tasks.ContactsSidebar
import net.horizonsend.ion.server.features.sidebar.tasks.PlayerLocationSidebar
import net.horizonsend.ion.server.features.sidebar.tasks.WaypointsSidebar
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.waypoint.WaypointManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar
import net.megavex.scoreboardlibrary.api.sidebar.component.ComponentSidebarLayout
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import org.bukkit.entity.Player
import java.util.Locale

class MainSidebar(private val player: Player, val backingSidebar: Sidebar) {
	companion object {
		const val MIN_LENGTH = 0
		const val WAYPOINT_MAX_LENGTH = 30
		const val CONTACTS_RANGE = 6000
		const val MAX_NAME_LENGTH = 64
		//const val CONTACTS_SQRANGE = CONTACTS_RANGE * CONTACTS_RANGE
	}

	fun tick() {
		// Title
		val title = SidebarComponent.dynamicLine {
			val worldName = player.world.name
				.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
			val direction = PlayerLocationSidebar.getPlayerDirection(player)

			text()
				.append(text(worldName).color(DARK_GREEN))
				.append(text(direction).color(GREEN))
				.build()
		}

		val lines = SidebarComponent.builder()

		// Location
		val locationComponent: SidebarComponent = LocationSidebarComponent(player)
		lines.addComponent(locationComponent)

		// Starship
		val starshipsEnabled = PlayerCache[player.uniqueId].starshipsEnabled
		if (starshipsEnabled) {
			val starship = PilotedStarships[player]
			if (starship != null) {
				val starshipsHeaderSidebarComponent: SidebarComponent =
					StarshipsHeaderSidebarComponent(starship, player)
				val starshipsSidebarComponent1: SidebarComponent = StarshipsSidebarComponent1(starship, player)
				val starshipsSidebarComponent2: SidebarComponent = StarshipsSidebarComponent2(starship, player)
				val starshipsSidebarComponent3: SidebarComponent = StarshipsSidebarComponent3(starship, player)
				val starshipsSidebarComponent4: SidebarComponent = StarshipsSidebarComponent4(starship, player)
				lines.addComponent(starshipsHeaderSidebarComponent)
				lines.addComponent(starshipsSidebarComponent1)
				lines.addComponent(starshipsSidebarComponent2)
				lines.addComponent(starshipsSidebarComponent3)
				if (PlayerCache[player.uniqueId].advancedStarshipInfo) {
					lines.addComponent(starshipsSidebarComponent4)
				}
			}
		}

		// Contacts
		val contactsEnabled = PlayerCache[player.uniqueId].contactsEnabled
		if (contactsEnabled) {
			val contactsHeaderComponent: SidebarComponent = ContactsHeaderSidebarComponent(player)
			val contacts = ContactsSidebar.getPlayerContacts(player)
			val contactsComponents: MutableList<SidebarComponent> = mutableListOf()
			if (!ContactsJammingSidebar.jammedPlayers.containsKey(player.uniqueId)) {
				for (contact in contacts) {
					contactsComponents.add(ContactsSidebarComponent { contact })
				}
			} else {
				for (contact in contacts) {
					contactsComponents.add(ContactsSidebarComponent { ContactsSidebar.createJammedStarshipContact(contact) })
				}
			}
			if (contacts.isNotEmpty()) lines.addComponent(contactsHeaderComponent)
			for (component in contactsComponents) lines.addComponent(component)
		}

		// Waypoints
		val waypointsEnabled = PlayerCache[player.uniqueId].waypointsEnabled
		if (waypointsEnabled && WaypointManager.getNextWaypoint(player) != null) {
			val waypointsHeaderComponent: SidebarComponent = WaypointsHeaderSidebarComponent(player)
			lines.addComponent(waypointsHeaderComponent)

			// next waypoint (first waypoint in route)
			val nextWaypoint = WaypointManager.getNextWaypoint(player)?.replace('_', ' ')
			if (!nextWaypoint.isNullOrEmpty()) {
				val nextWaypointComponent: SidebarComponent = WaypointsNameSidebarComponent({ nextWaypoint }, false)
				lines.addComponent(nextWaypointComponent)
			}

			// route string
			val route = WaypointsSidebar.splitRouteString(player)
			val routeComponents: MutableList<SidebarComponent> = mutableListOf()
			for (routePart in route) {
				routeComponents.add(WaypointsSidebarComponent { routePart })
			}
			for (component in routeComponents) lines.addComponent(component)

			// last waypoint (final destination)
			val lastWaypoint = WaypointManager.getLastWaypoint(player)?.replace('_', ' ')
			if (!lastWaypoint.isNullOrEmpty()) {
				val lastWaypointComponent: SidebarComponent = WaypointsNameSidebarComponent({ lastWaypoint }, true)
				lines.addComponent(lastWaypointComponent)
			}
		}

		// Assemble title and components
		val componentSidebar = ComponentSidebarLayout(title, lines.build())
		componentSidebar.apply(backingSidebar)
	}
}