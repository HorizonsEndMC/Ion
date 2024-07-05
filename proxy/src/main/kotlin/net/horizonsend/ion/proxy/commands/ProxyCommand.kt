package net.horizonsend.ion.proxy.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.VelocityCommandManager

abstract class ProxyCommand : BaseCommand() {
	open fun onEnable(manager: VelocityCommandManager) {}
}
