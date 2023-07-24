package net.horizonsend.ion.server.features.space.encounters

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.space.encounters.Encounters.createLootChest
import net.horizonsend.ion.server.features.space.encounters.Encounters.setChestFlag
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.INACTIVE
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.minecraft.nbt.CompoundTag
import net.starlegacy.feature.nations.gui.skullItem
import net.starlegacy.util.MenuHelper
import org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS
import org.bukkit.Sound.BLOCK_NOTE_BLOCK_HARP
import org.bukkit.Sound.BLOCK_NOTE_BLOCK_SNARE
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import java.util.Random
import java.util.UUID

object Passcode : Encounter(identifier = "passcode") {

	private val headID = mapOf(
		0 to UUID.fromString("cb34f183-2b0a-44a4-b87c-df957a63a4f0"),
		1 to UUID.fromString("606d2a18-b91c-4f7f-9b0d-b3cd322e9214"),
		2 to UUID.fromString("e4d886d5-331e-4558-8f69-4cca32085bc9"),
		3 to UUID.fromString("eb6e7446-99b7-4b82-ae73-a93ab08ec12c"),
		4 to UUID.fromString("e4e8f360-b896-45fd-ab07-6983d871814a"),
		5 to UUID.fromString("529ee22e-2500-41e0-86ee-9855ec6fd9f1"),
		6 to UUID.fromString("3a54360c-a127-42c6-9b28-fda710ae64d3"),
		7 to UUID.fromString("eabe07f6-08ac-4aa3-a151-370907fcbead"),
		8 to UUID.fromString("5439b6ba-efc0-4baa-b72c-94de0bb9d79a"),
		9 to UUID.fromString("a67e73f4-c989-4586-b571-561fc2dc069e")
	)

	private val skinID = mapOf(
		0 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmZhNDU5MTFiMTYyOThjZmNhNGIyMjkxZWVkYTY2NjExM2JjNmYyYTM3ZGNiMmVjZDhjMjc1NGQyNGVmNiJ9fX0=",
		1 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2FmMWIyODBjYWI1OWY0NDY5ZGFiOWYxYTJhZjc5MjdlZDk2YTgxZGYxZTI0ZDUwYThlMzk4NGFiZmU0MDQ0In19fQ==",
		2 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTRiMWUxZDQyNjEyM2NlNDBjZDZhNTRiMGY4NzZhZDMwYzA4NTM5Y2Y1YTZlYTYzZTg0N2RjNTA3OTUwZmYifX19",
		3 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTA0Y2NmOGI1MzMyYzE5NmM5ZWEwMmIyMmIzOWI5OWZhY2QxY2M4MmJmZTNmN2Q3YWVlZGMzYzMzMjkwMzkifX19",
		4 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmI0ZmMxOGU5NzVmNGYyMjJkODg1MjE2ZTM2M2FkYzllNmQ0NTZhYTI5MDgwZTQ4ZWI0NzE0NGRkYTQzNmY3In19fQ==",
		5 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQ4YjIyMjM5NzEyZTBhZDU3OWE2MmFlNGMxMTUxMDNlNzcyODgyNWUxNzUwOGFjZDZjYzg5MTc0ZWU4MzgifX19",
		6 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWVlZmJhZDE2NzEyYTA1Zjk4ZTRmMGRlNWI0NDg2YWYzOTg3YjQ2ZWE2YWI0ZTNiZTkzZDE0YTgzMmM1NmUifX19",
		7 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTNlNjlmYTk0MmRmM2Q1ZWE1M2EzYTk3NDkxNjE3NTEwOTI0YzZiOGQ3YzQzNzExOTczNzhhMWNmMmRlZjI3In19fQ==",
		8 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2QxODRmZDRhYjUxZDQ2MjJmNDliNTRjZTdhMTM5NWMyOWYwMmFkMzVjZTVhYmQ1ZDNjMjU2MzhmM2E4MiJ9fX0=",
		9 to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWIyNDU0YTVmYWEyNWY3YzRmNTc3MWQ1MmJiNGY1NWRlYjE5MzlmNzVlZmQ4ZTBhYzQyMTgxMmJhM2RjNyJ9fX0="
	)

	val coordinates = mapOf(
		0 to Pair(1, 3),
		1 to Pair(0, 2),
		2 to Pair(1, 2),
		3 to Pair(2, 2),
		4 to Pair(0, 1),
		5 to Pair(1, 1),
		6 to Pair(2, 1),
		7 to Pair(0, 0),
		8 to Pair(1, 0),
		9 to Pair(2, 0)
	)

	private fun paneInteractCheck(player: Player, chest: Chest, passcode: String, currentCode: String) {
		when (compareCode(passcode, currentCode)) {
			false -> playerFails(player)
			true -> playerSucceeds(player, chest)
			null -> playerContinues(chest)
		}
	}

	private fun compareCode(passcode: String, currentCode: String): Boolean? {
		return if (currentCode.isNotEmpty() && (currentCode.length > passcode.length || currentCode.last() != passcode[currentCode.length - 1])) {
			false
		} else if (currentCode == passcode) {
			true
		} else null
	}

	private fun playerFails(player: Player) {
		player.closeInventory()
		player.userError("You entered the wrong passcode!")
		player.playSound(player, BLOCK_NOTE_BLOCK_BASS, 5.0f, 1.0f)
	}

	private fun playerSucceeds(player: Player, chest: Chest) {
		player.closeInventory()
		player.success("You entered the correct passcode! The chest is now unlocked.")
		chest.location.world.playSound(chest.location, BLOCK_NOTE_BLOCK_HARP, 5.0f, 2.0f)
		setChestFlag(chest, INACTIVE, "true")
	}

	private fun playerContinues(chest: Chest) {
		chest.location.world.playSound(chest.location, BLOCK_NOTE_BLOCK_SNARE, 5.0f, 1.0f)
	}

	override fun onChestInteract(event: PlayerInteractEvent) {
		val targetedBlock = event.clickedBlock!!

		event.isCancelled = true
		val chest = (targetedBlock.state as? Chest) ?: return
		val passcode = Random(targetedBlock.location.hashCode().toLong()).nextInt(0, 999999).toString().padStart(6, '0')
		var currentCode = ""

		event.player.information("Enter this passcode: $passcode")

		MenuHelper.apply {
			val pane = staticPane(3, 0, 3, 4)

			for (i in 0..9) {
				pane.addItem(
					guiButton(skullItem(i.toString(), headID[i]!!, skinID[i]!!)) {
						currentCode += i.toString()
						paneInteractCheck(event.player, chest, passcode, currentCode)
					}.setName(miniMessage().deserialize(i.toString())),
					coordinates[i]!!.first, coordinates[i]!!.second
				)
			}

			gui(4, "Enter passcode:").withPane(pane).show(event.player)
		}
	}

	override fun constructChestNBT(): CompoundTag {
		return createLootChest("horizonsend:chests/gun_parts")
	}
}
