package net.horizonsend.ion.server.features.starship.subsystem.misc.tug

import io.papermc.paper.raytracing.RayTraceTarget
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.multiblock.type.starship.misc.TugBaseMultiblock
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.movement.TransformationAccessor
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.ProceduralSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.FiredSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.listener.misc.ProtectionListener
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
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.concurrent.CompletableFuture
import kotlin.math.roundToInt

class TugSubsystem(starship: Starship, pos: Vec3i, override var face: BlockFace, val multiblock: TugBaseMultiblock) : FiredSubsystem(starship, pos), DirectionalSubsystem, ManualWeaponSubsystem, ProceduralSubsystem {
	private var controlMode: TugControlMode = TugControlMode.FOLLOW
	var towState: TowState = TowState.Empty; private set

	override fun canFire(dir: Vector, target: Vector): Boolean {
		return towState.canStartDiscovery() && intact
	}

	override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
		starship.highlightBlock(Vec3i(target), 10L)
		return target.clone().subtract(getFirePosition().toVector()).normalize()
	}

	fun setControlMode(new: TugControlMode) {
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

		val distance = distance(target, originLoc.toVector())
		val beamDistance = minOf(100, distance.roundToInt())

		// Draw beam
		originLoc
			.alongVector(dir.clone().multiply(beamDistance), beamDistance)
			.forEach { intermediate ->
				starship.world.spawnParticle(Particle.SOUL_FIRE_FLAME, intermediate.x, intermediate.y, intermediate.z, 0, 0.0, 0.0, 0.0, 0.0, null, true)
			}

		val hitBlock = starship.world.rayTrace {
			it.direction(dir)
			it.start(originLoc)
			it.blockFilter { verifyBlock(shooter.player, it) }
			it.maxDistance(100.0)
			it.targets(RayTraceTarget.BLOCK)
		}?.hitBlock ?: return

		if (existingState is TowState.Full) {
			if (existingState.blocks.contains(hitBlock.x, hitBlock.y, hitBlock.z)) {
				towState = TowState.Empty

				starship.information("Released {0} blocks", existingState.blocks.blocks.size)

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

			starship.information("Acquired {0} blocks", blocks.blocks.size)

			towState = TowState.Full(blocks)
		}

		towState = TowState.Discovering(future = future, previous = towState)

		Tasks.async {
			val new = TowedBlocks.build(player, Vec3i(hitBlock.location), this)
			if (!new.isSuccess()) new.sendReason(starship)

			future.complete(new.result)
		}
	}

	override fun onMovement(movement: TransformationAccessor, success: Boolean) {
		if (movement !is TranslateMovement || !success || controlMode != TugControlMode.FOLLOW) return
		towState.handleMovement(movement)
	}

	override fun tick() {
		val state = towState as? TowState.Full ?: return

		val minVec = state.blocks.minPoint
		val maxVec = state.blocks.maxPoint.plus(Vec3i(1, 1, 1))

		cube(
            minVec.toLocation(starship.world),
            maxVec.toLocation(starship.world)
        ).forEach { t ->
			starship.playerPilot?.spawnParticle(Particle.ELECTRIC_SPARK, t.x, t.y, t.z, 1, 0.0, 0.0, 0.0, 0.0, null, true)
		}
	}

	fun verifyBlock(player: Player, block: Block): Boolean {
		val type = block.getTypeSafe() ?: return false
		if (type.isAir) return false

		if (starship.contains(block.x, block.y, block.z)) {
			return false
		}

		if (block.world.ion.detectionForbiddenBlocks.contains(toBlockKey(block.x, block.y, block.z))) return false

		if (ActiveStarships.findByBlock(block) != null) {
			return false
		}

		if (DeactivatedPlayerStarships.getContaining(block.world, block.x, block.y, block.z) != null) {
			return false
		}

		return !ProtectionListener.denyBlockAccess(player, block.location, null)
	}

	companion object {
		const val MAX_LENGTH = 150
	}

	fun getTowed(): TowedBlocks? = (towState as? TowState.Full)?.blocks

	fun getTowLimit() = if (intact) length * 5000 else 0

	override fun getMaxPerShot(): Int? {
		return null
	}

	var length = 0
	var intact: Boolean = false

	override fun detectStructure() {
		val structureDirection = face.oppositeFace
		val detectionOrigin = multiblock.getTileOrigin(pos, structureDirection)

		var length = 0
		var failure = false

		while (length < MAX_LENGTH) {
			val position = multiblock.getOriginRelativePosition(detectionOrigin, structureDirection, length + 1)
			val block = starship.world.getBlockAt(position.x, position.y, position.z)

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

		intact = !failure
		this.length = length
	}

	override fun isIntact(): Boolean {
		if (!intact) return false

		val structureDirection = face.oppositeFace
		if (!multiblock.blockMatchesStructure(starship.world.getBlockAt(pos.x, pos.y, pos.z).getRelative(structureDirection), structureDirection)) return false

		val tileOrigin = multiblock.getTileOrigin(pos, structureDirection)

		for (prog in 1..length) {
			val progPosition = multiblock.getOriginRelativePosition(tileOrigin, structureDirection, prog)
			val block = starship.world.getBlockAt(progPosition.x, progPosition.y, progPosition.z)

			if (!multiblock.originMatchesTiledStructure(origin = block, direction = structureDirection, loadChunks = false)) return false
		}

		val endOrigin = multiblock.getOriginRelativePosition(tileOrigin, structureDirection, length + 1)
		val endBlock = starship.world.getBlockAt(endOrigin.x, endOrigin.y, endOrigin.z)

		debugAudience.highlightBlock(endOrigin, 30L)

		return multiblock.originMatchesCapStructure(origin = endBlock, direction = structureDirection, loadChunks = false)
	}

	private fun getFirePosition(): Location {
		val structureDirection = face.oppositeFace

		val tileOrigin = multiblock.getTileOrigin(pos, structureDirection)
		val tileEnd = multiblock.getOriginRelativePosition(tileOrigin, structureDirection, length)
		val capEnd = getRelative(tileEnd, structureDirection, right = multiblock.firePosOffset.x, up = multiblock.firePosOffset.y, forward = multiblock.firePosOffset.z)

		return capEnd.toCenterVector().toLocation(starship.world)
	}
}
