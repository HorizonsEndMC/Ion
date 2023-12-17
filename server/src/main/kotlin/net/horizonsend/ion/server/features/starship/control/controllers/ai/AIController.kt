package net.horizonsend.ion.server.features.starship.control.controllers.ai

import net.horizonsend.ion.server.configuration.AIShipConfiguration.AIStarshipTemplate.WeaponSet
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactory
import net.horizonsend.ion.server.features.starship.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.ai.util.StarshipTarget
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.sortedByValue
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import java.util.concurrent.TimeUnit

/**
 * AI Controller.
 * This class can be used to control a starship. It is ticked along with the world it is in.
 *
 * @param starship: The starship this controller controls.
 * @param name: The name of the controller.
 * @param damager: The damager of this starship. If transferring to a new controller, preserve this value, otherwise duplicate entries may appear on damage trackers.
 *
 * @param displayName: The display name of the controller. Used in place of a player's name.
 *
 * @param manualWeaponSets: The manual weapon sets, and their ranges. This value is stored here to allow easier transition between controllers.
 * @param autoWeaponSets: The auto weapon sets. See manual weapon sets.
 **/
class AIController(
	starship: ActiveStarship,
	name: String,
	damager: Damager,

	private var displayName: Component,

	val manualWeaponSets: Set<WeaponSet> = setOf(),
	val autoWeaponSets: Set<WeaponSet> = setOf(),
) : Controller(damager, starship, name) {
	constructor(
		starship: ActiveStarship,
		name: String,
		damager: Damager,
		pilotName: Component,
		manualWeaponSets: Set<WeaponSet>,
		autoWeaponSets: Set<WeaponSet>,
		createModules: (AIController) -> AIControllerFactory.Builder.ModuleBuilder
	) : this(starship, name, damager, pilotName, manualWeaponSets, autoWeaponSets) {
		modules.putAll(createModules(this).build())
	}

	/** AI modules are a collection of classes that are ticked along with the starship. These can control movement, positioning, pathfinding, or more. */
	val modules: MutableMap<String, AIModule> = mutableMapOf()

	override fun getPilotName(): Component = displayName

	// Control variables
	private var isShiftFlying: Boolean = false
	override fun isSneakFlying(): Boolean = isShiftFlying
	fun setShiftFlying(value: Boolean) { isShiftFlying = value }

	override var pitch: Float = 0f
	override var yaw: Float = 0f

	override var selectedDirectControlSpeed: Int = 1

	var lastRotation: Long = 0L

	/** Checks whether the ship's movement has been blocked within the specified time */
	fun hasBeenBlockedWithin(millis: Long = TimeUnit.SECONDS.toMillis(5)) = (starship as ActiveControlledStarship).lastBlockedTime > (System.currentTimeMillis() - millis)

	// Disallow mining lasers and other block placement / destroying things for now
	override fun canDestroyBlock(block: Block): Boolean = false
	override fun canPlaceBlock(block: Block, newState: BlockState, placedAgainst: Block): Boolean = false

	/** Use the direct control center as a sort of cache to avoid the type conversion if possible */
	fun getCenter(): Location = (starship as? ActiveControlledStarship)?.directControlCenter ?: starship.centerOfMass.toLocation(starship.world)
	fun getCenterVec3i(): Vec3i = starship.centerOfMass

	fun getWorld(): World = starship.world


	// Shield Health indicators
	val shields get() = starship.shields
	val shieldCount get() = shields.size
	val averageHealth get() = shields.sumOf { it.powerRatio } / shieldCount.toDouble()

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

	/** Gets and filters targets within the starship's world */
	fun getNearbyTargets(filter: (AITarget) -> Boolean): Set<AITarget> {
		val targets = mutableSetOf<AITarget>()

		targets += ActiveStarships.getInWorld(starship.world).map { StarshipTarget(it) }.filter(filter)
//		targets += starship.world.players.map { PlayerTarget(it) }.filter(filter) // TODO uncomment this

		return targets
	}

	/** Gets and filters targets within the starship's world in the specified radius */
	fun getNearbyTargetsInRadius(minRange: Double, maxRange: Double, filter: (AITarget) -> Boolean): Set<AITarget> = getNearbyTargets(filter)
			.associateWith { it.getVec3i().distance(starship.centerOfMass) }
			.sortedByValue()
			.filter { it.value in minRange..maxRange }
			.keys

	override fun toString(): String {
		return """
			Controller: $name
			Starship: ${starship.identifier}

			Is Shift Flying: $isShiftFlying
			Movement Pitch: $pitch
			Movement Yaw: $yaw

			Last Rotated: $lastRotation

			Modules: ${modules.entries.joinToString { (key, value) -> "$key : $value" }}
		""".trimIndent()
	}
}
