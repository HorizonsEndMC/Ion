package net.horizonsend.ion.server.features.bounties

import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.common.extensions.userError
import org.bukkit.entity.Player

fun Player.acceptBounty(target: Player) {
	val playerData = PlayerData[this.uniqueId] ?: return
	val targetData = PlayerData[target.uniqueId] ?: return

	if (playerData.uuid == targetData.uuid) {
		userError("Cannot accept a bounty on yourself")
		return
	}

	if (playerData.acceptedBounty != null) {
		userError("Cannot accept more then 1 bounty")
		return
	}

	playerData.update {
		acceptedBounty = targetData.uuid.value
	}

	sendRichMessage("<gray>Accepted </gray>${targetData.bounty}<gray> bounty on </gray>${targetData.username}")
}
