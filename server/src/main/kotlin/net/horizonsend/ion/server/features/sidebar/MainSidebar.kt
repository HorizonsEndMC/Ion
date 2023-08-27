package net.horizonsend.ion.server.features.sidebar

import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.sidebar.component.ContactsHeaderSidebarComponent
import net.horizonsend.ion.server.features.sidebar.component.ContactsSidebarComponent
import net.horizonsend.ion.server.features.sidebar.component.LocationSidebarComponent
import net.horizonsend.ion.server.features.sidebar.component.WaypointsHeaderSidebarComponent
import net.horizonsend.ion.server.features.sidebar.component.WaypointsNameSidebarComponent
import net.horizonsend.ion.server.features.sidebar.component.WaypointsSidebarComponent
import net.horizonsend.ion.server.features.sidebar.tasks.ContactsSidebar
import net.horizonsend.ion.server.features.sidebar.tasks.PlayerLocationSidebar
import net.horizonsend.ion.server.features.sidebar.tasks.WaypointsSidebar
import net.horizonsend.ion.server.features.waypoint.WaypointManager
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar
import net.megavex.scoreboardlibrary.api.sidebar.component.ComponentSidebarLayout
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import org.bukkit.entity.Player
import java.util.Locale

class MainSidebar(private val player: Player, val backingSidebar: Sidebar) {
	companion object {
		const val MIN_LENGTH = 40
		const val WAYPOINT_MAX_LENGTH = 30
		private const val CONTACTS_RANGE = 6000
		const val CONTACTS_SQRANGE = CONTACTS_RANGE * CONTACTS_RANGE
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

		// Contacts
		val contactsEnabled = PlayerCache[player.uniqueId].contactsEnabled
		if (contactsEnabled) {
			val contactsHeaderComponent: SidebarComponent = ContactsHeaderSidebarComponent(player)
			val contacts = ContactsSidebar.getPlayerContacts(player)
			val contactsComponents: MutableList<SidebarComponent> = mutableListOf()
			for (contact in contacts) {
				contactsComponents.add(ContactsSidebarComponent { contact })
			}
			lines.addComponent(contactsHeaderComponent)
			for (component in contactsComponents) lines.addComponent(component)
		}

		// Waypoints
		val waypointsEnabled = PlayerCache[player.uniqueId].waypointsEnabled
		if (waypointsEnabled) {
			val waypointsHeaderComponent: SidebarComponent = WaypointsHeaderSidebarComponent(player)
			lines.addComponent(waypointsHeaderComponent)

			// next waypoint (first waypoint in route)
			val nextWaypoint = WaypointManager.getNextWaypoint(player)
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
			val lastWaypoint = WaypointManager.getLastWaypoint(player)
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