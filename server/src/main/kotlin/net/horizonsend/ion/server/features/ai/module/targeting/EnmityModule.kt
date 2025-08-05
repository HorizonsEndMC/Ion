package net.horizonsend.ion.server.features.ai.module.targeting

import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.ai.configuration.AIEmities
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.module.listeners.AIModuleHandlePlayerDeath
import net.horizonsend.ion.server.features.ai.module.listeners.AIModuleHandleShipSink
import net.horizonsend.ion.server.features.ai.module.misc.AIFleetManageModule
import net.horizonsend.ion.server.features.ai.module.misc.CaravanModule
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.module.misc.FactionManagerModule
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.GoalTarget
import net.horizonsend.ion.server.features.ai.util.PlayerTarget
import net.horizonsend.ion.server.features.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.player.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.features.progression.Bounties
import net.horizonsend.ion.server.features.starship.Interdiction
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.event.StarshipSunkEvent
import net.horizonsend.ion.server.features.starship.fleet.Fleet
import net.horizonsend.ion.server.features.starship.fleet.FleetMember
import net.horizonsend.ion.server.features.starship.fleet.toFleetMember
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import org.bukkit.GameMode
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.function.Supplier
import kotlin.math.cbrt
import kotlin.math.max
import kotlin.math.pow
import kotlin.random.Random

