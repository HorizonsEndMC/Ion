package net.horizonsend.ion.server.features.starship.subsystem.misc.tug

import io.papermc.paper.raytracing.RayTraceTarget
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.multiblock.type.starship.misc.TugMultiblock
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.movement.TransformationAccessor
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
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
import net.horizonsend.ion.server.miscellaneous.utils.getTypeSafe
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.concurrent.CompletableFuture
import kotlin.math.roundToInt

class TugSubsystem(starship: Starship, pos: Vec3i, override var face: BlockFace, val multiblock: TugMultiblock) : WeaponSubsystem(starship, pos), DirectionalSubsystem, ManualWeaponSubsystem {
	override val balancing = ConfigurationFiles.starshipBalancing().platformBalancing.weapons.aiHeavyLaser

	override val powerUsage: Int = 0

	private var controlMode: TugControlMode = TugControlMode.FOLLOW
	var towState: TowState = TowState.Empty; private set

	override fun canFire(dir: Vector, target: Vector): Boolean {
		return towState.canStartDiscovery()
	}

	override fun getName(): Component {
		return Component.text("Tug")
	}

	override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
		return target.clone().subtract(getFirePosition().toVector()).normalize()
	}

	private fun getFirePosition(): Location {
		val firePos = getRelative(pos.getRelative(face.oppositeFace), face.oppositeFace, multiblock.firePosOffset.x, multiblock.firePosOffset.y, multiblock.firePosOffset.z)
		return firePos.toCenterVector().toLocation(starship.world)
	}

	override fun isIntact(): Boolean {
		val origin = pos.getRelative(face.oppositeFace)
		starship.highlightBlock(origin, 10L)
		return multiblock.blockMatchesStructure(starship.world.getBlockAt(origin.x, origin.y, origin.z), face.oppositeFace)
	}

	fun setControlMode(new: TugControlMode) {
		controlMode.onStop(starship)
		controlMode = new
		new.onSetup(starship)
	}

	override fun manualFire(shooter: Damager, dir: Vector, target: Vector) {
		if (shooter !is PlayerDamager) return
		val player = shooter.player

		if (towState is TowState.Discovering) return

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

		if (starship.contains(block.x, block.y, block.z)) return false

		if (block.world.ion.detectionForbiddenBlocks.contains(toBlockKey(block.x, block.y, block.z))) return false

		if (ActiveStarships.findByBlock(block) != null) {
			return false
		}

		return !ProtectionListener.denyBlockAccess(player, block.location, null)
	}

	companion object {
		const val MAX_ASTEROID_SIZE = 1_000_000
	}

	fun getTowed(): TowedBlocks? = (towState as? TowState.Full)?.blocks
}
