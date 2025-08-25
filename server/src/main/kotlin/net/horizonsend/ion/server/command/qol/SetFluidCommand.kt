package net.horizonsend.ion.server.command.qol

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.FluidPropertyTypeKeys
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringMultiblock
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidProperty
import net.horizonsend.ion.server.features.transport.fluids.properties.type.FluidPropertyType
import net.horizonsend.ion.server.miscellaneous.utils.getSelection
import org.bukkit.block.data.type.WallSign
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
	@CommandCompletion("@fluids 0|1000|500000|2147483647 main|primaryin|primaryout|secondaryin|secondaryout|pollutionout @nothing")
	@Suppress("unused")
	fun onSetFluid(sender: Player, fluid: FluidType, amount: Double, storeName: String) {
		val selection = runCatching { sender.getSelection() }.getOrNull() ?: fail { "You must make a selection!" }

		if (sender.world.name != selection.world?.name) return

		val entities = mutableSetOf<FluidStoringMultiblock>()

		for (blockPosition in selection) {
			val x = blockPosition.x()
			val y = blockPosition.y()
			val z = blockPosition.z()

			val data = sender.world.getBlockData(x, y, z)
			if (data is WallSign) {
				val entity = MultiblockEntities.getMultiblockEntity(x, y, z, sender.world, data)

				if (entity is FluidStoringMultiblock) {
					entities.add(entity)
				}
			}

			val entity = MultiblockEntities.getMultiblockEntity(sender.world, x, y ,z)
			if (entity !is FluidStoringMultiblock) continue

			entities.add(entity)
		}

		val success = entities.count { entity -> entity.getNamedStorage(storeName)?.setContents(FluidStack(fluid, amount)) != null }

		if (success == 0) return sender.userError("No multiblocks set.")

		sender.success("Set $success multiblocks' $storeName's ${fluid.key.key} to $amount.")
	}

	@Subcommand("property")
	@Suppress("unused")
	fun onSetFluidProperty(sender: Player, propertyString: String, value: Double, storeName: String) {
		val property: IonRegistryKey<FluidPropertyType<*>, out FluidPropertyType<*>> = FluidPropertyTypeKeys[propertyString] ?: fail { "Invalid property $propertyString" }
		val selection = runCatching { sender.getSelection() }.getOrNull() ?: fail { "You must make a selection!" }

		if (sender.world.name != selection.world?.name) return

		val entities = mutableSetOf<FluidStoringMultiblock>()

		for (blockPosition in selection) {
			val x = blockPosition.x()
			val y = blockPosition.y()
			val z = blockPosition.z()

			val data = sender.world.getBlockData(x, y, z)
			if (data is WallSign) {
				val entity = MultiblockEntities.getMultiblockEntity(x, y, z, sender.world, data)

				if (entity is FluidStoringMultiblock) {
					entities.add(entity)
				}
			}

			val entity = MultiblockEntities.getMultiblockEntity(sender.world, x, y ,z)
			if (entity !is FluidStoringMultiblock) continue

			entities.add(entity)
		}

		val success = entities.count { entity ->
			val contents = entity.getNamedStorage(storeName)?.getContents() ?: return@count false

			if (property == FluidPropertyTypeKeys.PRESSURE) contents.setData(FluidPropertyTypeKeys.PRESSURE.getValue(), FluidProperty.Pressure(value))
			if (property == FluidPropertyTypeKeys.TEMPERATURE) contents.setData(FluidPropertyTypeKeys.TEMPERATURE.getValue(), FluidProperty.Temperature(value))
			if (property == FluidPropertyTypeKeys.SALINITY) contents.setData(FluidPropertyTypeKeys.SALINITY.getValue(), FluidProperty.Salinity(value))

			true
		}

		if (success == 0) return sender.userError("No multiblocks set.")

		sender.success("Set $success multiblocks' $storeName's ${property.key} to $value.")
	}
}
