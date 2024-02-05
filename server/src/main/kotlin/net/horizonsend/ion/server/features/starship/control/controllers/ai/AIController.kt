package net.horizonsend.ion.server.features.starship.control.controllers.ai

import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.configuration.AISpawningConfiguration.AIStarshipTemplate.WeaponSet
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactory
import net.horizonsend.ion.server.features.starship.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.ai.util.PlayerTarget
import net.horizonsend.ion.server.features.starship.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.sortedByValue
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Color
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import java.util.concurrent.TimeUnit

/**
 * AI Controller.
 * This class can be used to control a starship. It is ticked along with the world it is in.
 *
 * @param starship The starship this controller controls.
 * @param name The name of the controller.
 * @param damager The damager of this starship. If transferring to a new controller, preserve this value, otherwise duplicate entries may appear on damage trackers.
 **/
class AIController private constructor(
	starship: ActiveStarship,
	name: String,
	damager: Damager
) : Controller(damager, starship, name) {
	/** Build the controller using a module builder */
	constructor(
		starship: ActiveStarship,
		name: String,
		damager: Damager,
		pilotName: Component,
		manualWeaponSets: Set<WeaponSet>,
		autoWeaponSets: Set<WeaponSet>,
		createModules: (AIController) -> AIControllerFactory.Builder.ModuleBuilder
	) : this(starship, name, damager) {
		modules.putAll(createModules(this).build())
		setPilotName(pilotName)
		manualWeaponSets.forEach(::addManualWeaponSet)
		autoWeaponSets.forEach(::addAutoWeaponSet)
	}

	/** AI modules are a collection of classes that are ticked along with the starship. These can control movement, positioning, pathfinding, or more. */
	val modules: MutableMap<String, AIModule> = mutableMapOf()

	override var pitch: Float = 0f
	override var yaw: Float = 0f

	override var selectedDirectControlSpeed: Int = 1

	var lastRotation: Long = 0L


	// Disallow mining lasers and other block placement / destroying things for now
	override fun canDestroyBlock(block: Block): Boolean = false
	override fun canPlaceBlock(block: Block, newState: BlockState, placedAgainst: Block): Boolean = false


	// Pass through functions for starship information
	fun getCenter(): Vec3i = starship.centerOfMass
	fun getWorld(): World = starship.world


	// Shield Health indicators
	fun getShields() = starship.shields
	fun getShieldCount() = getShields().size
	fun getAverageShieldHealth() = (getShields().sumOf { it.powerRatio }) / getShieldCount().toDouble()
	fun getMinimumShieldHealth() = (getShields().minOfOrNull { it.powerRatio } ?: 0.0)


	// Control variables
	private var isShiftFlying: Boolean = false
	override fun isSneakFlying(): Boolean = isShiftFlying
	fun setShiftFlying(value: Boolean) { isShiftFlying = value }


	// The variable color, settable
	private var color: Color = super.getColor()
	fun setColor(value: Color) { color = value }
	override fun getColor(): Color = color


	// Settable name
	private var pilotName: Component = text("AI Controller")
	override fun getPilotName(): Component = pilotName
	fun setPilotName(value: Component) { pilotName = value }


	// Weapon sets
	private val manualWeaponSets: MutableSet<WeaponSet> = mutableSetOf()
	fun addManualWeaponSet(set: WeaponSet) = manualWeaponSets.add(set)
	fun getAllManualSets() = manualWeaponSets
	/** Returns the weapon set that's range contains the specified distance */
	fun getManualSetInRange(distance: Double): WeaponSet? {
		return manualWeaponSets.firstOrNull { it.engagementRange.containsDouble(distance) }
	}

	private val autoWeaponSets: MutableSet<WeaponSet> = mutableSetOf()
	fun addAutoWeaponSet(set: WeaponSet) = autoWeaponSets.add(set)
	fun getAllAutoSets() = autoWeaponSets
	/** Returns the weapon set that's range contains the specified distance */
	fun getAutoSetInRange(distance: Double): WeaponSet? {
		return autoWeaponSets.firstOrNull { it.engagementRange.containsDouble(distance) }
	}

	inline fun <reified T> getModuleByType(): T? = modules.values.filterIsInstance<T>().firstOrNull()

	//Functionality
	override fun tick() {
		for ((_, module) in modules) {
			module.tick()
		}
	}

	override fun destroy() {
		for ((_, module) in modules) {
			module.shutDown()
		}
	}

	override fun onDamaged(damager: Damager) {
		for ((_, module) in modules) {
			module.onDamaged(damager)
		}
	}

	override fun onMove(movement: StarshipMovement) {
		for ((_, module) in modules) {
			module.onMove(movement)
		}
	}

	override fun onBlocked(movement: StarshipMovement, reason: StarshipMovementException, location: Vec3i?) {
		for ((_, module) in modules) {
			module.onBlocked(movement, reason, location)
		}
	}

	/** Checks whether the ship's movement has been blocked within the specified time */
	fun hasBeenBlockedWithin(millis: Long = TimeUnit.SECONDS.toMillis(5)) = (starship as ActiveControlledStarship).lastBlockedTime > (System.currentTimeMillis() - millis)

	/** Gets and filters targets within the starship's world in the specified radius */
	fun getNearbyTargetsInRadius(minRange: Double, maxRange: Double, filter: (AITarget) -> Boolean): Set<AITarget> {
		val targets = mutableSetOf<AITarget>()

		targets += ActiveStarships.getInWorld(starship.world).map { StarshipTarget(it) }
			.filter(filter)
			.associateWith { it.getVec3i().distance(starship.centerOfMass) }
			.filter { it.value in minRange..maxRange }
			.sortedByValue()
			.keys

		targets += starship.world.players.map { PlayerTarget(it) }
			.filter(filter)
			.associateWith { it.getVec3i().distance(starship.centerOfMass) }
			.filter { it.value in 0.0..50.0 }
			.sortedByValue()
			.keys

		return targets
	}

	override fun toString(): String {
		return "AI Controller[Display Name: ${pilotName.plainText()} Starship: ${starship.identifier}]"
	}
}
