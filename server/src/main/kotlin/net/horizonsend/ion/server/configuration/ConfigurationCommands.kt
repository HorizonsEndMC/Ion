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
	private val starshipTypes = StarshipTypeBalancing::class.memberProperties
	private val starshipBalancingOptions = StarshipBalancing::class.memberProperties
	private val weaponTypes = StarshipWeapons::class.memberProperties
	private val weaponFields = StarshipWeapons.StarshipWeapon::class.memberProperties.filterIsInstance<KMutableProperty<*>>()
	private val starshipFields = StarshipBalancing::class.memberProperties.filterIsInstance<KMutableProperty<*>>()

	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerCompletion("starshipTypes") {
			starshipTypes.map { it.name }
		}

		manager.commandCompletions.registerCompletion("balancingOptions") {
			starshipBalancingOptions.map { it.name }
		}

		manager.commandCompletions.registerCompletion("weaponTypes") {
			weaponTypes.map { it.name }
		}

		manager.commandCompletions.registerCompletion("balancingValues") {
			weaponFields.map { it.name }
		}

		manager.commandCompletions.registerCompletion("starshipValues") {
			starshipFields.map { it.name }
		}
	}

	@Subcommand("config set starship properties")
	@CommandCompletion("@starshipTypes @starshipValues @nothing")
	fun setStarshipProperties(sender: CommandSender, starshipTypeName: String, fieldName: String, value: String) {
		val starshipType = starshipTypes.find { it.name == starshipTypeName } ?: run {
			sender.userError("Starship type $starshipTypeName not found")
			return
		}

		val field = starshipFields.find { it.name == fieldName } ?: run {
			sender.userError("Field not found")
			return
		}

		val starshipBalancing = starshipType.get(IonServer.starshipBalancing) as? StarshipBalancing ?: return sender.userError("$starshipType is not StarshipBalancing!")

		try { setField(field, starshipBalancing, value) } catch (e: Throwable) { return sender.userError("Error: ${e.message}") }

		sender.success("Set $starshipTypeName's $fieldName to $value")
	}

	@Subcommand("config set starship weapon")
	@CommandCompletion("@starshipTypes @weaponTypes @balancingValues @nothing")
	fun setStarshipWeapons(sender: CommandSender, starshipTypeName: String, weaponName: String, fieldName: String, value: String) {
		val starshipType = starshipTypes.find { it.name == starshipTypeName } ?: run {
			sender.userError("Starship type $starshipTypeName not found")
			return
		}

		val weaponType = weaponTypes.find { it.name == weaponName } ?: run {
			sender.userError("Weapon type $weaponName not found")
			return
		}

		val field = weaponFields.find { it.name == fieldName } ?: run {
			sender.userError("Field not found")
			return
		}

		val starshipBalancing = (starshipType.get(IonServer.starshipBalancing) as? StarshipBalancing)?.weapons ?: return sender.userError("$starshipType is not StarshipBalancing!")
		val weapon = weaponType.get(starshipBalancing) as? StarshipWeapons.StarshipWeapon  ?: return sender.userError("$starshipType is not StarshipBalancing!")

		try { setField(field, weapon, value) } catch (e: Throwable) { return sender.userError("Error: ${e.message}") }

		sender.success("Set $starshipTypeName's $weaponName's $fieldName to $value")
	}

	private fun setField(field: KMutableProperty<*>, containing: Any, value: String) {
		when (field.returnType) {
			Int::class.createType() -> {
				field.setter.call(containing, value.toInt())
			}

			Double::class.createType() -> {
				field.setter.call(containing, value.toDouble())
			}

			Float::class.createType() -> {
				field.setter.call(containing, value.toFloat())
			}

			Long::class.createType() -> {
				field.setter.call(containing, value.toLong())
			}

			Boolean::class.createType() -> {
				field.setter.call(containing, value.toBooleanStrict())
			}

			String::class.createType() -> {
				field.setter.call(containing, value)
			}

			else -> throw NotImplementedError("type is: ${field.returnType.javaType.typeName}, to add in the switch case")
		}
	}

//	@Subcommand("config get")
//	@CommandCompletion("@balancingFields @balancingValues")
//	fun set(sender: CommandSender, typeName: String, fieldName: String) {
//		val type = starshipTypes.find { it.name == typeName } ?: run {
//			sender.userError("Type not found")
//			return
//		}
//
//		val field = changeableFields.find { it.name == fieldName } ?: run {
//			sender.userError("Field not found")
//			return
//		}
//
//		val obj =
//			field.getter.call(type.get(balancing) as StarshipWeapons.StarshipWeapon)
//		sender.success("Value $fieldName of $typeName: $obj")
//	}

	@Subcommand("config save")
	fun configSave(sender: CommandSender) {
		Configuration.save(IonServer.configuration, IonServer.dataFolder, "server.json")
		Configuration.save(IonServer.pvpBalancing, IonServer.dataFolder, "server.json")
		Configuration.save(IonServer.starshipBalancing, IonServer.dataFolder, "server.json")

		sender.success("Saved configs with current runtime values.")
	}

	@Subcommand("config reload")
	fun onConfigReload(sender: CommandSender) {
		IonServer.configuration = Configuration.load(IonServer.dataFolder, "server.json")
		IonServer.gassesConfiguration = Configuration.load(IonServer.dataFolder, "gasses.json")
		IonServer.tradeConfiguration = Configuration.load(IonServer.dataFolder, "trade.json")
		IonServer.aiShipConfiguration = Configuration.load(IonServer.dataFolder, "aiships.json")
		IonServer.pvpBalancing = Configuration.load(IonServer.dataFolder, "pvpbalancing.json")
		IonServer.starshipBalancing = Configuration.load(IonServer.dataFolder, "starshipbalancing.json")

		reloadOthers()

		sender.success("Reloaded configs.")
	}

	private fun reloadOthers() {}
}
