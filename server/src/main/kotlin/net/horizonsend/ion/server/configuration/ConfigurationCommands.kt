package net.horizonsend.ion.server.configuration

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.Configuration
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.SLCommand
import org.bukkit.command.CommandSender
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

@CommandAlias("ion")
@CommandPermission("ion.config")
@Suppress("unused")
object ConfigurationCommands : SLCommand() {
	private val turretTypes = BalancingConfiguration.StarshipWeapons::class.memberProperties
	private val changeableFields = BalancingConfiguration.StarshipWeapons.StarshipWeapon::class.memberProperties
		.filterIsInstance<KMutableProperty<*>>()

	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerCompletion("balancingFields") {
			turretTypes.map { it.name }
		}

		manager.commandCompletions.registerCompletion("balancingValues") {
			changeableFields.map { it.name }
		}
	}

	@Subcommand("config set")
	@CommandCompletion("@balancingFields @balancingValues @nothing")
	fun set(sender: CommandSender, typeName: String, fieldName: String, value: String) {
		val type = turretTypes.find { it.name == typeName } ?: run {
			sender.userError("Type not found")
			return
		}

		val field = changeableFields.find { it.name == fieldName } ?: run {
			sender.userError("Field not found")
			return
		}

		val obj = type.get(IonServer.balancing.starshipWeapons)
		var done = false
		when (type.returnType) {
			Int::class.createType() -> {
				field.setter.call(obj, value.toInt())
				done = true
			}

			Double::class.createType() -> {
				field.setter.call(obj, value.toDouble())
				done = true
			}

			Float::class.createType() -> {
				field.setter.call(obj, value.toFloat())
				done = true
			}

			Long::class.createType() -> {
				field.setter.call(obj, value.toLong())
				done = true
			}

			Boolean::class.createType() -> {
				field.setter.call(obj, value.toBooleanStrict())
				done = true
			}

			String::class.createType() -> {
				field.setter.call(obj, value)
				done = true
			}

			else -> {
				sender.userError("type is: ${type.returnType.javaType.typeName}, to add in the switch case")
			}
		}

		if (done)
			sender.success("changed balancing value")
		else
			sender.userError("type error, value isn't what the field accepts or read above")
	}

	@Subcommand("config get")
	@CommandCompletion("@balancingFields @balancingValues")
	fun set(sender: CommandSender, typeName: String, fieldName: String) {
		val type = turretTypes.find { it.name == typeName } ?: run {
			sender.userError("Type not found")
			return
		}

		val field = changeableFields.find { it.name == fieldName } ?: run {
			sender.userError("Field not found")
			return
		}

		val obj =
			field.getter.call(type.get(IonServer.balancing.starshipWeapons) as BalancingConfiguration.StarshipWeapons.StarshipWeapon)
		sender.success("Value $fieldName of $typeName: $obj")
	}

	@Subcommand("config save")
	fun configSave(sender: CommandSender) {
		Configuration.save(IonServer.configuration, IonServer.dataFolder, "server.json")
		Configuration.save(IonServer.balancing, IonServer.dataFolder, "server.json")

		sender.success("Saved configs with current runtime values.")
	}

	@Subcommand("config reload")
	fun onConfigReload(sender: CommandSender) {
		IonServer.configuration = Configuration.load(IonServer.dataFolder, "server.json")
		IonServer.balancing = Configuration.load(IonServer.dataFolder, "balancing.json")
		IonServer.gasConfiguration = Configuration.load(IonServer.dataFolder, "gasses.json")

		reloadOthers()

		sender.success("Reloaded configs.")
	}

	private fun reloadOthers() {}
}