open class EnmityModule(
	controller: AIController,
	val difficulty: DifficultyModule,
	val targetMode: AITarget.TargetMode,
	val configSupplier: Supplier<AIEmities.AIEmityConfiguration>
	= Supplier { ConfigurationFiles.aiEmityConfiguration().defaultAIEmityConfiguration },
	var enmityFilter: (starship: Starship, aiTarget: AITarget, targetMode: AITarget.TargetMode) -> Boolean = fleetAwareTargetFilter(controller)
) : AIModule(controller), AIModuleHandlePlayerDeath, AIModuleHandleShipSink {
	val config get() = configSupplier.get()
	val enmityList: MutableList<AIOpponent> = mutableListOf()
	var findTargetOverride: (() -> AITarget)? = null

	val tickRate = 20 * 2
	var ticks = 0 + randomInt(0, tickRate) //randomly offset targeting updates

	init {
		//effectivity anchor ship to spawn point if nothing else is going on
		// inverting priority allways places this at the bottom of the agro list
		val spawnTarget = GoalTarget(starship.centerOfMass, starship.world, false, attack = false, weight = 1 / 1500.0)
		addTarget(spawnTarget, baseWeight = 0.1, decay = false, aggroed = true, isAnchor = true)
	}

	override fun tick() {
		ticks++
		if (ticks % tickRate != 0) return
		ticks = 0
		removeOldEnmityTargets()
		decayEnmity()
		generateEnmity()
		updateAggro()
		debugAudience.debug("Number of targets: ${enmityList.size}, agrooed: ${enmityList.filter { it.aggroed }.size}")
		sortEnmity()
	}

	/**
	 * Returns an AI target that's in the same world as the AI, can be overwritten by navigation via
	 * `findTargetOverride`
	 * this is used for driving
	 */
	fun findTarget(): AITarget? {
		if (findTargetOverride != null) {
			return findTargetOverride!!.invoke()
		}
		return findTargets().firstOrNull()
	}

	/**
	 * Returns AI opponents that's in the same world as the AI
	 *  this is used shooting at multiple targets only
	 */
	fun findTargets(): List<AITarget> {
		if (difficulty.doNavigation) {
			return findTargetsAnywhere().filter { it.getWorld() == world }
		} else {
			return findTargetsAnywhere().filter {
				it.getWorld() == world
					&& (getOpponentDistance(it)!! < config.aggroRange || it is GoalTarget)
			}
		}
	}

	/**
	 * Returns an AI target in any world, used for navigation
	 * `findTargetOverride`
	 */
	fun findTargetAnywhere(): AITarget? {
		return findTargetsAnywhere().firstOrNull()
	}

	fun findTargetsAnywhere(): List<AITarget> {
		return enmityList.filter { it.aggroed }.map { it.target }
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
			if (!enmityFilter(starship, tempTarget.target, targetMode)) continue
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
			if (!enmityFilter(starship, tempTarget.target, targetMode)) continue
			val dist = getOpponentDistance(tempTarget.target)!! + 1e-4
			val weight: Double
			if (dist > config.aggroRange) {
				weight = ((config.aggroRange / dist).pow(2)
					* config.distanceAggroWeight * difficulty.outOfRangeAggro)
					.coerceAtMost(config.distanceAggroWeight)
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
			if (player.gameMode == GameMode.SPECTATOR) continue
			if (ActiveStarships.findByPassenger(player) != null) continue
			val tempTarget = AIOpponent(PlayerTarget(player))
			if (!enmityFilter(starship, tempTarget.target, targetMode)) continue
			val dist = getOpponentDistance(tempTarget.target)!!
			if (dist > config.aggroRange) continue
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
		val damagerEntries = starship.damagers.entries
		val damagerEntry = damagerEntries.weightedRandom({ it.value.points.get().toDouble() }) ?: return
		val points = damagerEntry.value.points.get()
		val tempTarget = damagerEntry.key.getAITarget()?.let { AIOpponent(it) } ?: return

		//extra friendly fire check for AI ships
		if ((tempTarget.target as? StarshipTarget)?.ship?.controller is AIController) {
			val targetController = (tempTarget.target as StarshipTarget).ship.controller as AIController

			val fleetModule = controller.getUtilModule(AIFleetManageModule::class.java)
			val fleet = fleetModule?.fleet

			val targetFleet = targetController.getUtilModule(AIFleetManageModule::class.java)?.fleet
			if (targetFleet != null && targetFleet == fleet) return
			if (!evaluateFaction(controller, targetController)
				&& !targetFilter(starship, tempTarget.target, targetMode)) return
		} else {
			if (!targetFilter(starship, tempTarget.target, targetMode)) return
		}


		val index = enmityList.indexOf(tempTarget)
		if (index != -1) {
			if (enmityList[index].damagePoints >= points) return
			enmityList[index].damagePoints = points
			debugAudience.debug("damagePoints : $points")
			enmityList[index].damagerWeight += config.damagerAggroWeight //the highest damager will generate base emity
			debugAudience.debug("damagerWeight : ${enmityList[index].damagerWeight}")
		} else {
			tempTarget.damagerWeight = config.damagerAggroWeight
			tempTarget.damagePoints = points
			enmityList.add(tempTarget)
		}
	}

	/** Returns one element with probability proportional to `weight`.  */
	private fun <T> Iterable<T>.weightedRandom(
		weight: (T) -> Double,
		rnd: Random = Random.Default
	): T? {
		// First pass – total weight
		var total = 0.0
		for (e in this) total += weight(e)
		if (total == 0.0) return null

		// Second pass – find the bucket
		val r = rnd.nextDouble() * total
		var acc = 0.0
		for (e in this) {
			acc += weight(e)
			if (r < acc) return e
		}
		return null
	}

	private fun updateAggro() {
		val fleet = controller.getUtilModule(AIFleetManageModule::class.java)?.fleet

		enmityList.filter { !it.aggroed }.forEach {
			if (it.baseWeight >= config.initialAggroThreshold) it.aggroed = true
			if (it.damagerWeight >= config.initialAggroThreshold * 4) it.aggroed = true
			if (it.aggroed && it.target.attack && fleet != null) propagateToFleet(it, fleet)
		}
	}

	private fun propagateToFleet(opponent: AIOpponent, fleet: Fleet) {
		debugAudience.debug("propagating agro from ${starship.getDisplayNamePlain()} to fleet")
		val aiFleetMembers = fleet.members.filterIsInstance<FleetMember.AIShipMember>().mapNotNull { it.shipRef.get() }
		aiFleetMembers.forEach { starship ->
			debugAudience.debug("propagating agro to ${starship.getDisplayNamePlain()}")
			val enmityModule = (starship.controller as? AIController)?.getCoreModuleByType<EnmityModule>() ?: return
			val otherList = enmityModule.enmityList
			val index = otherList.indexOf(opponent)

			if (index != -1) {
				otherList[index].aggroed = opponent.aggroed
				otherList[index].baseWeight = max(config.initialAggroThreshold, opponent.baseWeight)
				otherList[index].damagerWeight = max(otherList[index].damagerWeight, opponent.damagerWeight)
			} else {
				val newAIOpponent = AIOpponent(
					opponent.target, baseWeight = config.initialAggroThreshold,
					damagerWeight = opponent.damagerWeight,
					aggroed = opponent.aggroed,
					decay = opponent.decay
				)
				otherList.add(newAIOpponent)
			}

		}
	}

	fun decayEnmity() {
		enmityList.forEach {
			if (it.decay && randomDouble(0.0, 1.0) > difficulty.decayEmityThreshold) {
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

		starship.damagers.entries.associate {
			Pair(it.key.getAITarget(), it.value.points.get())
		}

		enmityList.sortBy {
			-1 * (//descending
				(it.baseWeight
					+ config.damagerWeight * it.damagerWeight
					+ config.sizeWeight * cbrt((it.target as? StarshipTarget)?.ship?.initialBlockCount?.toDouble() ?: 0.0)
					+ config.distanceWeight * calcDistFactor(it.target))
					* (if (it.isAnchor) -1 else 1)
				)
		}
	}

	fun removeOldEnmityTargets() {
		val markedForRemoval = mutableListOf<AIOpponent>()

		for (target in enmityList) {
			val aiTarget = target.target
			when (aiTarget) {
				is StarshipTarget -> if (aiTarget.ship !in ActiveStarships.all()) markedForRemoval.add(target)
				is PlayerTarget -> if (!aiTarget.player.isOnline) markedForRemoval.add(target)
			}
		}

		for (removed in markedForRemoval) enmityList.remove(removed)
	}

	private fun calcDistFactor(target: AITarget): Double {
		return ((config.aggroRange - (getOpponentDistance(target) ?: 0.0)) / (config.aggroRange + 1e-4)).coerceAtLeast(0.0)
	}

	fun getOpponentDistance(target: AITarget): Double? {
		if (target.getWorld() != world) {
			return null
		}
		return target.getLocation(false).toVector().distance(location.toVector())
	}

	fun addTarget(target: AITarget, baseWeight: Double = 1.0, aggroed: Boolean = false, decay: Boolean = true, isAnchor: Boolean = false) {
		val existing = enmityList.find { it.target == target }
		if (existing != null) return

		enmityList.add(AIOpponent(target, baseWeight, aggroed = aggroed, decay = decay, isAnchor = isAnchor))
	}

	fun removeTarget(target: AITarget) {
		enmityList.removeIf { it.target == target }
	}

	fun removeAnchor() {
		enmityList.removeIf { it.isAnchor }
	}

	fun anchorOnly() : Boolean{
		return enmityList.size == 1 && enmityList.first().isAnchor
	}


	override fun onShipSink(event: StarshipSunkEvent) {
		val tempTarget = AIOpponent(StarshipTarget(event.starship))
		enmityList.remove(tempTarget)
	}

	override fun onPLayerDeath(event: PlayerDeathEvent) {
		val tempTarget = AIOpponent(PlayerTarget(event.player))
		enmityList.remove(tempTarget)
	}

	data class AIOpponent(
		var target: AITarget,
		var baseWeight: Double = 0.0,
		var damagerWeight: Double = 0.0,
		var aggroed: Boolean = false,
		val decay: Boolean = true,
		val isAnchor: Boolean = false,
		var damagePoints: Int = 0
	) {

		override fun hashCode(): Int {
			return target.hashCode()
		}

		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (other is AITarget) {
				return target == other
			}
			if (javaClass != other?.javaClass) return false

			other as AIOpponent

			return target == other.target
		}
	}

	companion object {
		fun targetFilter(starship: Starship, aiTarget: AITarget, targetMode: AITarget.TargetMode): Boolean {
			when {
				aiTarget is StarshipTarget && aiTarget.ship.controller is PlayerController -> {
					if (targetMode == AITarget.TargetMode.AI_ONLY) return false
					val player = (aiTarget.ship.controller as PlayerController).player
					if (starship.world.ion.hasFlag(WorldFlag.NOT_SECURE)) return true //ignore prot in unsafe areas
					if (!player.hasProtection()) return true // check for prot
					if (starship.damagers.keys.any { (it as PlayerDamager).player == player }) return true //fire first
				}

				aiTarget is StarshipTarget && aiTarget.ship.controller is AIController -> {
					return if (targetMode == AITarget.TargetMode.PLAYER_ONLY) {
						false
					} else {
						aiTarget.ship.controller != starship.controller
					}
				}

				aiTarget is PlayerTarget && targetMode != AITarget.TargetMode.AI_ONLY -> {
					if (starship.world.ion.hasFlag(WorldFlag.NOT_SECURE)) return true
					return !aiTarget.player.hasProtection()
				}
			}
			return false
		}

		/** returns true if the faction is different*/
		private fun evaluateFaction(controller: AIController, otherController : AIController) : Boolean{
			val otherFaction = otherController.getUtilModule(FactionManagerModule::class.java)?.faction ?: return false
			val thisFaction = controller.getUtilModule(FactionManagerModule::class.java)?.faction ?: return false
			return thisFaction != otherFaction
		}

		fun fleetAwareTargetFilter(controller: AIController): (Starship, AITarget, AITarget.TargetMode) -> Boolean =
			FleetAwareTargetFilter@{ starship, target, targetMode ->
				val fleetModule = controller.getUtilModule(AIFleetManageModule::class.java)
				val fleet = fleetModule?.fleet
				val isCaravanProtected = controller.getUtilModule(CaravanModule::class.java) != null

				// Block targeting players in the same fleet or if protected by caravan
				if (target is PlayerTarget) {
					val isSameFleet = fleet?.isMember(target.player.toFleetMember()) == true
					if (isSameFleet || isCaravanProtected) return@FleetAwareTargetFilter false
				}

				if (target is StarshipTarget) {
					val targetController = target.ship.controller

					// Block if it's a player in the same fleet
					if (targetController is PlayerController) {
						val player = targetController.player
						val isSameFleet = fleet?.isMember(player.toFleetMember()) == true
						if (isSameFleet || isCaravanProtected) return@FleetAwareTargetFilter false
					}

					// Block if it's an AI in the same fleet
					if (targetController is AIController) {
						val targetFleet = targetController.getUtilModule(AIFleetManageModule::class.java)?.fleet
						if (targetFleet != null && targetFleet == fleet) return@FleetAwareTargetFilter false
					}
				}

				// All other cases fall back on normal targeting
				targetFilter(starship, target, targetMode)
			}

		fun naughtyFilter(controller: AIController): (Starship, AITarget, AITarget.TargetMode) -> Boolean =
			FleetAwareTargetFilter@{ starship, target, targetMode ->
				val fleetModule = controller.getUtilModule(AIFleetManageModule::class.java)
				val fleet = fleetModule?.fleet

				val isCaravanProtected = controller.getUtilModule(CaravanModule::class.java) != null

				// Block targeting players in the same fleet or if protected by caravan
				if (target is PlayerTarget) {
					val isSameFleet = fleet?.isMember(target.player.toFleetMember()) == true
					val hasHighBounty = PlayerCache[target.player].bounty > 100000
					if (isSameFleet || !hasHighBounty) return@FleetAwareTargetFilter false
				}

				if (target is StarshipTarget) {
					val targetController = target.ship.controller

					// Block if it's a player in the same fleet
					if (targetController is PlayerController) {
						val player = targetController.player
						val isSameFleet = fleet?.isMember(player.toFleetMember()) == true
						val hasHighBounty = PlayerCache[targetController.player].bounty > 100000
						if (isSameFleet || !hasHighBounty) return@FleetAwareTargetFilter false
					}

					// Block if it's an AI in the same fleet
					if (targetController is AIController) {
						if (isCaravanProtected) return@FleetAwareTargetFilter false
						val targetFleet = targetController.getUtilModule(AIFleetManageModule::class.java)?.fleet
						if (targetFleet != null && targetFleet == fleet) return@FleetAwareTargetFilter false
					}
				}

				// All other cases fall back on normal targeting
				targetFilter(starship, target, targetMode)
			}
	}
}
