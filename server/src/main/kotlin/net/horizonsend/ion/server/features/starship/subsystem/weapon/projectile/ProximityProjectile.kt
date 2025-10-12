package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import org.bukkit.Location
import kotlin.math.pow

interface ProximityProjectile {
    val proximityRange: Double

    fun getStarshipsInProximity(loc: Location): List<Starship> {
        return ActiveStarships.all().filter { starship -> starship.world == loc.world &&
                starship.centerOfMass.toVector().distanceSquared(loc.toVector()) <= proximityRange.pow(2.0)
        }
    }
}