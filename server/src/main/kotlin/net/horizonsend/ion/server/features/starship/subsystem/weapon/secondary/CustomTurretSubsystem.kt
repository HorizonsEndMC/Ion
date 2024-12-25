package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.client.display.modular.BlockDisplayWrapper
import net.horizonsend.ion.server.features.client.display.modular.MultiBlockDisplay
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.TurretBaseMultiblock
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.StarshipSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getChunkAtIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.toChunkLocal
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.network.protocol.game.ClientboundGameEventPacket
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket
import net.minecraft.world.entity.PositionMoveRotation
import net.minecraft.world.entity.Relative
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.Blocks.AIR
import net.minecraft.world.phys.Vec3
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.joml.Vector2d
import java.util.ArrayDeque
import java.util.EnumSet
import java.util.LinkedList
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class CustomTurretSubsystem(starship: Starship, pos: Vec3i, override var face: BlockFace) : StarshipSubsystem(starship, pos), DirectionalSubsystem {
	override fun isIntact(): Boolean {
		val block = getBlockIfLoaded(starship.world, pos.x, pos.y, pos.z) ?: return false
		return TurretBaseMultiblock.shape.checkRequirements(block, face, loadChunks = false, particles = false)
	}

	lateinit var blockDisplay: MultiBlockDisplay

	var blocks = mutableSetOf<Vec3i>()
	val captiveSubsystems = LinkedList<StarshipSubsystem>()

	fun detectTurret() {
		if (!starship.contains(pos.x, pos.y + 1, pos.z)) return

		val visitedBlocks = ObjectOpenHashSet<Block>()
		val toVisit = ArrayDeque<Block>()

		toVisit.add(starship.world.getBlockAt(pos.x, pos.y + 1, pos.z))

		while (toVisit.isNotEmpty()) {
			val block = toVisit.removeFirst()

			if (!canDetect(block)) continue

			visitedBlocks.add(block)

			for (offsetX in -1..1) for (offsetY in -1..1) for (offsetZ in -1..1) {
				val newBlock = block.getRelative(offsetX, offsetY, offsetZ)

				if (block == newBlock) continue

				if (!starship.contains(newBlock.x, newBlock.y, newBlock.z)) continue

				if (visitedBlocks.contains(newBlock)) continue

				toVisit.addLast(newBlock)
			}
		}

		blocks.addAll(visitedBlocks.map { Vec3i(it.x, it.y, it.z) })
		starship.subsystems.filterTo(captiveSubsystems) { blocks.contains(it.pos) }

		createDisplay()
		sendBlockRemovals()
		Tasks.sync {
			blockDisplay.special?.let {
				spectate(it.second, starship.playerPilot ?: return@sync)
			}
		}
	}

	fun createDisplay() {
		blockDisplay = MultiBlockDisplay.createFromBlocks(
			starship.world,
			pos,
			Vector2d(face.direction.x, face.direction.z),
			blocks.associate { vec3i -> vec3i.minus(pos) to starship.world.getBlockData(vec3i.x, vec3i.y, vec3i.z) }
		)
	}

	private fun canDetect(block: Block): Boolean {
		return block.y > pos.y
	}

	override fun handleRelease() {
		if (!::blockDisplay.isInitialized) return
		blockDisplay.remove()
		restoreTurretBlocks()

		captiveSubsystems.forEach { Bukkit.getPlayer("GutinGongoozler")?.highlightBlock(it.pos, 150L) }
	}

	override fun onMovement(movement: StarshipMovement, success: Boolean) {
		if (!success) return
		if (!::blockDisplay.isInitialized) return
		blockDisplay.displace(movement)
		blocks = blocks.mapTo(mutableSetOf()) { vec -> Vec3i(movement.displaceX(vec.x, vec.z), movement.displaceY(vec.y), movement.displaceZ(vec.z, vec.z)) }
		sendBlockRemovals()
	}

	fun sendBlockRemovals() = Tasks.async {
		val byChunk = blocks.groupBy { ChunkPos(it.x.shr(4), it.z.shr(4)) }

		for ((chunk, blocks) in byChunk) {
			val chunk = starship.world.getChunkAtIfLoaded(chunk.x, chunk.z)?.minecraft ?: continue
			val packets = blocks.map { ClientboundBlockUpdatePacket(BlockPos(it.x, it.y, it.z), AIR.defaultBlockState()) }

			chunk.`moonrise$getChunkAndHolder`().holder.`moonrise$getPlayers`(false).forEach { player ->
				packets.forEach { packet -> player.connection.send(packet) }
			}
		}
	}

	fun restoreTurretBlocks() {
		val byChunk = blocks.groupBy { ChunkPos(it.x.shr(4), it.z.shr(4)) }
		for ((chunk, blocks) in byChunk) {
			val chunk = starship.world.getChunkAtIfLoaded(chunk.x, chunk.z)?.minecraft ?: continue

			val packets = blocks.map {
				val local = it.toChunkLocal()
				val state = chunk.getBlockState(local.x, local.y, local.z)
				ClientboundBlockUpdatePacket(BlockPos(it.x, it.y, it.z), state)
			}

			chunk.`moonrise$getChunkAndHolder`().holder.`moonrise$getPlayers`(false).forEach { player ->
				packets.forEach { packet -> player.connection.send(packet) }
			}
		}
	}

	private var lastTick = System.currentTimeMillis()

	fun rotate() {
		if (!::blockDisplay.isInitialized) return
		val playerPilot = starship.playerPilot ?: return

		// Use a delta so lag doesn't impact rotation too bad
		val time = System.currentTimeMillis()
		val deltaSeconds = (time - lastTick) / 1000.0
		lastTick = time

		val old = blockDisplay.heading // Current position
		val ideal = Vector2d(playerPilot.location.direction.x, playerPilot.location.direction.z)

		val difference = old.angle(ideal)
		val traversal = calculateTraversal(difference, deltaSeconds)

		// Rotate the current angle position by the clamped rotation
		val angleCos = cos(traversal)
		val angleSin = sin(traversal)

		val clamped = Vector2d(
			(old.x * angleCos) + (angleSin * old.y),
			(old.x * -angleSin) + (angleCos * old.y),
		)

		blockDisplay.heading = clamped
	}

	override fun tick() {
		rotate()
	}

	companion object {
		// In radians per second
		val TRAVERSAL_SPEED: Double = Math.toRadians(45.0)
		val TRAVERSAL_SPEED_MIN: Double = Math.toRadians(15.0)

		fun calculateTraversal(differenceRadians: Double, delta: Double): Double {
			var speed = TRAVERSAL_SPEED * delta

			if (abs(differenceRadians) < TRAVERSAL_SPEED) {
				// Decelerate if close to the destination, to a minimum speed
				speed = ((((TRAVERSAL_SPEED - TRAVERSAL_SPEED_MIN) / TRAVERSAL_SPEED) * abs(differenceRadians)) + TRAVERSAL_SPEED_MIN) * delta
			}

			// Clamp the rotation angle at a max speed
			var traversal = minOf(abs(differenceRadians), speed)

			if (differenceRadians > 0) {
				traversal *= -1.0
			}

			return traversal
		}

		fun spectate(wrapper: BlockDisplayWrapper, player: Player) {
			val nms = player.minecraft

			nms.connection.send(ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, GameType.byId(GameMode.SPECTATOR.value).id.toFloat()))
			nms.connection.internalTeleport(
				PositionMoveRotation(wrapper.getEntity().position(), Vec3.ZERO, wrapper.getEntity().xRotO, wrapper.getEntity().yRot),
				EnumSet.noneOf(Relative::class.java)
			)
			nms.connection.send(ClientboundSetCameraPacket(wrapper.getEntity()))
			nms.connection.resetPosition()
		}
	}
}
