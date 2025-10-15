package net.horizonsend.ion.proxy.registration

import net.horizonsend.ion.proxy.commands.ProxyCommand
import net.horizonsend.ion.proxy.commands.velocity.MessageCommand
import net.horizonsend.ion.proxy.commands.velocity.ReplyCommand
import net.horizonsend.ion.proxy.commands.velocity.VelocityInfoCommand
import net.horizonsend.ion.proxy.commands.velocity.VelocityMailCommand

val commands: List<ProxyCommand> = listOf(
	MessageCommand,
	ReplyCommand,
	VelocityInfoCommand,
	VelocityMailCommand
)
