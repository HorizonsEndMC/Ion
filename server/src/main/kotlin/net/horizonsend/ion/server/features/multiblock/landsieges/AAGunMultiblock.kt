package net.horizonsend.ion.server.features.multiblock.landsieges

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.database.schema.misc.SLPlayer
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.database.slPlayerId
import net.horizonsend.ion.server.features.landsieges.AAGuns
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.PlayerCache
import org.bukkit.block.Sign
import org.bukkit.entity.Player

object AAGunMultiblock : Multiblock(), PowerStoringMultiblock {
	override val name: String = "aagun"
	override val maxPower: Int get() = 1_000_000

	private val ACTIVE_STATE = text("Active", NamedTextColor.GREEN).apply { this.decoration(TextDecoration.BOLD) }
	private val INACTIVE_STATE = text("Inactive", NamedTextColor.RED).apply { this.decoration(TextDecoration.BOLD) }

	override val signText: Array<Component?> = arrayOf(
		text("Anti-Air", NamedTextColor.GOLD),
		text("Particle Gun", NamedTextColor.AQUA),
		null,
		INACTIVE_STATE
	)

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).anyWool()
	}

	override fun onTransformSign(player: Player, sign: Sign) {
		super<PowerStoringMultiblock>.onTransformSign(player, sign)

		if (!sign.world.name.lowercase().contains("moon")) {
			player.userError("You must be on a moon to setup an AA gun.")
			sign.block.breakNaturally()
			return
		}

		if (sign.line(3) == INACTIVE_STATE)
			sign.line(3, ACTIVE_STATE)
		else sign.line(3, INACTIVE_STATE)

		sign.update(true, false)

		Nation.addAAGun(PlayerCache[player].nationOid ?: run {
			player.userError("You need a nation to setup an AA gun.")
			sign.block.breakNaturally()

			return
		}, sign.location)

		(AAGuns.map[PlayerCache[player].nationOid!!] ?: run {
			AAGuns.map[PlayerCache[player].nationOid!!] = mutableListOf(sign.location)
			return
		}).add(sign.location)
	}
}
