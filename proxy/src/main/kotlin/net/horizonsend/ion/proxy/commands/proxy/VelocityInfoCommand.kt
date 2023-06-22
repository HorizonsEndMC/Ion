 package net.horizonsend.ion.proxy.commands.proxy

 import com.velocitypowered.api.command.SimpleCommand
 import net.kyori.adventure.text.Component.text
 import net.kyori.adventure.text.event.ClickEvent
 import net.kyori.adventure.text.format.NamedTextColor
 import net.kyori.adventure.text.format.TextColor
 import net.kyori.adventure.text.format.TextDecoration

 class VelocityInfoCommand : SimpleCommand {

 	@Suppress("Unused")
	override fun execute(invocation: SimpleCommand.Invocation) {
		invocation.source().sendMessage(
 			text()
 				.append(
 					text("Here are a few links of potential use:\n")
 						.color(TextColor.fromHexString("#8888ff"))
 				)
 				.append(
					text("Survival Web Map\n")
 						.clickEvent(ClickEvent.openUrl("https://survival.horizonsend.net"))
 						.color(NamedTextColor.WHITE)
 						.decorate(TextDecoration.UNDERLINED)
 				)
 				.append(
					text("Creative Web Map\n")
 						.clickEvent(ClickEvent.openUrl("https://creative.horizonsend.net"))
 						.color(NamedTextColor.WHITE)
						.decorate(TextDecoration.UNDERLINED)
 				)
 				.append(
					text("Discord Server\n")
 						.clickEvent(ClickEvent.openUrl("https://discord.gg/RPvgQsGzKM"))
 						.color(NamedTextColor.WHITE)
						.decorate(TextDecoration.UNDERLINED)
 				)
 				.append(
					text("Resource Pack\n")
 						.clickEvent(ClickEvent.openUrl("https://github.com/HorizonsEndMC/ResourcePack/releases/latest"))
 						.color(NamedTextColor.WHITE)
						.decorate(TextDecoration.UNDERLINED)
 				)
 				.append(
					text("Wiki\n")
 						.clickEvent(ClickEvent.openUrl("https://wiki.horizonsend.net"))
 						.color(NamedTextColor.WHITE)
						.decorate(TextDecoration.UNDERLINED)
 				)
 				.append(
					text("Patreon\n")
 						.clickEvent(ClickEvent.openUrl("https://www.patreon.com/horizonsendmc"))
 						.color(NamedTextColor.WHITE)
						.decorate(TextDecoration.UNDERLINED)
 				)
 				.append(
					text("Server Rules")
 						.clickEvent(ClickEvent.openUrl( "https://wiki.horizonsend.net/rules"))
 						.color(NamedTextColor.WHITE)
						.decorate(TextDecoration.UNDERLINED)
 				)
 				.build()
 		)
 	}
 }
