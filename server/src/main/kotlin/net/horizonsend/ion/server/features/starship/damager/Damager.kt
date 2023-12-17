package net.horizonsend.ion.server.features.starship.damager

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.progression.SLXP
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.ai.util.PlayerTarget
import net.horizonsend.ion.server.features.starship.ai.util.StarshipTarget
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
	fun getAITarget(): AITarget? //TODO non null eventually
}

interface PlayerDamager : Damager {
	val player: Player

	override fun getDisplayName(): Component = player.displayName()

	override fun rewardMoney(credits: Double) {
		VAULT_ECO.depositPlayer(player, credits)
	}

	override fun rewardXP(xp: Int) {
		SLXP.addAsync(player, xp)
	}
}

/** Wrapper class for players not in starships **/
class PlayerDamagerWrapper(override val player: Player) : PlayerDamager {
	override val starship: ActiveStarship? get() = ActiveStarships.findByPassenger(player)

	override val color: Color
		get() = PlayerCache[player].nationOid?.let { Color.fromRGB( NationCache[it].color ) } ?: Color.RED
	override fun getAITarget(): AITarget = PlayerTarget(player)

	override fun toString(): String = "PlayerDamager[${player.name}]"
}

val noOpDamager = NoOpDamager()

val entityDamagerCache: LoadingCache<Entity, Damager> = CacheBuilder.newBuilder()
	.weakKeys()
	.build(CacheLoader.from { entity -> getDamager(entity) })

fun Entity.damager(): Damager = entityDamagerCache[this]

fun getDamager(entity: Entity) : Damager {
	if (entity is Player) return PlayerDamagerWrapper(entity)

	return EntityDamager(entity)
}

class EntityDamager(val entity: Entity) : NoOpDamager() {
	override fun getDisplayName(): Component = entity.name()
	override fun getAITarget(): AITarget? = null
}

open class NoOpDamager : Damager {
	override val starship: ActiveStarship? = null
	override val color: Color = Color.RED
	override fun getDisplayName(): Component = Component.text("none")
	override fun rewardMoney(credits: Double) { }
	override fun rewardXP(xp: Int) { }
	override fun getAITarget(): AITarget? = null
}

class AIShipDamager(override val starship: ActiveStarship, override val color: Color = Color.RED): Damager {
	override fun getDisplayName(): Component = starship.getDisplayName()
	override fun rewardMoney(credits: Double) {}
	override fun rewardXP(xp: Int) {}
	override fun getAITarget(): AITarget = StarshipTarget(starship)

	override fun toString(): String = "AIDamager[${starship.controller}]"
}

fun addToDamagers(world: World, block: Block, shooter: Entity) {
	val damager = entityDamagerCache[shooter]

	addToDamagers(world, block, damager)
}

fun addToDamagers(world: World, block: Block, shooter: Damager) {
	val x = block.x
	val y = block.y
	val z = block.z

	for (otherStarship: ActiveStarship in ActiveStarships.getInWorld(world)) {
		if (otherStarship == shooter.starship || !otherStarship.contains(x, y, z)) continue

		val event = ImpactStarshipEvent(shooter, otherStarship)

		event.callEvent()

		if (event.isCancelled) return

		otherStarship.addToDamagers(shooter)
	}
}
