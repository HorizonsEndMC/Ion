package net.horizonsend.ion.server.features.cryopods

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.block.Sign
import org.bukkit.entity.Player

object CryoPodMultiblock : Multiblock() {
	override val name = "cryopod"

	override val signText = createSignText(
		line1 = "&1-[Cryo Pod]-",
		line2 = null,
		line3 = null,
		line4 = null
	)

	override fun MultiblockShape.buildStructure() {
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
		sign.line(1, text("[PASSIVE]").color(NamedTextColor.GREEN))
		sign.line(2, text(player.uniqueId.toString()).decorate(TextDecoration.OBFUSCATED))
		sign.line(3, text(player.name))
	}

	fun isOwner(sign: Sign, player: Player): Boolean {
		return (sign.line(2) as TextComponent).content() == player.uniqueId.toString()
	}
}
