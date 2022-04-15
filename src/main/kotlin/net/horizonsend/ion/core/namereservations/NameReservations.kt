package net.horizonsend.ion.core.namereservations

import java.io.FileNotFoundException
import java.util.UUID
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.starlegacy.PLUGIN
import org.bukkit.entity.Player

object NameReservations {
	private val nameReservationData =
		try {
			Json.decodeFromString(PLUGIN.dataFolder.resolve("nameReservations.json").readText())
		} catch (exception: FileNotFoundException) {
			NameReservationData()
		}

	fun canCreateOrganisationWithName(name: String, player: Player): Boolean {
		val nationNameOwner = nameReservationData.nations[name.lowercase()] ?: player.uniqueId
		val settlementNameOwner = nameReservationData.settlements[name.lowercase()] ?: player.uniqueId

		return settlementNameOwner == player.uniqueId && nationNameOwner == player.uniqueId
	}

	fun getNameReservationData(): NameReservationData =
		NameReservationData(
			nameReservationData.settlements.toMutableMap(),
			nameReservationData.nations.toMutableMap()
		)

	fun doesSettlementReservationExist(name: String) = nameReservationData.settlements.containsKey(name.lowercase())

	fun doesNationReservationExist(name: String) = nameReservationData.nations.containsKey(name.lowercase())

	fun addSettlementReservation(name: String, player: UUID) {
		nameReservationData.settlements[name.lowercase()] = player
		updateReservationFile()
	}

	fun addNationReservation(name: String, player: UUID) {
		nameReservationData.nations[name.lowercase()] = player
		updateReservationFile()
	}

	fun removeSettlementReservation(name: String) {
		nameReservationData.settlements.remove(name.lowercase())
		updateReservationFile()
	}

	fun removeNationReservation(name: String) {
		nameReservationData.nations.remove(name.lowercase())
		updateReservationFile()
	}

	private fun updateReservationFile() =
		PLUGIN.dataFolder.resolve("nameReservations.json").writeText(Json.encodeToString(nameReservationData))
}