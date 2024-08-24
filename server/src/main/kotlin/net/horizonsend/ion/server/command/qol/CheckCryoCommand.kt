package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.database.schema.Cryopod
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.multiblock.type.misc.CryoPodMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.bukkitLocation
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.litote.kmongo.descending
import org.litote.kmongo.eq

@CommandAlias("checkcryo")
object CheckCryoCommand : SLCommand() {
	@Default
	fun onCheckCryo(sender: Player) = asyncCommand(sender) {
		val cryoPod = Cryopod.find(Cryopod::owner eq sender.slPlayerId).sort(descending(Cryopod::lastSelectedAt)).firstOrNull()

		if (cryoPod == null) {
			sender.userError("You do not have a selected cryopod!")
			return@asyncCommand
		}

		Tasks.sync {
			val signPosition = cryoPod.bukkitLocation()
			val sign = signPosition.block.state as? Sign

			if (sign == null) {
				sender.serverError("Cryopod sign at ${cryoPod.x}, ${cryoPod.y}, ${cryoPod.z} in ${cryoPod.worldName} is missing!")
				return@sync
			}

			if (!CryoPodMultiblock.signMatchesStructure(sign, loadChunks = true)) {
				sender.serverError("Cryopod at ${cryoPod.x}, ${cryoPod.y}, ${cryoPod.z} in ${cryoPod.worldName} is not intact!")
				return@sync
			}

			sender.success("Your selected cryopod is at ${cryoPod.x}, ${cryoPod.y}, ${cryoPod.z} in ${cryoPod.worldName}")
		}
	}
}
