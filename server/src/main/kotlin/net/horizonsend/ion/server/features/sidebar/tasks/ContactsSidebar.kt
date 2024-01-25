package net.horizonsend.ion.server.features.sidebar.tasks

import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.utils.text.repeatString
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.sidebar.MainSidebar
import net.horizonsend.ion.server.features.sidebar.Sidebar.fontKey
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.CROSSHAIR_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.GENERIC_STARSHIP_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.HYPERSPACE_BEACON_ENTER_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.INTERDICTION_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.PLANET_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.STAR_ICON
import net.horizonsend.ion.server.features.space.CachedPlanet
import net.horizonsend.ion.server.features.space.CachedStar
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.LastPilotedStarship
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.controllers.NoOpController
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.player.ActivePlayerController
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.hyperspace.MassShadows
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.*
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.abs

object ContactsSidebar {
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

    private fun relationColor(player: Player, otherController: Controller): NamedTextColor {
        when (otherController) {
            is NoOpController -> return GRAY
            is AIController -> return DARK_GRAY
            is PlayerController -> {
                val viewerNation = PlayerCache[player].nationOid ?: return GRAY
                val otherNation = PlayerCache[otherController.player].nationOid ?: return GRAY
                return RelationCache[viewerNation, otherNation].color
            }
            else -> return GRAY
        }
    }

    private fun getDirectionToObject(direction: Vector): String {
        val directionString = StringBuilder("")

        if (direction.z != 0.0 && abs(direction.z) > 0.4) {
            directionString.append(if (direction.z > 0) "S" else "N")
        }
        if (direction.x != 0.0 && abs(direction.x) > 0.4) {
            directionString.append(if (direction.x > 0) "E" else "W")
        }

        return directionString.toString()
    }

    // Main method for generating all contacts a player cna see
    fun getPlayerContacts(player: Player): List<ContactsData> {
        val contactsList: MutableList<ContactsData> = mutableListOf()
        val playerVector = player.location.toVector()

        val starshipsEnabled = PlayerCache[player].contactsStarships
        val lastStarshipEnabled = PlayerCache[player].lastStarshipEnabled
        val planetsEnabled = PlayerCache[player].planetsEnabled
        val starsEnabled = PlayerCache[player].starsEnabled
        val beaconsEnabled = PlayerCache[player].beaconsEnabled

        // identify contacts that should be displayed (enabled and in range)
        val starships: List<ActiveStarship> = if (starshipsEnabled) {
            ActiveStarships.all().filter {
                it.world == player.world &&
                        it.centerOfMass.toVector().distanceSquared(playerVector) <= MainSidebar.CONTACTS_SQRANGE &&
                        it.controller !== ActiveStarships.findByPilot(player)?.controller &&
                        (it.controller as? PlayerController)?.player?.gameMode != GameMode.SPECTATOR
            }
        } else listOf()

        val planets: List<CachedPlanet> = if (planetsEnabled) {
            Space.getPlanets().filter {
                it.spaceWorld == player.world && it.location.toVector()
                    .distanceSquared(playerVector) <= MainSidebar.CONTACTS_SQRANGE
            }
        } else listOf()

        val stars: List<CachedStar> = if (starsEnabled) {
            Space.getStars().filter {
                it.spaceWorld == player.world && it.location.toVector()
                    .distanceSquared(playerVector) <= MainSidebar.CONTACTS_SQRANGE
            }
        } else listOf()

        val beacons: List<ServerConfiguration.HyperspaceBeacon> = if (beaconsEnabled) {
            IonServer.configuration.beacons.filter {
                it.spaceLocation.bukkitWorld() == player.world &&
                        it.spaceLocation.toLocation().toVector()
                            .distanceSquared(playerVector) <= MainSidebar.CONTACTS_SQRANGE
            }
        } else listOf()

        // Add contacts to main contacts list
        if (starshipsEnabled) {
            addStarshipContacts(starships, playerVector, contactsList, player)
        }

        if (lastStarshipEnabled) {
            addLastStarshipContact(player, playerVector, contactsList)
        }

        if (planetsEnabled) {
            addPlanetContacts(planets, playerVector, contactsList)
        }

        if (starsEnabled) {
            addStarContacts(stars, playerVector, contactsList)
        }

        if (beaconsEnabled) {
            addBeaconContacts(beacons, playerVector, contactsList)
        }

        // append spaces
        for (contact in contactsList) {
            contact.padding = text(repeatString(" ", 1))
        }

        contactsList.sortBy { it.distanceInt }
        return contactsList
    }

