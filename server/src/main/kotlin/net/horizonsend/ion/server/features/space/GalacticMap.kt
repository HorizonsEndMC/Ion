package net.horizonsend.ion.server.features.space

import net.horizonsend.ion.common.database.schema.nations.DominionTerritory
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.utils.text.createHtmlLink
import net.horizonsend.ion.common.utils.text.wrapStyle
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.nations.NationsMap
import net.horizonsend.ion.server.features.world.SpaceRegion
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.Color
import org.dynmap.bukkit.DynmapPlugin
import org.dynmap.markers.Marker
import org.dynmap.markers.MarkerAPI

object GalacticMap : IonServerComponent(true) {
    private fun syncOnly(block: () -> Unit) = when {
        Bukkit.isPrimaryThread() -> block()
        else -> Tasks.sync(block)
    }

    private val markerAPI: MarkerAPI get() = DynmapPlugin.plugin.markerAPI
    private var galacticMapWorldExists = false
    private const val GALACTIC_MAP_WORLD = "GalacticMap"
    val galacticMarkerSet get() = markerAPI.getMarkerSet("galactic")
        ?: markerAPI.createMarkerSet("galactic", "Galactic Map", null, false)

	override fun onEnable() {
        if (!NationsMap.dynmapLoaded) {
            log.warn("Dynmap not enabled! Galactic map will not be enabled.")
            return
        }

        galacticMapWorldExists = Bukkit.getWorld(GALACTIC_MAP_WORLD) != null
        if (!galacticMapWorldExists) {
            log.warn("Galactic map world does not exist! Galactic map will not be enabled.")
            return
        }

        for (beacon in ConfigurationFiles.serverConfiguration().beacons) {
            paths.add(setOf(beacon.spaceLocation.world, beacon.destination.world))
        }
        println("Current paths: $paths")
	}

    fun addGalacticIcons() {
        if (!NationsMap.dynmapLoaded) return
        if (!galacticMapWorldExists) return

        systems.forEach(::addGalacticIcon)

        for (path in paths) {
            val sourceSystem = systems.firstOrNull { it.name == path.elementAtOrNull(0) } ?: continue
            val destinationSystem = systems.firstOrNull { it.name == path.elementAtOrNull(1) } ?: continue

            galacticMarkerSet.createPolyLineMarker(
                galacticMarkerPrefix(path.joinToString { it }),
                "${sourceSystem.name} -> ${destinationSystem.name}",
                false,
                GALACTIC_MAP_WORLD,
                doubleArrayOf(sourceSystem.galacticX.toDouble(), destinationSystem.galacticX.toDouble()),
                doubleArrayOf(192.0, 192.0),
                doubleArrayOf(sourceSystem.galacticZ.toDouble(), destinationSystem.galacticZ.toDouble()),
                false
            ).setLineStyle(3, 1.0, Color.PURPLE.asRGB())
        }
    }

    fun updateGalacticIcons() {
        if (!NationsMap.dynmapLoaded) return
        if (!galacticMapWorldExists) return

        systems.forEach(::updateGalacticIcon)
    }

