package net.horizonsend.ion.server.features.gas.collectionfactors

import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.Locale

abstract class CollectionFactor {
    abstract fun factor(location: Location): Boolean

    companion object {
        fun collectionSetFromString(string: String): List<CollectionFactor> {
            val collectionFactors: MutableList<CollectionFactor> = ArrayList()
            for (text in string.split(";".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()) collectionFactors.add(
                valueOf(text)
            )
            return collectionFactors
        }

        fun valueOf(text: String): CollectionFactor {
            val params = text.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            when (params[0].lowercase(Locale.getDefault())) {
                "atmosphereheight" -> return AtmosphereHeightFactor(
                    params[1].toInt().toDouble(), params[2].toInt().toDouble()
                )

                "distance" -> {
                    val locationParams = params[1].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    return DistanceFactor(
                        Location(
                            Bukkit.getWorld(locationParams[0]), locationParams[1].toInt()
                                .toDouble(), locationParams[2].toInt().toDouble(), locationParams[3].toInt().toDouble()
                        ), params[2].toInt().toDouble(), params[3].toFloat()
                    )
                }

                "random" -> return RandomFactor(params[1].toFloat())
                "hyperspaceonly" -> return HyperspaceOnlyFactor()
                "spaceonly" -> return SpaceOnlyFactor()
                "worldlimit" -> return WorldLimitFactor(params[1].split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray())

                "worldchance" -> return WorldChanceFactor(params[1].toFloat(), params[2])
                "skylight", "outdoors" -> return OutdoorsFactor()
            }
            return RandomFactor(1.0f)
        }
    }
}
