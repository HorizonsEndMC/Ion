package net.horizonsend.ion.server.features.gui.custom.settings.button.general.collection

import net.horizonsend.ion.common.utils.input.InputResult
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

class MapEntryEditorMenu<K : Any, V : Any>(
	viewer: Player,
	initKey: K,
	initValue: V,
	title: String,
	entryValidator: (Pair<K, V>) -> InputResult,
	keyItemFormatter: (K) -> ItemStack,
	keyNameFormatter: (K) -> Component,
	newKey: MapEntryCreationMenu<K, V>.(Player, Consumer<K>) -> Unit,
	valueItemFormatter: (V) -> ItemStack,
	valueNameFormatter: (V) -> Component,
	newValue: MapEntryCreationMenu<K, V>.(Player, Consumer<V>) -> Unit,
	valueConsumer: Consumer<Pair<K, V>>
) : MapEntryCreationMenu<K, V>(viewer, title, entryValidator, keyItemFormatter, keyNameFormatter, newKey, valueItemFormatter, valueNameFormatter, newValue, valueConsumer) {
	override var key: K? = initKey
	override var value: V? = initValue
}
