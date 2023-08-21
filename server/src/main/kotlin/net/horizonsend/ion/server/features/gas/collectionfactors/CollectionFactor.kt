package net.horizonsend.ion.server.features.gas.collectionfactors

import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.Locale

abstract class CollectionFactor {
    abstract fun factor(location: Location): Boolean

	abstract fun canBeFound(location: Location): Boolean

    companion object {
        fun valueOf(text: String): CollectionFactor {
            val params = text.split(":".toRegex()).dropLastWhile { it.isEmpty() }

            when (params[0].lowercase(Locale.getDefault())) {
                "atmosphereheight" -> return AtmosphereHeightFactor(params[1].toInt().toDouble(), params[2].toInt().toDouble())
                "distance" -> {
                    val locationParams = params[1].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    return DistanceFactor(
                        Location(
                            Bukkit.getWorld(locationParams[0]), locationParams[1].toInt()
                                .toDouble(), locationParams[2].toDouble(), locationParams[3].toDouble()
                        ), params[2].toInt().toDouble(), params[3].toFloat()
                    )
                }
                "random" -> return RandomFactor(params[1].toFloat())
                "hyperspaceonly" -> return HyperspaceOnlyFactor()
                "spaceonly" -> return SpaceOnlyFactor()
                "worldlimit" -> return WorldLimitFactor(params[1].split(",".toRegex()).dropLastWhile { it.isEmpty() })

                "worldchance" -> return WorldChanceFactor(
					params[1].split(",".toRegex()).dropLastWhile { it.isEmpty() }.associate {
						println(it)
						val pair = it.split("=".toRegex())

						pair[0] to pair[1].toDouble()
					}
				)

                "skylight", "outdoors" -> return OutdoorsFactor()
				"randomheight" -> return RandomByHeightFactor(params[1].toDouble(), params[2].toDouble(), params[3].toDouble(), params[4].toDouble())
            }

            return RandomFactor(1.0f)
        }
    }
}
