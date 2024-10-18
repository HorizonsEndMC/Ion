package net.horizonsend.ion.server.features.multiblock.type.particleshield

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.Player

object EventShieldMultiblock : ShieldMultiblock(), PermissionWeaponSubsystem {
	override val isReinforced: Boolean = false
	override val permission: String = "ioncore.eventweapon"

	override val signText = createSignText(
		line1 = "&EVENT Shield",
		line2 = "&7Generator",
		line3 = null,
		line4 = "DONOTUSE"
	)

	override fun setupSign(player: Player, sign: Sign) {
		if (!player.hasPermission("ioncore.eventweapon")) return player.userError("Nuh uh")

		super.setupSign(player, sign)
	}

	override fun getShieldBlocks(sign: Sign): List<Vec3i> {
		return listOf()
	}

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).sponge()
				x(+0).chetheriteBlock()
				x(+1).sponge()
			}

			y(+0) {
				x(-1).anyGlassPane()
				x(+0).type(Material.LAPIS_BLOCK)
				x(+1).anyGlassPane()
			}
		}
	}
}
