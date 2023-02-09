package net.horizonsend.ion.server.features.bounties

import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.server.extensions.sendUserError
import org.bukkit.entity.Player

fun Player.acceptBounty(target: Player) {
	val playerData = PlayerData[this.uniqueId]
	val targetData = PlayerData[target.uniqueId]

	if (playerData.minecraftUUID == targetData.minecraftUUID) {
		sendUserError("Cannot accept a bounty on yourself")
		return
	}

	if (playerData.acceptedBounty != null) {
		sendUserError("Cannot accept more then 1 bounty")
		return
	}

	playerData.update {
		acceptedBounty = targetData.minecraftUUID
	}

	sendRichMessage("<gray>Accepted </gray>${targetData.bounty}<gray> bounty on </gray>${targetData.minecraftUsername}")
}
