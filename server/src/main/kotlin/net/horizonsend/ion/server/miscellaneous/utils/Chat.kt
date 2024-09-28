package net.horizonsend.ion.server.miscellaneous.utils

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Deprecated(
	"Use Paper's MiniMessage Function",
)
infix fun CommandSender.msg(text: String) = this.sendMessage(text.colorize())

@Deprecated(
	"Use Paper's MiniMessage Function",
)
infix fun CommandSender.msg(text: TextComponent) = sendMessage(text)

@Deprecated(
	"Use Ion MiniMessage Extension Functions",
)
infix fun Player.action(text: TextComponent) = action(text.toLegacyText())

@Deprecated(
	"Use Ion MiniMessage Extension Functions",
)
infix fun Player.action(text: String) = sendActionBar(text.colorize())

@Deprecated(
	"Use Ion MiniMessage Extension Functions",
)
infix fun Player.actionAndMsg(text: String) {
	this.action(text)
	this.msg(text)
}

@Deprecated("Use Ion MiniMessage Extension Functions")
fun Player.title(
	title: TextComponent = "".text(),
	subtitle: TextComponent = "".text(),
	fadeIn: Int = 10,
	stay: Int = 70,
	fadeOut: Int = 20
) {
	sendTitle(title.toLegacyText(), subtitle.toLegacyText(), fadeIn, stay, fadeOut)
}

fun execConsoleCmd(cmd: String): Boolean = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)

@Deprecated("Use Ion MiniMessage Extension Functions")
fun String.colorize(): String = ChatColor.translateAlternateColorCodes('&', this)

@Deprecated("Use Ion MiniMessage Extension Functions")
fun String.stripColor(): String = ChatColor.stripColor(this)

@Deprecated("Use Ion MiniMessage Extension Functions")
fun String.text(): TextComponent = TextComponent(this)

@Deprecated("Use Ion MiniMessage Extension Functions")
fun String.style(style: SLTextStyle) = text().style(style)

@Deprecated("Use Ion MiniMessage Extension Functions")
fun TextComponent.style(style: SLTextStyle): TextComponent = apply { color = style.wrappedColor }

@Deprecated("Use Ion MiniMessage Extension Functions")
fun TextComponent.bold() = apply { isBold = true }

@Deprecated("Use Ion MiniMessage Extension Functions")
fun aqua(s: String) = s.style(SLTextStyle.AQUA)

@Deprecated("Use Ion MiniMessage Extension Functions")
fun red(s: String) = s.style(SLTextStyle.RED)

@Deprecated("Use Ion MiniMessage Extension Functions")
fun yellow(s: String) = s.style(SLTextStyle.YELLOW)

@Suppress("Unused")
@Deprecated("Use Ion MiniMessage Extension Functions")
enum class SLTextStyle(val wrappedColor: ChatColor) {
	BLACK(ChatColor.BLACK),
	DARK_GREEN(ChatColor.DARK_GREEN),
	DARK_AQUA(ChatColor.DARK_AQUA),
	DARK_RED(ChatColor.DARK_RED),
	DARK_PURPLE(ChatColor.DARK_PURPLE),
	GOLD(ChatColor.GOLD),
	GRAY(ChatColor.GRAY),
	DARK_GRAY(ChatColor.DARK_GRAY),
	BLUE(ChatColor.BLUE),
	GREEN(ChatColor.GREEN),
	AQUA(ChatColor.AQUA),
	RED(ChatColor.RED),
	LIGHT_PURPLE(ChatColor.LIGHT_PURPLE),
	YELLOW(ChatColor.YELLOW),
	WHITE(ChatColor.WHITE),
	OBFUSCATED(ChatColor.MAGIC),
	RESET(ChatColor.RESET);

	override fun toString() = wrappedColor.toString()
}
