package net.horizonsend.ion.common.utils.text

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.miscellaneous.toText
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.gui.icons.GuiIconType
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.KeybindComponent
import net.kyori.adventure.text.ScoreComponent
import net.kyori.adventure.text.SelectorComponent
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.flattener.ComponentFlattener
import net.kyori.adventure.text.flattener.FlattenerListener
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.TranslationStore
import java.text.MessageFormat
import java.util.Locale
import java.util.function.Consumer

// Component manipulation
operator fun Component.plus(other: ComponentLike): Component = this.append(other)

/** Shorthand for Component#textOfChildren */
fun ofChildren(vararg children: ComponentLike) = Component.textOfChildren(*children)

/** Analogue of Any#toString */
@JvmOverloads
fun Any.toComponent(vararg decorations: TextDecoration, color: TextColor = WHITE): Component = text(toString(), color, *decorations)
fun Any.toComponent(color: TextColor): Component = text(toString(), color)
fun Any.toComponent(): Component = text(toString())

/**
 * Formats the number into credit format, so it is rounded to the nearest hundredth,
 * commas are placed every 3 digits to the left of the decimal point,
 * and "C" is placed at the beginning of the string.
 */
fun Number.toCreditComponent(): Component = text("C${toDouble().roundToHundredth().toText()}", NamedTextColor.GOLD)

/** Joins the collection of components using the provided separator between each entry */
fun Iterable<ComponentLike>.join(separator: Component? = text(", ")): Component {
	val iterator = this.iterator()

	val builder = text()

	while (iterator.hasNext()) {
		builder.append(iterator.next())

		if (iterator.hasNext() && separator != null) builder.append(separator)
	}

	return builder.build()
}

/** Analogue of Any#toString */
fun Any.toComponent(color: TextColor = WHITE, vararg decorations: TextDecoration): Component = text(toString(), color, *decorations)

/** Returns an empty component if the provided component was null */
fun Component?.orEmpty(): Component = this ?: empty()

fun formatLink(showText: String, link: String): Component {
	return text(showText, BLUE, TextDecoration.UNDERLINED)
		.clickEvent(ClickEvent.openUrl(link))
		.hoverEvent(text(link))
}

// Allow easier static imports
val OBFUSCATED = TextDecoration.OBFUSCATED
val BOLD = TextDecoration.BOLD
val STRIKETHROUGH = TextDecoration.STRIKETHROUGH
val UNDERLINED = TextDecoration.UNDERLINED
val ITALIC = TextDecoration.ITALIC
val HORIZONS_END = text("Horizon's End", HE_LIGHT_GRAY, BOLD)
val HORIZONS_END_BRACKETED = bracketed(text("Horizon's End", HE_LIGHT_GRAY, BOLD))

// Audience utils
fun Audience.sendMessage(vararg message: Component) {
	sendMessage(ofChildren(*message))
}

fun Collection<Audience>.sendMessage(message: ComponentLike) = ForwardingAudience { this }.sendMessage(message)

//<editor-fold desc="Custom GUI helper functions">/

val SPECIAL_FONT_KEY = Key.key("horizonsend:special")
fun yFontKey(y: Int) = Key.key("horizonsend:y$y")

const val TEXT_HEIGHT = 9
// DEFAULT_GUI_WIDTH is the width that text will be formatted within. The actual GUI image width will be this plus
// some margin, usually 8 pixels for each side. The default GUI width in pixels is 176 pixels wide (with an additional
// spacer pixel).
const val DEFAULT_GUI_WIDTH = 169
const val GUI_MARGIN = 8
const val GUI_HEADER_MARGIN = 3
const val SLOT_OVERLAY_WIDTH = 18

const val SHIFT_LEFT_MIN = 1
const val SHIFT_LEFT_MAX = 256
const val SHIFT_RIGHT_MIN = 1
const val SHIFT_RIGHT_MAX = 256
const val SHIFT_DOWN_MIN = -22
const val SHIFT_DOWN_MAX = 110

// Custom characters begin

const val SHIFT_LEFT_BEGIN = 0xE000
const val SHIFT_LEFT_END = 0xE0FF
const val SHIFT_RIGHT_BEGIN = 0xE100
const val SHIFT_RIGHT_END = 0xE1FF

const val SHIFT_LEFT_BEGIN_MIN_1 = 0xDFFF
const val SHIFT_RIGHT_BEGIN_MIN_1 = 0xE0FF

