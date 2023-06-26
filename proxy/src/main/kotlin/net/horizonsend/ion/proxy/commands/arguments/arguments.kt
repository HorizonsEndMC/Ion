package net.horizonsend.ion.proxy.commands.arguments

import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.proxy.Player
import net.horizonsend.ion.common.commands.arguments.argumentImplied
import net.horizonsend.ion.common.commands.arguments.error
import net.horizonsend.ion.common.commands.dsl.DslCommandBuilder
import net.horizonsend.ion.proxy.IonProxy
import kotlin.jvm.optionals.getOrNull

fun <S> DslCommandBuilder<S>.player(name: String) = argumentImplied<S, Player>(
	name,
	{
		IonProxy.proxy.getPlayer(it.readString()).getOrNull() ?: error("Player not found")
	}
) { commandContext: CommandContext<S>, s: String -> commandContext.getArgument(s, Player::class.java) }

