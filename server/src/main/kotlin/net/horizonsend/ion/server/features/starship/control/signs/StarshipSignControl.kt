package net.horizonsend.ion.server.features.starship.control.signs

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

object StarshipSignControl : IonServerComponent() {
	@EventHandler
	fun onSignClick(event: PlayerInteractEvent) {
		if (event.hand != EquipmentSlot.HAND) {
			return
		}

		val rightClick = event.action == Action.RIGHT_CLICK_BLOCK

		val player = event.player

		val block = event.clickedBlock ?: return
		val sign = block.state as? Sign ?: return

		clickSign(player, rightClick, sign)
	}

	@EventHandler
	fun onSignPlace(event: BlockPlaceEvent) {
		val sign = event.block as? Sign ?: return
		Tasks.syncDelay(1) {
			clickSign(event.player, true, sign)
		}
	}

	private fun clickSign(player: Player, rightClick: Boolean, sign: Sign) {
		for (signType in StarshipSigns.values()) {
			if (rightClick && firstLineMatchesUndetected(sign, signType)) {
				detectSign(signType, player, sign)
				return
			}

			if (!matchesDetectedSign(signType, sign)) {
				continue
			}

			signType.onClick(player, sign, rightClick)
			return
		}
	}

	private fun firstLineMatchesUndetected(sign: Sign, signType: StarshipSigns): Boolean {
		return sign.getLine(0) == signType.undetectedText
	}

	private fun matchesDetectedSign(signType: StarshipSigns, sign: Sign): Boolean {
		val baseLines = signType.baseLines
		return baseLines.withIndex().none { (index, line) ->
			line != null && sign.getLine(index) != line
		}
	}

	private fun detectSign(signType: StarshipSigns, player: Player, sign: Sign) {
		if (signType.onDetect(player, sign)) {
			for ((index, line) in signType.baseLines.withIndex()) {
				if (line == null) {
					continue
				}
				sign.setLine(index, line)
			}
			sign.update()
		}
	}
}