const val DEFAULT_BACKGROUND_CHARACTER = '\uF8FF'
const val CHETHERITE_CHARACTER = '\uF8FE'
const val SLOT_OVERLAY_CHARACTER = '\uF8FD'
const val SPACE_SCREEN_CHARACTER = '\uF8FC'
const val BACKGROUND_EXTENDER = '\uF8FB'
const val ANVIL_BACKGROUND = '\uF8FA'
const val SPACE_BLUE_NEBULA_CHARACTER = '\uF8F9'
const val SPACE_RED_NEBULA_CHARACTER = '\uF8F8'
const val SPACE_STARRY_BACKGROUND_CHARACTER = '\uF8F7'
const val SPACE_MAIN_HYPERSPACE_ROUTES_CHARACTER = '\uF8F6'
const val SPACE_MINOR_HYPERSPACE_ROUTES_CHARACTER = '\uF8F5'
const val MULTIBLOCK_WORKBENCH = '\uF8F4'
const val ADVANCED_SHIP_FACTORY_CHARACTER = '\uF8F3'
const val BAZAAR_BUY_ORDER_MENU_CHARACTER = '\uF8F2'
const val TEXT_INPUT_LEFT_CHARACTER = '\uF8F1'
const val TEXT_INPUT_CENTER_CHARACTER = '\uF8F0'
const val TEXT_INPUT_RIGHT_CHARACTER = '\uF8EF'
const val PENCIL_CHARACTER = '\uF8ED'
const val TRASHCAN_CHARACTER = '\uF8EC'
const val CHECKMARK_CHARACTER = '\uF8EB'
const val ICON_BORDER_CHARACTER = '\uF8EA'
const val CROSS_CHARACTER = '\uF8E6'
const val EMPTY_ICON_CHARACTER = '\uF8E9'
const val SIMPLE_GUI_BORDER_CHARACTER = '\uF8E8'
const val BAZAAR_ORDER_HEADER_ICON = '\uF8E7'
const val BAZAAR_LISTING_HEADER_ICON = '\uF8EE'
const val DEPOSIT_ICON = '\uF8E5'
const val WITHDRAW_ICON = '\uF8E4'
const val RED_SLOT_ICON = '\uF8E3'
const val BAZAAR_ORDER_MANAGE_HEADER_ICON = '\uF8E2'
const val BAZAAR_LISTING_MANAGE_HEADER_ICON = '\uF8E1'

// Custom characters end

val Component.minecraftLength: Int
	get() = this.plainText().minecraftLength

/**
 * Gets the width (in pixels) of a string rendered in the default Minecraft font.
 */
val String.minecraftLength: Int get() = this.sumOf { it.minecraftLength }

val Char.minecraftLength get() = when (code) {
	// for some reason, SHIFT_LEFT_BEGIN and the MIN_1 value does not work here
	in SHIFT_LEFT_BEGIN..SHIFT_LEFT_END -> 0xDFFF - code
	in SHIFT_RIGHT_BEGIN..SHIFT_RIGHT_END -> -0xE0FF + code
	else -> when (this) {
		'\n' -> 0
		'i', '!', ',', '.', '\'', ':', ';', '|' -> 2
		'l', '`' -> 3
		'I', 't', ' ', '\"', '(', ')', '*', '[', ']', '{', '}' -> 4
		'k', 'f', '<', '>' -> 5
		'@', '~', '«', '»' -> 7
		CHETHERITE_CHARACTER -> 10
		SLOT_OVERLAY_CHARACTER -> 19
		else -> GuiIconType.getByDisplayChar(this)?.width?.plus(1) ?: 6
	}
}

/**
 * Create a new Component, shifting text left or right
 * @param shift number of pixels to shift left between -169 and -1, or number of pixels to shift right between 1 and 169
 */
fun shift(shift: Int): Component {
	return when {
		shift > 0 -> rightShift(shift) // positive = right
		shift < 0 -> leftShift(-shift) // negative = left
		else -> empty()
	}
}

/**
 * Create a new Component starting with a left shift
 * @param shift number of pixels to shift between 1 and 169
 */