    private fun addStarshipContacts(
        starships: List<ActiveStarship>,
        playerVector: Vector,
        contactsList: MutableList<ContactsData>,
        player: Player
    ) {
        for (starship in starships) {
            val otherController = starship.controller
            val vector = when (otherController) {
                is ActivePlayerController -> otherController.player.location.toVector()
                else -> starship.centerOfMass.toVector()
            }

            val distance = vector.distance(playerVector).toInt()
            val direction = getDirectionToObject(vector.clone().subtract(playerVector).normalize())
            val height = vector.y.toInt()
            val color = distanceColor(distance)
            val currentStarship = PilotedStarships[player]

            contactsList.add(
                ContactsData(
                    name = text(starship.identifier, color),
                    prefix = constructPrefixTextComponent(starship.type.icon, relationColor(player, otherController)),
                    suffix = constructSuffixTextComponent(
                        if (currentStarship != null) {
                            autoTurretTextComponent(currentStarship, starship)
                        } else Component.empty(),
                        if (starship.isInterdicting) {
                            interdictionTextComponent(distance, starship.balancing.interdictionRange, true)
                        } else Component.empty()
                    ),
                    heading = constructHeadingTextComponent(direction, color),
                    height = constructHeightTextComponent(height, color),
                    distance = constructDistanceTextComponent(distance, color),
                    distanceInt = distance,
                    padding = Component.empty()
                )
            )
        }
    }

    private fun addLastStarshipContact(
        player: Player,
        playerVector: Vector,
        contactsList: MutableList<ContactsData>
    ) {
        val lastStarship = LastPilotedStarship.map[player.uniqueId]

        if (lastStarship != null &&
            lastStarship.world == player.world &&
            lastStarship.toVector().distanceSquared(playerVector) <= MainSidebar.CONTACTS_SQRANGE
        ) {
            val vector = lastStarship.toVector()
            val distance = vector.distance(playerVector).toInt()
            val direction = getDirectionToObject(vector.clone().subtract(playerVector).normalize())
            val height = vector.y.toInt()
            val color = distanceColor(distance)

            contactsList.add(
                ContactsData(
                    name = text("Last Piloted Starship", color),
                    prefix = constructPrefixTextComponent(GENERIC_STARSHIP_ICON.text, YELLOW),
                    suffix = Component.empty(),
                    heading = constructHeadingTextComponent(direction, color),
                    height = constructHeightTextComponent(height, color),
                    distance = constructDistanceTextComponent(distance, color),
                    distanceInt = distance,
                    padding = Component.empty()
                )
            )
        }
    }

    private fun addPlanetContacts(
        planets: List<CachedPlanet>,
        playerVector: Vector,
        contactsList: MutableList<ContactsData>
    ) {
        for (planet in planets) {
            val vector = planet.location.toVector()
            val distance = vector.distance(playerVector).toInt()
            val direction = getDirectionToObject(vector.clone().subtract(playerVector).normalize())
            val height = vector.y.toInt()
            val color = distanceColor(distance)

            contactsList.add(
                ContactsData(
                    name = text(planet.name, color),
                    prefix = constructPrefixTextComponent(PLANET_ICON.text, DARK_AQUA),
                    suffix = constructSuffixTextComponent(
                        interdictionTextComponent(
                            distance,
                            MassShadows.PLANET_RADIUS,
                            false
                        )
                    ),
                    heading = constructHeadingTextComponent(direction, color),
                    height = constructHeightTextComponent(height, color),
                    distance = constructDistanceTextComponent(distance, color),
                    distanceInt = distance,
                    padding = Component.empty()
                )
            )
        }
    }

