package net.horizonsend.ion.server.features.ai.module.targeting

import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.ai.configuration.AIEmities
import net.horizonsend.ion.server.features.ai.configuration.steering.AISteeringConfiguration
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.PlayerTarget
import net.horizonsend.ion.server.features.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.player.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.features.starship.Interdiction
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import java.util.function.Supplier
import kotlin.math.cbrt

open class EmityModule(
	controller: AIController,
	val difficulty : DifficultyModule,
	val targetAI : Boolean,
	val configSupplier: Supplier<AIEmities.AIEmityConfiguration>
	= Supplier(ConfigurationFiles.aiEmityConfiguration()::defaultAIEmityConfiguration),
	val emityFilter : (starship : Starship, AITarget : AITarget, targetAI : Boolean) -> Boolean = Companion::targetFilter
) : AIModule(controller){
	val config get() = configSupplier.get()
	val emityList : MutableList<AIOpponent> = mutableListOf()
	var findTargetOverride : (() -> AITarget)? = null

	val tickRate = 20 * 2
	var ticks = 0 + randomInt(0,tickRate) //randomly offset targeting updates

	override fun tick() {
		ticks++
		if (ticks % tickRate != 0) return
		ticks = 0
		decayEmity()
		generateEmity()
		updateAggro()
		sortEmity()
	}

	/**
	 * Returns an AI target that's in the same world as the AI, can be overwritten by navigation via
	 * `findTargetOverride`
	 * this is used for driving
	 */
	fun findTarget() : AITarget?{
		if (findTargetOverride != null) {
			return findTargetOverride!!.invoke()
		}
		return findTargets().firstOrNull()
	}

	/**
	 * Returns AI opponents that's in the same world as the AI
	 *  this is used shooting at multiple targets only
	 */
	fun findTargets() : List<AITarget> {
		return findTargetsAnywhere().filter { it.getWorld() == world }
	}

	/**
	 * Returns an AI target in any world, used for navigation
	 * `findTargetOverride`
	 */
	fun findTargetAnywhere() : AITarget? {
		return findTargetsAnywhere().firstOrNull()
	}
	fun findTargetsAnywhere() : List<AITarget>{
		return emityList.map { it.target }
	}

	fun generateEmity() {
		aggroOnWell()
		aggroOnDistance()
		aggroOnDamager()
	}

	private fun aggroOnWell() {
		for (starship in ActiveStarships.getInWorld(starship.world)) {
			if (!starship.isInterdicting) continue
			val tempTarget = AIOpponent(StarshipTarget(starship))
			if (emityFilter(starship,tempTarget.target,targetAI)) continue
			val dist = getOpponentDistance(tempTarget.target)!!
			if (dist > Interdiction.starshipInterdictionRangeEquation(starship)) continue
			val index = emityList.indexOf(tempTarget)
			if (index != -1) {
				emityList[index].baseWeight += config.gravityWellAggro * 0.1 //kepping a well up will make ai pissed on you
			} else {
				tempTarget.baseWeight = config.gravityWellAggro
				emityList.add(tempTarget)
			}

		}
	}

	private fun aggroOnDistance() {
		for (starship in ActiveStarships.getInWorld(starship.world)) {
			val tempTarget = AIOpponent(StarshipTarget(starship))
			if (emityFilter(starship,tempTarget.target,targetAI)) continue
			val dist = getOpponentDistance(tempTarget.target)!! + 1e-4
			val weight : Double
			if (dist > config.aggroRange) {
				weight = (config.aggroRange / dist * config.distanceAggroWeight * difficulty.outOfRangeAggro).coerceAtMost(config.distanceAggroWeight)
			} else {
				weight = config.distanceAggroWeight
			}
			val index = emityList.indexOf(tempTarget)
			if (index != -1) {
				val entry = emityList[index]
				if (!entry.aggroed) entry.baseWeight += weight
			} else {
				tempTarget.baseWeight += weight
				emityList.add(tempTarget)
			}
		}

		for (player in starship.world.players) {
			//TODO check if a player is already riding a ship
			val tempTarget = AIOpponent(PlayerTarget(player))
			if (emityFilter(starship,tempTarget.target,targetAI)) continue
			val dist = getOpponentDistance(tempTarget.target)!!
			if (dist > config.aggroRange ) continue
			val index = emityList.indexOf(tempTarget)
			if (index != -1) {
				val entry = emityList[index]
				if (!entry.aggroed) entry.baseWeight += config.distanceAggroWeight
			} else {
				tempTarget.baseWeight += config.distanceAggroWeight
				emityList.add(tempTarget)
			}
		}
	}

	private fun aggroOnDamager() {
		val topDamager = starship.damagers.maxByOrNull { it.value.points.get() } ?: return
		val tempTarget  = topDamager.key.getAITarget()?.let { AIOpponent(it) } ?: return
		if (emityFilter(starship,tempTarget.target,targetAI)) return
		val index = emityList.indexOf(tempTarget)
		if (index != -1) {
			emityList[index].baseWeight += config.damagerAggroWeight * 0.1 //the highest damager will generate base emity
		} else {
			tempTarget.baseWeight = config.damagerAggroWeight
			emityList.add(tempTarget)
		}
	}

	private fun updateAggro() {
		emityList.forEach {
			if (it.baseWeight >= config.initialAggroThreshold) it.aggroed = true
		}
	}

	fun decayEmity() {
		emityList.forEach {
			if (it.decay && randomDouble(0.0,1.0) < difficulty.decayEmityThreshold) {
				val dist = getOpponentDistance(it.target)
				if (dist == null) {
					it.baseWeight *= config.outOfSystemDecay
				} else {
					if (dist > config.aggroRange) it.baseWeight *= config.outOfRangeDecay
				}
			}
		}
		emityList.removeAll { it.baseWeight < 1e-4 }
	}

	open fun sortEmity() {

		val damagersMap = starship.damagers.entries.associate {
			Pair(it.key.getAITarget(), it.value.points.get())
		}

		emityList.sortBy {-1 *(//descending
			it.baseWeight
			+ config.damagerWeight * damagersMap.getOrDefault(it.target,0)
			+ config.sizeWeight * cbrt((it.target as? StarshipTarget)?.ship?.initialBlockCount?.toDouble() ?: 0.0)
			+ config.distanceWeight * calcDistFactor(it.target)
		)}
	}

	private fun calcDistFactor(target : AITarget) : Double{
		return ((config.aggroRange  - (getOpponentDistance(target) ?: 0.0)) / (config.aggroRange + 1e-4)).coerceAtMost(0.0)
	}

	fun getOpponentDistance(target : AITarget) : Double?{
		if (target.getWorld() != world) {
			return null
		}
		return target.getLocation(false).toVector().distance(location.toVector())
	}

	data class AIOpponent(
		var target : AITarget,
		var baseWeight : Double = 0.0,
		var aggroed : Boolean = false,
		val decay : Boolean = true,
	) {

		override fun hashCode(): Int {
			return target.hashCode()
		}

		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (other is StarshipTarget) {
				return target == other
			}
			if (javaClass != other?.javaClass) return false

			other as AIOpponent

			return target == other.target
		}
	}

	companion object {
		fun targetFilter(starship: Starship, aiTarget: AITarget, targetAI : Boolean) : Boolean {
			when  {
				aiTarget is StarshipTarget && aiTarget.ship.controller is PlayerController -> {
					if (targetAI) return false
					val player = (aiTarget.ship.controller as PlayerController).player
					if (!player.hasProtection()) return true // check for prot
					if (starship.world.ion.hasFlag(WorldFlag.NOT_SECURE)) return true //ignore prot in unsafe areas
					if (starship.damagers.keys.any{(it as PlayerDamager).player == player}) return true //fire first
				}
				aiTarget is StarshipTarget && aiTarget.ship.controller is AIController -> {
					return targetAI && aiTarget.ship.controller != starship.controller
				}
				aiTarget is PlayerTarget && !targetAI -> {
					if (starship.world.ion.hasFlag(WorldFlag.NOT_SECURE)) return true
					return !aiTarget.player.hasProtection()
				}
			}
			return false
		}
	}
}
