package net.starlegacy.util

import com.destroystokyo.paper.Title
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

infix fun CommandSender.msg(text: String) = this.sendMessage(text.colorize())

infix fun CommandSender.msg(text: TextComponent) = sendMessage(text)

infix fun Player.action(text: TextComponent) = action(text.toLegacyText())

infix fun Player.action(text: String) = sendActionBar(text.colorize())

infix fun Player.actionAndMsg(text: String) {
    this.action(text)
    this.msg(text)
}

infix fun Player.title(title: Title) = sendTitle(title)

fun Player.title(
    title: TextComponent = "".text(),
    subtitle: TextComponent = "".text(),
    fadeIn: Int = 10,
    stay: Int = 70,
    fadeOut: Int = 20
) {
    sendTitle(title.toLegacyText(), subtitle.toLegacyText(), fadeIn, stay, fadeOut)
}

fun broadcastMessage(text: TextComponent) = Bukkit.broadcast(text)

fun broadcastGlobal(message: String) = Notify.online(message)

fun execConsoleCmd(cmd: String): Boolean = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)

fun String.colorize(): String = ChatColor.translateAlternateColorCodes('&', this)

fun String.stripColor(): String = ChatColor.stripColor(this)

fun String.fromLegacy(): TextComponent = TextComponent(*TextComponent.fromLegacyText(this.colorize()))

fun <T> Iterable<T>.joinToText(
    commaColor: SLTextStyle = SLTextStyle.GRAY,
    transform: (T) -> TextComponent = { toString().fromLegacy() }
): TextComponent {
    val component = TextComponent()
    val valueCount = this.count()
    for ((index, value) in this.withIndex()) {
        component.addExtra(transform(value))
        if (index >= valueCount - 1) continue
        component.addExtra(", ".style(commaColor))
    }
    return component
}

fun String.isAlphanumeric() = matches("^[a-zA-Z0-9]*$".toRegex())

fun String.text(): TextComponent = TextComponent(this)

operator fun TextComponent.plus(that: TextComponent) = apply { addExtra(that) }

fun String.style(style: SLTextStyle) = text().style(style)

fun TextComponent.style(style: SLTextStyle): TextComponent = apply { color = style.wrappedColor }

fun TextComponent.black() = style(SLTextStyle.BLACK)
fun TextComponent.darkBlue() = style(SLTextStyle.DARK_BLUE)
fun TextComponent.darkGreen() = style(SLTextStyle.DARK_GREEN)
fun TextComponent.darkAqua() = style(SLTextStyle.DARK_AQUA)
fun TextComponent.darkRed() = style(SLTextStyle.DARK_RED)
fun TextComponent.darkPurple() = style(SLTextStyle.DARK_PURPLE)
fun TextComponent.gold() = style(SLTextStyle.GOLD)
fun TextComponent.gray() = style(SLTextStyle.GRAY)
fun TextComponent.darkGray() = style(SLTextStyle.DARK_GRAY)
fun TextComponent.blue() = style(SLTextStyle.BLUE)
fun TextComponent.green() = style(SLTextStyle.GREEN)
fun TextComponent.aqua() = style(SLTextStyle.AQUA)
fun TextComponent.red() = style(SLTextStyle.RED)
fun TextComponent.lightPurple() = style(SLTextStyle.LIGHT_PURPLE)
fun TextComponent.yellow() = style(SLTextStyle.YELLOW)
fun TextComponent.white() = style(SLTextStyle.WHITE)
fun TextComponent.obfuscated() = style(SLTextStyle.OBFUSCATED)
fun TextComponent.bold() = apply { isBold = true }
fun TextComponent.strikethrough() = apply { isStrikethrough = true }
fun TextComponent.underline() = apply { isUnderlined = true }
fun TextComponent.italic() = apply { isItalic = true }
fun TextComponent.resetColor() = style(SLTextStyle.RESET)

fun black(s: String) = s.style(SLTextStyle.BLACK)
fun darkBlue(s: String) = s.style(SLTextStyle.DARK_BLUE)
fun darkGreen(s: String) = s.style(SLTextStyle.DARK_GREEN)
fun darkAqua(s: String) = s.style(SLTextStyle.DARK_AQUA)
fun darkRed(s: String) = s.style(SLTextStyle.DARK_RED)
fun darkPurple(s: String) = s.style(SLTextStyle.DARK_PURPLE)
fun gold(s: String) = s.style(SLTextStyle.GOLD)
fun gray(s: String) = s.style(SLTextStyle.GRAY)
fun darkGray(s: String) = s.style(SLTextStyle.DARK_GRAY)
fun blue(s: String) = s.style(SLTextStyle.BLUE)
fun green(s: String) = s.style(SLTextStyle.GREEN)
fun aqua(s: String) = s.style(SLTextStyle.AQUA)
fun red(s: String) = s.style(SLTextStyle.RED)
fun lightPurple(s: String) = s.style(SLTextStyle.LIGHT_PURPLE)
fun yellow(s: String) = s.style(SLTextStyle.YELLOW)
fun white(s: String) = s.style(SLTextStyle.WHITE)
fun obfuscated(s: String) = s.style(SLTextStyle.OBFUSCATED)
fun resetColor(s: String) = s.style(SLTextStyle.RESET)

enum class SLTextStyle(val wrappedColor: ChatColor) {
    BLACK(ChatColor.BLACK),
    DARK_BLUE(ChatColor.DARK_BLUE),
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
    ITALIC(ChatColor.ITALIC),
    RESET(ChatColor.RESET);

    override fun toString() = wrappedColor.toString()
}
