package net.horizonsend.ion.server.configuration

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.text.formatException
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.configuration.starship.NewStarshipBalancing
import net.horizonsend.ion.server.configuration.starship.NewStarshipBalancing.WeaponDefaults
import net.horizonsend.ion.server.configuration.starship.StarshipProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipTypeBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipWeaponBalancing
import net.horizonsend.ion.server.features.ai.spawning.AISpawningManager.schematicCache
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.command.CommandSender
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

@CommandAlias("ion")
@CommandPermission("ion.config")
object ConfigurationCommands : SLCommand() {
	private val starshipTypes = NewStarshipBalancing.ShipClasses::class.memberProperties
	private val starshipBalancingOptions = StarshipTypeBalancing::class.memberProperties
	private val weaponDefaults = WeaponDefaults::class.memberProperties
	private val weaponFields = StarshipWeaponBalancing::class.memberProperties.filterIsInstance<KMutableProperty<*>>()
	private val projectileFields = StarshipProjectileBalancing::class.memberProperties.filterIsInstance<KMutableProperty<*>>()
	private val starshipFields = StarshipTypeBalancing::class.memberProperties.filterIsInstance<KMutableProperty<*>>()

	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerCompletion("starshipTypes") {
			starshipTypes.map { it.name }
		}

		manager.commandCompletions.registerCompletion("balancingOptions") {
			starshipBalancingOptions.map { it.name }
		}

		manager.commandCompletions.registerCompletion("weaponDefaults") {
			weaponDefaults.map { it.name }
		}

		manager.commandCompletions.registerCompletion("balancingValues") {
			weaponFields.map { it.name }
		}

		manager.commandCompletions.registerCompletion("starshipValues") {
			starshipFields.map { it.name }
		}
	}
//
//	@Subcommand("config set starship properties")
//	@CommandCompletion("@starshipTypes @starshipValues @nothing")
//	fun setStarshipProperties(sender: CommandSender, starshipTypeName: String, fieldName: String, value: String) {
//		val starshipType = starshipTypes.find { it.name == starshipTypeName } ?: run {
//			sender.userError("Starship type $starshipTypeName not found")
//			return
//		}
//
//		val field = starshipFields.find { it.name == fieldName } ?: run {
//			sender.userError("Field not found")
//			return
//		}
//
//		val starshipBalancing = starshipType.get(ConfigurationFiles.starshipBalancing()) as? StarshipBalancing ?: return sender.userError("$starshipType is not StarshipBalancing!")
//
//		try { setField(field, starshipBalancing, value) } catch (e: Throwable) { return sender.userError("Error: ${e.message}") }
//
//		sender.success("Set $starshipTypeName's $fieldName to $value")
//	}
//
//	@Subcommand("config set starship weapon")
//	@CommandCompletion("@starshipTypes @weaponTypes @balancingValues @nothing")
//	fun setStarshipWeapons(sender: CommandSender, starshipTypeName: String, weaponName: String, fieldName: String, value: String) {
//		val starshipType = starshipTypes.find { it.name == starshipTypeName } ?: run {
//			sender.userError("Starship type $starshipTypeName not found")
//			return
//		}
//
//		val weaponType = weaponDefaults.find { it.name == weaponName } ?: run {
//			sender.userError("Weapon type $weaponName not found")
//			return
//		}
//
//		val field = weaponFields.find { it.name == fieldName } ?: run {
//			sender.userError("Field not found")
//			return
//		}
//
//		val starshipBalancing = (starshipType.get(ConfigurationFiles.starshipBalancing()) as? StarshipBalancing)?.weapons ?: return sender.userError("$starshipType is not StarshipBalancing!")
//		val weapon = weaponType.get(starshipBalancing) as? StarshipWeapon  ?: return sender.userError("$starshipType is not StarshipBalancing!")
//
//		try { setField(field, weapon, value) } catch (e: Throwable) { return sender.userError("Error: ${e.message}") }
//
//		sender.success("Set $starshipTypeName's $weaponName's $fieldName to $value")
//	}
//
//	private fun setField(field: KMutableProperty<*>, containing: Any, value: String) {
//		when (field.returnType) {
//			Int::class.createType() -> {
//				field.setter.call(containing, value.toInt())
//			}
//
//			Double::class.createType() -> {
//				field.setter.call(containing, value.toDouble())
//			}
//
//			Float::class.createType() -> {
//				field.setter.call(containing, value.toFloat())
//			}
//
//			Long::class.createType() -> {
//				field.setter.call(containing, value.toLong())
//			}
//
//			Boolean::class.createType() -> {
//				field.setter.call(containing, value.toBooleanStrict())
//			}
//
//			String::class.createType() -> {
//				field.setter.call(containing, value)
//			}
//
//			else -> throw NotImplementedError("type is: ${field.returnType.javaType.typeName}, to add in the switch case")
//		}
//	}
//
//
//	@Subcommand("config set starship properties")
//	@CommandCompletion("@starshipTypes @starshipValues @nothing")
//	fun getStarshipProperties(sender: CommandSender, starshipTypeName: String, fieldName: String) {
//		val starshipType = starshipTypes.find { it.name == starshipTypeName } ?: run {
//			sender.userError("Starship type $starshipTypeName not found")
//			return
//		}
//
//		val field = starshipFields.find { it.name == fieldName } ?: run {
//			sender.userError("Field not found")
//			return
//		}
//
//		val starshipBalancing = starshipType.get(ConfigurationFiles.starshipBalancing()) as? StarshipBalancing ?: return sender.userError("$starshipType is not StarshipBalancing!")
//
//		val fieldValue = field.getter.call(starshipBalancing)
//		sender.success("$starshipTypeName's $fieldName is $fieldValue")
//	}
//
//	@Subcommand("config get starship weapon")
//	@CommandCompletion("@starshipTypes @weaponTypes @balancingValues @nothing")
//	fun getStarshipWeapons(sender: CommandSender, starshipTypeName: String, weaponName: String, fieldName: String) {
//		val starshipType = starshipTypes.find { it.name == starshipTypeName } ?: run {
//			sender.userError("Starship type $starshipTypeName not found")
//			return
//		}
//
//		val weaponType = weaponDefaults.find { it.name == weaponName } ?: run {
//			sender.userError("Weapon type $weaponName not found")
//			return
//		}
//
//		val field = weaponFields.find { it.name == fieldName } ?: run {
//			sender.userError("Field not found")
//			return
//		}
//
//		val starshipBalancing = (starshipType.get(ConfigurationFiles.starshipBalancing()) as? StarshipBalancing)?.weapons ?: return sender.userError("$starshipType is not StarshipBalancing!")
//		val weapon = weaponType.get(starshipBalancing) as? StarshipWeapon  ?: return sender.userError("$starshipType is not StarshipBalancing!")
//
//		val fieldValue = field.getter.call(weapon)
//		sender.success("$starshipTypeName's $weaponName's $fieldName is $fieldValue")
//	}

	@Subcommand("config save")
	fun configSave(sender: CommandSender) {
		ConfigurationFiles.saveToDisk()

		sender.success("Saved configs with current runtime values.")
	}

	@Subcommand("config reload")
	fun onConfigReload(sender: CommandSender) {
		Tasks.async {
			kotlin.runCatching { ConfigurationFiles.reload() }.onFailure { sender.sendMessage(formatException(it)) }

			Tasks.sync {
				reloadOthers()

				sender.success("Reloaded configs.")
			}
		}
	}

	private fun reloadOthers() {
		schematicCache.invalidateAll()
	}
}
