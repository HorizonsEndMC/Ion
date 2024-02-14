package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.formatSpacePrefix
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.space.SpaceWorlds
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.IonWorld.Companion.environments
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.features.world.environment.Environment
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.World
import org.bukkit.command.CommandSender

@CommandAlias("ionworld")
@CommandPermission("ion.admin.world")
object WorldCommand : SLCommand() {
	@Subcommand("flag add")
	@Suppress("unused")
	fun onAddWorldFlag(sender: CommandSender, world: World, flag: WorldFlag) {
		val ionWorld = IonWorld[world]

		if (ionWorld.configuration.flags.add(flag)) sender.success("Added flag $flag")
		else return sender.userError("World ${world.name} already had the flag $flag")

		ionWorld.saveConfiguration()
		SpaceWorlds.cache.invalidate(world)
	}

	@Subcommand("flag remove")
	@Suppress("unused")
	fun onRemoveWorldFlag(sender: CommandSender, world: World, flag: WorldFlag) {
		val ionWorld = IonWorld[world]

		if (ionWorld.configuration.flags.remove(flag)) sender.success("Removed flag $flag")
		else return sender.userError("World ${world.name} did not have flag $flag")

		ionWorld.saveConfiguration()
		SpaceWorlds.cache.invalidate(world)
	}

	@Subcommand("flag list")
	@Suppress("unused")
	fun onListFlags(sender: CommandSender, world: World, @Optional page: Int?) {
		if ((page ?: 1) <= 0) return sender.userError("Page must not be less than or equal to zero!")

		val builder = text()

		builder.append(text("${world.name} Flags:", HE_LIGHT_GRAY, TextDecoration.BOLD), Component.newline())

		val flags = IonWorld[world].configuration.flags.toList()

		val body = formatPaginatedMenu(
			flags.size,
			"/ionworld flag list",
			page ?: 1
		) {
			val flag = flags[it]

			val remove = formatSpacePrefix(bracketed(text("Remove")))
				.clickEvent(ClickEvent.runCommand("/ionworld flag remove $flag"))
				.hoverEvent(text("/ionworld flag remove $flag"))

			ofChildren(flag.toComponent(HE_LIGHT_GRAY), text( ), remove)
		}

		builder.append(body)

		sender.sendMessage(builder.build())
	}

	@Subcommand("environment add")
	@Suppress("unused")
	fun onAddWorldEnvironment(sender: CommandSender, world: World, environment: Environment) {
		val ionWorld = IonWorld[world]

		if (ionWorld.configuration.environments.add(environment)) sender.success("Added flag $environment")
		else return sender.userError("World ${world.name} already had the flag $environment")

		ionWorld.saveConfiguration()
		SpaceWorlds.cache.invalidate(world)
	}

	@Subcommand("environment remove")
	@Suppress("unused")
	fun onRemoveWorldEnvironment(sender: CommandSender, world: World, environment: Environment) {
		val ionWorld = IonWorld[world]

		if (ionWorld.configuration.environments.remove(environment)) sender.success("Removed flag $environment")
		else return sender.userError("World ${world.name} did not have flag $environment")

		ionWorld.saveConfiguration()
		SpaceWorlds.cache.invalidate(world)
	}

	@Subcommand("environment list")
	@Suppress("unused")
	fun onListEnvironments(sender: CommandSender, world: World, @Optional page: Int?) {
		if ((page ?: 1) <= 0) return sender.userError("Page must not be less than or equal to zero!")

		val builder = text()

		builder.append(text("${world.name} Environments:", HE_LIGHT_GRAY, TextDecoration.BOLD), Component.newline())

		val flags = world.environments().toList()

		val body = formatPaginatedMenu(
			flags.size,
			"/ionworld environment list",
			page ?: 1
		) {
			val flag = flags[it]

			val remove = formatSpacePrefix(bracketed(text("Remove")))
				.clickEvent(ClickEvent.runCommand("/ionworld environment remove $flag"))
				.hoverEvent(text("/ionworld environment remove $flag"))

			ofChildren(flag.toComponent(HE_LIGHT_GRAY), text( ), remove)
		}

		builder.append(body)

		sender.sendMessage(builder.build())
	}

	@Subcommand("apply preset")
	@Suppress("unused")
	fun setPreset(sender: CommandSender, world: World, preset: WorldPreset) {
		preset.setup(world)
		sender.success("Applied preset $preset to ${world.name}")
	}

	/** World setting presets */
	enum class WorldPreset {
		SPACE {
			override fun setup(world: World) {
				val ionWorld = world.ion

				ionWorld.configuration.flags.add(WorldFlag.SPACE_WORLD)
				ionWorld.configuration.flags.add(WorldFlag.ALLOW_SPACE_STATIONS)
				ionWorld.configuration.flags.add(WorldFlag.ALLOW_AI_SPAWNS)
				ionWorld.configuration.flags.add(WorldFlag.SPEEDERS_EXPLODE)
				ionWorld.configuration.flags.add(WorldFlag.ALLOW_MINING_LASERS)

				ionWorld.configuration.environments.add(Environment.NO_GRAVITY)
				ionWorld.configuration.environments.add(Environment.VACUUM)

				ionWorld.saveConfiguration()
				SpaceWorlds.cache.invalidate(world)
			}
		},
		HYPERSPACE {
			override fun setup(world: World) {
				val ionWorld = world.ion

				ionWorld.configuration.flags.add(WorldFlag.SPEEDERS_EXPLODE)

				ionWorld.configuration.environments.add(Environment.NO_GRAVITY)
				ionWorld.configuration.environments.add(Environment.VACUUM)

				ionWorld.saveConfiguration()
			}
		}

		;

		abstract fun setup(world: World)
	}
}
