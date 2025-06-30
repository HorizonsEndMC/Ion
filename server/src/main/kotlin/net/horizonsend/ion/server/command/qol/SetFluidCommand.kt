package net.horizonsend.ion.server.command.qol

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringMultiblock
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.miscellaneous.utils.getSelection
import org.bukkit.entity.Player

@CommandAlias("setfluid")
@CommandPermission("ion.setfluid")
object SetFluidCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerCompletion("fluids") { FluidTypeKeys.allStrings() }

		manager.commandContexts.registerContext(FluidType::class.java) {
			val id = it.popFirstArg()
			FluidTypeKeys[id]?.getValue() ?: throw InvalidCommandArgument("Fluid $id not found!")
		}

		manager.commandCompletions.setDefaultCompletion("fluids", FluidType::class.java)
	}

	@Default
	@CommandCompletion("@fluids 0|1000|500000|2147483647 main")
	@Suppress("unused")
	fun onSetFluid(sender: Player, fluid: FluidType, amount: Double, storeName: String) {
		val selection = runCatching { sender.getSelection() }.getOrNull() ?: fail { "You must make a selection!" }

		if (sender.world.name != selection.world?.name) return

		var hits = 0

		for (blockPosition in selection) {
			val x = blockPosition.x()
			val y = blockPosition.y()
			val z = blockPosition.z()

			sender.debug("checking block at $x $y $z")

			val entity = MultiblockEntities.getMultiblockEntity(sender.world, x, y ,z)
			if (entity !is FluidStoringMultiblock) continue

			entity.getNamedStorage(storeName)?.setContents(FluidStack(fluid, amount))
			hits++

			sender.debug("power sent")
		}

		sender.success("Set $hits multiblocks' $storeName's ${fluid.key.key} to $amount.")
	}
}
