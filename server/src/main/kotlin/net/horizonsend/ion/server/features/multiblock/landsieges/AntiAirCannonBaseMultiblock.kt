package net.horizonsend.ion.server.features.multiblock.landsieges

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.multiblock.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.Multiblocks
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.turret.RotatingMultiblock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.starlegacy.feature.machine.AntiAirCannons
import net.starlegacy.feature.space.Space
import net.starlegacy.feature.starship.control.StarshipControl
import net.starlegacy.util.CARDINAL_BLOCK_FACES
import net.starlegacy.util.Notify
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getFacing
import net.starlegacy.util.rightFace
import okhttp3.internal.notify
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector

object AntiAirCannonBaseMultiblock : Multiblock(), PowerStoringMultiblock, InteractableMultiblock {
	override val name: String = "antiaircannon"
	override val maxPower: Int = 1_000_000

	private val turretPivotPoint = Vec3i(0, 0, 3)

	/** Gets the coordinate of the pivot point of the turret **/
	fun getTurretPivotPoint(sign: Sign): Vec3i {
		val (x, y, z) = turretPivotPoint
		val facing = sign.getFacing().oppositeFace
		val right = facing.rightFace

		return Vec3i(
			x = (right.modX * x) + (facing.modX * z),
			y = y + 3,
			z = (right.modZ * x) + (facing.modZ * z)
		) + Vec3i(sign.location)
	}

	override val signText: Array<Component?> = arrayOf(
		text("Anti-Air", NamedTextColor.GOLD),
		text("Particle Gun", NamedTextColor.AQUA),
		null,
		null
	)

	override fun setupSign(player: Player, sign: Sign) {
		if (Space.moonWorldCache[sign.world].isEmpty) {
			player.userError("You must be on a moon to setup an AA gun.")
			return
		}
		super.setupSign(player, sign)
	}

	/** Provided the sign of the base, get the turret **/
	fun turretIntact(sign: Sign): BlockFace? {
		val (x, y, z) = getTurretPivotPoint(sign)

		val originBlock =  sign.world.getBlockAt(x, y, z)

		for (face: BlockFace in CARDINAL_BLOCK_FACES) {
			println(face)

			if (AntiAirCannonTurretMultiblock.blockMatchesStructure(originBlock, face)) return face
		}

		return null
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		if (!StarshipControl.isHoldingController(player)) return

		if (Multiblocks[sign] !is AntiAirCannonBaseMultiblock) return

		val turretFacing = turretIntact(sign) ?: return player.userError("Turret not intact!")

		println(turretFacing)

		// this is just to insert them into the cooldown to prevent accidentally shooting right when boardingm
		// and to prevent leaving right after entering
		AntiAirCannons.cooldown.tryExec(player) { player.teleport(AntiAirCannonTurretMultiblock.getPilotLoc(sign, turretFacing)) }

		event.isCancelled = true
	}

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).stainedTerracotta()
				x(+0).diamondBlock()
				x(+1).stainedTerracotta()
			}

			y(+0) {
				x(-1).anyStairs()
				x(+0).diamondBlock()
				x(+1).anyStairs()
			}
		}

		z(+1) {
			y(-1) {
				x(-2).anyWall()
				x(-1).stainedTerracotta()
				x(+0).concrete()
				x(+1).stainedTerracotta()
				x(+2).anyWall()
			}

			y(+0) {
				x(-2).anyWall()
				x(-1).ironBlock()
				x(+0).ironBlock()
				x(+1).ironBlock()
				x(+2).anyWall()
			}
		}

		z(+2) {
			y(-1) {
				x(-2).stainedTerracotta()
				x(-1).concrete()
				x(+0).concrete()
				x(+1).concrete()
				x(+2).stainedTerracotta()
			}

			y(+0) {
				x(-2).stainedTerracotta()
				x(-1).ironBlock()
				x(+0).ironBlock()
				x(+1).ironBlock()
				x(+2).stainedTerracotta()
			}

			y(+1) {
				x(-1).anyWall()
				x(+0).type(Material.BEACON)
				x(+1).anyWall()
			}
		}

		z(+3) {
			y(-1) {
				x(-2).anyWall()
				x(-1).stainedTerracotta()
				x(+0).concrete()
				x(+1).stainedTerracotta()
				x(+2).anyWall()
			}

			y(+0) {
				x(-2).anyWall()
				x(-1).ironBlock()
				x(+0).ironBlock()
				x(+1).ironBlock()
				x(+2).anyWall()
			}
		}

		z(+4) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
		}
	}
}

object AntiAirCannonTurretMultiblock: RotatingMultiblock() {
	/** Cooldown between shots **/
	const val cooldownMillis: Long = 1000

	override val name: String = javaClass.simpleName

	override val signText: Array<Component?> = arrayOf(
		null,
		null,
		null,
		null
	)

	init {
		shape.ignoreDirection()
	}

	// Centered on pivot point
	override fun MultiblockShape.buildStructure() {
		z(-2..+2) {
			y(+0) {
				x(-1..+1) {
					carbyne()
				}
			}
		}

		z(-1..+1) {
			y(1..2) {
				x(-1..+1) {
					anyGlass()
				}
			}
		}
	}

	fun getPilotOffset(): Vec3i = Vec3i(+0, +5, +4)

	private val pilotOffsets: Map<BlockFace, Vec3i> = CARDINAL_BLOCK_FACES.associate { inward ->
		val right = inward.rightFace
		val (x, y, z) = getPilotOffset()
		val vec = Vec3i(x = right.modX * x + inward.modX * z, y = y, z = right.modZ * x + inward.modZ * z)

		return@associate inward to vec
	}

	fun getPilotLoc(sign: Sign, face: BlockFace): Location {
		return getPilotLoc(sign.world, sign.x, sign.y, sign.z, face.oppositeFace)
	}

	fun getPilotLoc(world: World, x: Int, y: Int, z: Int, face: BlockFace): Location {
		println(face)
		println("$x, $y, $z")

		println(pilotOffsets)

		return pilotOffsets.getValue(face).toLocation(world).add(x + 0.5, y + 0.0, z + 0.5)
	}

	fun getSignFromPilot(player: Player): Sign? {
		for (face in CARDINAL_BLOCK_FACES) {
			val (x, y, z) = pilotOffsets.getValue(face)
			val loc = player.location.subtract(x.toDouble(), y.toDouble(), z.toDouble())

			val sign = loc.block.getState(false) as? Sign ?: continue

			if (Multiblocks[sign] === this) return sign
		}

		return null
	}

	fun moveEntitiesInWindow(sign: Sign, oldFace: BlockFace, newFace: BlockFace) {
		if (oldFace == newFace) return

		val oldPilotBlock = getPilotLoc(sign, oldFace).block
		val newPilotLoc = getPilotLoc(sign, newFace)
		for (entity in oldPilotBlock.chunk.entities) {
			val entityLoc = entity.location

			if (entityLoc.block != oldPilotBlock) {
				continue
			}

			val loc = newPilotLoc.clone()
			loc.direction = entityLoc.direction
			entity.teleport(newPilotLoc)
		}
	}

	fun shoot(world: World, pos: Vec3i, face: BlockFace, dir: Vector, shooter: Player) { Notify.all(text("pew")) }
}
