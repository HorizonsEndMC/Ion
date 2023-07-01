package net.horizonsend.ion.server.features.multiblock.landsieges

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.Multiblocks
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import net.horizonsend.ion.server.features.multiblock.starshipweapon.turret.RotatingMultiblock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.feature.machine.AntiAirCannons
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.TerritoryRegion
import net.starlegacy.feature.space.Space
import net.starlegacy.feature.starship.PilotedStarships
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.control.StarshipControl
import net.starlegacy.feature.starship.subsystem.weapon.projectile.TurretLaserProjectile
import net.starlegacy.util.CARDINAL_BLOCK_FACES
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getFacing
import net.starlegacy.util.rightFace
import net.starlegacy.util.squared
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector

object AntiAirCannonMultiblock : RotatingMultiblock(), PowerStoringMultiblock, InteractableMultiblock {
	override val name: String = "antiaircannon"
	override val maxPower: Int = 1_000_000

	private val ACTIVE_STATE = text("Active", NamedTextColor.GREEN).apply { this.decoration(TextDecoration.BOLD) }
	private val INACTIVE_STATE = text("Inactive", NamedTextColor.RED).apply { this.decoration(TextDecoration.BOLD) }

	/** Cooldown between shots **/
	const val cooldownMillis: Long = 1000

	override val signText: Array<Component?> = arrayOf(
		text("Anti-Air", NamedTextColor.GOLD),
		text("Particle Gun", NamedTextColor.AQUA),
		null,
		INACTIVE_STATE
	)

	fun getPilotOffset(): Vec3i = Vec3i(+0, 4, +1)

	fun shoot(world: World, pos: Vec3i, face: BlockFace, dir: Vector, shooter: Player) { TODO() }


	override fun onTransformSign(player: Player, sign: Sign) {
		if (Space.moonWorldCache[sign.world].isEmpty) {
			player.userError("You must be on a moon to setup an AA gun.")
			sign.block.breakNaturally()
			return
		}

		if (sign.line(3) == INACTIVE_STATE)
			sign.line(3, ACTIVE_STATE)
		else sign.line(3, INACTIVE_STATE)

		sign.update(true, false)
	}

	private val pilotOffsets: Map<BlockFace, Vec3i> = CARDINAL_BLOCK_FACES.associate { inward ->
		val right = inward.rightFace
		val (x, y, z) = getPilotOffset()
		val vec = Vec3i(x = right.modX * x + inward.modX * z, y = y, z = right.modZ * x + inward.modZ * z)
		return@associate inward to vec
	}

	fun getPilotLoc(sign: Sign, face: BlockFace): Location {
		return getPilotLoc(sign.world, sign.x, sign.y, sign.z, face)
	}

	fun getPilotLoc(world: World, x: Int, y: Int, z: Int, face: BlockFace): Location {
		return pilotOffsets.getValue(face).toLocation(world).add(x + 0.5, y + 0.0, z + 0.5)
	}

	fun getSignFromPilot(player: Player): Sign? {
		for (face in CARDINAL_BLOCK_FACES) {
			val (x, y, z) = pilotOffsets.getValue(face)
			val loc = player.location.subtract(x.toDouble(), y.toDouble(), z.toDouble())
			val sign = loc.block.getState(false) as? Sign
				?: continue

			if (Multiblocks[sign] === this) {
				return sign
			}
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

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		if (!StarshipControl.isHoldingController(player)) return

		val multiblock = Multiblocks[sign] as? AntiAirCannonMultiblock ?: return
		val face = multiblock.getFacing(sign)

		// this is just to insert them into the cooldown to prevent accidentally shooting right when boardingm
		// and to prevent leaving right after entering
		AntiAirCannons.cooldown.tryExec(player) { player.teleport(multiblock.getPilotLoc(sign, face)) }

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

			y(+2) {
				x(-1).anyWall()
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
