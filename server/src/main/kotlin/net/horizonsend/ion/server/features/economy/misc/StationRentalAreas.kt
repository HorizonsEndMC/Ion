package net.horizonsend.ion.server.features.economy.misc

import net.horizonsend.ion.common.database.schema.economy.StationRentalArea
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.input.FutureInputResult
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.input.PotentiallyFutureResult
import net.horizonsend.ion.common.utils.miscellaneous.getDurationBreakdownString
import net.horizonsend.ion.common.utils.text.BACKGROUND_EXTENDER
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionNPCSpaceStation
import net.horizonsend.ion.server.features.nations.region.types.RegionRentalArea
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.gui.invui.economy.RentalAreaHomeMenu
import net.horizonsend.ion.server.gui.invui.misc.util.ConfirmationMenu
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
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
import org.bukkit.Particle
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
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

	@EventHandler
	fun onClickSign(event: PlayerInteractEvent) {
		val state = event.clickedBlock?.state as? Sign ?: return
		val rentalArea = getFromSign(state) ?: return

		when (rentalArea.owner) {
			null -> return openPurchaseMenu(rentalArea, event.player)
			event.player.slPlayerId -> return openManagementMenu(rentalArea, event.player)
		}

		Tasks.async {
			event.player.information("${rentalArea.name} is owned by ${rentalArea.owner?.let(SLPlayer::getName)}.")
		}
	}

	private fun openPurchaseMenu(region: RegionRentalArea, player: Player) {
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

	private fun openManagementMenu(region: RegionRentalArea, player: Player) {
		if (region.owner != player.slPlayerId) return
		RentalAreaHomeMenu(player, region).openGui()
	}

	fun getFromSign(sign: Sign): RegionRentalArea? {
		val npcStation = Regions.findFirstOf<RegionNPCSpaceStation>(sign.location) ?: return null
		val inWorld = Regions.getAllOfInWorld<RegionRentalArea>(sign.world)
		return inWorld.filter { it.station == npcStation.id }.firstOrNull { it.signLocation == Vec3i(sign.location) }
	}

	fun highlightBoundaries(audience: Player, rentalArea: RegionRentalArea, duration: Duration) {
		val world = rentalArea.bukkitWorld ?: return
		val points = cube(Vec3i(rentalArea.minPoint).toLocation(world), Vec3i(rentalArea.maxPoint).toLocation(world).add(1.0, 1.0, 1.0))
		val startTime = System.currentTimeMillis()

		runnable {
			if ((System.currentTimeMillis() - startTime) > duration.toMillis() || !audience.isOnline) {
				cancel()
				return@runnable
			}

			points.forEach { audience.spawnParticle(Particle.SOUL_FIRE_FLAME, it, 1, 0.0, 0.0, 0.0, 0.0) }

		}.runTaskTimerAsynchronously(IonServer, 10L, 10L)
	}

	fun getSign(area: RegionRentalArea): Sign? {
		return area.bukkitWorld?.getBlockState(area.signLocation.x, area.signLocation.y, area.signLocation.z) as? Sign
	}

	private fun setupSign(area: RegionRentalArea, sign: Sign) {
		val newLines = listOf(
			text(area.name, NamedTextColor.AQUA),
			text(area.owner?.let(SLPlayer::getName) ?: "[UNCLAIMED]", NamedTextColor.WHITE),
			template(text("Rent: {0}", NamedTextColor.BLACK), area.rent.toCreditComponent()),
			text("Per Week.", NamedTextColor.BLACK)
		)

		Tasks.sync { with(sign.front()) { newLines.forEachIndexed(::line) }; sign.update() }
	}

	fun refreshSign(area: RegionRentalArea) {
		Tasks.sync { getSign(area)?.let { Tasks.async { setupSign(area, it) } } }
	}

	fun collectRents() {
		val duration = getTimeUntilCollection()
		Notify.chatAndGlobal(text(getDurationBreakdownString(duration.toMillis())))
	}

	fun getTimeUntilCollection(): Duration {
		val separation = collectionDay.value - dayOfWeek.value

		val nextCollection = ZonedDateTime.now().withHour(0).withMinute(0).withSecond(0).toInstant().epochSecond + TimeUnit.DAYS.toSeconds(separation.toLong())
		return Duration.ofMillis(TimeUnit.SECONDS.toMillis(nextCollection - Instant.now().epochSecond))
	}

	fun abandon(player: Player, area: RegionRentalArea): PotentiallyFutureResult {
		if (player.slPlayerId != area.owner) return InputResult.FailureReason(listOf(text("You don't own that area!", RED)))
		val future = FutureInputResult()

		Tasks.async {
			StationRentalArea.removeOwner(area.id)
			Tasks.sync {
				player.depositMoney(area.rentBalance)
			}

			future.complete(InputResult.SuccessReason(listOf(
				template(text("You gave up ownership of {0} at {1}.", HE_MEDIUM_GRAY), area.name, area.getParentRegion().name),
				template(text("{0} of the remaining rent balance has been added to your accont.", GREEN), area.rentBalance.toCreditComponent()),
			)))
		}

		return future
	}

	fun purchase(player: Player, area: RegionRentalArea): PotentiallyFutureResult {
		if (player.slPlayerId == area.owner) return InputResult.FailureReason(listOf(text("You already own that area!", RED)))
		if (area.owner != null) return InputResult.FailureReason(listOf(text("Someone else already owns that area!", RED)))

		val future = FutureInputResult()

		Tasks.async {
			StationRentalArea.claim(area.id, player.slPlayerId)
			future.complete(InputResult.InputSuccess)
		}

		return future
	}

	fun depositBalance(player: Player, area: RegionRentalArea, depositAmount: Double): FutureInputResult {
		val future = FutureInputResult()

		Tasks.sync {
			if (!player.hasEnoughMoney(depositAmount)) {
				future.complete(InputResult.FailureReason(listOf(text("You don't have enough money for that!", RED))))
			}

			player.withdrawMoney(depositAmount)
			Tasks.async {
				StationRentalArea.depositMoney(area.id, depositAmount)
			}

			future.complete(InputResult.SuccessReason(listOf(template(text("You deposited {0} to the rent balance.", GREEN), depositAmount.toCreditComponent()))))
		}

		return future
	}

	fun transferOwnership(player: Player, area: RegionRentalArea, newOwner: SLPlayerId): PotentiallyFutureResult {
		val result = FutureInputResult()

		Tasks.async {
			val name = SLPlayer.getName(newOwner)
			if (name == null) {
				result.complete(InputResult.FailureReason(listOf(text("Player not found!", RED))))
				return@async
			}

			ConfirmationMenu.promptConfirmation(player, GuiText("Confirm transfer to $name?")) {
				StationRentalArea.transferOwnership(area.id, newOwner)
				Tasks.sync {
					player.closeInventory()
				}
			}
		}

		return result
	}
}