fun leftShift(shift: Int): Component = if (shift in SHIFT_LEFT_MIN..SHIFT_LEFT_MAX) {
	text((SHIFT_LEFT_BEGIN_MIN_1 + shift).toChar()).font(SPECIAL_FONT_KEY)
} else if (shift > 0) {
	// more than one line: chain multiple 256 left shifts and a remainder shift
	text(repeatString(SHIFT_LEFT_END.toChar().toString(), shift / SHIFT_LEFT_MAX) +
			(SHIFT_LEFT_BEGIN_MIN_1 + (shift % SHIFT_LEFT_MAX)).toChar()).font(SPECIAL_FONT_KEY)
} else empty()

/**
 * Create a new Component starting with a right shift
 * @param shift number of pixels to shift between 1 and 169
 */
fun rightShift(shift: Int): Component = if (shift in SHIFT_RIGHT_MIN..SHIFT_RIGHT_MAX) {
	text((SHIFT_RIGHT_BEGIN_MIN_1 + shift).toChar()).font(SPECIAL_FONT_KEY)
} else if (shift > 0) {
	// more than one line: chain multiple 256 right shifts and a remainder shift
	text(repeatString(SHIFT_RIGHT_END.toChar().toString(), shift / SHIFT_RIGHT_MAX) +
			(SHIFT_RIGHT_BEGIN_MIN_1 + (shift % SHIFT_RIGHT_MAX)).toChar()).font(SPECIAL_FONT_KEY)
} else empty()

/**
 * Add a horizontal shift that returns the text to the start position of the Component
 */
fun Component.shiftToStartOfComponent(): Component {
	return this.append(shift(-this.minecraftLength))
}

/**
 * Add a downward shift to the entire Component
 * @param shift number of pixels to shift between 1 and 110
 */
fun Component.shiftDown(shift: Int): Component = if (shift in SHIFT_DOWN_MIN..SHIFT_DOWN_MAX) {
	this.font(yFontKey(shift))
} else this

/**
 * Add a downward shift to the entire Component equivalent to the next line, plus some offset
 * @param line number of lines to shift down
 * @param shift number of additional pixels to shift down
 */
fun Component.shiftToLine(line: Int, shift: Int = 0): Component = this.shiftDown((line + 1) * TEXT_HEIGHT + shift)

/**
 * Creates a slot overlay component, intended for blocking out slots in an Inventory
 * @param line number of lines to shift down. This must be an even number as each inventory slot is two lines tall
 */
fun slotOverlay(line: Int) = ofChildren(shift(-1), text(SLOT_OVERLAY_CHARACTER, WHITE).shiftToLine(line, GUI_HEADER_MARGIN))

/**
 * Splits the text of the current component so that the text fits within a certain width
 * Implementation based on https://stackoverflow.com/questions/17586/best-word-wrap-algorithm
 * @param width the width in pixels to limit the text to
 */
fun Component.wrap(width: Int): List<Component> {

	// regex: positive lookbehind, matching any character that is newline, tab, space, or hyphen
	val regex = Regex("(?<=[\n\t -])")
	// list of components acting as lines on a GUI
	val lines = mutableListOf<Component>()
	// component for constructing portions of a line
	var currentComponent: Component
	// stores the current style
	var currentStyle: Style = style()
	// list for storing components in each line
	val componentsInLine = mutableListOf<Component>()
	// for parsing component plaintext and adding to new components
	val stringBuilder = StringBuilder()

	val flattener = ADVANCED_FLATTENER

	// The FlattenerListener iterates over a Component and processes over its children. When the listener encounters
	// a new component or style, its functions can be overridden to process the incoming data.
	val listener = object : FlattenerListener {
		var lineLength: Int = 0

		// processes the plaintext of a component
		override fun component(text: String) {
			val words = text.split(regex)

			// always reset currentComponent when encountering a new component (there may be a different Style)
			currentComponent = text("")

			for (word in words) {
				val wordLength = word.minecraftLength

				if (lineLength + wordLength <= width) {
					// add plaintext to the current plaintext of this line
					stringBuilder.append(word)

					// record line length
					lineLength += wordLength

					continue

				} else {
					// line is too long, check if there is only whitespace
					if (lineLength > 0) {

						// current component complete; render current text to a new component and append it to the line component
						currentComponent = text(stringBuilder.toString(), currentStyle)
						componentsInLine.add(currentComponent)
						lines.add(ofChildren(*componentsInLine.toTypedArray()))

						// reset current component, component line list, plaintext, and current line width
						currentComponent = text("")
						componentsInLine.clear()
						stringBuilder.clear()
						lineLength = 0
					}

					// add plaintext to the new line
					stringBuilder.append(word)
					// record line length
					lineLength += wordLength
				}
			}

			// end of this component's processing; render current text to a new component and add it to the line
			currentComponent = text(stringBuilder.toString(), currentStyle)
			componentsInLine.add(currentComponent)
			// reset stringBuilder to make way for the next component
			// do not reset line width as it still keeps track of the total line width
			stringBuilder.clear()
		}

		// get the current component's style
		override fun pushStyle(style: Style) {
			currentStyle = style
		}
	}

	// process component
	flattener.flatten(this, listener)

	// process leftover components that did not fill up the current line
	currentComponent = text(stringBuilder.toString(), currentStyle)
	componentsInLine.add(currentComponent)
	lines.add(ofChildren(*componentsInLine.toTypedArray()))

	return lines
}

