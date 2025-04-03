package net.horizonsend.ion.server.features.ai.module.targeting

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.ai.configuration.AIEmities
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.module.listeners.AIModuleHandlePlayerDeath
import net.horizonsend.ion.server.features.ai.module.listeners.AIModuleHandleShipSink
import net.horizonsend.ion.server.features.ai.module.misc.AIFleetManageModule
import net.horizonsend.ion.server.features.ai.module.misc.CaravanModule
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
import net.horizonsend.ion.server.features.starship.event.StarshipSunkEvent
import net.horizonsend.ion.server.features.starship.fleet.toFleetMember
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.function.Supplier
import kotlin.math.cbrt

open class EnmityModule(
	controller: AIController,
	val difficulty : DifficultyModule,
	val targetAI : Boolean,
	val configSupplier: Supplier<AIEmities.AIEmityConfiguration>
	= Supplier(ConfigurationFiles.aiEmityConfiguration()::defaultAIEmityConfiguration),
	val enmityFilter : (starship : Starship, AITarget : AITarget, targetAI : Boolean) -> Boolean = fleetAwareTargetFilter(controller)
) : AIModule(controller), AIModuleHandlePlayerDeath, AIModuleHandleShipSink{
	val config get() = configSupplier.get()
	val enmityList : MutableList<AIOpponent> = mutableListOf()
	var findTargetOverride : (() -> AITarget)? = null

	val tickRate = 20 * 2
	var ticks = 0 + randomInt(0,tickRate) //randomly offset targeting updates

	override fun tick() {
		ticks++
		if (ticks % tickRate != 0) return
		ticks = 0
		decayEnmity()
		generateEnmity()
		updateAggro()
		debugAudience.information("Number of targets: ${enmityList.size}")
		sortEnmity()
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
		return findTargetsAnywhere().filter { it.getWorld() == world && getOpponentDistance(it)!! < config.aggroRange}
	}

	/**
	 * Returns an AI target in any world, used for navigation
	 * `findTargetOverride`
	 */
	fun findTargetAnywhere() : AITarget? {
		return findTargetsAnywhere().firstOrNull()
	}
	fun findTargetsAnywhere() : List<AITarget>{
		return enmityList.map { it.target }
	}

	fun generateEnmity() {
		aggroOnWell()
		aggroOnDistance()
		aggroOnDamager()
	}

	private fun aggroOnWell() {
		for (otherStarship in ActiveStarships.getInWorld(starship.world)) {
			if (!otherStarship.isInterdicting) continue
			val tempTarget = AIOpponent(StarshipTarget(otherStarship))
			if (!enmityFilter(starship,tempTarget.target,targetAI)) continue
			val dist = getOpponentDistance(tempTarget.target)!!
			if (dist > Interdiction.starshipInterdictionRangeEquation(otherStarship)) continue
			val index = enmityList.indexOf(tempTarget)
			if (index != -1) {
				enmityList[index].baseWeight += config.gravityWellAggro * 0.1 //kepping a well up will make ai pissed on you
			} else {
				tempTarget.baseWeight = config.gravityWellAggro
				enmityList.add(tempTarget)
			}

		}
	}

	private fun aggroOnDistance() {
		for (otherStarship in ActiveStarships.getInWorld(starship.world)) {
			val tempTarget = AIOpponent(StarshipTarget(otherStarship))
			if (!enmityFilter(starship,tempTarget.target,targetAI)) continue
			val dist = getOpponentDistance(tempTarget.target)!! + 1e-4
			val weight : Double
			if (dist > config.aggroRange) {
				weight = (config.aggroRange / dist * config.distanceAggroWeight * difficulty.outOfRangeAggro).coerceAtMost(config.distanceAggroWeight)
			} else {
				weight = config.distanceAggroWeight
			}
			val index = enmityList.indexOf(tempTarget)
			if (index != -1) {
				val entry = enmityList[index]
				if (!entry.aggroed) entry.baseWeight += weight
			} else {
				tempTarget.baseWeight += weight
				enmityList.add(tempTarget)
			}
		}

		for (player in starship.world.players) {
			if (ActiveStarships.findByPassenger(player) != null) continue
			val tempTarget = AIOpponent(PlayerTarget(player))
			if (!enmityFilter(starship,tempTarget.target,targetAI)) continue
			val dist = getOpponentDistance(tempTarget.target)!!
			if (dist > config.aggroRange ) continue
			val index = enmityList.indexOf(tempTarget)
			if (index != -1) {
				val entry = enmityList[index]
				if (!entry.aggroed) entry.baseWeight += config.distanceAggroWeight
			} else {
				tempTarget.baseWeight += config.distanceAggroWeight
				enmityList.add(tempTarget)
			}
		}
	}

	private fun aggroOnDamager() {
		val topDamager = starship.damagers.maxByOrNull { it.value.points.get() } ?: return
		val tempTarget  = topDamager.key.getAITarget()?.let { AIOpponent(it) } ?: return
		if (!targetFilter(starship,tempTarget.target,targetAI)) return

		//extra friendly fire check
		if ((tempTarget.target as? StarshipTarget)?.ship?.controller is AIController) {
			val targetController = (tempTarget.target as StarshipTarget).ship.controller as AIController

			val fleetModule = controller.getUtilModule(AIFleetManageModule::class.java)
			val fleet = fleetModule?.fleet

			val targetFleet = targetController.getUtilModule(AIFleetManageModule::class.java)?.fleet
			if (targetFleet != null && targetFleet == fleet) return
		}


		val index = enmityList.indexOf(tempTarget)
		if (index != -1) {
			enmityList[index].damagerWeight += config.damagerAggroWeight * 0.5 //the highest damager will generate base emity
		} else {
			tempTarget.damagerWeight = config.damagerAggroWeight
			enmityList.add(tempTarget)
		}
	}

	private fun updateAggro() {
		enmityList.forEach {
			if (it.baseWeight >= config.initialAggroThreshold) it.aggroed = true
			if (it.damagerWeight >= config.initialAggroThreshold * 4) it.aggroed = true
		}
	}

	fun decayEnmity() {
		enmityList.forEach {
			if (it.decay && randomDouble(0.0,1.0) < difficulty.decayEmityThreshold) {
				val dist = getOpponentDistance(it.target)
				if (dist == null) {
					it.baseWeight *= config.outOfSystemDecay
				} else {
					if (dist > config.aggroRange) it.baseWeight *= config.outOfRangeDecay
				}
			}
			it.damagerWeight *= config.damagerDeacy
		}
		enmityList.removeAll { it.baseWeight + it.damagerWeight < 1e-4 }
	}

	open fun sortEnmity() {

		val damagersMap = starship.damagers.entries.associate {
			Pair(it.key.getAITarget(), it.value.points.get())
		}

		enmityList.sortBy {-1 *(//descending
			it.baseWeight
			+ config.damagerWeight * it.damagerWeight
			+ config.sizeWeight * cbrt((it.target as? StarshipTarget)?.ship?.initialBlockCount?.toDouble() ?: 0.0)
			+ config.distanceWeight * calcDistFactor(it.target)
		)}
	}

	private fun calcDistFactor(target : AITarget) : Double{
		return ((config.aggroRange  - (getOpponentDistance(target) ?: 0.0)) / (config.aggroRange + 1e-4)).coerceAtLeast(0.0)
	}

	fun getOpponentDistance(target : AITarget) : Double?{
		if (target.getWorld() != world) {
			return null
		}
		return target.getLocation(false).toVector().distance(location.toVector())
	}

	fun addTarget(target: AITarget,baseWeight : Double = 1.0, aggroed: Boolean = false, decay: Boolean = true,  ) {
		val existing = enmityList.find { it.target == target }
		if (existing != null) return

		enmityList.add(AIOpponent(target, baseWeight, aggroed = aggroed, decay = decay))
	}

	fun removeTarget(target: AITarget) {
		enmityList.removeIf { it.target == target }
	}


	override fun onShipSink(event : StarshipSunkEvent) {
		val tempTarget = AIOpponent(StarshipTarget(event.starship))
		enmityList.remove(tempTarget)
	}

	override fun onPLayerDeath(event: PlayerDeathEvent) {
		val tempTarget = AIOpponent(PlayerTarget(event.player))
		enmityList.remove(tempTarget)
	}

	data class AIOpponent(
		var target : AITarget,
		var baseWeight : Double = 0.0,
		var damagerWeight : Double = 0.0,
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

		fun fleetAwareTargetFilter(controller: AIController): (Starship, AITarget, Boolean) -> Boolean =
			FleetAwareTargetFilter@{ starship, target, targetAI ->
				val fleetModule = controller.getUtilModule(AIFleetManageModule::class.java)
				val fleet = fleetModule?.fleet
				val isCaravanProtected = controller.getUtilModule(CaravanModule::class.java) != null

				// Block targeting players in the same fleet or if protected by caravan
				if (target is PlayerTarget) {
					val isSameFleet = fleet?.isMember(target.player.toFleetMember()) ?: false
					if (isSameFleet || isCaravanProtected) return@FleetAwareTargetFilter false
				}

				if (target is StarshipTarget) {
					val targetController = target.ship.controller

					// Block if it's a player in the same fleet
					if (targetController is PlayerController) {
						val player = targetController.player
						val isSameFleet = fleet?.isMember(player.toFleetMember()) ?: false
						if (isSameFleet || isCaravanProtected) return@FleetAwareTargetFilter false
					}

					// Block if it's an AI in the same fleet
					if (targetController is AIController) {
						val targetFleet = targetController.getUtilModule(AIFleetManageModule::class.java)?.fleet
						if (targetFleet != null && targetFleet == fleet) return@FleetAwareTargetFilter false
					}
				}

				// All other cases fall back on normal targeting
				targetFilter(starship, target, targetAI)
			}
	}
}
