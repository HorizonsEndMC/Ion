package net.horizonsend.ion.server.features.starship

import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.progression.SLXP
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.entity.Player

/** An object capable of damaging a starship **/
interface Damager : Audience {
	val starship: ActiveStarship?
	val color: Color
	fun getDisplayName() : Component
	fun rewardXP(xp: Int)
	fun rewardMoney(credits: Double)
}

interface PlayerDamager : Damager {
	val player: Player

	override fun getDisplayName(): Component = player.displayName()

	override fun rewardMoney(credits: Double) {
		VAULT_ECO.depositPlayer(player, credits)
	}

	override fun rewardXP(xp: Int) {
		SLXP.addAsync(player.uniqueId, xp)
	}
}

/** Wrapper class for players not in starships **/
class PlayerDamagerWrapper(override val player: Player, override val starship: ActiveStarship?) : PlayerDamager {
	override val color: Color
		get() = PlayerCache[player].nationOid?.let { Color.fromRGB( NationCache[it].color ) } ?: Color.RED
}

val noOpDamager = NoOpDamager()

class NoOpDamager : Damager {
	override val starship: ActiveStarship? = null
	override val color: Color = Color.RED
	override fun getDisplayName(): Component = Component.text("none")
	override fun rewardMoney(credits: Double) { }
	override fun rewardXP(xp: Int) { }
}
