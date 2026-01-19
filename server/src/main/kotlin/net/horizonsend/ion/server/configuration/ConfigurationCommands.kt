package net.horizonsend.ion.server.configuration

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.text.formatException
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.configuration.starship.NewStarshipBalancing
import net.horizonsend.ion.server.configuration.starship.NewStarshipBalancing.WeaponDefaults
import net.horizonsend.ion.server.configuration.starship.StarshipProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipTypeBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipWeaponBalancing
import net.horizonsend.ion.server.core.registration.IonRegistries
import net.horizonsend.ion.server.features.ai.spawning.AISpawningManager.schematicCache
import net.horizonsend.ion.server.features.world.generation.generators.configuration.AsteroidConfigurations
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.command.CommandSender
import java.util.concurrent.TimeUnit
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

@CommandAlias("ion")
@CommandPermission("ion.config")
object ConfigurationCommands : SLCommand() {
	private val starshipTypes = NewStarshipBalancing.ShipClasses::class.memberProperties
	private val starshipBalancingOptions = StarshipTypeBalancing::class.memberProperties
	private val weaponDefaults = WeaponDefaults::class.memberProperties
	private val weaponFields = StarshipWeaponBalancing::class.memberProperties.filterIsInstance<KMutableProperty<*>>()
	private val projectileFields = StarshipProjectileBalancing::class.memberProperties.filterIsInstance<KMutableProperty<*>>()
	private val starshipFields = StarshipTypeBalancing::class.memberProperties.filterIsInstance<KMutableProperty<*>>()

	private val meleeWeaponTypes = PVPBalancingConfiguration.MeleeWeapons::class.memberProperties
	private val throwableTypes = PVPBalancingConfiguration.Throwables::class.memberProperties
	private val consumableTypes = PVPBalancingConfiguration.Consumables::class.memberProperties
	private val armorTypes = PVPBalancingConfiguration.Armor::class.memberProperties
	private val blasterTypes = PVPBalancingConfiguration.BlasterWeapons::class.memberProperties

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

		manager.commandCompletions.registerCompletion("meleeWeaponTypes") {
			meleeWeaponTypes.map { it.name }
		}

		manager.commandCompletions.registerCompletion("throwablesTypes") {
			throwableTypes.map { it.name }
		}

		manager.commandCompletions.registerCompletion("consumablesTypes") {
			consumableTypes.map { it.name }
		}

		manager.commandCompletions.registerCompletion("armorTypes") {
			armorTypes.map { it.name }
		}

