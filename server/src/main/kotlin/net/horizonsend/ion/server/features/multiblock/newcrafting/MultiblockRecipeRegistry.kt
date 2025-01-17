package net.horizonsend.ion.server.features.multiblock.newcrafting

import io.papermc.paper.util.Tick
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.multiblock.entity.type.RecipeProcessingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.newcrafting.input.FurnaceEnviornment
import net.horizonsend.ion.server.features.multiblock.newcrafting.input.RecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.FurnaceMultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.NewMultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement.ItemRequirement
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement.PowerRequirement
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.result.ItemResult
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.result.ResultHolder
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.result.WarmupResult
import net.horizonsend.ion.server.features.multiblock.type.industry.CentrifugeMultiblock
import net.horizonsend.ion.server.features.multiblock.type.industry.CompressorMultiblock
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import net.kyori.adventure.sound.Sound
import org.bukkit.SoundCategory
import java.time.Duration
import kotlin.reflect.KClass

object MultiblockRecipeRegistry : IonServerComponent() {
	val recipes = mutableListOf<NewMultiblockRecipe<*>>()
	val byMultiblock = multimapOf<KClass<out RecipeProcessingMultiblockEntity<*>>, NewMultiblockRecipe<*>>()

	val URANIUM_ENRICHMENT = register(FurnaceMultiblockRecipe(
		identifier = "URANIUM_ENRICHMENT",
		clazz = CentrifugeMultiblock.CentrifugeMultiblockEntity::class,
		smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemRegistry.URANIUM),
		fuelItem = null,
		power = PowerRequirement(100),
		result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
			duration = Duration.ofSeconds(5),
			normalResult = ItemResult.simpleResult(CustomItemRegistry.ENRICHED_URANIUM),
		))
			.playSound(Sound.sound(NamespacedKeys.packKey("industry.centrifuge"), SoundCategory.BLOCKS, 1.0f, 1.0f), true)
			.updateFurnace()
	))

	val URANIUM_CORE_COMPRESSION = register(FurnaceMultiblockRecipe(
		identifier = "URANIUM_CORE_COMPRESSION",
		clazz = CompressorMultiblock.CompressorMultiblockEntity::class,
		smeltingItem = ItemRequirement.CustomItemRequirement(CustomItemRegistry.URANIUM_CORE),
		fuelItem = null,
		power = PowerRequirement(100),
		result = ResultHolder.of(WarmupResult<FurnaceEnviornment>(
			Tick.of(60L * 60L * 20L),
			ItemResult.simpleResult(CustomItemRegistry.URANIUM_ROD),
		))
			.updateProgressText()
			.updateFurnace()
	))

	fun <E: RecipeEnviornment, R: NewMultiblockRecipe<E>> register(recipe: R): R {
		recipes.add(recipe)
		byMultiblock[recipe.entityType].add(recipe)

		return recipe
	}

	fun <E: RecipeEnviornment> getRecipesFor(entity: RecipeProcessingMultiblockEntity<E>): Collection<NewMultiblockRecipe<E>> {
		@Suppress("UNCHECKED_CAST")
		return byMultiblock[entity::class] as Collection<NewMultiblockRecipe<E>>
	}
}
