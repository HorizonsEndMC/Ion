package net.horizonsend.ion.server.features.sidebar

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.miscellaneous.repeatString
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar
import net.megavex.scoreboardlibrary.api.sidebar.component.ComponentSidebarLayout
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import net.starlegacy.feature.space.CachedPlanet
import net.starlegacy.feature.space.CachedStar
import net.starlegacy.feature.space.Space
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.hyperspace.MassShadows
import net.starlegacy.util.toVector
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

class MainSidebar(private val player: Player, private val sidebar: Sidebar) {
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
            val direction = getPlayerDirection(player)

            text()
                .append(text(worldName).color(DARK_GREEN))
                .append(text(direction).color(GREEN))
                .build()
        }

        // Location
        val locationComponent: SidebarComponent = LocationSidebarComponent(player)

        // Contacts
        val contacts = getPlayerContacts(player)
        val contactsComponents: MutableList<SidebarComponent> = mutableListOf()
        for (contact in contacts) {
            contactsComponents.add(ContactsSidebarComponent { contact })
        }

        // Build components
        val lines = SidebarComponent.builder()
        lines.addComponent(locationComponent)
        for (component in contactsComponents) lines.addComponent(component)

        val componentSidebar = ComponentSidebarLayout(title, lines.build())
        componentSidebar.apply(sidebar)
    }

    private fun distanceColor(distance: Int): NamedTextColor {
        return when {
            distance < 500 -> RED
            distance < 1500 -> GOLD
            distance < 2500 -> YELLOW
            distance < 3500 -> DARK_GREEN
            distance < 6000 -> GREEN
            else -> GREEN
        }
    }

    private fun getPlayerDirection(player: Player): String {
        val playerDirection: Vector = player.location.direction
        playerDirection.setY(0).normalize()

        val directionString = StringBuilder(" ")

        if (playerDirection.z != 0.0 && abs(playerDirection.z) > 0.4) {
            directionString.append( if (playerDirection.z > 0) "south" else "north" )
        }

        if (playerDirection.x != 0.0 && abs(playerDirection.x) > 0.4) {
            directionString.append( if (playerDirection.x > 0) "east" else "west" )
        }

        return directionString.toString()
    }

    private fun getDirectionToObject(direction: Vector): String {
        val directionString = StringBuilder("")

        if (direction.z != 0.0 && abs(direction.z) > 0.4) {
            directionString.append( if (direction.z > 0) "S" else "N" )
        }
        if (direction.x != 0.0 && abs(direction.x) > 0.4) {
            directionString.append( if (direction.x > 0) "E" else "W" )
        }

        return directionString.toString()
    }

    private fun getPlayerContacts(player: Player): List<ContactsData> {
        val contactsList: MutableList<ContactsData> = mutableListOf()
        val playerVector = player.location.toVector()

        val starships: List<ActiveStarship> = ActiveStarships.getInWorld(player.world).filter {
            it.centerOfMass.toVector().distanceSquared(playerVector) <= CONTACTS_SQRANGE &&
                    (it as ActivePlayerStarship).pilot !== player &&
                    (it as ActivePlayerStarship).pilot?.gameMode != GameMode.SPECTATOR
        }

        val planets: List<CachedPlanet> = Space.getPlanets().filter {
            it.spaceWorld == player.world && it.location.toVector().distanceSquared(playerVector) <= CONTACTS_SQRANGE
        }

        val stars: List<CachedStar> = Space.getStars().filter {
            it.spaceWorld == player.world && it.location.toVector().distanceSquared(playerVector) <= CONTACTS_SQRANGE
        }

        val beacons: List<ServerConfiguration.HyperspaceBeacon> = IonServer.configuration.beacons.filter {
            it.spaceLocation.bukkitWorld() == player.world &&
                    it.spaceLocation.toLocation().toVector().distanceSquared(playerVector) <= CONTACTS_SQRANGE
        }

        for (starship in starships) {
            val vector = starship.centerOfMass.toVector()
            val distance = vector.distance(playerVector).toInt()
            val color = distanceColor(distance)

            contactsList.add(ContactsData(
                name = text((starship as ActivePlayerStarship).pilot?.name ?: "Unpiloted ${starship.type.displayName}").color(color),
                prefix = when (starship.type.isWarship) {
                    true -> text("△")
                    else -> text("◇")
                },
                suffix = if (starship.isInterdicting && distance <= starship.type.interdictionRange) {
                    text("∅").color(RED)
                } else if (starship.isInterdicting) {
                    text("∅").color(GOLD)
                } else empty(),
                heading = text(getDirectionToObject(vector.clone().subtract(playerVector).normalize())).color(color),
                height = text("${starship.centerOfMass.y}y").color(color),
                distance = text("${distance}m").color(color),
                distanceInt = distance
            ))
        }

        for (planet in planets) {
            val vector = planet.location.toVector()
            val distance = vector.distance(playerVector).toInt()
            val color = distanceColor(distance)

            contactsList.add(ContactsData(
                name = text(planet.name).color(color),
                prefix = text("○").color(DARK_AQUA),
                suffix = if (distance <= MassShadows.PLANET_RADIUS) {
                    text("∅").color(RED)
                } else empty(),
                heading = text(getDirectionToObject(vector.clone().subtract(playerVector).normalize())).color(color),
                height = text("${planet.location.y}y").color(color),
                distance = text("${distance}m").color(color),
                distanceInt = distance
            ))
        }

        for (star in stars) {
            val vector = star.location.toVector()
            val distance = vector.distance(playerVector).toInt()
            val color = distanceColor(distance)

            contactsList.add(ContactsData(
                name = text(star.name).color(color),
                prefix = text("☀").color(YELLOW),
                suffix = if (distance <= MassShadows.STAR_RADIUS) {
                    text("∅").color(RED)
                } else empty(),
                heading = text(getDirectionToObject(vector.clone().subtract(playerVector).normalize())).color(color),
                height = text("${star.location.y}y").color(color),
                distance = text("${distance}m").color(color),
                distanceInt = distance
            ))
        }

        for (beacon in beacons) {
            val vector = beacon.spaceLocation.toBlockPos().toVector()
            val distance = vector.distance(playerVector).toInt()
            val color = distanceColor(distance)

            contactsList.add(ContactsData(
                name = text(beacon.name).color(color),
                prefix = text("⏩").color(BLUE),
                suffix = if (beacon.prompt?.contains("⚠") == true) text("⚠").color(RED) else empty(),
                heading = text(getDirectionToObject(vector.clone().subtract(playerVector).normalize())).color(color),
                height = text("${beacon.spaceLocation.y}y").color(color),
                distance = text("${distance}m").color(color),
                distanceInt = distance
            ))
        }

        // get the longest contact name length
        var maxNameLength = 0
        for (contact in contactsList) {
            if (contact.name.content().length + (contact.suffix.content().length * 2) > maxNameLength) {
                maxNameLength = contact.name.content().length
            }
        }

        // append spaces (not perfect)
        for (contact in contactsList) {
            contact.suffix = contact.suffix.append(
                text(repeatString(" ", max(0, maxNameLength - contact.name.content().length + 3)))
            )
        }

        contactsList.sortBy { it.distanceInt }
        return contactsList
    }
}

data class ContactsData(
    val name: TextComponent,
    val prefix: TextComponent,
    var suffix: TextComponent,
    val heading: TextComponent,
    val height: TextComponent,
    val distance: TextComponent,
    val distanceInt: Int
)