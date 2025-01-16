package net.horizonsend.ion.server.features.multiblock.entity.type

import net.horizonsend.ion.common.utils.miscellaneous.roundToTenThousanth
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.LAST_PROGRESS_TICK
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.PROGRESS
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
import org.bukkit.persistence.PersistentDataType.DOUBLE
import org.bukkit.persistence.PersistentDataType.LONG
import java.text.DecimalFormat
import java.time.Duration

interface ProgressMultiblock {
	val progressManager: ProgressManager
	fun tickProgress(totalDuration: Duration): Boolean {
		return progressManager.addProgress(totalDuration)
	}

	class ProgressManager(data: PersistentMultiblockData) {
		var lastProgressTick = System.currentTimeMillis()
		private var currentProgress: Double = data.getAdditionalDataOrDefault(PROGRESS, DOUBLE, 0.0)

		fun getCurrentProgress(): Double = currentProgress

		/**
		 * Increments progress with a percentage of a total duration.
		 * Returns whether the progress has reached 100%
		 **/
		fun addProgress(totalDuration: Duration): Boolean {
			val now = System.currentTimeMillis()
			val additionalPercent = calculatePercentageGain(now, totalDuration)
			lastProgressTick = now

			currentProgress += additionalPercent

			return isComplete()
		}

		fun wouldComplete(totalDuration: Duration): Boolean {
			val additionalPercent = calculatePercentageGain(System.currentTimeMillis(), totalDuration)

			return (currentProgress + additionalPercent) >= totalDuration.toMillis()
		}

		fun isComplete(): Boolean {
			return currentProgress > 1.0
		}

		fun calculatePercentageGain(now: Long, totalDuration: Duration): Double {
			val deltaMillis = now - lastProgressTick
			val delta = Duration.ofMillis(deltaMillis)

			return delta.toMillis().toDouble() / totalDuration.toMillis().toDouble()
		}

		fun reset() {
			currentProgress = 0.0
		}

		fun saveProgressData(store: PersistentMultiblockData) {
			store.addAdditionalData(PROGRESS, DOUBLE, currentProgress)
			store.addAdditionalData(LAST_PROGRESS_TICK, LONG, lastProgressTick)
		}

		fun formatProgress(color: TextColor): Component {
			val percent = DecimalFormat("##.##").format(getCurrentProgress().roundToTenThousanth() * 100.0)
			return ofChildren(text(percent, color), text('%', HEColorScheme.HE_LIGHT_GRAY))
		}
	}
}