fun Component.iterateChildren(consumer: Consumer<Component>) {
	val iterationList = arrayListOf(this)

	while (iterationList.isNotEmpty()) {
		// Remove last so you get a DFS
		val current = iterationList.removeLast()

		consumer.accept(current)

		iterationList.addAll(current.children())
	}
}

fun TextColor.asShadowColor(alpha: Int) = ShadowColor.shadowColor(this, alpha)

private const val ELLIPSES = "..."

fun Component.clip(width: Int, useEllipses: Boolean = true): Component {
	val minecraftLength = this.minecraftLength

	if (minecraftLength <= width) return this

	val flattener = ADVANCED_FLATTENER

	val new = text()

	flattener.flatten(this, object : FlattenerListener {
		var currentStyle: Style = Style.empty()
		var runningLength = 0

		override fun component(text: String) {
			val remaining = minecraftLength - runningLength

			// Cannot append any of the string
			if (remaining <= 0) return

			// Can append the whole string
			if ((remaining - text.minecraftLength) > 0) {
				runningLength += text.minecraftLength
				new.append(text(text, currentStyle))
				return
			}

			val stringBuilder = StringBuilder()

			for (char in text) {
				val charLength = char.minecraftLength
				if (runningLength + charLength > width - (if (useEllipses) ELLIPSES.minecraftLength else 0)) break

				stringBuilder.append(char)
				runningLength += char.minecraftLength
			}

			new.append(text(stringBuilder.toString(), currentStyle))
			if (useEllipses) new.append(text("...", currentStyle))
		}

		override fun shouldContinue(): Boolean {
			return runningLength < width
		}

		override fun pushStyle(style: Style) {
			currentStyle = style
		}
	})

	return new.build()
}

// Modified basic flattener that handles translatable components
val ADVANCED_FLATTENER = ComponentFlattener.builder()
	.mapper(KeybindComponent::class.java) { component -> component.keybind() }
	.mapper(ScoreComponent::class.java) { _ -> "" }
	.mapper(SelectorComponent::class.java, SelectorComponent::pattern)
	.mapper(TextComponent::class.java, TextComponent::content)
	.mapper(TranslatableComponent::class.java) { component -> GlobalTranslator.render(component, Locale.ENGLISH).plainText() }
	.build()

