package net.starlegacy.feature.multiblock.misc

import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.Multiblock
import org.bukkit.ChatColor
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import java.util.UUID

object CryoPodMultiblock : Multiblock() {
	override val name = "cryopod"

	override val signText = createSignText(
		line1 = "&1-[Cryo Pod]-",
		line2 = null,
		line3 = null,
		line4 = null
	)

	override fun LegacyMultiblockShape.buildStructure() {
		z(-2) {
			y(-1) {
				x(0).anyDoor()
			}
		}

		z(-1) {
			y(-2) {
				x(0).anyGlass()
			}

			y(-1) {
				x(-1).anyGlass()
				x(+1).anyGlass()
			}
			y(+0) {
				x(-1).anyGlass()
				x(+1).anyGlass()
			}

			y(+1) {
				x(0).anyGlass()
			}
		}

		z(+0) {
			y(-1) {
				x(0).anyGlass()
			}

			y(+0) {
				x(0).anyGlass()
			}
		}
	}

	override fun onTransformSign(player: Player, sign: Sign) {
		sign.setLine(1, ChatColor.GREEN.toString() + "[PASSIVE]")
		sign.setLine(2, ChatColor.MAGIC.toString() + player.uniqueId.toString())
		sign.setLine(3, player.name)
	}

	fun isOwner(sign: Sign, player: Player): Boolean {
		return ChatColor.stripColor(sign.getLine(2)) == player.uniqueId.toString() ||
			ChatColor.stripColor(sign.getLine(2)) == player.uniqueId.hashCode().toString()
	}

	fun getOwner(sign: Sign): UUID? {
		return getOwner(sign.lines)
	}

	fun getOwner(lines: Array<String>): UUID? {
		return try {
			UUID.fromString(ChatColor.stripColor(lines[2]))
		} catch (exception: Exception) {
			UUID.fromString(lines[3])
		}
	}
}