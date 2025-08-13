package net.horizonsend.ion.server.miscellaneous

import net.horizonsend.ion.server.configuration.starship.StarshipSounds.SoundInfo
import net.horizonsend.ion.server.features.nations.utils.toPlayersInRadius
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.min

fun nearSoundVolumeMod(distance: Double, cutoff: Double): Float {
    val normalized = distance / cutoff
    return when (distance.toInt()) {
        in 0 until (cutoff * 0.5).toInt() -> 1f
        // -0.666667x + 1.33333
        in (cutoff * 0.5).toInt() until (cutoff * 2).toInt() -> ((-0.666667 * normalized) + 1.33333).toFloat()
        else -> 0f
    }
}

fun farSoundVolumeMod(distance: Double, cutoff: Double): Float {
    val normalized = distance / cutoff
    return when (distance.toInt()) {
        in 0 until (cutoff * 2).toInt() -> 1f
        // -0.0555556x + 1.11111
        in (cutoff * 2).toInt() until (cutoff * 20).toInt() -> ((-0.0555556 * normalized) + 1.11111).toFloat()
        else -> 0f
    }
}

fun playSoundInRadius(origin: Location, radius: Double, sound: Sound) {
    toPlayersInRadius(origin, radius) { player ->
        player.playSound(sound)
    }
}

fun playDirectionalStarshipSound(origin: Location, player: Player, nearSound: SoundInfo?, farSound: SoundInfo?, cutoff: Double) {
    val distance = player.location.distance(origin)
    val dir = Vector(origin.x - player.location.x, origin.y - player.location.y, origin.z - player.location.z)
    val offsetDistance = min(distance, 16.0)
    val soundLoc = player.location.add(dir.normalize().multiply(offsetDistance))

    nearSound?.let { player.playSound(
        Sound.sound(
            Key.key(nearSound.key),
            nearSound.source,
            nearSound.volume * nearSoundVolumeMod(distance, cutoff),
            nearSound.pitch
        ),
        soundLoc.x,
        soundLoc.y,
        soundLoc.z,
    ) }

    farSound?.let { player.playSound(
        Sound.sound(
            Key.key(farSound.key),
            farSound.source,
            farSound.volume * farSoundVolumeMod(distance, cutoff),
            farSound.pitch
        ),
        soundLoc.x,
        soundLoc.y,
        soundLoc.z,
    ) }
}
