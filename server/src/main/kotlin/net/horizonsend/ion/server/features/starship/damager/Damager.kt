package net.horizonsend.ion.server.features.starship.damager

import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.progression.SLXP
import net.horizonsend.ion.server.features.progression.ShipKillXP
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.event.ImpactStarshipEvent
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Entity
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

class EntityDamager(val entity: Entity) : NoOpDamager() {
	override fun getDisplayName(): Component = entity.name()

	companion object {
		fun Entity.damager(starship: ActiveStarship? = null) : Damager {
			if (this is Player) return PlayerDamagerWrapper(this, starship)
			return EntityDamager(this)
		}
	}
}

open class NoOpDamager : Damager {
	override val starship: ActiveStarship? = null
	override val color: Color = Color.RED
	override fun getDisplayName(): Component = Component.text("none")
	override fun rewardMoney(credits: Double) { }
	override fun rewardXP(xp: Int) { }
}

fun addToDamagers(world: World, block: Block, shooter: Damager) {
	val x = block.x
	val y = block.y
	val z = block.z
	for (otherStarship in ActiveStarships.getInWorld(world)) {
		if (otherStarship == shooter.starship || !otherStarship.contains(x, y, z)) continue

		val event = ImpactStarshipEvent(shooter, otherStarship)

		event.callEvent()

		if (event.isCancelled) return

		otherStarship.damagers.getOrPut(shooter) { ShipKillXP.ShipDamageData() }.points.incrementAndGet()
	}
}
