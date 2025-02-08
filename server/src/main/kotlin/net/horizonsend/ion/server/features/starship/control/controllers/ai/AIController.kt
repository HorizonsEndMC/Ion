package net.horizonsend.ion.server.features.starship.control.controllers.ai

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.features.ai.AIControllerFactory
import net.horizonsend.ion.server.features.ai.configuration.AIStarshipTemplate.WeaponSet
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.module.steering.SteeringSolverModule
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.PlayerTarget
import net.horizonsend.ion.server.features.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.input.AIShiftFlightInput
import net.horizonsend.ion.server.features.starship.control.movement.MovementHandler
import net.horizonsend.ion.server.features.starship.control.movement.ShiftFlightHandler
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
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockState
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit
import java.util.function.Supplier
import kotlin.reflect.KClass

/**
 * AI Controller.
 * This class can be used to control a starship. It is ticked along with the world it is in.
 *
 * @param starship The starship this controller controls.
 * @param damager The damager of this starship. If transferring to a new controller, preserve this value, otherwise duplicate entries may appear on damage trackers.
 **/
class AIController private constructor(starship: ActiveStarship, damager: Damager) : Controller(damager, starship, "AIController") {
	override var pilotName: Component = text("AI Controller")
	private var color: Color = super.getColor()
	override var movementHandler: MovementHandler = ShiftFlightHandler(this,AIShiftFlightInput(this))
		set(value) {
			field.destroy()
			field = value
			value.create()
			information("Updated control mode to ${value.name}")
		}

	fun setColor(color: Color) { this.color = color }

	/** Build the controller using a module builder */
	constructor(
		starship: ActiveStarship,
		damager: Damager,
		pilotName: Component,
		setupCoreModules: (AIController) -> AIControllerFactory.Builder.ModuleBuilder,
		setupUtilModules: (AIController) -> Set<AIModule>,
		manualWeaponSets: Set<WeaponSet> = setOf(),
		autoWeaponSets: Set<WeaponSet> = setOf(),
	) : this(starship, damager) {
		this.coreModules.putAll(setupCoreModules(this).build())
		this.utilModules.addAll(setupUtilModules(this))

		this.pilotName = pilotName

		this.manualWeaponSets.addAll(manualWeaponSets)
		this.autoWeaponSets.addAll(autoWeaponSets)
	}
	/** AI modules are a collection of classes that are ticked along with the starship. These can control movement, positioning, pathfinding, or more. */
	val coreModules: MutableMap<KClass<out AIModule>, AIModule> = mutableMapOf()

	/** Util modules provide less heavy-duty functions like the glow and don't need to be accessed often. */
	private val utilModules: MutableSet<AIModule> = mutableSetOf()

	fun addUtilModule(module: AIModule) = utilModules.add(module)

	fun <T: AIModule> getUtilModule(clazz: Class<T>) = utilModules.filterIsInstance(clazz).firstOrNull()

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

	// Weapon sets
	private val manualWeaponSets: MutableSet<WeaponSet> = mutableSetOf()

	/** Returns the weapon set that's range contains the specified distance */
	fun getManualSetInRange(distance: Double): WeaponSet? {
		return manualWeaponSets.firstOrNull { it.engagementRange.containsDouble(distance) }
	}

	/** Returns the weapon set that's range contains the specified distance */
	fun getManualSetsInRange(distance: Double): List<WeaponSet> {
		return manualWeaponSets.filter { it.engagementRange.containsDouble(distance) }
	}

	private val autoWeaponSets: MutableSet<WeaponSet> = mutableSetOf()

	/** Returns the weapon set that's range contains the specified distance */
	fun getAutoSetInRange(distance: Double): WeaponSet? {
		return autoWeaponSets.firstOrNull { it.engagementRange.containsDouble(distance) }
	}

	inline fun <reified T> getCoreModuleByType(): T? = coreModules.values.filterIsInstance<T>().firstOrNull()

	// Functionality
	override fun tick() {
		for ((_, module) in coreModules) {
			module.tick()
		}

		for (module in utilModules) {
			module.tick()
		}
	}

	override fun destroy() {
		for ((_, module) in coreModules) {
			module.shutDown()
		}

		for (module in utilModules) {
			module.shutDown()
		}
	}

	override fun onDamaged(damager: Damager) {
		for ((_, module) in coreModules) {
			module.onDamaged(damager)
		}

		for (module in utilModules) {
			module.onDamaged(damager)
		}
	}

	override fun onMove(movement: StarshipMovement) {
		for ((_, module) in coreModules) {
			module.onMove(movement)
		}

		for (module in utilModules) {
			module.onMove(movement)
		}
	}

	override fun onBlocked(movement: StarshipMovement, reason: StarshipMovementException, location: Vec3i?) {
		for ((_, module) in coreModules) {
			module.onBlocked(movement, reason, location)
		}
	}

	/** Checks whether the ship's movement has been blocked within the specified time */
	fun hasBeenBlockedWithin(millis: Long = TimeUnit.SECONDS.toMillis(5)) = starship.lastBlockedTime > (System.currentTimeMillis() - millis)

	/** Gets and filters targets within the starship's world in the specified radius */
	fun getNearbyTargetsInRadius(minRange: Double, maxRange: Double, playerRange: Double = 750.0, filter: (AITarget) -> Boolean): Set<AITarget> {
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
			.filter { it.value in 0.0..playerRange }
			.sortedByValue()
			.keys

		return targets
	}

	override fun toString(): String {
		return "AI Controller[Display Name: ${pilotName.plainText()} Starship: ${starship.identifier}]"
	}

	fun <T: AIModule> getCoreModuleSupplier(identifier: KClass<out AIModule>): Supplier<T> = Supplier {
		@Suppress("UNCHECKED_CAST")
		coreModules[identifier] as T
	}
}
