package net.horizonsend.ion.server.screens

import net.kyori.adventure.key.Key.key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor.color
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory

class TextScreen(text: TextComponent) : Screen(constructTextScreenInventory(text))

private fun constructTextScreenInventory(text: TextComponent): Inventory {
	println(text)

	val initialComponent = text().append(
			text("\uE007\uF8FF\uE0A8")
				.font(key("horizonsend:special"))
				.color(color(255, 255, 255))
		)

	val newComponent = text()

	val content = text.content()

	var buffer = ""

	var y = 0
	var x = 0

	for (char in content) {
		if (char == '\n') {
			if (buffer.isNotEmpty()) {
				var subComponent: Component = text(buffer)
				if (y > 0) subComponent = subComponent.font(key("horizonsend:y$y"))
				newComponent.append(subComponent)

				if (x > 0) {
					newComponent.append(text((0xDFFF+x).toChar()).font(key("horizonsend:special")))
					x += 1
				}
			}

			buffer = ""
			y += 10
			x = 0

		} else {
			x += when (char) {
				'i', '!', ',' -> 2
				'l' -> 3
				't', ' ' -> 4
				'k', 'f' -> 5
				else -> 6
			}

			buffer += char
		}
	}

	var subComponent: Component = text(buffer)
	if (y > 0) subComponent = subComponent.font(key("horizonsend:y$y"))
	newComponent.append(subComponent)

	val builtComponent = initialComponent.append(newComponent).build()

	println(builtComponent)

	return Bukkit.createInventory(null, 54, builtComponent)
}