package net.horizonsend.ion.server.configuration

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.Configuration
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.events.IonEnableEvent
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties

@CommandAlias("ion")
@CommandPermission("ion.config")
@Suppress("unused")
class ConfigurationCommands : BaseCommand(), Listener {
	private val turretTypes = BalancingConfiguration.StarshipWeapons::class.memberProperties
	private val changeableFields = BalancingConfiguration.StarshipWeapons.StarshipWeapon::class.memberProperties

	@EventHandler
	fun enable(e: IonEnableEvent) {
		e.manager.commandCompletions.registerCompletion("balancingFields") {
			turretTypes.map { it.name }
		}

		e.manager.commandCompletions.registerCompletion("balancingValues") {
			changeableFields.map { it.name }
		}
	}

	@Subcommand("config ships set")
	@CommandCompletion("@balancingFields @balancingValues @nothing")
	fun set(sender: CommandSender, typeName: String, fieldName: String, value: String) {
		val type = turretTypes.find { it.name == typeName } ?: run {
			sender.userError("Type not found")
			return
		}

		val field = changeableFields.find { it.name == fieldName } as KMutableProperty<*>? ?: run {
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
		}

		if (done)
			sender.success("changed balancing value")
		else
			sender.userError("type error, value isn't what the field accepts")
	}

	@Subcommand("config ships get")
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

		val obj = field.get(type.get(IonServer.balancing.starshipWeapons) as BalancingConfiguration.StarshipWeapons.StarshipWeapon)
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
		sender.success("Reloaded configs.")
	}
}
