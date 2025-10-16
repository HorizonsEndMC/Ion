package net.horizonsend.ion.server.features.starship.subsystem.misc.tractor

import io.papermc.paper.raytracing.RayTraceTarget
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.configuration.starship.NewStarshipBalancing
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.multiblock.type.defense.passive.areashield.AreaShield
import net.horizonsend.ion.server.features.multiblock.type.starship.misc.TractorBeamBaseMultiblock
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.movement.TransformationAccessor
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.features.starship.subsystem.BalancedSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.ProceduralSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.FiredSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.listener.misc.ProtectionListener
import net.horizonsend.ion.server.miscellaneous.playSoundInRadius
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.alongVector
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.cube
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distance
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.getTypeSafe
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.text.DecimalFormat
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier
import kotlin.math.roundToInt

class TractorBeamSubsystem(
	starship: Starship,
	pos: Vec3i,
	override var face: BlockFace,
	val multiblock: TractorBeamBaseMultiblock
) : FiredSubsystem(starship, pos), DirectionalSubsystem, ManualWeaponSubsystem, ProceduralSubsystem, BalancedSubsystem<NewStarshipBalancing.TractorBalancing> {
	override val balancingSupplier: Supplier<NewStarshipBalancing.TractorBalancing> = starship.balancingManager.getSubsystemSupplier(TractorBeamSubsystem::class)

	/**
	 * Store of the length of the tiled section of the procedural multiblock structure
	 **/
	private var structureLength = 0

	/**
	 * Stores whether the detection of the procedural structure was successful
	 **/
	private var structureIntact: Boolean = false

	private var controlMode: TractorControlMode = TractorControlMode.FOLLOW
	var towState: TowState = TowState.Empty
		private set(value) {
			field = value
			starship.recalculateManualMoveCooldown()
			starship.generateThrusterMap()
		}

	override fun canFire(dir: Vector, target: Vector): Boolean {
		return towState.canStartDiscovery() && structureIntact
	}

	override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
		return target.clone().subtract(getFirePosition().toVector()).normalize()
	}

	fun setControlMode(new: TractorControlMode) {
		controlMode.onStop(starship)
		controlMode = new
		new.onSetup(starship)
	}

	override fun manualFire(shooter: Damager, dir: Vector, target: Vector) {
		if (shooter !is PlayerDamager) return
		val player = shooter.player

		val existingState = towState
		if (existingState is TowState.Discovering) return

		val originLoc = getFirePosition()

		val hitBlock = starship.world.rayTrace {
			it.direction(dir)
			it.start(originLoc)
			it.blockFilter { verifyBlock(shooter.player, it) }
			it.maxDistance(balancing.range)
			it.targets(RayTraceTarget.BLOCK)
		}?.hitBlock

		playSoundInRadius(originLoc, balancing.range * 20.0, balancing.shootSound.sound)

		val distance = distance(target, originLoc.toVector())
		var beamDistance = minOf(balancing.range.roundToInt(), distance.roundToInt())

		if (hitBlock != null) beamDistance = minOf(beamDistance, getFirePosition().distance(hitBlock.location).roundToInt())

		// Draw beam
		originLoc
			.alongVector(dir.clone().multiply(beamDistance), beamDistance)
			.forEach { intermediate ->
				starship.world.spawnParticle(Particle.SOUL_FIRE_FLAME, intermediate.x, intermediate.y, intermediate.z, 0, 0.0, 0.0, 0.0, 0.0, null, true)
			}

		if (hitBlock == null) return

		if (existingState is TowState.Full) {
			if (existingState.blocks.contains(hitBlock.x, hitBlock.y, hitBlock.z)) {
				towState = TowState.Empty

				starship.information("Released {0} blocks.", existingState.blocks.blocks.size)
				starship.playSound(balancing.releaseSound.sound)

				return
			}
		}

		val future = CompletableFuture<TowedBlocks?>()

		future.whenComplete { blocks: TowedBlocks?, exception: Throwable? ->
			if (exception != null) {
				towState = TowState.Empty
				starship.information("There was an error when collecting blocks, the load has been released.")
				return@whenComplete
			}

			if (blocks == null) {
				// Fall back to previous if failed
				(towState as? TowState.Discovering)?.let { discovering -> towState = discovering.previous }
				return@whenComplete
			}

			var amount = blocks.mass
			var unit = "kg"

			if (amount > 1000.0) {
				unit = "tons"
				amount /= 1000.0
			}

			val massFormatted = "${massFormat.format(amount)} $unit"

			starship.information("Acquired {0} blocks, weighing {1}.", blocks.blocks.size, massFormatted)
			starship.playSound(balancing.acquireSound.sound)

			towState = TowState.Full(blocks)
		}

		towState = TowState.Discovering(future = future, previous = towState)

		Tasks.async {
			val new = TowedBlocks.build(player, Vec3i(hitBlock.location), this)
			if (!new.isSuccess()) new.sendReason(starship)

			future.complete(new.result)
		}
	}

	override fun onMovement(oldWorld: World, movement: TransformationAccessor, success: Boolean) {
		if (movement !is TranslateMovement || !success || controlMode != TractorControlMode.FOLLOW) return

		towState.handleMovement(movement)
	}

	override fun tick() {
		val state = towState as? TowState.Full ?: return

		if (checkStateDistance(state)) {
			starship.alert("The towed load has gone out of range and been lost!")

			towState = TowState.Empty

			return
		}

		val minVec = state.blocks.minPoint
		val maxVec = state.blocks.maxPoint.plus(Vec3i(1, 1, 1))

		cube(
            minVec.toLocation(starship.world),
            maxVec.toLocation(starship.world)
        ).forEach { t ->
			starship.playerPilot?.spawnParticle(Particle.ELECTRIC_SPARK, t.x, t.y, t.z, 1, 0.0, 0.0, 0.0, 0.0, null, true)
		}
	}

	/**
	 * Returns true if the towed load becomes out of range
	 **/
	fun checkStateDistance(state: TowState.Full): Boolean {
		val range = balancing.range
		val firePos = Vec3i(getFirePosition())

		val minPoint = state.blocks.minPoint
		val maxPoint = state.blocks.maxPoint

		val distances: Set<Double> = setOf(
			Vec3i(minPoint.x, minPoint.y, minPoint.z).distance(firePos),
			Vec3i(minPoint.x, minPoint.y, maxPoint.z).distance(firePos),
			Vec3i(maxPoint.x, minPoint.y, minPoint.z).distance(firePos),
			Vec3i(maxPoint.x, minPoint.y, maxPoint.z).distance(firePos),
			Vec3i(minPoint.x, maxPoint.y, minPoint.z).distance(firePos),
			Vec3i(minPoint.x, maxPoint.y, maxPoint.z).distance(firePos),
			Vec3i(maxPoint.x, maxPoint.y, minPoint.z).distance(firePos),
			Vec3i(maxPoint.x, maxPoint.y, maxPoint.z).distance(firePos)
		)

		return !distances.any { it <= range }
	}

	fun verifyBlock(player: Player, block: Block): Boolean {
		val type = block.getTypeSafe() ?: return false

		when {
			type.isAir -> return false
			type == Material.WATER -> return false
			type == Material.LAVA -> return false
		}

		if (starship.contains(block.x, block.y, block.z)) {
			return false
		}

		if (block.world.ion.detectionForbiddenBlocks.contains(toBlockKey(block.x, block.y, block.z))) {
			return false
		}

		if (ActiveStarships.findByBlock(block) != null) {
			return false
		}

		if (DeactivatedPlayerStarships.getContaining(block.world, block.x, block.y, block.z) != null) {
			return false
		}

		if (Space.isCelestialBody(starship.world, block.x.toDouble(), block.y.toDouble(), block.z.toDouble())) {
			return false
		}

		if (starship.world.ion.multiblockManager.getMultiblockEntity(block.x, block.y, block.z) is AreaShield.AreaShieldEntity) {
			return false
		}

		return !ProtectionListener.denyBlockAccess(player, block.location, null)
	}

	companion object {
		const val MAX_STRUCTURE_LENGTH = 150
		val massFormat = DecimalFormat("##.##")
	}

	fun getTowed(): TowedBlocks? = (towState as? TowState.Full)?.blocks

	fun getTowLimit() = if (structureIntact) structureLength * 10000 else 0

	override fun getMaxPerShot(): Int? {
		return null
	}

	override fun detectStructure() {
		val structureDirection = face.oppositeFace
		val detectionOrigin = multiblock.getTileOrigin(pos, structureDirection)

		var length = 0
		var failure = false

		while (length < MAX_STRUCTURE_LENGTH) {
			val position = multiblock.getOriginRelativePosition(detectionOrigin, structureDirection, length + 1)
			val block = starship.world.getBlockAt(position.x, position.y, position.z)

			debugAudience.highlightBlock(Vec3i(position.x, position.y, position.z), 20L)

			if (multiblock.originMatchesTiledStructure(origin = block, direction = structureDirection, loadChunks = false)) {
				length++
				continue
			}

			if (multiblock.originMatchesCapStructure(origin = block, direction = structureDirection, loadChunks = false)) {
				break
			}

			failure = true
			break
		}

		structureIntact = !failure
		this.structureLength = length
	}

	override fun isIntact(): Boolean {
		if (!structureIntact) return false

		val structureDirection = face.oppositeFace
		if (!multiblock.blockMatchesStructure(starship.world.getBlockAt(pos.x, pos.y, pos.z).getRelative(structureDirection), structureDirection)) return false

		val tileOrigin = multiblock.getTileOrigin(pos, structureDirection)

		for (prog in 1..structureLength) {
			val progPosition = multiblock.getOriginRelativePosition(tileOrigin, structureDirection, prog)
			val block = starship.world.getBlockAt(progPosition.x, progPosition.y, progPosition.z)

			if (!multiblock.originMatchesTiledStructure(origin = block, direction = structureDirection, loadChunks = false)) return false
		}

		val endOrigin = multiblock.getOriginRelativePosition(tileOrigin, structureDirection, structureLength + 1)
		val endBlock = starship.world.getBlockAt(endOrigin.x, endOrigin.y, endOrigin.z)

		return multiblock.originMatchesCapStructure(origin = endBlock, direction = structureDirection, loadChunks = false)
	}

	private fun getFirePosition(): Location {
		val structureDirection = face.oppositeFace

		val tileOrigin = multiblock.getTileOrigin(pos, structureDirection)
		val tileEnd = multiblock.getOriginRelativePosition(tileOrigin, structureDirection, structureLength)
		val capEnd = getRelative(tileEnd, structureDirection, right = multiblock.firePosOffset.x, up = multiblock.firePosOffset.y, forward = multiblock.firePosOffset.z)

		return capEnd.toCenterVector().toLocation(starship.world)
	}
}
