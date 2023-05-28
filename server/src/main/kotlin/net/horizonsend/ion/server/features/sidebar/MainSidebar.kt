package net.horizonsend.ion.server.features.sidebar

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.miscellaneous.repeatString
import net.kyori.adventure.key.Key.key
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
import net.starlegacy.feature.starship.StarshipType.CORVETTE
import net.starlegacy.feature.starship.StarshipType.DESTROYER
import net.starlegacy.feature.starship.StarshipType.FRIGATE
import net.starlegacy.feature.starship.StarshipType.GUNSHIP
import net.starlegacy.feature.starship.StarshipType.HEAVY_FREIGHTER
import net.starlegacy.feature.starship.StarshipType.LIGHT_FREIGHTER
import net.starlegacy.feature.starship.StarshipType.MEDIUM_FREIGHTER
import net.starlegacy.feature.starship.StarshipType.SHUTTLE
import net.starlegacy.feature.starship.StarshipType.STARFIGHTER
import net.starlegacy.feature.starship.StarshipType.TRANSPORT
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
            val direction = getDirectionToObject(vector.clone().subtract(playerVector).normalize())
            val height = vector.y.toInt()
            val color = distanceColor(distance)

            contactsList.add(ContactsData(
                name = text((starship as ActivePlayerStarship).pilot?.name ?: "Unpiloted Ship").color(color),
                prefix = when (starship.type) {
                    STARFIGHTER -> text("\uE000").font(key("horizonsend:sidebar"))
                    GUNSHIP -> text("\uE001").font(key("horizonsend:sidebar"))
                    CORVETTE -> text("\uE002").font(key("horizonsend:sidebar"))
                    FRIGATE -> text("\uE003").font(key("horizonsend:sidebar"))
                    DESTROYER -> text("\uE004").font(key("horizonsend:sidebar"))
                    SHUTTLE -> text("\uE010").font(key("horizonsend:sidebar"))
                    TRANSPORT -> text("\uE011").font(key("horizonsend:sidebar"))
                    LIGHT_FREIGHTER -> text("\uE012").font(key("horizonsend:sidebar"))
                    MEDIUM_FREIGHTER -> text("\uE013").font(key("horizonsend:sidebar"))
                    HEAVY_FREIGHTER -> text("\uE014").font(key("horizonsend:sidebar"))
                    else -> text("\uE032").font(key("horizonsend:sidebar"))
                } as TextComponent,
                suffix = if (starship.isInterdicting && distance <= starship.type.interdictionRange) {
                    text("\uE033").font(key("horizonsend:sidebar")).color(RED) as TextComponent
                } else if (starship.isInterdicting) {
                    text("\uE033").font(key("horizonsend:sidebar")).color(GOLD) as TextComponent
                } else empty(),
                heading = text(direction)
                    .append(text(repeatString(" ", 2 - direction.length))
                        .font(key("horizonsend:sidebar")))
                    .color(color),
                height = text("$height")
                    .append(text(repeatString(" ", 3 - height.toString().length))
                        .font(key("horizonsend:sidebar")))
                    .color(color),
                distance = text("${distance}m")
                    .append(text(repeatString(" ", 4 - distance.toString().length))
                        .font(key("horizonsend:sidebar")))
                    .color(color),
                distanceInt = distance,
                padding = empty()
            ))
        }

        for (planet in planets) {
            val vector = planet.location.toVector()
            val distance = vector.distance(playerVector).toInt()
            val direction = getDirectionToObject(vector.clone().subtract(playerVector).normalize())
            val height = vector.y.toInt()
            val color = distanceColor(distance)

            contactsList.add(ContactsData(
                name = text(planet.name).color(color),
                prefix = text("\uE020").font(key("horizonsend:sidebar")).color(DARK_AQUA) as TextComponent,
                suffix = if (distance <= MassShadows.PLANET_RADIUS) {
                    text("\uE033").font(key("horizonsend:sidebar")).color(RED) as TextComponent
                } else empty(),
                heading = text(direction)
                    .append(text(repeatString(" ", 2 - direction.length))
                        .font(key("horizonsend:sidebar")))
                    .color(color),
                height = text("$height")
                    .append(text(repeatString(" ", 3 - height.toString().length))
                        .font(key("horizonsend:sidebar")))
                    .color(color),
                distance = text("${distance}m")
                    .append(text(repeatString(" ", 4 - distance.toString().length))
                        .font(key("horizonsend:sidebar")))
                    .color(color),
                distanceInt = distance,
                padding = empty()
            ))
        }

        for (star in stars) {
            val vector = star.location.toVector()
            val distance = vector.distance(playerVector).toInt()
            val direction = getDirectionToObject(vector.clone().subtract(playerVector).normalize())
            val height = vector.y.toInt()
            val color = distanceColor(distance)

            contactsList.add(ContactsData(
                name = text(star.name).color(color),
                prefix = text("\uE021").font(key("horizonsend:sidebar")).color(YELLOW) as TextComponent,
                suffix = if (distance <= MassShadows.STAR_RADIUS) {
                    text("\uE033").font(key("horizonsend:sidebar")).color(RED) as TextComponent
                } else empty(),
                heading = text(direction)
                    .append(text(repeatString(" ", 2 - direction.length))
                        .font(key("horizonsend:sidebar")))
                    .color(color),
                height = text("$height")
                    .append(text(repeatString(" ", 3 - height.toString().length))
                        .font(key("horizonsend:sidebar")))
                    .color(color),
                distance = text("${distance}m")
                    .append(text(repeatString(" ", 4 - distance.toString().length))
                        .font(key("horizonsend:sidebar")))
                    .color(color),
                distanceInt = distance,
                padding = empty()
            ))
        }

        for (beacon in beacons) {
            val vector = beacon.spaceLocation.toBlockPos().toVector()
            val distance = vector.distance(playerVector).toInt()
            val direction = getDirectionToObject(vector.clone().subtract(playerVector).normalize())
            val height = vector.y.toInt()
            val color = distanceColor(distance)

            contactsList.add(ContactsData(
                name = text(beacon.name).color(color),
                prefix = text("\uE022").font(key("horizonsend:sidebar")).color(BLUE) as TextComponent,
                suffix = if (beacon.prompt?.contains("⚠") == true) text("⚠").color(RED) else empty(),
                heading = text(direction)
                    .append(text(repeatString(" ", 2 - direction.length))
                        .font(key("horizonsend:sidebar")))
                    .color(color),
                height = text("$height")
                    .append(text(repeatString(" ", 3 - height.toString().length))
                        .font(key("horizonsend:sidebar")))
                    .color(color),
                distance = text("${distance}m")
                    .append(text(repeatString(" ", 4 - distance.toString().length))
                        .font(key("horizonsend:sidebar")))
                    .color(color),
                distanceInt = distance,
                padding = empty()
            ))
        }

        // append spaces
        for (contact in contactsList) {
            contact.padding = text(repeatString(" ", 1))
        }

        contactsList.sortBy { it.distanceInt }
        return contactsList
    }
}

data class ContactsData(
    val name: TextComponent,
    val prefix: TextComponent,
    val suffix: TextComponent,
    val heading: TextComponent,
    val height: TextComponent,
    val distance: TextComponent,
    val distanceInt: Int,
    var padding: TextComponent
)