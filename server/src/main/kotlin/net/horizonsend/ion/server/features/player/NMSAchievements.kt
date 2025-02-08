package net.horizonsend.ion.server.features.player

import io.papermc.paper.adventure.PaperAdventure
import net.horizonsend.ion.server.IonServerComponent
import net.kyori.adventure.text.Component
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.advancements.AdvancementNode
import net.minecraft.advancements.AdvancementTree
import net.minecraft.advancements.AdvancementType
import net.minecraft.advancements.TreeNodePosition
import net.minecraft.advancements.critereon.InventoryChangeTrigger
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.ServerAdvancementManager
import net.minecraft.world.level.block.Blocks
import java.util.function.Consumer

object NMSAchievements : IonServerComponent() {
	override fun onEnable() {
//		boostrapAchievements()
	}

	private fun boostrapAchievements() {
		val advancements = mutableMapOf<ResourceLocation, AdvancementHolder>()

		val consumer = Consumer<AdvancementHolder> { advancementHolder ->
			advancements[advancementHolder.id] = advancementHolder
		}

		Advancement.Builder.advancement()
			.display(
				Blocks.SCULK,
				PaperAdventure.asVanilla(Component.text("HE TEST")),
				PaperAdventure.asVanilla(Component.text("HE TEST DESCRIPTION")),
				ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png"),
				AdvancementType.TASK,
				false,
				false,
				false
			)
			.addCriterion("crafting_table", InventoryChangeTrigger.TriggerInstance.hasItems(Blocks.CRAFTING_TABLE))
			.save(consumer, "test/root")

		apply(advancements)
	}

	fun apply(advancements: Map<ResourceLocation, AdvancementHolder>) {
		val manager = MinecraftServer.getServer().advancements
		val plusOld = manager.advancements.plus(advancements)

		val advancementTree = AdvancementTree()
		advancementTree.addAll(plusOld.values)

		val iterator: Iterator<*> = advancementTree.roots().iterator()

		while (iterator.hasNext()) {
			val advancementNode = iterator.next() as AdvancementNode

			if (advancementNode.holder().value().display().isPresent) {
				TreeNodePosition.run(advancementNode)
			}
		}

		val tree = ServerAdvancementManager::class.java.getDeclaredField("tree")
		tree.isAccessible = true
		tree.set(manager, advancementTree)

		manager.advancements = plusOld
		Registries.ADVANCEMENT.registry()
	}
}
