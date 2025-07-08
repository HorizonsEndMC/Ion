package net.horizonsend.ion.server.features.economy.misc

import net.horizonsend.ion.common.database.schema.economy.StationRentalZone
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.input.FutureInputResult
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.input.PotentiallyFutureResult
import net.horizonsend.ion.common.utils.miscellaneous.getDurationBreakdown
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.text.BACKGROUND_EXTENDER
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.colors.PIRATE_DARK_RED
import net.horizonsend.ion.common.utils.text.colors.PIRATE_SATURATED_RED
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.misc.ServerInboxes
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionNPCSpaceStation
import net.horizonsend.ion.server.features.nations.region.types.RegionRentalZone
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.gui.invui.economy.RentalZoneHomeMenu
import net.horizonsend.ion.server.gui.invui.misc.util.ConfirmationMenu
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.cube
import net.horizonsend.ion.server.miscellaneous.utils.depositMoney
import net.horizonsend.ion.server.miscellaneous.utils.front
import net.horizonsend.ion.server.miscellaneous.utils.hasEnoughMoney
import net.horizonsend.ion.server.miscellaneous.utils.runnable
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.withdrawMoney
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object StationRentalZones : IonServerComponent() {
	private val collectionDay get() = ConfigurationFiles.serverConfiguration().rentalZoneCollectionDay
	private val dayOfWeek get() = LocalDate.now(ZoneId.of("UTC")).dayOfWeek

	override fun onEnable() {
		Tasks.asyncAtHour(0) {
			if (collectionDay != dayOfWeek) return@asyncAtHour
			collectRents()
		}
	}

	private fun getFromSign(sign: Sign): RegionRentalZone? {
		val npcStation = Regions.findFirstOf<RegionNPCSpaceStation>(sign.location) ?: return null
		val inWorld = Regions.getAllOfInWorld<RegionRentalZone>(sign.world)
		return inWorld.filter { it.station == npcStation.id }.firstOrNull { it.signLocation == Vec3i(sign.location) }
	}

	fun getSign(zoneRegion: RegionRentalZone): Sign? {
		return zoneRegion.bukkitWorld?.getBlockState(zoneRegion.signLocation.x, zoneRegion.signLocation.y, zoneRegion.signLocation.z) as? Sign
	}

	private fun setupSign(zoneRegion: RegionRentalZone, sign: Sign) {
		val newLines = listOf(
			text(zoneRegion.name, NamedTextColor.AQUA),
			text(zoneRegion.owner?.let(SLPlayer::getName) ?: "[UNCLAIMED]", WHITE),
			template(text("Rent: {0}", NamedTextColor.BLACK), zoneRegion.rent.toCreditComponent()),
			text("Per Week.", NamedTextColor.BLACK)
		)

		Tasks.sync { with(sign.front()) { newLines.forEachIndexed(::line) }; sign.update() }
	}

	fun refreshSign(zoneRegion: RegionRentalZone) {
		Tasks.sync { getSign(zoneRegion)?.let { Tasks.async { setupSign(zoneRegion, it) } } }
	}

	@EventHandler
	fun onClickSign(event: PlayerInteractEvent) {
		val state = event.clickedBlock?.state as? Sign ?: return
		val zoneRegion = getFromSign(state) ?: return

		when (zoneRegion.owner) {
			null -> return openPurchaseMenu(zoneRegion, event.player)
			event.player.slPlayerId -> return openManagementMenu(zoneRegion, event.player)
		}

		Tasks.async {
			event.player.information("${zoneRegion.name} is owned by ${zoneRegion.owner?.let(SLPlayer::getName)}.")
		}
	}

	private fun openPurchaseMenu(region: RegionRentalZone, player: Player) {
		val text = GuiText("")
			.addBackground(GuiText.GuiBackground(BACKGROUND_EXTENDER, verticalShift = -17))
			.add(template(text("{0} is available for rent."), getMenuTitleName(text(region.name, WHITE)), getMenuTitleName(region.rent.toCreditComponent())), line = -3, alignment = GuiText.TextAlignment.CENTER)
			.add(template(text("{0} will be charged weekly."), getMenuTitleName(region.rent.toCreditComponent())), line = -2, alignment = GuiText.TextAlignment.CENTER)

		val fallbackLore = listOf(
			template(text("You will be required to pay {0} to", HE_MEDIUM_GRAY), region.rent.toCreditComponent()),
			text("cover the first weeks rent.", HE_MEDIUM_GRAY)
		)

		ConfirmationMenu.promptConfirmation(player, text, confirmationFallbackLore = { fallbackLore }) {
			val result = purchase(player, region)
			result.sendReason(player)
			result.withResult {
				Tasks.asyncDelay(5L) { openManagementMenu(region, player) }
			}
		}
	}

	private fun openManagementMenu(region: RegionRentalZone, player: Player) {
		if (region.owner != player.slPlayerId) return
		RentalZoneHomeMenu(player, region).openGui()
	}

	fun highlightBoundaries(audience: Player, zoneRegion: RegionRentalZone, duration: Duration) {
		val world = zoneRegion.bukkitWorld ?: return
		val points = cube(Vec3i(zoneRegion.minPoint).toLocation(world), Vec3i(zoneRegion.maxPoint).toLocation(world).add(1.0, 1.0, 1.0))
		val startTime = System.currentTimeMillis()

		runnable {
			if ((System.currentTimeMillis() - startTime) > duration.toMillis() || !audience.isOnline) {
				cancel()
				return@runnable
			}

			points.forEach { audience.spawnParticle(Particle.SOUL_FIRE_FLAME, it, 1, 0.0, 0.0, 0.0, 0.0) }

		}.runTaskTimerAsynchronously(IonServer, 10L, 10L)
	}

	@EventHandler
	fun onLogin(event: PlayerJoinEvent) {
		Tasks.async {
			val regions = Regions.getAllOf<RegionRentalZone>().filter { it.owner == event.player.slPlayerId }
			if (regions.isEmpty()) return@async

			val cantPayRent = regions.filterNot(::canPayRent)
			if (cantPayRent.isEmpty()) return@async

			for (region in cantPayRent.filter { it.rentBalance >= 0 }) {
				val warning = bracketed(text("WARNING", YELLOW))
				val message = template(
					text("{0}: Your rented zone {1} at {2} does not have enough balance to pay rent this week! It will be unclaimed if there is not enough to pay for a subsequent week.", HE_MEDIUM_GRAY),
					paramColor = PIRATE_SATURATED_RED,
					warning,
					region.name,
					region.getParentRegion().name
				)
				event.player.sendMessage(message)
			}

			// Super important
			val (days, hours, minutes, seconds) = getDurationBreakdown(getTimeUntilCollection().toMillis())

			for (region in cantPayRent.filter { it.rentBalance < 0 }) {
				val warning = bracketed(text("WARNING", PIRATE_DARK_RED))
				val message = template(
					text("{0}: Your rented zone {1} at {2} has a negative balance! It will be unclaimed in {3} days, {4} hours, {5} minutes, and {6} seconds.", HE_MEDIUM_GRAY),
					paramColor = PIRATE_SATURATED_RED,
					warning,
					region.name,
					region.getParentRegion().name,
					days, hours, minutes, seconds
				)
				event.player.sendMessage(message)
			}
		}
	}

	fun collectRents() {
		val all = Regions.getAllOf<RegionRentalZone>()

		for (zoneRegion in all.filterNot(::canPayRent)) {
			if (zoneRegion.rentBalance < 0) {
				val owner = zoneRegion.owner ?: continue

				ServerInboxes.sendServerMessage(owner, text("Failure to Pay Rent", RED), template(text("Your rental zone {0} at {1} was unclaimed due to unpaid rent!", RED), zoneRegion.name, zoneRegion.getParentRegion().name))

				StationRentalZone.removeOwner(zoneRegion.id)

				continue
			}
		}

		for (zoneRegion in all.filter { it.owner != null }) {
			collectRent(zoneRegion)
		}
	}

	private fun collectRent(zoneRegion: RegionRentalZone) {
		val owner = zoneRegion.owner ?: return
		val requiredAmount = getRequiredAmount(zoneRegion)

		val collectFromPlayerBalance = zoneRegion.collectRentFromOwnerBalance

		var fromRentalBalance = requiredAmount

		if (collectFromPlayerBalance && VAULT_ECO.getBalance(Bukkit.getOfflinePlayer(owner.uuid)) > requiredAmount - minOf(zoneRegion.rentBalance, requiredAmount)) {
			fromRentalBalance = minOf(zoneRegion.rentBalance, requiredAmount)
		}

		if (fromRentalBalance > 0) StationRentalZone.depositMoney(zoneRegion.id, -fromRentalBalance)

		zoneRegion.owner?.uuid?.let(Bukkit::getPlayer)?.alert("Your rental zone ${zoneRegion.name} at ${zoneRegion.getParentRegion().name} was charged ${requiredAmount.toCreditsString()} for rent!")

		if (!collectFromPlayerBalance) return

		Tasks.sync {
			val removeFromPlayer = requiredAmount - fromRentalBalance
			VAULT_ECO.withdrawPlayer(Bukkit.getOfflinePlayer(owner.uuid), removeFromPlayer)
		}
	}

	private fun getRequiredAmount(region: RegionRentalZone): Double {
		var charged = region.rent
		if (region.rentBalance < 0) charged -= region.rentBalance

		return charged
	}

	fun canPayRent(region: RegionRentalZone): Boolean {
		val owner = region.owner ?: return true

		val fromBalance = region.collectRentFromOwnerBalance

		if (region.rentBalance < region.rent) {
			return fromBalance && VAULT_ECO.getBalance(Bukkit.getOfflinePlayer(owner.uuid)) >= getRequiredAmount(region)
		}

		return true
	}

	fun getTimeUntilCollection(): Duration {
		val now = ZonedDateTime.now(ZoneId.of("UTC"))
		val separation = (collectionDay.value - dayOfWeek.value).toLong()
		var nextCollection = ZonedDateTime.now(ZoneId.of("UTC")).withHour(0).withMinute(0).withSecond(0).plusDays(separation)

		if (nextCollection.isBefore(now)) {
			nextCollection = nextCollection.plusDays(7)
		}

		val timeUntil = nextCollection.toEpochSecond() - now.toEpochSecond()

		return Duration.ofMillis(TimeUnit.SECONDS.toMillis(timeUntil))
	}

	fun abandon(player: Player, zoneRegion: RegionRentalZone): PotentiallyFutureResult {
		if (player.slPlayerId != zoneRegion.owner) return InputResult.FailureReason(listOf(text("You don't own that zone!", RED)))
		val future = FutureInputResult()

		Tasks.async {
			StationRentalZone.removeOwner(zoneRegion.id)

			val messages = mutableListOf(template(text("You gave up ownership of {0} at {1}.", HE_MEDIUM_GRAY), zoneRegion.name, zoneRegion.getParentRegion().name))

			if (zoneRegion.rentBalance > 0) messages.add(template(text("{0} of the remaining rent balance has been added to your accont.", GREEN), zoneRegion.rentBalance.toCreditComponent()))

			Tasks.sync {
				if (zoneRegion.rentBalance > 0) player.depositMoney(zoneRegion.rentBalance)
			}

			future.complete(InputResult.SuccessReason(messages))
		}

		return future
	}

	fun purchase(player: Player, zoneRegion: RegionRentalZone): PotentiallyFutureResult {
		if (player.slPlayerId == zoneRegion.owner) return InputResult.FailureReason(listOf(text("You already own that zone!", RED)))
		if (zoneRegion.owner != null) return InputResult.FailureReason(listOf(text("Someone else already owns that zone!", RED)))

		val future = FutureInputResult()

		Tasks.sync {
			if (!player.hasEnoughMoney(zoneRegion.rent)) {
				future.complete(InputResult.FailureReason(listOf(text("You can't afford that!", RED))))
				return@sync
			}

			player.withdrawMoney(zoneRegion.rent)

			Tasks.async {
				StationRentalZone.claim(zoneRegion.id, player.slPlayerId)
				future.complete(InputResult.InputSuccess)
			}
		}

		return future
	}

	fun depositBalance(player: Player, zoneRegion: RegionRentalZone, depositAmount: Double): FutureInputResult {
		val future = FutureInputResult()

		Tasks.sync {
			if (!player.hasEnoughMoney(depositAmount)) {
				future.complete(InputResult.FailureReason(listOf(text("You don't have enough money for that!", RED))))
			}

			player.withdrawMoney(depositAmount)
			Tasks.async {
				StationRentalZone.depositMoney(zoneRegion.id, depositAmount)
			}

			future.complete(InputResult.SuccessReason(listOf(template(text("You deposited {0} to the rent balance.", GREEN), depositAmount.toCreditComponent()))))
		}

		return future
	}

	fun transferOwnership(player: Player, zoneRegion: RegionRentalZone, newOwner: SLPlayerId): PotentiallyFutureResult {
		val result = FutureInputResult()

		Tasks.async {
			val name = SLPlayer.getName(newOwner)
			if (name == null) {
				result.complete(InputResult.FailureReason(listOf(text("Player not found!", RED))))
				return@async
			}

			ConfirmationMenu.promptConfirmation(player, GuiText("Confirm transfer to $name?")) {
				StationRentalZone.transferOwnership(zoneRegion.id, newOwner)
				Tasks.sync {
					player.closeInventory()
				}
			}
		}

		return result
	}
}