		manager.commandCompletions.registerCompletion("blasterTypes") {
			blasterTypes.map { it.name }
		}
	}

	@Subcommand("server start")
	fun startServer(sender: CommandSender) = asyncCommand(sender) {
		val serverConfiguration = ConfigurationFiles.serverConfiguration.get()
		failIf(serverConfiguration.serverStartDate != null) {"The server has already been started!"}
		val time = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
		sender.success("Input this timestamp into the configuration: $time")
	}

	@Subcommand("server startdate change")
	fun changeServerStartDate(sender: CommandSender) = asyncCommand(sender) {
		val time = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
		sender.success("Input this timestamp into the configuration: $time")
	}

	@Subcommand("config get meleeWeapons")
	@CommandCompletion("@meleeWeaponTypes property")
	fun getMeleeWeaponProperties(sender: CommandSender, meleeWeaponName: String, fieldName: String) = asyncCommand(sender) {
		getConfigProperty(
			sender,
			meleeWeaponTypes,
			ConfigurationFiles.pvpBalancing.get().meleeWeapons,
			meleeWeaponName,
			fieldName
		)
	}

	@Subcommand("config set meleeWeapons")
	@CommandCompletion("@meleeWeaponTypes property value")
	fun setMeleeWeaponProperties(sender: CommandSender, meleeWeaponName: String, fieldName: String, value: String) = asyncCommand(sender) {
		setConfigProperty(
			sender,
			meleeWeaponTypes,
			ConfigurationFiles.pvpBalancing.get().meleeWeapons,
			meleeWeaponName,
			fieldName,
			value
		)
	}

	@Subcommand("config get throwables")
	@CommandCompletion("@throwablesTypes property")
	fun getThrowablesProperties(sender: CommandSender, throwableName: String, fieldName: String) = asyncCommand(sender) {
		getConfigProperty(
			sender,
			throwableTypes,
			ConfigurationFiles.pvpBalancing.get().throwables,
			throwableName,
			fieldName
		)
	}

	@Subcommand("config set throwables")
	@CommandCompletion("@throwablesTypes property value")
	fun setThrowablesProperties(sender: CommandSender, throwableName: String, fieldName: String, value: String) = asyncCommand(sender) {
		setConfigProperty(
			sender,
			throwableTypes,
			ConfigurationFiles.pvpBalancing.get().throwables,
			throwableName,
			fieldName,
			value
		)
	}

	@Subcommand("config get consumables")
	@CommandCompletion("@consumablesTypes property")
	fun getConsumablesProperties(sender: CommandSender, consumableName: String, fieldName: String) = asyncCommand(sender) {
		getConfigProperty(
			sender,
			consumableTypes,
			ConfigurationFiles.pvpBalancing.get().consumables,
			consumableName,
			fieldName
		)
	}

	@Subcommand("config set consumables")
	@CommandCompletion("@consumablesTypes property value")
	fun setConsumablesProperties(sender: CommandSender, consumableName: String, fieldName: String, value: String) = asyncCommand(sender) {
		setConfigProperty(
			sender,
			consumableTypes,
			ConfigurationFiles.pvpBalancing.get().consumables,
			consumableName,
			fieldName,
			value
		)
	}

	@Subcommand("config get armor")
	@CommandCompletion("@armorTypes property")
	fun getArmorProperties(sender: CommandSender, armorName: String, fieldName: String) = asyncCommand(sender) {
		getConfigProperty(
			sender,
			armorTypes,
			ConfigurationFiles.pvpBalancing.get().armour,
			armorName,
			fieldName
		)
	}

	@Subcommand("config set armor")
	@CommandCompletion("@armorTypes property value")
	fun setArmorProperties(sender: CommandSender, armorName: String, fieldName: String, value: String) = asyncCommand(sender) {
		setConfigProperty(
			sender,
			armorTypes,
			ConfigurationFiles.pvpBalancing.get().armour,
			armorName,
			fieldName,
			value
		)
	}

	@Subcommand("config get blaster")
	@CommandCompletion("@blasterTypes property value")
	fun getBlasterProperties(sender: CommandSender, blasterName: String, fieldName: String) = asyncCommand(sender) {
		getConfigProperty(
			sender,
			blasterTypes,
			ConfigurationFiles.pvpBalancing.get().blasterWeapons,
			blasterName,
			fieldName
		)
	}

	@Subcommand("config set blaster")
	@CommandCompletion("@blasterTypes property value")
	fun setBlasterProperties(sender: CommandSender, blasterName: String, fieldName: String, value: String) = asyncCommand(sender) {
		setConfigProperty(
			sender,
			blasterTypes,
			ConfigurationFiles.pvpBalancing.get().blasterWeapons,
			blasterName,
			fieldName,
			value
		)
	}

	private fun <T : Any> getConfigProperty(sender: CommandSender, collection: Collection<KProperty1<T, *>>, balancingConfiguration: T, typeName: String, fieldName: String) = asyncCommand(sender) {
		val (typeBalancing, field) = getBalancingAndField(collection, typeName, balancingConfiguration, fieldName)

		val value = try { getField(field, typeBalancing) } catch (e: Throwable) { fail { "Error: ${e.message}" } }
		sender.information("$typeName property $fieldName: $value")
	}

	private fun <T : Any> setConfigProperty(sender: CommandSender, collection: Collection<KProperty1<T, *>>, balancingConfiguration: T, typeName: String, fieldName: String, value: String) = asyncCommand(sender) {
		val (typeBalancing, field) = getBalancingAndField(collection, typeName, balancingConfiguration, fieldName)

		try { setField(field, typeBalancing, value) } catch (e: Throwable) { fail { "Error: ${e.message}" } }
		sender.success("Set $typeName property $fieldName to $value")
	}

	private fun <T : Any> getBalancingAndField(collection: Collection<KProperty1<T, *>>, typeName: String, balancingConfiguration: T, fieldName: String): Pair<Any, KMutableProperty<*>> {
		val type = collection.find { it.name == typeName } ?: fail { "Type $typeName not found in configuration" }
		val typeBalancing =
			type.get(balancingConfiguration) ?: fail { "Balancing configuration for $typeName not found" }
		val fields = typeBalancing::class.declaredMemberProperties.filterIsInstance<KMutableProperty<*>>()
		val field = fields.find { it.name == fieldName }
			?: fail { "Field $fieldName not found in $typeName's balancing configuration" }
		return Pair(typeBalancing, field)
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

	@Subcommand("registries reload")
	fun onRegistriesReload(sender: CommandSender) {
		sender.information("Reloading registries")
		Tasks.syncBlocking { IonRegistries.bootstrapAll() }
	}

	private fun reloadOthers() {
		schematicCache.invalidateAll()
		AsteroidConfigurations.reload()
	}

	private fun getField(field: KMutableProperty<*>, containing: Any): Any? {
		when (field.returnType) {
			Int::class.createType() -> {
				return field.getter.call(containing)
			}

			Double::class.createType() -> {
				return field.getter.call(containing)
			}

			Float::class.createType() -> {
				return field.getter.call(containing)
			}

			Long::class.createType() -> {
				return field.getter.call(containing)
			}

			Boolean::class.createType() -> {
				return field.getter.call(containing)
			}

			String::class.createType() -> {
				return field.getter.call(containing)
			}

			else -> throw NotImplementedError("type is: ${field.returnType.javaType.typeName}, to add in the switch case")
		}
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
}
