package net.horizonsend.ion.server.features.sidebar.tasks

import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.sidebar.MainSidebar
import net.horizonsend.ion.server.features.space.CachedPlanet
import net.horizonsend.ion.server.features.space.CachedStar
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.LastPilotedStarship
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.active.ActivePlayerStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.hyperspace.MassShadows
import net.horizonsend.ion.server.miscellaneous.utils.repeatString
import net.horizonsend.ion.server.miscellaneous.utils.toVector
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.abs

object Contacts {
    private fun distanceColor(distance: Int): NamedTextColor {
        return when {
            distance < 500 -> NamedTextColor.RED
            distance < 1500 -> NamedTextColor.GOLD
            distance < 2500 -> NamedTextColor.YELLOW
            distance < 3500 -> NamedTextColor.DARK_GREEN
            distance < 6000 -> NamedTextColor.GREEN
            else -> NamedTextColor.GREEN
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
            contact.padding = Component.text(repeatString(" ", 1))
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
                    name = Component.text((starship as ActivePlayerStarship).pilot?.name ?: "Unpiloted Ship")
                        .color(color),
                    prefix = when (starship.type) {
                        StarshipType.STARFIGHTER -> Component.text("\uE000").font(Key.key("horizonsend:sidebar"))
                        StarshipType.GUNSHIP -> Component.text("\uE001").font(Key.key("horizonsend:sidebar"))
                        StarshipType.CORVETTE -> Component.text("\uE002").font(Key.key("horizonsend:sidebar"))
                        StarshipType.FRIGATE -> Component.text("\uE003").font(Key.key("horizonsend:sidebar"))
                        StarshipType.DESTROYER -> Component.text("\uE004").font(Key.key("horizonsend:sidebar"))
                        StarshipType.SHUTTLE -> Component.text("\uE010").font(Key.key("horizonsend:sidebar"))
                        StarshipType.TRANSPORT -> Component.text("\uE011").font(Key.key("horizonsend:sidebar"))
                        StarshipType.LIGHT_FREIGHTER -> Component.text("\uE012")
                            .font(Key.key("horizonsend:sidebar"))

                        StarshipType.MEDIUM_FREIGHTER -> Component.text("\uE013")
                            .font(Key.key("horizonsend:sidebar"))

                        StarshipType.HEAVY_FREIGHTER -> Component.text("\uE014")
                            .font(Key.key("horizonsend:sidebar"))

                        else -> Component.text("\uE032").font(Key.key("horizonsend:sidebar"))
                    }.run {
                        val viewerNation = PlayerCache[player].nationOid ?: return@run this.color(NamedTextColor.GRAY)
                        val pilotNation =
                            PlayerCache[starship.pilot ?: return@run this.color(NamedTextColor.DARK_GRAY)].nationOid
                                ?: return@run this.color(
                                    NamedTextColor.GRAY
                                )

                        return@run this.color(NationRelation.getRelationActual(viewerNation, pilotNation).color)
                    } as TextComponent,

                    suffix = if (starship.isInterdicting && distance <= starship.type.interdictionRange) {
                        Component.text("\uE033")
                            .font(Key.key("horizonsend:sidebar")).color(NamedTextColor.RED) as TextComponent
                    } else if (starship.isInterdicting) {
                        Component.text("\uE033")
                            .font(Key.key("horizonsend:sidebar")).color(NamedTextColor.GOLD) as TextComponent
                    } else Component.empty(),
                    heading = Component.text(direction)
                        .append(
                            Component.text(repeatString(" ", 2 - direction.length))
                                .font(Key.key("horizonsend:sidebar"))
                        )
                        .color(color),
                    height = Component.text("$height")
                        .append(Component.text("y"))
                        .append(
                            Component.text(repeatString(" ", 3 - height.toString().length))
                                .font(Key.key("horizonsend:sidebar"))
                        )
                        .color(color),
                    distance = Component.text("$distance")
                        .append(Component.text("m"))
                        .append(
                            Component.text(repeatString(" ", 4 - distance.toString().length))
                                .font(Key.key("horizonsend:sidebar"))
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
                    name = Component.text("Last Piloted Starship").color(color),
                    prefix = Component.text("\uE032")
                        .font(Key.key("horizonsend:sidebar")).color(NamedTextColor.YELLOW) as TextComponent,
                    suffix = Component.empty(),
                    heading = Component.text(direction)
                        .append(
                            Component.text(repeatString(" ", 2 - direction.length))
                                .font(Key.key("horizonsend:sidebar"))
                        )
                        .color(color),
                    height = Component.text("$height")
                        .append(Component.text("y"))
                        .append(
                            Component.text(repeatString(" ", 3 - height.toString().length))
                                .font(Key.key("horizonsend:sidebar"))
                        )
                        .color(color),
                    distance = Component.text("$distance")
                        .append(Component.text("m"))
                        .append(
                            Component.text(repeatString(" ", 4 - distance.toString().length))
                                .font(Key.key("horizonsend:sidebar"))
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
                    name = Component.text(planet.name).color(color),
                    prefix = Component.text("\uE020")
                        .font(Key.key("horizonsend:sidebar")).color(NamedTextColor.DARK_AQUA) as TextComponent,
                    suffix = if (distance <= MassShadows.PLANET_RADIUS) {
                        Component.text("\uE033")
                            .font(Key.key("horizonsend:sidebar")).color(NamedTextColor.RED) as TextComponent
                    } else Component.empty(),
                    heading = Component.text(direction)
                        .append(
                            Component.text(repeatString(" ", 2 - direction.length))
                                .font(Key.key("horizonsend:sidebar"))
                        )
                        .color(color),
                    height = Component.text("$height")
                        .append(Component.text("y"))
                        .append(
                            Component.text(repeatString(" ", 3 - height.toString().length))
                                .font(Key.key("horizonsend:sidebar"))
                        )
                        .color(color),
                    distance = Component.text("$distance")
                        .append(Component.text("m"))
                        .append(
                            Component.text(repeatString(" ", 4 - distance.toString().length))
                                .font(Key.key("horizonsend:sidebar"))
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
                    name = Component.text(star.name).color(color),
                    prefix = Component.text("\uE021")
                        .font(Key.key("horizonsend:sidebar")).color(NamedTextColor.YELLOW) as TextComponent,
                    suffix = if (distance <= MassShadows.STAR_RADIUS) {
                        Component.text("\uE033")
                            .font(Key.key("horizonsend:sidebar")).color(NamedTextColor.RED) as TextComponent
                    } else Component.empty(),
                    heading = Component.text(direction)
                        .append(
                            Component.text(repeatString(" ", 2 - direction.length))
                                .font(Key.key("horizonsend:sidebar"))
                        )
                        .color(color),
                    height = Component.text("$height")
                        .append(Component.text("y"))
                        .append(
                            Component.text(repeatString(" ", 3 - height.toString().length))
                                .font(Key.key("horizonsend:sidebar"))
                        )
                        .color(color),
                    distance = Component.text("$distance")
                        .append(Component.text("m"))
                        .append(
                            Component.text(repeatString(" ", 4 - distance.toString().length))
                                .font(Key.key("horizonsend:sidebar"))
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
                    name = Component.text(beacon.name).color(color),
                    prefix = Component.text("\uE022")
                        .font(Key.key("horizonsend:sidebar")).color(NamedTextColor.BLUE) as TextComponent,
                    suffix = if (beacon.prompt?.contains("⚠") == true) Component.text("⚠")
                        .color(NamedTextColor.RED) else Component.empty(),
                    heading = Component.text(direction)
                        .append(
                            Component.text(repeatString(" ", 2 - direction.length))
                                .font(Key.key("horizonsend:sidebar"))
                        )
                        .color(color),
                    height = Component.text("$height")
                        .append(Component.text("y"))
                        .append(
                            Component.text(repeatString(" ", 3 - height.toString().length))
                                .font(Key.key("horizonsend:sidebar"))
                        )
                        .color(color),
                    distance = Component.text("$distance")
                        .append(Component.text("m"))
                        .append(
                            Component.text(repeatString(" ", 4 - distance.toString().length))
                                .font(Key.key("horizonsend:sidebar"))
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

