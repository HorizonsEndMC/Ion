package net.starlegacy.feature.starship.subsystem.reactor

import com.destroystokyo.paper.Title
import net.starlegacy.util.colorize
import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

class HeavyWeaponBooster(val subsystem: ReactorSubsystem) {
    val output: Double = subsystem.output
    private var warmup: Long = 0
    private var type: String? = null
    private var lastHeavyWeaponBoost: Long = -1

    fun boost(newHeavyWeaponType: String?, newWarmup: Long): Double {
        if (this.type != newHeavyWeaponType) {
            handleNewType(newHeavyWeaponType, newWarmup)
            return 0.0
        }

        if (!isAvailable()) {
            return 0.0
        }

        // update warmup AFTER the duration has been confirmed to have passed
        // because that way, the shortened warmup from using a fraction of the
        // heavy lasers is used.
        resetWarmup(newWarmup)
        return output
    }

    private fun handleNewType(newHeavyWeaponType: String?, newWarmup: Long) {
        this.type = newHeavyWeaponType
        sendMessage("&e&oHeavy weapon boost requested, charging...")
        // reset warmup when charging new type
        resetWarmup(newWarmup)
    }

    private fun isAvailable(): Boolean {
        return System.nanoTime() - this.lastHeavyWeaponBoost >= this.warmup
    }

    private fun resetWarmup(newWarmup: Long) {
        this.warmup = newWarmup
        this.lastHeavyWeaponBoost = System.nanoTime()
        scheduleAvailabilityMessage()
    }

    private fun scheduleAvailabilityMessage() {
        val delay = TimeUnit.NANOSECONDS.toMillis(this.warmup)

        Timer().schedule(delay) {
            if (!isAvailable()) {
                return@schedule
            }

            sendMessage("&a&oHeavy weapon boost available.")
        }
    }

    private fun sendMessage(message: String) {
        subsystem.starship.sendTitle(
            Title.builder()
                .fadeIn(1)
                .stay(8)
                .fadeOut(1)
                .title("")
                .subtitle(message.colorize())
                .build()
        )
    }

    fun reduceWarmup(remainingPower: Double) {
        // make it so e.g. using only 2/3 of the heavy weapon boost output results in 2/3 the warmup
        val consumedFraction = (output - remainingPower) / output
        warmup = (warmup * consumedFraction).toLong()
    }
}
