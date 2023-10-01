package net.horizonsend.ion.server.features.sidebar.tasks

import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.sidebar.MainSidebar
import net.horizonsend.ion.server.features.space.CachedPlanet
import net.horizonsend.ion.server.features.space.CachedStar
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.LastPilotedStarship
import net.horizonsend.ion.server.features.starship.StarshipType.CORVETTE
import net.horizonsend.ion.server.features.starship.StarshipType.DESTROYER
import net.horizonsend.ion.server.features.starship.StarshipType.FRIGATE
import net.horizonsend.ion.server.features.starship.StarshipType.GUNSHIP
import net.horizonsend.ion.server.features.starship.StarshipType.HEAVY_FREIGHTER
import net.horizonsend.ion.server.features.starship.StarshipType.LIGHT_FREIGHTER
import net.horizonsend.ion.server.features.starship.StarshipType.MEDIUM_FREIGHTER
import net.horizonsend.ion.server.features.starship.StarshipType.SHUTTLE
import net.horizonsend.ion.server.features.starship.StarshipType.STARFIGHTER
import net.horizonsend.ion.server.features.starship.StarshipType.TRANSPORT
import net.horizonsend.ion.server.features.starship.active.ActivePlayerStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.hyperspace.MassShadows
import net.horizonsend.ion.server.miscellaneous.utils.repeatString
import net.horizonsend.ion.server.miscellaneous.utils.toVector
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
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

    fun getPlayerContacts(player: Player): List<ContactsData> {
        val contactsList: MutableList<ContactsData> = mutableListOf()
        val playerVector = player.location.toVector()

        val starshipsEnabled = PlayerCache[player].contactsStarships
        val lastStarshipEnabled = PlayerCache[player].lastStarshipEnabled
        val planetsEnabled = PlayerCache[player].planetsEnabled
        val starsEnabled = PlayerCache[player].starsEnabled
        val beaconsEnabled = PlayerCache[player].beaconsEnabled

        // identify valid contacts
        val starships: List<ActiveStarship> = if (starshipsEnabled) {
            ActiveStarships.all().filter {
                it.serverLevel.world == player.world &&
                        it.centerOfMass.toVector().distanceSquared(playerVector) <= MainSidebar.CONTACTS_SQRANGE &&
                        (it as ActivePlayerStarship).pilot !== player &&
                        (it as ActivePlayerStarship).pilot?.gameMode != GameMode.SPECTATOR
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
            val vector = when (starship) {
                is ActivePlayerStarship -> starship.controller?.playerPilot?.location?.toVector()
                    ?: starship.centerOfMass.toVector()

                else -> starship.centerOfMass.toVector()
            }
            val distance = vector.distance(playerVector).toInt()
            val direction = getDirectionToObject(vector.clone().subtract(playerVector).normalize())
            val height = vector.y.toInt()
            val color = distanceColor(distance)

            contactsList.add(
                ContactsData(
                    name = text((starship as ActivePlayerStarship).pilot?.name ?: "Unpiloted Ship")
                        .color(color),
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
                    }.run {
                        val viewerNation = PlayerCache[player].nationOid ?: return@run this.color(GRAY)
                        val pilotNation =
                            PlayerCache[starship.pilot ?: return@run this.color(DARK_GRAY)].nationOid
                                ?: return@run this.color(GRAY)
                        return@run this.color(RelationCache[viewerNation, pilotNation].color)
                    } as TextComponent,

                    suffix = if (starship.isInterdicting && distance <= starship.type.interdictionRange) {
                        text("\uE033")
                            .font(key("horizonsend:sidebar")).color(RED) as TextComponent
                    } else if (starship.isInterdicting) {
                        text("\uE033")
                            .font(key("horizonsend:sidebar")).color(GOLD) as TextComponent
                    } else Component.empty(),
                    heading = text(direction)
                        .append(
                            text(repeatString(" ", 2 - direction.length))
                                .font(key("horizonsend:sidebar"))
                        )
                        .color(color),
                    height = text("$height")
                        .append(text("y"))
                        .append(
                            text(repeatString(" ", 3 - height.toString().length))
                                .font(key("horizonsend:sidebar"))
                        )
                        .color(color),
                    distance = text("$distance")
                        .append(text("m"))
                        .append(
                            text(repeatString(" ", 4 - distance.toString().length))
                                .font(key("horizonsend:sidebar"))
                        )
                        .color(color),
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
                    name = text("Last Piloted Starship").color(color),
                    prefix = text("\uE032")
                        .font(key("horizonsend:sidebar")).color(YELLOW) as TextComponent,
                    suffix = Component.empty(),
                    heading = text(direction)
                        .append(
                            text(repeatString(" ", 2 - direction.length))
                                .font(key("horizonsend:sidebar"))
                        )
                        .color(color),
                    height = text("$height")
                        .append(text("y"))
                        .append(
                            text(repeatString(" ", 3 - height.toString().length))
                                .font(key("horizonsend:sidebar"))
                        )
                        .color(color),
                    distance = text("$distance")
                        .append(text("m"))
                        .append(
                            text(repeatString(" ", 4 - distance.toString().length))
                                .font(key("horizonsend:sidebar"))
                        )
                        .color(color),
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
                    name = text(planet.name).color(color),
                    prefix = text("\uE020")
                        .font(key("horizonsend:sidebar")).color(DARK_AQUA) as TextComponent,
                    suffix = if (distance <= MassShadows.PLANET_RADIUS) {
                        text("\uE033")
                            .font(key("horizonsend:sidebar")).color(RED) as TextComponent
                    } else Component.empty(),
                    heading = text(direction)
                        .append(
                            text(repeatString(" ", 2 - direction.length))
                                .font(key("horizonsend:sidebar"))
                        )
                        .color(color),
                    height = text("$height")
                        .append(text("y"))
                        .append(
                            text(repeatString(" ", 3 - height.toString().length))
                                .font(key("horizonsend:sidebar"))
                        )
                        .color(color),
                    distance = text("$distance")
                        .append(text("m"))
                        .append(
                            text(repeatString(" ", 4 - distance.toString().length))
                                .font(key("horizonsend:sidebar"))
                        )
                        .color(color),
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
                    name = text(star.name).color(color),
                    prefix = text("\uE021")
                        .font(key("horizonsend:sidebar")).color(YELLOW) as TextComponent,
                    suffix = if (distance <= MassShadows.STAR_RADIUS) {
                        text("\uE033")
                            .font(key("horizonsend:sidebar")).color(RED) as TextComponent
                    } else Component.empty(),
                    heading = text(direction)
                        .append(
                            text(repeatString(" ", 2 - direction.length))
                                .font(key("horizonsend:sidebar"))
                        )
                        .color(color),
                    height = text("$height")
                        .append(text("y"))
                        .append(
                            text(repeatString(" ", 3 - height.toString().length))
                                .font(key("horizonsend:sidebar"))
                        )
                        .color(color),
                    distance = text("$distance")
                        .append(text("m"))
                        .append(
                            text(repeatString(" ", 4 - distance.toString().length))
                                .font(key("horizonsend:sidebar"))
                        )
                        .color(color),
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
            val vector = beacon.spaceLocation.toBlockPos().toVector()
            val distance = vector.distance(playerVector).toInt()
            val direction = getDirectionToObject(vector.clone().subtract(playerVector).normalize())
            val height = vector.y.toInt()
            val color = distanceColor(distance)

            contactsList.add(
                ContactsData(
                    name = text(beacon.name).color(color),
                    prefix = text("\uE022")
                        .font(key("horizonsend:sidebar")).color(BLUE) as TextComponent,
                    suffix = if (beacon.prompt?.contains("⚠") == true) text("⚠")
                        .color(RED) else Component.empty(),
                    heading = text(direction)
                        .append(
                            text(repeatString(" ", 2 - direction.length))
                                .font(key("horizonsend:sidebar"))
                        )
                        .color(color),
                    height = text("$height")
                        .append(text("y"))
                        .append(
                            text(repeatString(" ", 3 - height.toString().length))
                                .font(key("horizonsend:sidebar"))
                        )
                        .color(color),
                    distance = text("$distance")
                        .append(text("m"))
                        .append(
                            text(repeatString(" ", 4 - distance.toString().length))
                                .font(key("horizonsend:sidebar"))
                        )
                        .color(color),
                    distanceInt = distance,
                    padding = Component.empty()
                )
            )
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
}

