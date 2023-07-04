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
import net.starlegacy.feature.starship.subsystem.weapon.projectile.TurretLaserProjectile
import net.starlegacy.util.CARDINAL_BLOCK_FACES
import net.starlegacy.util.Notify
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getFacing
import net.starlegacy.util.rightFace
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

	private val turretPivotPoint = Vec3i(0, 3, -4)

	/** Gets the coordinate of the pivot point of the turret **/
	fun getTurretPivotPoint(sign: Sign): Vec3i = getTurretPivotPointOffset(sign.getFacing()) + Vec3i(sign.location)

	fun getTurretPivotPointOffset(face: BlockFace): Vec3i {
		val (x, y, z) = turretPivotPoint
		val right = face.rightFace

		return Vec3i(
			x = (right.modX * x) + (face.modX * z),
			y = y,
			z = (right.modZ * x) + (face.modZ * z)
		)
	}

	override val signText: Array<Component?> = arrayOf(
		text("Anti-Air", NamedTextColor.GOLD),
		text("Particle Cannon", NamedTextColor.AQUA),
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
			if (AntiAirCannonTurretMultiblock.shape.checkRequirementsSpecific(originBlock, face, loadChunks = true, false)) {
				return face
			}
		}

		return null
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		if (!StarshipControl.isHoldingController(player)) return

		if (Multiblocks[sign] !is AntiAirCannonBaseMultiblock) return

		val turretFacing = turretIntact(sign) ?: return player.userError("Turret not intact!")

		// this is just to insert them into the cooldown to prevent accidentally shooting right when boardingm
		// and to prevent leaving right after entering
		val pilotLoc = AntiAirCannonTurretMultiblock.getPilotLoc(sign) ?: return player.userError("Turret not intact!")
		pilotLoc.direction = turretFacing.direction

		AntiAirCannons.cooldown.tryExec(player) {
			player.teleport(pilotLoc)
		}

		event.isCancelled = true
	}

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).anyStairs()
				x(0).wireInputComputer()
				x(1).anyStairs()
			}

			y(0) {
				x(0).ironBlock()
			}

			y(+1) {
				x(0).anyWall()
			}

			y(+2) {
				x(-1).anyStairs()
				x(0).ironBlock()
				x(1).anyStairs()
			}
		}

		z(+1) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).lodestone()
				x(0).lodestone()
				x(1).lodestone()
				x(2).ironBlock()
			}

			y(0) {
				x(-2).anyWall()
				x(-1).anyGlass()
				x(0).anyGlass()
				x(1).anyGlass()
				x(2).anyWall()
			}

			y(+1) {
				x(-2).anyWall()
				x(-1).anyGlass()
				x(0).anyGlass()
				x(1).anyGlass()
				x(2).anyWall()
			}

			y(+2) {
				x(-2).ironBlock()
				x(-1).lodestone()
				x(0).lodestone()
				x(1).lodestone()
				x(2).ironBlock()
			}
		}

		z(+2) {
			y(-1) {
				x(-3).anyStairs()
				x(-2).lodestone()
				x(-1).sponge()
				x(0).sponge()
				x(1).sponge()
				x(2).lodestone()
				x(3).anyStairs()
			}

			y(0) {
				x(-2).anyGlass()
				x(-1).sponge()
				x(0).endRod()
				x(1).sponge()
				x(2).anyGlass()
			}

			y(+1) {
				x(-2).anyGlass()
				x(-1).sponge()
				x(0).endRod()
				x(1).sponge()
				x(2).anyGlass()
			}

			y(+2) {
				x(-3).anyStairs()
				x(-2).lodestone()
				x(-1).sponge()
				x(0).sponge()
				x(1).sponge()
				x(2).lodestone()
				x(3).anyStairs()
			}
		}

		z(+3) {
			y(-1) {
				x(-3).ironBlock()
				x(-2).lodestone()
				x(-1).sponge()
				x(0).diamondBlock()
				x(1).sponge()
				x(2).lodestone()
				x(3).ironBlock()
			}

			y(0) {
				x(-3).anyWall()
				x(-2).anyGlass()
				x(-1).endRod()
				x(0).diamondBlock()
				x(1).endRod()
				x(2).anyGlass()
				x(3).anyWall()
			}

			y(+1) {
				x(-3).anyWall()
				x(-2).anyGlass()
				x(-1).endRod()
				x(0).diamondBlock()
				x(1).endRod()
				x(2).anyGlass()
				x(3).anyWall()
			}

			y(+2) {
				x(-3).ironBlock()
				x(-2).lodestone()
				x(-1).sponge()
				x(0).diamondBlock()
				x(1).sponge()
				x(2).lodestone()
				x(3).ironBlock()
			}
		}

		z(+4) {
			y(-1) {
				x(-3).anyStairs()
				x(-2).lodestone()
				x(-1).sponge()
				x(0).sponge()
				x(1).sponge()
				x(2).lodestone()
				x(3).anyStairs()
			}

			y(0) {
				x(-2).anyGlass()
				x(-1).sponge()
				x(0).endRod()
				x(1).sponge()
				x(2).anyGlass()
			}

			y(+1) {
				x(-2).anyGlass()
				x(-1).sponge()
				x(0).endRod()
				x(1).sponge()
				x(2).anyGlass()
			}

			y(+2) {
				x(-3).anyStairs()
				x(-2).lodestone()
				x(-1).sponge()
				x(0).sponge()
				x(1).sponge()
				x(2).lodestone()
				x(3).anyStairs()
			}
		}

		z(+5) {
			y(-1) {
				x(-2).ironBlock()
				x(-1).lodestone()
				x(0).lodestone()
				x(1).lodestone()
				x(2).ironBlock()
			}

			y(0) {
				x(-2).anyWall()
				x(-1).anyGlass()
				x(0).anyGlass()
				x(1).anyGlass()
				x(2).anyWall()
			}

			y(+1) {
				x(-2).anyWall()
				x(-1).anyGlass()
				x(0).anyGlass()
				x(1).anyGlass()
				x(2).anyWall()
			}

			y(+2) {
				x(-2).ironBlock()
				x(-1).lodestone()
				x(0).lodestone()
				x(1).lodestone()
				x(2).ironBlock()
			}
		}

		z(+6) {
			y(-1) {
				x(-1).anyStairs()
				x(0).ironBlock()
				x(1).anyStairs()
			}

			y(0) {
				x(0).anyWall()
			}

			y(+1) {
				x(0).anyWall()
			}

			y(+2) {
				x(-1).anyStairs()
				x(0).ironBlock()
				x(1).anyStairs()
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

	val firePoints = listOf(
		Vec3i(-3, 3, 7), // Left
		Vec3i(3, 3, 7) // Right
	)

	// Centered on pivot point
	override fun MultiblockShape.buildStructure() {
		y(+0) {
			z(-3) {
				x(-1).stainedTerracotta()
				x(+0).stainedTerracotta()
				x(+1).stainedTerracotta()
			}
			z(-2) {
				x(-2).stainedTerracotta()
				x(-1).carbyne()
				x(+0).carbyne()
				x(+1).carbyne()
				x(+2).stainedTerracotta()
			}
			z(-1) {
				x(-3).anyStairs()
				x(-2).carbyne()
				x(-1).carbyne()
				x(+0).carbyne()
				x(+1).carbyne()
				x(+2).carbyne()
				x(+3).anyStairs()
			}
			z(0) {
				x(-3).stainedTerracotta()
				x(-2).carbyne()
				x(-1).carbyne()
				x(+0).carbyne()
				x(+1).carbyne()
				x(+2).carbyne()
				x(+3).stainedTerracotta()
			}
			z(1) {
				x(-3).anyStairs()
				x(-2).carbyne()
				x(-1).carbyne()
				x(+0).carbyne()
				x(+1).carbyne()
				x(+2).carbyne()
				x(+3).anyStairs()
			}
			z(2) {
				x(-2).stainedTerracotta()
				x(-1).carbyne()
				x(+0).carbyne()
				x(+1).carbyne()
				x(+2).stainedTerracotta()
			}
			z(3) {
				x(-1).anyStairs()
				x(+0).stainedTerracotta()
				x(+1).anyStairs()
			}
		}
		y(+1) {
			z(-4) {
				x(0).anyStairs()
			}
			z(-3) {
				x(-2).anySlab()
				x(-1).stainedTerracotta()
				x(+0).carbyne()
				x(+1).stainedTerracotta()
				x(+2).anySlab()
			}
			z(-2) {
				x(-2).stainedTerracotta()
				x(-1).carbyne()
				x(+0).carbyne()
				x(+1).carbyne()
				x(+2).stainedTerracotta()
			}
			z(-1) {
				x(-2).stainedTerracotta()
				x(-1).carbyne()
				x(+0).carbyne()
				x(+1).carbyne()
				x(+2).stainedTerracotta()
			}
			z(0) {
				x(-2).stainedTerracotta()
				x(-1).carbyne()
				x(+0).carbyne()
				x(+1).carbyne()
				x(+2).stainedTerracotta()
			}
			z(1) {
				x(-2).anySlab()
				x(-1).stainedTerracotta()
				x(+0).stainedTerracotta()
				x(+1).stainedTerracotta()
				x(+2).anySlab()
			}
			z(2) {
				x(-1).anyTrapdoor()
				x(+0).anySlab()
				x(+1).anyTrapdoor()
			}
		}
		y(+2) {
			z(-4) {
				x(-1).anyStairs()
				x(+0).stainedTerracotta()
				x(+1).anyStairs()
			}
			z(-3) {
				x(-2).stainedTerracotta()
				x(-1).carbyne()
				x(+0).carbyne()
				x(+1).carbyne()
				x(+2).stainedTerracotta()
			}
			z(-2) {
				x(-3).anyTrapdoor()
				x(-2).stainedTerracotta()
				x(-1).carbyne()
				x(+0).carbyne()
				x(+1).carbyne()
				x(+2).stainedTerracotta()
				x(+3).anyTrapdoor()
			}
			z(-1) {
				x(-4).anyStairs()
				x(-3).anySlab()
				x(-2).stainedTerracotta()
				x(-1).carbyne()
				x(+0).carbyne()
				x(+1).carbyne()
				x(+2).stainedTerracotta()
				x(+3).anySlab()
				x(+4).anyStairs()
			}
			z(0) {
				x(-3).anySlab()
				x(-2).type(Material.IRON_BARS)
				x(-1).anyStairs()
				x(+0).anyGlass()
				x(+1).anyStairs()
				x(+2).type(Material.IRON_BARS)
				x(+3).anySlab()
			}
			z(+1) {
				x(-3).anyTrapdoor()
				x(+0).anyGlass()
				x(+3).anyTrapdoor()
			}
		}
		y(+3) {
			z(-3) {
				x(-3).anyWall()
				x(-2).type(Material.IRON_BARS)
				x(-1).anySlab()
				x(0).anyStairs()
				x(+1).anySlab()
				x(+2).type(Material.IRON_BARS)
				x(+3).anyWall()
			}
			z(-2) {
				x(-3).netheriteBlock()
				x(-2).anyWall()
				x(-1).stainedTerracotta()
				x(0).stainedTerracotta()
				x(+1).stainedTerracotta()
				x(+2).anyWall()
				x(+3).netheriteBlock()
			}
			z(-1) {
				x(-4).anyWall()
				x(-3).dispenser()
				x(-2).titaniumBlock()
				x(-1).stainedTerracotta()
				x(0).carbyne()
				x(+1).stainedTerracotta()
				x(+2).titaniumBlock()
				x(+3).dispenser()
				x(+4).anyWall()
			}
			z(0) {
				x(-3).netheriteBlock()
				x(-2).anyWall()
				x(-1).type(Material.IRON_BARS)
				x(0).anyGlass()
				x(+1).type(Material.IRON_BARS)
				x(+2).anyWall()
				x(+3).netheriteBlock()
			}
			z(+1) {
				x(-3).type(Material.ANVIL)
				x(-2).type(Material.IRON_BARS)
				// Air
				x(+2).type(Material.IRON_BARS)
				x(+3).type(Material.ANVIL)
			}
			z(+2) {
				x(-3).type(Material.GRINDSTONE)
				// Air
				x(+3).type(Material.GRINDSTONE)
			}
			z(+3) {
				x(-3).type(Material.GRINDSTONE)
				// Air
				x(+3).type(Material.GRINDSTONE)
			}
			z(+4) {
				x(-3).endRod()
				// Air
				x(+3).endRod()
			}
			z(+5) {
				x(-3).endRod()
				// Air
				x(+3).endRod()
			}
			z(+6) {
				x(-3).endRod()
				// Air
				x(+3).endRod()
			}
		}
		y(+4) {
			z(-2) {
				x(-3).anyTrapdoor()
				x(+3).anyTrapdoor()
			}

			z(-1) {
				x(-4).anyStairs()
				x(-3).anySlab()
				x(-1).anyTrapdoor()
				x(0).anySlab()
				x(+1).anyTrapdoor()
				x(+3).anySlab()
				x(+4).anyStairs()
			}

			z(+0) {
				x(-3).anyTrapdoor()
				x(+3).anyTrapdoor()
			}
		}
	}

	override fun getFacing(sign: Sign): BlockFace = AntiAirCannonBaseMultiblock.turretIntact(sign) ?:
		error("Failed to find a face for sign at ${sign.location}")

	// From the pivot point
	private val pilotOffset: Vec3i = Vec3i(+0, +2, +0)

	// Convert from local multiblock offset to absolute offsets, provided which direction its facing
	private val pilotOffsets: Map<BlockFace, Vec3i> = CARDINAL_BLOCK_FACES.associate { inward ->
		val vec = getTurretPilotPointOffset(inward)

		return@associate inward to vec
	}

	fun getTurretPilotPointOffset(face: BlockFace): Vec3i {
		val (x, y, z) = pilotOffset
		val right = face.rightFace

		return Vec3i(
			x = (right.modX * x) + (face.modX * z),
			y = y,
			z = (right.modZ * x) + (face.modZ * z)
		)
	}

	// Function for getting the offset
	fun getPilotOffset(face: BlockFace): Vec3i = pilotOffsets.getValue(face)

	fun getPilotLoc(sign: Sign, facing: BlockFace): Location {
		// In absolute coordinates
		val pivotPoint = AntiAirCannonBaseMultiblock.getTurretPivotPoint(sign)

		// In local offset from the pivot point
		val turretOffset = getPilotOffset(facing.oppositeFace)

		val pilotLoc: Vec3i = pivotPoint + turretOffset

		return pilotLoc.toLocation(sign.world)
			// Center location at bottom of block
			.add(0.5, 0.0, 0.5)
	}

	fun getPilotLoc(sign: Sign): Location? {
		val facing: BlockFace = AntiAirCannonBaseMultiblock.turretIntact(sign) ?: return null

		return getPilotLoc(sign, facing)
	}

	private fun getPivotPointFromPilot(player: Player): Vec3i? {
		val window = Vec3i(player.location)

		for (face in CARDINAL_BLOCK_FACES) {
			val offset = getPilotOffset(face)
			val pivotPoint = window - offset
			val (x, y, z) = pivotPoint

			val originBlock = player.world.getBlockAt(x, y, z)

			if (AntiAirCannonTurretMultiblock.shape.checkRequirementsSpecific(originBlock, face.oppositeFace, loadChunks = true, false)) {
				return pivotPoint
			}
		}

		return null
	}

	private fun getSignFromPivotPoint(world: World, pivotPoint: Vec3i): Sign? {
		for (face in CARDINAL_BLOCK_FACES) {
			val offset = AntiAirCannonBaseMultiblock.getTurretPivotPointOffset(face)
			val signLoc = pivotPoint - offset
			val (x, y, z) = signLoc

			val originBlock = world.getBlockAt(x, y, z)
			val sign = originBlock.state as? Sign ?: continue

			if (Multiblocks[sign] === AntiAirCannonBaseMultiblock) return sign
		}

		return null
	}

	fun getSignFromPilot(player: Player): Sign? {
		val pivotPoint = getPivotPointFromPilot(player) ?: return null

		return getSignFromPivotPoint(player.world, pivotPoint)
	}

	fun moveEntitiesInWindow(sign: Sign, oldFace: BlockFace, newFace: BlockFace) {
		if (oldFace == newFace) return

		val oldPilotBlock = getPilotLoc(sign, oldFace).block
		val newPilotLoc = getPilotLoc(sign, newFace)

		for (entity in oldPilotBlock.chunk.entities) {
			val entityLoc = entity.location

			if (entityLoc.block != oldPilotBlock) continue

			val loc = newPilotLoc.clone()
			loc.direction = entityLoc.direction

			entity.teleport(
				loc
			)
		}
	}

	fun shoot(shooter: Player, turretBaseSign: Sign) {
//		val dir = shooter.location.direction
//
//		getFirePointOffset
//
//		val origin = firePoints.random()
//
//		TurretLaserProjectile(
//			starship = null,
//
//		)
	}
}