fun bootstrapCustomTranslations() {
	val store = TranslationStore.messageFormat(Key.key("horizonsend", "translator"))

	store.register("death.attack.interceptor_cannon.player", Locale.US, MessageFormat("{0} was killed by {1} using interceptor cannon", Locale.US))
	store.register("death.attack.laser_cannon.player", Locale.US, MessageFormat("{0} was killed by {1} using laser cannon", Locale.US))
	store.register("death.attack.plasma_cannon.player", Locale.US, MessageFormat("{0} was killed by {1} using plasma cannon", Locale.US))
	store.register("death.attack.pulse_cannon.player", Locale.US, MessageFormat("{0} was killed by {1} using pulse cannon", Locale.US))
	store.register("death.attack.capital_beam.player", Locale.US, MessageFormat("{0} was killed by {1} using capital beam", Locale.US))
	store.register("death.attack.cthulhu_beam.player", Locale.US, MessageFormat("{0} was killed by {1} using cthulhu beam", Locale.US))
	store.register("death.attack.fire_wave.player", Locale.US, MessageFormat("{0} was killed by {1} using fire wave", Locale.US))
	store.register("death.attack.flamethrower.player", Locale.US, MessageFormat("{0} was killed by {1} using flamethrower", Locale.US))
	store.register("death.attack.gaze.player", Locale.US, MessageFormat("%s{0}as killed by %s{1}sing the gaze", Locale.US))
	store.register("death.attack.mini_phaser.player", Locale.US, MessageFormat("{0} was killed by {1} using mini phaser", Locale.US))
	store.register("death.attack.pumpkin_cannon.player", Locale.US, MessageFormat("{0} was killed by {1} using pumpkin cannon", Locale.US))
	store.register("death.attack.skull_thrower.player", Locale.US, MessageFormat("{0} was killed by {1} using skull thrower", Locale.US))
	store.register("death.attack.sonic_missile.player", Locale.US, MessageFormat("{0} was killed by {1} using sonic missile", Locale.US))
	store.register("death.attack.arsenal_rocket.player", Locale.US, MessageFormat("{0} was killed by {1} using arsenal rocket", Locale.US))
	store.register("death.attack.doomsday_device.player", Locale.US, MessageFormat("{0} was killed by {1} using doomsday device", Locale.US))
	store.register("death.attack.heavylaser.player", Locale.US, MessageFormat("{0} was killed by {1} using heavy laser", Locale.US))
	store.register("death.attack.phaser.player", Locale.US, MessageFormat("{0} was killed by {1} using phaser", Locale.US))
	store.register("death.attack.oriomium_rocket.player", Locale.US, MessageFormat("{0} was killed by {1} using oriomium rocket", Locale.US))
	store.register("death.attack.torpedo.player", Locale.US, MessageFormat("{0} was killed by {1} using torpedo", Locale.US))
	store.register("death.attack.point_defense.player", Locale.US, MessageFormat("{0} was killed by {1} using point defense", Locale.US))
	store.register("death.attack.interceptor_cannon", Locale.US, MessageFormat("{0} was killed by interceptor cannon", Locale.US))
	store.register("death.attack.laser_cannon", Locale.US, MessageFormat("{0} was killed by laser cannon", Locale.US))
	store.register("death.attack.plasma_cannon", Locale.US, MessageFormat("{0} was killed by plasma cannon", Locale.US))
	store.register("death.attack.pulse_cannon", Locale.US, MessageFormat("{0} was killed by pulse cannon", Locale.US))
	store.register("death.attack.capital_beam", Locale.US, MessageFormat("{0} was killed by capital beam", Locale.US))
	store.register("death.attack.cthulhu_beam", Locale.US, MessageFormat("{0} was killed by cthulhu beam", Locale.US))
	store.register("death.attack.fire_wave", Locale.US, MessageFormat("{0} was killed by fire wave", Locale.US))
	store.register("death.attack.flamethrower", Locale.US, MessageFormat("{0} was killed by flamethrower", Locale.US))
	store.register("death.attack.gaze", Locale.US, MessageFormat("{0} was killed by the gaze", Locale.US))
	store.register("death.attack.mini_phaser", Locale.US, MessageFormat("{0} was killed by mini phaser", Locale.US))
	store.register("death.attack.pumpkin_cannon", Locale.US, MessageFormat("{0} was killed by pumpkin cannon", Locale.US))
	store.register("death.attack.skull_thrower", Locale.US, MessageFormat("{0} was killed by skull thrower", Locale.US))
	store.register("death.attack.sonic_missile", Locale.US, MessageFormat("{0} was killed by sonic missile", Locale.US))
	store.register("death.attack.arsenal_rocket", Locale.US, MessageFormat("{0} was killed by arsenal rocket", Locale.US))
	store.register("death.attack.doomsday_device", Locale.US, MessageFormat("{0} was killed by doomsday device", Locale.US))
	store.register("death.attack.heavylaser", Locale.US, MessageFormat("{0} was killed by heavy laser", Locale.US))
	store.register("death.attack.phaser", Locale.US, MessageFormat("{0} was killed by phaser", Locale.US))
	store.register("death.attack.oriomium_rocket", Locale.US, MessageFormat("{0} was killed by oriomium rocket", Locale.US))
	store.register("death.attack.torpedo", Locale.US, MessageFormat("{0} was killed by torpedo", Locale.US))
	store.register("death.attack.point_defense", Locale.US, MessageFormat("{0} was killed by point defense", Locale.US))

	GlobalTranslator.translator().addSource(store)
}

//</editor-fold>
