package net.horizonsend.ion.server.features.sidebar.tasks

import net.horizonsend.ion.common.database.cache.BookmarkCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.schema.misc.Bookmark
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.common.utils.text.repeatString
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.misc.CachedCapturableStation
import net.horizonsend.ion.server.features.misc.CapturableStationCache
import net.horizonsend.ion.server.features.sidebar.Sidebar.fontKey
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.BOOKMARK_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.CROSSHAIR_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.GENERIC_STARSHIP_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.HYPERSPACE_BEACON_ENTER_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.INTERDICTION_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.PLANET_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.SIEGE_STATION_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.STAR_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.STATION_ICON
import net.horizonsend.ion.server.features.space.CachedPlanet
import net.horizonsend.ion.server.features.space.CachedStar
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.spacestations.CachedNationSpaceStation
import net.horizonsend.ion.server.features.space.spacestations.CachedPlayerSpaceStation
import net.horizonsend.ion.server.features.space.spacestations.CachedSettlementSpaceStation
import net.horizonsend.ion.server.features.space.spacestations.CachedSpaceStation
import net.horizonsend.ion.server.features.space.spacestations.SpaceStationCache
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
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN
import net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE
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
    private fun getContactsDistanceSq(player: Player): Int {
        return PlayerCache.getIfOnline(player)?.contactsDistance?.squared() ?: 0
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

    private fun playerRelationType(player: Player, otherController: Controller): ContactsRelation {
        when (otherController) {
            is NoOpController -> return ContactsRelation.NONE
            is AIController -> return ContactsRelation.AI
            is PlayerController -> {
                val viewerNation = PlayerCache.getIfOnline(player)?.nationOid ?: return ContactsRelation.NONE
                val otherNation = PlayerCache.getIfOnline(otherController.player)?.nationOid ?: return ContactsRelation.NONE
                return convertRelationType(RelationCache[viewerNation, otherNation])
            }
            else -> return ContactsRelation.NONE
        }
    }

    private fun playerRelationColor(player: Player, otherController: Controller): NamedTextColor {
        when (otherController) {
            is NoOpController -> return GRAY
            is AIController -> return DARK_GRAY
            is PlayerController -> {
                val viewerNation = PlayerCache.getIfOnline(player)?.nationOid ?: return GRAY
                val otherNation = PlayerCache.getIfOnline(otherController.player)?.nationOid ?: return GRAY
                return RelationCache[viewerNation, otherNation].color
            }
            else -> return GRAY
        }
    }

    private fun stationRelationType(player: Player, station: CachedSpaceStation<*, *, *>): ContactsRelation {
        when (station) {
            is CachedPlayerSpaceStation -> return if (station.hasOwnershipContext(player.slPlayerId)) ContactsRelation.NATION else ContactsRelation.NONE
            is CachedSettlementSpaceStation -> return if (station.hasOwnershipContext(player.slPlayerId)) ContactsRelation.NATION else ContactsRelation.NONE
            is CachedNationSpaceStation -> {
                val viewerNation = PlayerCache.getIfOnline(player)?.nationOid ?: return ContactsRelation.NONE
                val otherNation = station.owner
                return convertRelationType(RelationCache[viewerNation, otherNation])
            }
            else -> return ContactsRelation.NONE
        }
    }

    private fun stationRelationColor(player: Player, station: CachedSpaceStation<*, *, *>): NamedTextColor {
        when (station) {
            is CachedPlayerSpaceStation -> return if (station.hasOwnershipContext(player.slPlayerId)) GREEN else GRAY
            is CachedSettlementSpaceStation -> return if (station.hasOwnershipContext(player.slPlayerId)) GREEN else GRAY
            is CachedNationSpaceStation -> {
                val viewerNation = PlayerCache.getIfOnline(player)?.nationOid ?: return GRAY
                val otherNation = station.owner
                return RelationCache[viewerNation, otherNation].color
            }
            else -> return GRAY
        }
    }

    private fun capturableStationRelationType(player: Player, station: CachedCapturableStation): ContactsRelation {
        val viewerNation = PlayerCache.getIfOnline(player)?.nationOid ?: return ContactsRelation.NONE
        val otherNation = station.nation ?: return ContactsRelation.NONE
        return convertRelationType(RelationCache[viewerNation, otherNation])
    }

    private fun capturableStationRelationColor(player: Player, station: CachedCapturableStation): NamedTextColor {
        val viewerNation = PlayerCache.getIfOnline(player)?.nationOid ?: return GRAY
        val otherNation = station.nation ?: return GRAY
        return RelationCache[viewerNation, otherNation].color
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

    // Main method for generating all contacts a player can see
    fun getPlayerContacts(player: Player): List<ContactsData> {
        val contactsList: MutableList<ContactsData> = mutableListOf()
        val sourceVector = PilotedStarships[player]?.centerOfMass?.toVector() ?: player.location.toVector()
        val playerVector = player.location.toVector()

        val starshipsEnabled = PlayerCache.getIfOnline(player)?.contactsStarships ?: true
        val lastStarshipEnabled = PlayerCache.getIfOnline(player)?.lastStarshipEnabled ?: true
        val planetsEnabled = PlayerCache.getIfOnline(player)?.planetsEnabled ?: true
        val starsEnabled = PlayerCache.getIfOnline(player)?.starsEnabled ?: true
        val beaconsEnabled = PlayerCache.getIfOnline(player)?.beaconsEnabled ?: true
        val stationsEnabled = PlayerCache.getIfOnline(player)?.stationsEnabled ?: true
        val bookmarksEnabled = PlayerCache.getIfOnline(player)?.bookmarksEnabled ?: true

        // identify contacts that should be displayed (enabled and in range)
        val starships: List<ActiveStarship> = if (starshipsEnabled) {
            ActiveStarships.all().filter {
                it.world == player.world &&
                        it.centerOfMass.toVector().distanceSquared(sourceVector) <= getContactsDistanceSq(player) &&
                        it.controller !== ActiveStarships.findByPilot(player)?.controller &&
                        (it.controller as? PlayerController)?.player?.gameMode != GameMode.SPECTATOR
            }
        } else listOf()

        val planets: List<CachedPlanet> = if (planetsEnabled) {
            Space.getPlanets().filter {
                it.spaceWorld == player.world && it.location.toVector()
                    .distanceSquared(sourceVector) <= getContactsDistanceSq(player)
            }
        } else listOf()

        val stars: List<CachedStar> = if (starsEnabled) {
            Space.getStars().filter {
                it.spaceWorld == player.world && it.location.toVector()
                    .distanceSquared(sourceVector) <= getContactsDistanceSq(player)
            }
        } else listOf()

        val beacons: List<ServerConfiguration.HyperspaceBeacon> = if (beaconsEnabled) {
            IonServer.configuration.beacons.filter {
                it.spaceLocation.bukkitWorld() == player.world &&
                        it.spaceLocation.toLocation().toVector()
                            .distanceSquared(sourceVector) <= getContactsDistanceSq(player)
            }
        } else listOf()

        val stations: List<CachedSpaceStation<*, *, *>> = if (stationsEnabled) {
            SpaceStationCache.all().filter {
                it.world == player.world.name && Vector(it.x, 192, it.z)
                    .distanceSquared(sourceVector) <= getContactsDistanceSq(player)
            }
        } else listOf()

        val capturableStations: List<CachedCapturableStation> = if (stationsEnabled) {
            CapturableStationCache.stations.filter {
                it.loc.world != null && it.loc.world.name == player.world.name && it.loc.toVector()
                    .distanceSquared(sourceVector) <= getContactsDistanceSq(player)
            }
        } else listOf()

        val bookmarks: List<Bookmark> = if (bookmarksEnabled) {
            BookmarkCache.getAll().filter { bm -> bm.owner == player.slPlayerId }.filter {
                it.worldName == player.world.name &&
                        Vector(it.x, it.y, it.z).distanceSquared(sourceVector) <= getContactsDistanceSq(player)
            }
        } else listOf()


        // Add contacts to main contacts list
        if (starshipsEnabled) {
            addStarshipContacts(starships, playerVector, contactsList, player)
        }

        if (lastStarshipEnabled) {
            addLastStarshipContact(player, sourceVector, contactsList)
        }

        if (planetsEnabled) {
            addPlanetContacts(planets, sourceVector, contactsList, player)
        }

        if (starsEnabled) {
            addStarContacts(stars, sourceVector, contactsList, player)
        }

        if (beaconsEnabled) {
            addBeaconContacts(beacons, sourceVector, contactsList, player)
        }

        if (stationsEnabled) {
            addStationContacts(stations, sourceVector, contactsList, player)
            addCapturableStationContacts(capturableStations, sourceVector, contactsList, player)
        }

        if (bookmarksEnabled) {
            addBookmarkContacts(bookmarks, sourceVector, contactsList, player)
        }

        // append spaces
        for (contact in contactsList) {
            contact.padding = text(repeatString(" ", 1))
        }

        sortContacts(contactsList, player)
        return contactsList
    }

    private fun addStarshipContacts(
        starships: List<ActiveStarship>,
        playerVector: Vector,
        contactsList: MutableList<ContactsData>,
        player: Player
    ) {
        val currentStarship = PilotedStarships[player]
        val interdictionLocation = currentStarship?.centerOfMass?.toVector() ?: playerVector
        val maxLength = PlayerCache[player.uniqueId].contactsMaxNameLength
        val colorSetting = PlayerCache[player.uniqueId].contactsColoring

        for (starship in starships) {
            val otherController = starship.controller
            val vector = when (otherController) {
                is ActivePlayerController -> otherController.player.location.toVector()
                else -> starship.centerOfMass.toVector()
            }

            val distance = vector.distance(playerVector).toInt()
            val interdictionDistance = starship.centerOfMass.toVector().distance(interdictionLocation).toInt()
            val direction = getDirectionToObject(vector.clone().subtract(playerVector).normalize())
            val height = vector.y.toInt()

            val color = when (colorSetting) {
                ContactsColoring.BY_DISTANCE.ordinal -> distanceColor(distance)
                ContactsColoring.BY_RELATION.ordinal -> playerRelationColor(player, otherController)
                else -> distanceColor(distance)
            }
            val prefixColor = when (colorSetting) {
                ContactsColoring.BY_DISTANCE.ordinal -> playerRelationColor(player, otherController)
                ContactsColoring.BY_RELATION.ordinal -> distanceColor(distance)
                else -> playerRelationColor(player, otherController)
            }

            contactsList.add(
                ContactsData(
                    name = text(starship.identifier.take(maxLength), color),
                    type = ContactsType.STARSHIP,
                    relation = playerRelationType(player, otherController),
                    prefix = constructPrefixTextComponent(starship.type.icon, prefixColor),
                    suffix = constructSuffixTextComponent(
                        if (currentStarship != null) {
                            autoTurretTextComponent(currentStarship, starship)
                        } else Component.empty(),
                        if (starship.isInterdicting) {
                            interdictionTextComponent(interdictionDistance, starship.balancing.interdictionRange, true)
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
        sourceVector: Vector,
        contactsList: MutableList<ContactsData>
    ) {
        val lastStarship = LastPilotedStarship.map[player.uniqueId]
        val maxLength = PlayerCache[player.uniqueId].contactsMaxNameLength
        val colorSetting = PlayerCache[player.uniqueId].contactsColoring

        if (lastStarship != null &&
            lastStarship.world == player.world &&
            lastStarship.toVector().distanceSquared(sourceVector) <= getContactsDistanceSq(player)
        ) {
            val vector = lastStarship.toVector()
            val distance = vector.distance(sourceVector).toInt()
            val direction = getDirectionToObject(vector.clone().subtract(sourceVector).normalize())
            val height = vector.y.toInt()

            val color = when (colorSetting) {
                ContactsColoring.BY_DISTANCE.ordinal -> distanceColor(distance)
                ContactsColoring.BY_RELATION.ordinal -> GRAY
                else -> distanceColor(distance)
            }
            val prefixColor = when (colorSetting) {
                ContactsColoring.BY_DISTANCE.ordinal -> YELLOW
                ContactsColoring.BY_RELATION.ordinal -> distanceColor(distance)
                else -> YELLOW
            }

            contactsList.add(
                ContactsData(
                    name = text("Last Piloted Starship".take(maxLength), color),
                    type = ContactsType.LAST_STARSHIP,
                    relation = null,
                    prefix = constructPrefixTextComponent(GENERIC_STARSHIP_ICON.text, prefixColor),
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
        sourceVector: Vector,
        contactsList: MutableList<ContactsData>,
        player: Player
    ) {
        val maxLength = PlayerCache[player.uniqueId].contactsMaxNameLength
        val colorSetting = PlayerCache[player.uniqueId].contactsColoring

        for (planet in planets) {
            val vector = planet.location.toVector()
            val distance = vector.distance(sourceVector).toInt()
            val direction = getDirectionToObject(vector.clone().subtract(sourceVector).normalize())
            val height = vector.y.toInt()

            val color = when (colorSetting) {
                ContactsColoring.BY_DISTANCE.ordinal -> distanceColor(distance)
                ContactsColoring.BY_RELATION.ordinal -> GRAY
                else -> distanceColor(distance)
            }
            val prefixColor = when (colorSetting) {
                ContactsColoring.BY_DISTANCE.ordinal -> DARK_AQUA
                ContactsColoring.BY_RELATION.ordinal -> distanceColor(distance)
                else -> DARK_AQUA
            }

            contactsList.add(
                ContactsData(
                    name = text(planet.name.take(maxLength), color),
                    type = ContactsType.PLANET,
                    relation = null,
                    prefix = constructPrefixTextComponent(PLANET_ICON.text, prefixColor),
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
        sourceVector: Vector,
        contactsList: MutableList<ContactsData>,
        player: Player
    ) {
        val maxLength = PlayerCache[player.uniqueId].contactsMaxNameLength
        val colorSetting = PlayerCache[player.uniqueId].contactsColoring

        for (star in stars) {
            val vector = star.location.toVector()
            val distance = vector.distance(sourceVector).toInt()
            val direction = getDirectionToObject(vector.clone().subtract(sourceVector).normalize())
            val height = vector.y.toInt()

            val color = when (colorSetting) {
                ContactsColoring.BY_DISTANCE.ordinal -> distanceColor(distance)
                ContactsColoring.BY_RELATION.ordinal -> GRAY
                else -> distanceColor(distance)
            }
            val prefixColor = when (colorSetting) {
                ContactsColoring.BY_DISTANCE.ordinal -> YELLOW
                ContactsColoring.BY_RELATION.ordinal -> distanceColor(distance)
                else -> YELLOW
            }

            contactsList.add(
                ContactsData(
                    name = text(star.name.take(maxLength), color),
                    type = ContactsType.STAR,
                    relation = null,
                    prefix = constructPrefixTextComponent(STAR_ICON.text, prefixColor),
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
        sourceVector: Vector,
        contactsList: MutableList<ContactsData>,
        player: Player
    ) {
        val maxLength = PlayerCache[player.uniqueId].contactsMaxNameLength
        val colorSetting = PlayerCache[player.uniqueId].contactsColoring

        for (beacon in beacons) {
            val vector = beacon.spaceLocation.toVector()
            val distance = vector.distance(sourceVector).toInt()
            val direction = getDirectionToObject(vector.clone().subtract(sourceVector).normalize())
            val height = vector.y.toInt()

            val color = when (colorSetting) {
                ContactsColoring.BY_DISTANCE.ordinal -> distanceColor(distance)
                ContactsColoring.BY_RELATION.ordinal -> GRAY
                else -> distanceColor(distance)
            }
            val prefixColor = when (colorSetting) {
                ContactsColoring.BY_DISTANCE.ordinal -> BLUE
                ContactsColoring.BY_RELATION.ordinal -> distanceColor(distance)
                else -> BLUE
            }

            contactsList.add(
                ContactsData(
                    name = text(beacon.name.take(maxLength), color),
                    type = ContactsType.BEACON,
                    relation = null,
                    prefix = constructPrefixTextComponent(HYPERSPACE_BEACON_ENTER_ICON.text, prefixColor),
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

    private fun addStationContacts(
        stations: List<CachedSpaceStation<*, *, *>>,
        sourceVector: Vector,
        contactsList: MutableList<ContactsData>,
        player: Player
    ) {
        val maxLength = PlayerCache[player.uniqueId].contactsMaxNameLength
        val colorSetting = PlayerCache[player.uniqueId].contactsColoring

        for (station in stations) {
            val vector = Vector(station.x, 192, station.z)
            val distance = vector.distance(sourceVector).toInt()
            val direction = getDirectionToObject(vector.clone().subtract(sourceVector).normalize())
            val height = vector.y.toInt()

            val color = when (colorSetting) {
                ContactsColoring.BY_DISTANCE.ordinal -> distanceColor(distance)
                ContactsColoring.BY_RELATION.ordinal -> stationRelationColor(player, station)
                else -> distanceColor(distance)
            }
            val prefixColor = when (colorSetting) {
                ContactsColoring.BY_DISTANCE.ordinal -> stationRelationColor(player, station)
                ContactsColoring.BY_RELATION.ordinal -> distanceColor(distance)
                else -> stationRelationColor(player, station)
            }

            contactsList.add(
                ContactsData(
                    name = text(station.name.take(maxLength), color),
                    type = ContactsType.STATION,
                    relation = stationRelationType(player, station),
                    prefix = constructPrefixTextComponent(STATION_ICON.text, prefixColor),
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

    private fun addCapturableStationContacts(
        capturableStations: List<CachedCapturableStation>,
        sourceVector: Vector,
        contactsList: MutableList<ContactsData>,
        player: Player
    ) {
        val maxLength = PlayerCache[player.uniqueId].contactsMaxNameLength
        val colorSetting = PlayerCache[player.uniqueId].contactsColoring

        for (station in capturableStations) {
            val vector = station.loc.toVector()
            val distance = vector.distance(sourceVector).toInt()
            val direction = getDirectionToObject(vector.clone().subtract(sourceVector).normalize())
            val height = vector.y.toInt()

            val color = when (colorSetting) {
                ContactsColoring.BY_DISTANCE.ordinal -> distanceColor(distance)
                ContactsColoring.BY_RELATION.ordinal -> capturableStationRelationColor(player, station)
                else -> distanceColor(distance)
            }
            val prefixColor = when (colorSetting) {
                ContactsColoring.BY_DISTANCE.ordinal -> capturableStationRelationColor(player, station)
                ContactsColoring.BY_RELATION.ordinal -> distanceColor(distance)
                else -> capturableStationRelationColor(player, station)
            }

            contactsList.add(
                ContactsData(
                    name = text(station.name.take(maxLength), color),
                    type = ContactsType.STATION,
                    relation = capturableStationRelationType(player, station),
                    prefix = constructPrefixTextComponent(SIEGE_STATION_ICON.text, prefixColor),
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

    private fun addBookmarkContacts(
        bookmarks: List<Bookmark>,
        sourceVector: Vector,
        contactsList: MutableList<ContactsData>,
        player: Player
    ) {
        val maxLength = PlayerCache[player.uniqueId].contactsMaxNameLength
        val colorSetting = PlayerCache[player.uniqueId].contactsColoring

        for (bookmark in bookmarks) {
            val vector = Vector(bookmark.x, bookmark.y, bookmark.z)
            val distance = vector.distance(sourceVector).toInt()
            val direction = getDirectionToObject(vector.clone().subtract(sourceVector).normalize())
            val height = vector.y.toInt()

            val color = when (colorSetting) {
                ContactsColoring.BY_DISTANCE.ordinal -> distanceColor(distance)
                ContactsColoring.BY_RELATION.ordinal -> GRAY
                else -> distanceColor(distance)
            }
            val prefixColor = when (colorSetting) {
                ContactsColoring.BY_DISTANCE.ordinal -> DARK_PURPLE
                ContactsColoring.BY_RELATION.ordinal -> distanceColor(distance)
                else -> DARK_PURPLE
            }

            contactsList.add(
                ContactsData(
                    name = text(bookmark.name.take(maxLength), color),
                    type = ContactsType.BOOKMARK,
                    relation = null,
                    prefix = constructPrefixTextComponent(BOOKMARK_ICON.text, prefixColor),
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

    private fun convertRelationType(relation: NationRelation.Level): ContactsRelation {
        return when (relation) {
            NationRelation.Level.ENEMY -> ContactsRelation.ENEMY
            NationRelation.Level.UNFRIENDLY -> ContactsRelation.UNFRIENDLY
            NationRelation.Level.NEUTRAL -> ContactsRelation.NEUTRAL
            NationRelation.Level.FRIENDLY -> ContactsRelation.FRIENDLY
            NationRelation.Level.ALLY -> ContactsRelation.ALLY
            NationRelation.Level.NATION -> ContactsRelation.NATION
            NationRelation.Level.NONE -> ContactsRelation.NONE
            else -> ContactsRelation.AI
        }
    }

    private fun sortContacts(contactsList: MutableList<ContactsData>, player: Player) {
        val sortOrder = ContactsSorting.entries[PlayerCache[player.uniqueId].contactsSort]

        when (sortOrder) {
            ContactsSorting.DISTANCE_ASCENDING -> contactsList.sortBy { it.distanceInt }

            ContactsSorting.DISTANCE_DESCENDING -> contactsList.sortByDescending { it.distanceInt }

            ContactsSorting.RELATION_ASCENDING -> contactsList.sortWith(Comparator<ContactsData> { o1, o2 ->
                // if both object relations are null, maintain order
                // if only object 1's relations are null, object 1 should appear after object 2
                if (o1.relation == null) if (o2.relation == null) return@Comparator 0 else return@Comparator 1

                // if only object 2's relations are null, object 1 should appear before object 2
                if (o2.relation == null) return@Comparator -1

                // then sort by relation
                return@Comparator o1.relation.compareTo(o2.relation)
            }
                // sort by distance as secondary sorting criteria (if negative, o1 appears before o2; vice versa)
                .thenComparing(Comparator<ContactsData> { o1, o2 -> return@Comparator o1.distanceInt.compareTo(o2.distanceInt) })
            )

            ContactsSorting.RELATION_DESCENDING -> contactsList.sortWith(Comparator<ContactsData> { o1, o2 ->
                // continue to move null relations to the end of the list
                if (o1.relation == null) {
                    if (o2.relation == null) return@Comparator 0 else return@Comparator 1
                }
                if (o2.relation == null) {
                    return@Comparator -1
                }
                return@Comparator o2.relation.compareTo(o1.relation)
            }
                // sort by distance
                .thenComparing(Comparator<ContactsData> { o1, o2 -> return@Comparator o1.distanceInt.compareTo(o2.distanceInt) })
            )

            ContactsSorting.TYPE_ASCENDING -> contactsList.sortWith(Comparator<ContactsData> { o1, o2 ->
                return@Comparator o1.type.compareTo(o2.type)
            }
                // sort by distance
                .thenComparing(Comparator<ContactsData> { o1, o2 -> return@Comparator o1.distanceInt.compareTo(o2.distanceInt) })
            )

            ContactsSorting.TYPE_DESCENDING -> contactsList.sortWith(Comparator<ContactsData> { o1, o2 ->
                return@Comparator o2.type.compareTo(o1.type)
            }
                // sort by distance
                .thenComparing(Comparator<ContactsData> { o1, o2 -> return@Comparator o1.distanceInt.compareTo(o2.distanceInt) })
            )
        }
    }

    data class ContactsData(
        val name: Component,
        val type: ContactsType,
        val relation: ContactsRelation?,
        val prefix: TextComponent,
        val suffix: TextComponent,
        val heading: TextComponent,
        val height: TextComponent,
        val distance: TextComponent,
        val distanceInt: Int,
        var padding: TextComponent
    )

    enum class ContactsType {
        STARSHIP,
        LAST_STARSHIP,
        PLANET,
        STAR,
        BEACON,
        STATION,
        BOOKMARK
    }

    enum class ContactsRelation {
        AI,
        NONE,
        ENEMY,
        UNFRIENDLY,
        NEUTRAL,
        FRIENDLY,
        ALLY,
        NATION
    }

    enum class ContactsSorting {
        DISTANCE_ASCENDING,
        DISTANCE_DESCENDING,
        TYPE_ASCENDING,
        TYPE_DESCENDING,
        RELATION_ASCENDING,
        RELATION_DESCENDING
    }

    enum class ContactsColoring {
        BY_DISTANCE,
        BY_RELATION
    }
}
