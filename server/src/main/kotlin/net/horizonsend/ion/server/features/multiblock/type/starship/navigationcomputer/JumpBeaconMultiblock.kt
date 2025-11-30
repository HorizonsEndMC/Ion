package net.horizonsend.ion.server.features.multiblock.type.starship.navigationcomputer

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs

object JumpBeaconMultiblock : NavigationComputerMultiblock(){
	override val description: Component get() = text("Creates a beacon that ships with a jump field generator can jump to")
	override val displayName: Component get() = text("Jump Beacon")
	override val signText = createSignText(
		line1 = "&6Jump",
		line2 = "&8Beacon",
		line3 = null,
		line4 = null
	)

	override val name = "jumpbeacon"
	override val baseRange: Int = 6942069

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(0) {
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(0).powerInput()
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(1) {
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).anyGlass()
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(1) {
			y(0) {
				x(1).aluminumBlock()
				x(0).sponge()
				x(-1).aluminumBlock()
			}
			y(1) {
				x(1).aluminumBlock()
				x(0).sponge()
				x(-1).aluminumBlock()
			}
		}
	}
}

