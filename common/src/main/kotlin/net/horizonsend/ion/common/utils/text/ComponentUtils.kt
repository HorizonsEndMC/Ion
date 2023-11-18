package net.horizonsend.ion.common.utils.text

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.miscellaneous.toText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

fun String.miniMessage() = MiniMessage.miniMessage().deserialize(this)
fun Component.plainText(): String = PlainTextComponentSerializer.plainText().serialize(this)
fun component(vararg children: ComponentLike) = Component.textOfChildren(*children)

/**
 * Formats the number into credit format, so it is rounded to the nearest hundredth,
 * commas are placed every 3 digits to the left of the decimal point,
 * and "C" is placed at the beginning of the string.
 */
fun Number.toCreditComponent(): Component = Component.text("C${toDouble().roundToHundredth().toText()}", NamedTextColor.GOLD)

fun Component.plusAssign(other: ComponentLike): Component = this.append(other)
