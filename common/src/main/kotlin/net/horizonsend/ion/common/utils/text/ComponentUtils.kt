package net.horizonsend.ion.common.utils.text

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.miscellaneous.toText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import java.util.regex.Pattern

fun String.miniMessage() = MiniMessage.miniMessage().deserialize(this)
fun ComponentLike.plainText(): String = PlainTextComponentSerializer.plainText().serialize(this.asComponent())

fun ofChildren(vararg children: ComponentLike) = Component.textOfChildren(*children)

/**
 * Formats the number into credit format, so it is rounded to the nearest hundredth,
 * commas are placed every 3 digits to the left of the decimal point,
 * and "C" is placed at the beginning of the string.
 */
fun Number.toCreditComponent(): Component = text("C${toDouble().roundToHundredth().toText()}", NamedTextColor.GOLD)

fun Component.plusAssign(other: ComponentLike): Component = this.append(other)

fun template(
	message: String,
	color: TextColor,
	paramColor: TextColor = NamedTextColor.WHITE,
	vararg parameters: Any
): Component {
	val messageComponent = text(message, color)

	var index = 0

	val replacement = TextReplacementConfig.builder()
		.match(Pattern.compile("\\{([0-9]*?)}"))
		.times(parameters.size)
		.replacement { built ->
			val param = parameters[index]

			index++

			return@replacement when (param) {
				is ComponentLike -> param
				is Number -> text(param.toString(), paramColor)
				else -> text("\"$param\"", paramColor)
			}
		}
		.build()

	return messageComponent.replaceText(replacement)
}

fun Iterable<ComponentLike>.join(separator: Component = text(", ")): Component {
	val iterator = this.iterator()

	val builder = text()

	while (iterator.hasNext()) {
		builder.append(iterator.next())

		if (iterator.hasNext()) builder.append(separator)
	}

	return builder.build()
}
