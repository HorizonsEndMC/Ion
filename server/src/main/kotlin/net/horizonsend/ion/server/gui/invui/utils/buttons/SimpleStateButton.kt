package net.horizonsend.ion.server.gui.invui.utils.buttons

import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import java.util.function.Supplier

abstract class SimpleStateButton(
	private val stateProvider: Supplier<Boolean>,
	private val onTrue: ItemProvider,
	private val onFalse: ItemProvider
) : AbstractItem() {

	constructor(
		state: Boolean,
		onTrue: ItemProvider,
		onFalse: ItemProvider
	) : this({ state }, onTrue, onFalse)

	constructor(
		state: Supplier<Boolean>,
		onTrue: ItemStack,
		onFalse: ItemStack
	) : this(state, { onTrue }, { onFalse })

	constructor(
		state: Boolean,
		onTrue: ItemStack,
		onFalse: ItemStack
	) : this({ state }, { onTrue }, { onFalse })

	override fun getItemProvider(): ItemProvider {
		return if (stateProvider.get()) onTrue else onFalse
	}
}