    fun addGalacticIcon(system: System) = syncOnly {
        if (!NationsMap.dynmapLoaded) return@syncOnly
        if (!galacticMapWorldExists) return@syncOnly

        try {
            removeGalacticIcon(system)

            galacticMarkerSet.createMarker(
                galacticMarkerPrefix(system.name),
                system.name,
                true, // use HTML markup
                GALACTIC_MAP_WORLD,
                system.galacticX.toDouble(),
                192.0,
                system.galacticZ.toDouble(),
                markerAPI.getMarkerIcon(galacticMarkerIcon(system.type)),
                false
            )

            updateGalacticIcon(system)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeGalacticIcon(system: System) = syncOnly {
        if (!NationsMap.dynmapLoaded) return@syncOnly
        if (!galacticMapWorldExists) return@syncOnly

        galacticMarkerSet.findMarker(galacticMarkerPrefix(system.name))?.deleteMarker()
    }

    fun updateGalacticIcon(system: System): Unit = syncOnly {
        if (!NationsMap.dynmapLoaded) return@syncOnly
        if (!galacticMapWorldExists) return@syncOnly

        galacticMarkerSet.findCircleMarker(galacticMarkerPrefix(system.name + "_area"))?.deleteMarker()
        val marker: Marker? = galacticMarkerSet.findMarker(galacticMarkerPrefix(system.name))

        val serverName = ConfigurationFiles.serverConfiguration().serverName
        val link = "https://$serverName.horizonsend.net/?worldname=${system.name}"

        if (marker == null) {
            log.warn("No marker for galactic system ${system.name}")
            addGalacticIcon(system)
            return@syncOnly
        }

        when (system.type) {
            SystemType.CORE -> {
                marker.setLabel(
                    wrapStyle(createHtmlLink(system.name, link, "#FFFFFF"), "h3", "font-size:30") +
                            "\n<p style=\"padding-top: 0;\">${system.region.name}</p>" +
                            "\n<p style=\"padding-top: 0;\">Core System</p>",
                    true
                )

                val circleMarker = galacticMarkerSet.createCircleMarker(
                    galacticMarkerPrefix(system.name + "_area"),
                    system.name + " Core System",
                    false,
                    GALACTIC_MAP_WORLD,
                    system.galacticX.toDouble(),
                    192.0,
                    system.galacticZ.toDouble(),
                    200.0,
                    200.0,
                    false
                )
                circleMarker.setLineStyle(5, 0.5, Color.WHITE.asRGB())
                circleMarker.setFillStyle(0.2, Color.WHITE.asRGB())
                return@syncOnly
            }
            SystemType.UNCLAIMABLE -> {
                marker.setLabel(
                    wrapStyle(createHtmlLink(system.name, link, "#FFFFFF"), "h3", "font-size:30") +
                            "\n<p style=\"padding-top: 0;\">${system.region.name}</p>" +
                            "\n<p style=\"padding-top: 0;\">Non-claimable System</p>",
                    true
                )
                return@syncOnly
            }
            SystemType.MINING -> {
                marker.setLabel(
                    wrapStyle(createHtmlLink(system.name, link, "#FFFFFF"), "h3", "font-size:30") +
                            "\n<p style=\"padding-top: 0;\">${system.region.name}</p>" +
                            "\n<p style=\"padding-top: 0;\">Mining System</p>",
                    true
                )
                return@syncOnly
            }
            SystemType.TRADE -> {
                marker.setLabel(
                    wrapStyle(createHtmlLink(system.name, link, "#FFFFFF"), "h3", "font-size:30") +
                            "\n<p style=\"padding-top: 0;\">${system.region.name}</p>" +
                            "\n<p style=\"padding-top: 0;\">Trade System</p>",
                    true
                )
                return@syncOnly
            }
            else -> { /* Fall through this if this is claimable */ }
        }

        val dominionTerritory = DominionTerritory.findByWorld(system.name)
        if (dominionTerritory == null) {
            marker.setLabel(
                wrapStyle(createHtmlLink(system.name, link, "#FFFFFF"), "h3", "font-size:30") +
                        "\n<p style=\"padding-top: 0;\">${system.region.name}</p>" +
                        "\n<p style=\"padding-top: 0;\">Claimable System; could not get info!</p>",
                true
            )
            return@syncOnly
        }

        val nation = dominionTerritory.nation?.let(Nation.Companion::findById)
        if (nation == null) {
            marker.setLabel(
                wrapStyle(createHtmlLink(system.name, link, "#FFFFFF"), "h3", "font-size:30") +
                        "\n<p style=\"padding-top: 0;\">${system.region.name}</p>" +
                        "\n<p style=\"padding-top: 0;\">Claimable System</p>" +
                        "\n<p style=\"padding-top: 0;\">Unclaimed</p>",
                true
            )
            return@syncOnly
        }

        val circleMarker = galacticMarkerSet.createCircleMarker(
            galacticMarkerPrefix(system.name + "_area"),
            system.name + " owned by ${nation.name}",
            false,
            GALACTIC_MAP_WORLD,
            system.galacticX.toDouble(),
            192.0,
            system.galacticZ.toDouble(),
            100.0,
            100.0,
            false
        )
        circleMarker.setLineStyle(3, 0.5, nation.color)
        circleMarker.setFillStyle(0.2, nation.color)

        marker.setLabel(
            wrapStyle(createHtmlLink(system.name, link, "#FFFFFF"), "h3", "font-size:30") +
                    "\n<p style=\"padding-top: 0;\">${system.region.name}</p>" +
                    "\n<p style=\"padding-top: 0;\">Claimable System</p>" +
                    "\n<p style=\"padding-top: 0;\">Claimed by ${nation.name}</p>",
            true
        )
    }

    fun updateGalacticIconByName(name: String) {
        val system = systems.firstOrNull { it.name == name } ?: return
        updateGalacticIcon(system)
    }

    private fun galacticMarkerPrefix(string: String) = "galactic_$string"

    private fun galacticMarkerIcon(systemType: SystemType) = when (systemType) {
        SystemType.CORE -> "star"
        SystemType.UNCLAIMABLE -> "exclamation"
        SystemType.CLAIMABLE -> "sun"
        SystemType.MINING -> "minecart"
        SystemType.TRADE -> "coins"
    }

    private val systems = listOf<System>(
        System("Asteri", 500, 500, SpaceRegion.WARD, SystemType.CORE),
        System("Venture", 500, 1000, SpaceRegion.WARD, SystemType.UNCLAIMABLE),
        System("Reserve", 1000, 1000, SpaceRegion.WARD, SystemType.UNCLAIMABLE),
        System("Traverse", 1000, 500, SpaceRegion.WARD, SystemType.UNCLAIMABLE),

        System("Trench", 1750, 750, SpaceRegion.BREACH, SystemType.MINING),
        System("Horizon", 1750, 250, SpaceRegion.BREACH, SystemType.UNCLAIMABLE),
        System("D-1LA", 2250, 250, SpaceRegion.BREACH, SystemType.UNCLAIMABLE),

        System("Regulus", 750, 2000, SpaceRegion.FRACTURE, SystemType.CORE),
        System("Meridian", 250, 2000, SpaceRegion.FRACTURE, SystemType.UNCLAIMABLE),
        System("LOA-7", 250, 2500, SpaceRegion.FRACTURE, SystemType.CLAIMABLE),
        System("BQ-5A", 750, 2500, SpaceRegion.FRACTURE, SystemType.CLAIMABLE),
        System("TT-91", 1250, 2500, SpaceRegion.FRACTURE, SystemType.CLAIMABLE),
        System("Fault", 1250, 3000, SpaceRegion.FRACTURE, SystemType.MINING),
        System("QIM-8", 750, 3000, SpaceRegion.FRACTURE, SystemType.CLAIMABLE),
        System("CXK-3", 250, 3000, SpaceRegion.FRACTURE, SystemType.CLAIMABLE),
        System("F3L-I", 250, 3500, SpaceRegion.FRACTURE, SystemType.CLAIMABLE),
        System("KRY-2", 750, 3500, SpaceRegion.FRACTURE, SystemType.CLAIMABLE),
        System("Sunder", 1250, 3500, SpaceRegion.FRACTURE, SystemType.TRADE),

        System("Sirius", 2000, 1750, SpaceRegion.SPINE, SystemType.CORE),
        System("URT-8", 3000, 1750, SpaceRegion.SPINE, SystemType.CLAIMABLE),
        System("Vertigo", 2500, 1750, SpaceRegion.SPINE, SystemType.MINING),
        System("TNS-44", 2500, 2250, SpaceRegion.SPINE, SystemType.CLAIMABLE),
        System("HL-81", 2500, 2750, SpaceRegion.SPINE, SystemType.CLAIMABLE),
        System("AXA-2", 2500, 3250, SpaceRegion.SPINE, SystemType.CLAIMABLE),
        System("KTR-18", 2500, 3750, SpaceRegion.SPINE, SystemType.CLAIMABLE),
        System("Anchor", 2500, 4250, SpaceRegion.SPINE, SystemType.MINING),
        System("LM-77", 3000, 2250, SpaceRegion.SPINE, SystemType.CLAIMABLE),
        System("Axis", 3000, 2750, SpaceRegion.SPINE, SystemType.TRADE),
        System("JCT-3", 3000, 3250, SpaceRegion.SPINE, SystemType.CLAIMABLE),
        System("PRM-16", 3000, 3750, SpaceRegion.SPINE, SystemType.CLAIMABLE),
        System("STR-29", 2500, 4750, SpaceRegion.SPINE, SystemType.CLAIMABLE),

        System("Ilios", 3250, 1000, SpaceRegion.MONOLITH, SystemType.CORE),
        System("XN-81", 3250, 500, SpaceRegion.MONOLITH, SystemType.CLAIMABLE),
        System("IX-Q3", 3750, 500, SpaceRegion.MONOLITH, SystemType.CLAIMABLE),
        System("VXM-11", 4250, 500, SpaceRegion.MONOLITH, SystemType.CLAIMABLE),
        System("DN-4V", 4250, 1000, SpaceRegion.MONOLITH, SystemType.CLAIMABLE),
        System("Conduit", 4250, 1500, SpaceRegion.MONOLITH, SystemType.TRADE),
        System("GRX-5", 4750, 1500, SpaceRegion.MONOLITH, SystemType.CLAIMABLE),
        System("PDN-2", 4750, 2000, SpaceRegion.MONOLITH, SystemType.CLAIMABLE),
        System("Reliquary", 4250, 2000, SpaceRegion.MONOLITH, SystemType.MINING),
        System("OQ-04", 4250, 3000, SpaceRegion.MONOLITH, SystemType.CLAIMABLE),
        System("XRW-9", 4250, 2500, SpaceRegion.MONOLITH, SystemType.CLAIMABLE),
        System("NTH-3", 3750, 2000, SpaceRegion.MONOLITH, SystemType.CLAIMABLE),
        System("TH-89", 3750, 2500, SpaceRegion.MONOLITH, SystemType.CLAIMABLE),
    )

    private val paths = mutableSetOf<Set<String>>()

    data class System(
        val name: String,
        val galacticX: Int,
        val galacticZ: Int,
        val region: SpaceRegion,
        val type: SystemType,
    )

    enum class SystemType {
        CORE,
        UNCLAIMABLE,
        CLAIMABLE,
        MINING,
        TRADE,
    }
}