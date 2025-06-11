package net.horizonsend.ion.server.features.economy.misc

import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.miscellaneous.getDurationBreakdown
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionNPCSpaceStation
import net.horizonsend.ion.server.features.nations.region.types.RegionRentalArea
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.Date
import java.util.concurrent.TimeUnit

object StationRentalAreas : IonServerComponent() {
	private val collectionDay get() = ConfigurationFiles.serverConfiguration().rentalAreaCollectionDay
	private val dayOfWeek get() = LocalDate.now().dayOfWeek

	override fun onEnable() {
		Tasks.asyncAtHour(0) {
			if (collectionDay != dayOfWeek) return@asyncAtHour
			collectRents()
		}
	}

	fun collectRents() {
		val duration = getTimeUntilCollection()
		Notify.chatAndGlobal(Component.text(getDurationBreakdown(duration.toMillis())))
	}

	fun getTimeUntilCollection(): Duration {
		val separation = collectionDay.value - dayOfWeek.value

		val nextCollection = ZonedDateTime.now().withHour(0).withMinute(0).withSecond(0).toInstant().epochSecond + TimeUnit.DAYS.toSeconds(separation.toLong())
		return Duration.ofMillis(TimeUnit.SECONDS.toMillis(nextCollection - Instant.now().epochSecond))
	}

	@EventHandler
	fun onClickSign(event: PlayerInteractEvent) {
		val state = event.clickedBlock?.state as? Sign ?: return
		val rentalArea = getFromSign(state) ?: return

		event.player.information("Station: ${rentalArea.station}")
		event.player.information("Rental area: ${rentalArea.name}")
		event.player.information("Sign Location: ${rentalArea.signLocation}")
		event.player.information("Min Point: ${rentalArea.minPoint}")
		event.player.information("Max Point: ${rentalArea.maxPoint}")
		event.player.information("Rent: ${rentalArea.rent}")
		event.player.information("Owner: ${rentalArea.owner?.let(SLPlayer::getName)}")
		event.player.information("Rent Balance: ${rentalArea.rentBalance}")
		event.player.information("Rent last charged: ${Date(rentalArea.rentLastCharged)}")
	}

	fun getFromSign(sign: Sign): RegionRentalArea? {
		val npcStation = Regions.findFirstOf<RegionNPCSpaceStation>(sign.location) ?: return null
		val inWorld = Regions.getAllOfInWorld<RegionRentalArea>(sign.world)
		return inWorld.filter { it.station == npcStation.id }.firstOrNull { it.signLocation == Vec3i(sign.location) }
	}
}
