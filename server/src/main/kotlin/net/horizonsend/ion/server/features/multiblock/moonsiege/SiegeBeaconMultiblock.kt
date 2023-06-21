package net.horizonsend.ion.server.features.multiblock.moonsiege

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.database.schema.nations.landsieges.SiegeTerritory
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.Multiblock
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.litote.kmongo.eq

object SiegeBeaconMultiblock : Multiblock() {
	override val name: String = "siegebeacon"

	private val ACTIVE_STATE = text("Active", NamedTextColor.GREEN).apply { this.decoration(TextDecoration.BOLD) }
	private val INACTIVE_STATE = text("Inactive", NamedTextColor.RED).apply { this.decoration(TextDecoration.BOLD) }

	override val signText: Array<Component?> = arrayOf(
		text("", NamedTextColor.DARK_GREEN),
		text(""),
		INACTIVE_STATE,
	)

	override fun onTransformSign(player: Player, sign: Sign) {
		val territoryName = (sign.line(1) as TextComponent).content()

		val territory = SiegeTerritory.findOne(SiegeTerritory::name eq territoryName) ?:
			return player.userError("Siege territory $territoryName")

		sign.line(2, text(territory.name, NamedTextColor.RED).apply { this.decoration(TextDecoration.ITALIC) })
	}

	fun isActive(sign: Sign): Boolean = sign.line(2) == ACTIVE_STATE

	fun setActive(sign: Sign, active: Boolean) = sign.line(2, if (active) ACTIVE_STATE else INACTIVE_STATE)

	override fun LegacyMultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).stainedTerracotta()
			}
		}
	}
}