    private fun addStarContacts(
        stars: List<CachedStar>,
        playerVector: Vector,
        contactsList: MutableList<ContactsData>
    ) {
        for (star in stars) {
            val vector = star.location.toVector()
            val distance = vector.distance(playerVector).toInt()
            val direction = getDirectionToObject(vector.clone().subtract(playerVector).normalize())
            val height = vector.y.toInt()
            val color = distanceColor(distance)

            contactsList.add(
                ContactsData(
                    name = text(star.name, color),
                    prefix = constructPrefixTextComponent(STAR_ICON.text, YELLOW),
                    suffix = constructSuffixTextComponent(
                        interdictionTextComponent(
                            distance,
                            MassShadows.STAR_RADIUS,
                            false
                        )
                    ),
                    heading = constructHeadingTextComponent(direction, color),
                    height = constructHeightTextComponent(height, color),
                    distance = constructDistanceTextComponent(distance, color),
                    distanceInt = distance,
                    padding = Component.empty()
                )
            )
        }
    }

    private fun addBeaconContacts(
        beacons: List<ServerConfiguration.HyperspaceBeacon>,
        playerVector: Vector,
        contactsList: MutableList<ContactsData>
    ) {
        for (beacon in beacons) {
            val vector = beacon.spaceLocation.toVector()
            val distance = vector.distance(playerVector).toInt()
            val direction = getDirectionToObject(vector.clone().subtract(playerVector).normalize())
            val height = vector.y.toInt()
            val color = distanceColor(distance)

            contactsList.add(
                ContactsData(
                    name = text(beacon.name, color),
                    prefix = constructPrefixTextComponent(HYPERSPACE_BEACON_ENTER_ICON.text, BLUE),
                    suffix = constructSuffixTextComponent(beaconTextComponent(beacon.prompt)),
                    heading = constructHeadingTextComponent(direction, color),
                    height = constructHeightTextComponent(height, color),
                    distance = constructDistanceTextComponent(distance, color),
                    distanceInt = distance,
                    padding = Component.empty()
                )
            )
        }
    }

    private fun constructPrefixTextComponent(icon: String, color: NamedTextColor) =
        text(icon)
            .font(fontKey)
            .color(color) as TextComponent

    private fun constructSuffixTextComponent(vararg components: Component): TextComponent {
        val returnComponent = text()
        for (component in components) {
            returnComponent.append(component)
            returnComponent.appendSpace()
        }
        return returnComponent.build()
    }

    private fun constructDistanceTextComponent(distance: Int, color: NamedTextColor) =
        text(distance)
            .append(text("m"))
            .append(text(repeatString(" ", 4 - distance.toString().length)).font(fontKey))
            .color(color)

    private fun constructHeightTextComponent(height: Int, color: NamedTextColor) =
        text(height)
            .append(text("y"))
            .append(text(repeatString(" ", 3 - height.toString().length)).font(fontKey))
            .color(color)

    private fun constructHeadingTextComponent(direction: String, color: NamedTextColor) =
        text(direction)
            .append(text(repeatString(" ", 2 - direction.length)).font(fontKey))
            .color(color)

    private fun interdictionTextComponent(distance: Int, interdictionDistance: Int, visibleOutOfRange: Boolean) =
        if (distance <= interdictionDistance) {
            text(INTERDICTION_ICON.text, RED).font(fontKey)
        } else if (visibleOutOfRange) {
            text(INTERDICTION_ICON.text, GOLD).font(fontKey)
        } else Component.empty()

    private fun beaconTextComponent(text: String?) =
        if (text?.contains("⚠") == true) text("⚠", RED)
        else Component.empty()

    private fun autoTurretTextComponent(starship: ActiveControlledStarship, otherStarship: ActiveStarship): Component {
        val textComponent = text()

        for (weaponset in starship.autoTurretTargets.entries) {
            when (otherStarship.controller) {
                is ActivePlayerController -> {
                    starship.autoTurretTargets.values.find {
                        it.identifier == (otherStarship.controller as PlayerController).player.name
                    } ?: continue
                }
                else -> {
                    starship.autoTurretTargets.values.find {
                        otherStarship.identifier.contains(it.identifier)
                    } ?: continue
                }
            }
            textComponent.append(text(CROSSHAIR_ICON.text, AQUA).font(fontKey))
            textComponent.append(text(weaponset.key, AQUA))
            textComponent.appendSpace()
        }

        return textComponent.build()
    }

    data class ContactsData(
        val name: Component,
        val prefix: TextComponent,
        val suffix: TextComponent,
        val heading: TextComponent,
        val height: TextComponent,
        val distance: TextComponent,
        val distanceInt: Int,
        var padding: TextComponent
    )
}
