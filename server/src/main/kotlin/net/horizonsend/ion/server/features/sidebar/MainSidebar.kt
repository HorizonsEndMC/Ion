package net.horizonsend.ion.server.features.sidebar

import net.horizonsend.ion.server.features.sidebar.component.ContactsHeaderSidebarComponent
import net.horizonsend.ion.server.features.sidebar.component.ContactsSidebarComponent
import net.horizonsend.ion.server.features.sidebar.component.LocationSidebarComponent
import net.horizonsend.ion.server.features.sidebar.tasks.Contacts
import net.horizonsend.ion.server.features.sidebar.tasks.PlayerLocation
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
		private const val CONTACTS_RANGE = 6000
		const val CONTACTS_SQRANGE = CONTACTS_RANGE * CONTACTS_RANGE
	}

	fun tick() {
		// Title
		val title = SidebarComponent.dynamicLine {
			val worldName = player.world.name
				.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
			val direction = PlayerLocation.getPlayerDirection(player)

			text()
				.append(text(worldName).color(DARK_GREEN))
				.append(text(direction).color(GREEN))
				.build()
		}

		// Location
		val locationComponent: SidebarComponent = LocationSidebarComponent(player)

		// Contacts
		val contactsHeaderComponent: SidebarComponent = ContactsHeaderSidebarComponent(player)
		val contacts = Contacts.getPlayerContacts(player)
		val contactsComponents: MutableList<SidebarComponent> = mutableListOf()
		for (contact in contacts) {
			contactsComponents.add(ContactsSidebarComponent { contact })
		}

		// Build components
		val lines = SidebarComponent.builder()
		lines.addComponent(locationComponent)
		lines.addComponent(contactsHeaderComponent)
		for (component in contactsComponents) lines.addComponent(component)

		val componentSidebar = ComponentSidebarLayout(title, lines.build())
		componentSidebar.apply(backingSidebar)
	}
}