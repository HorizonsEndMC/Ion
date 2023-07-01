package net.horizonsend.ion.server.features.multiblock.landsieges

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.database.schema.nations.NationRelation
import net.horizonsend.ion.server.database.schema.nations.moonsieges.SiegeBeacon
import net.horizonsend.ion.server.database.schema.nations.territories.SiegeTerritory
import net.horizonsend.ion.server.features.landsieges.BeaconSiegeBattles.tryBeginBeaconSiege
import net.horizonsend.ion.server.features.multiblock.InteractableMultiblock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.cache.nations.RelationCache
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getFacing
import net.starlegacy.util.isWallSign
import net.starlegacy.util.rightFace
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.litote.kmongo.eq

object SiegeBeaconMultiblock : Multiblock(), InteractableMultiblock {
	override val name: String = "siegebeacon"

	private val ACTIVE_STATE = text("Active", NamedTextColor.GREEN).apply { this.decoration(TextDecoration.BOLD) }
	private val INACTIVE_STATE = text("Inactive", NamedTextColor.RED).apply { this.decoration(TextDecoration.BOLD) }

	override val signText: Array<Component?> = arrayOf(
		text("", NamedTextColor.DARK_GREEN),
		text(""),
		INACTIVE_STATE,
		null
	)

	override fun onTransformSign(player: Player, sign: Sign) {
		val attacker = PlayerCache[player].nationOid ?: return

		val territoryName = (sign.line(1) as TextComponent).content()

		val territory = SiegeTerritory.findOne(SiegeTerritory::name eq territoryName) ?:
			return player.userError("Siege territory $territoryName not found")

		if (territory.nation != null && RelationCache[attacker, territory.nation].ordinal >= NationRelation.Level.ALLY.ordinal) {
			return player.userError("You cannot siege your allies!")
		}

		sign.line(1, text(territory.name, NamedTextColor.RED).apply { this.decoration(TextDecoration.ITALIC) })

		val beaconName = (sign.line(3) as TextComponent).content()

		val blocks = shape.getLocations(sign.getFacing().oppositeFace).map {
			(it + Vec3i(sign.location)).toBlockPos().asLong()
		}.toLongArray()

		SiegeBeacon.create(
			beaconName,
			territory._id,
			attacker,
			sign.world.name,
			Vec3i(sign.location),
			blocks
		)

		super.onTransformSign(player, sign)
	}

	private val centerOffset = Vec3i(0, 3, -2)

	fun getCenter(sign: Sign): Vec3i {
		val (x, y, z) = centerOffset
		val facing = sign.getFacing()
		val right = facing.rightFace

		return Vec3i(
			x = (right.modX * x) + (facing.modX * z),
			y = y,
			z = (right.modZ * x) + (facing.modZ * z)
		)
	}

	fun isActive(sign: Sign): Boolean = sign.line(3) == ACTIVE_STATE

	fun setActive(sign: Sign, active: Boolean) = sign.line(3, if (active) ACTIVE_STATE else INACTIVE_STATE)

	override fun onSignInteract(sign: Sign, player: Player) {
		tryBeginBeaconSiege(player, sign)
	}

	fun isIntact(signBlock: Block): Boolean {
		if (!signBlock.type.isWallSign) {
			return false
		}

		val sign = signBlock.getState(false) as Sign

		return this.signMatchesStructure(sign)
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
