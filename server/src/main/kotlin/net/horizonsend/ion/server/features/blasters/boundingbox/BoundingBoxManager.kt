package net.horizonsend.ion.server.features.blasters.boundingbox

import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import java.util.*

object BoundingBoxManager {
	private var PLAYER = PlayerBoundingBox()
	private var HUMANOID = DefaultHumanoidBoundingBox()
	private var WITHER_SKELETON = DefaultHumanoidBoundingBox(1.8, 0.45, 2.4)
	private var VILLAGER = DefaultHumanoidBoundingBox(1.4, 0.45, 2.0)
	private var CREEPER = DefaultHumanoidBoundingBox(1.2, 0.4, 1.7)
	private var ENDERMAN = DefaultHumanoidBoundingBox(2.4, 0.4, 3.0)
	private var IRON_GOLEM = ComplexHumanoidBoundingBox(2.1, 0.8, 2.9, 0.4)
	private var WITHER = ComplexHumanoidBoundingBox(2.5, 1.0, 3.5, 1.5)
	private var BAT = DefaultHumanoidBoundingBox(0.5, 0.4, 1.0)
	private var SILVERFISH = ComplexAnimalBoundingBox(0.3, 0.2, 0.0, 0.3, 0.05, 0.15)
	private var CAT = ComplexAnimalBoundingBox(0.4, 0.3, 0.15, 0.4, 0.1, 0.4)
	private var SPIDER = ComplexAnimalBoundingBox(0.8, 0.8, 0.2, 0.8, 0.2, 0.6)
	private var CHICKEN = ComplexAnimalBoundingBox(0.5, 0.25, 0.5, 0.9, 0.2, 0.1)
	private var COW = ComplexAnimalBoundingBox(1.3, 0.5, 1.0, 1.3, 0.25, 0.6)
	private var PHANTOM = ComplexAnimalBoundingBox(0.5, 1.0, 0.0, 0.5, 0.25, 0.15)
	private var PIG = ComplexAnimalBoundingBox(0.8, 0.5, 0.5, 1.0, 0.25, 0.6)
	private var HORSE = ComplexAnimalBoundingBox(1.3, 0.75, 1.3, 2.0, 0.25, 0.75)
	private var WOLF = ComplexAnimalBoundingBox(0.5, 0.3, 0.3, 0.5, 0.1, 0.3)

	private var FISH = ComplexAnimalBoundingBox(0.3, 0.3, 0.0, 0.3, 0.1, 0.21)
	private var ENDERDRAGON = ComplexAnimalBoundingBox(4.0, 8.0, 0.8, 4.0, 1.0, -3.0)
	private var CAVE_SPIDER = ComplexAnimalBoundingBox(0.5, 0.5, 0.15, 0.5, 0.2, 0.4)
	private var RAVAGER = ComplexAnimalBoundingBox(2.0, 1.0, 1.0, 2.2, 1.0, 1.5)
	private var GHAST = ComplexAnimalBoundingBox(4.0, 2.0, 0.0, 4.0, 2.0, 0.0)
	private var GUARDIAN = ComplexAnimalBoundingBox(1.3, 0.6, 0.0, 1.3, 0.6, 0.0)

	private val entityTypeBoundingBox: EnumMap<EntityType, AbstractBoundingBox> = EnumMap(EntityType::class.java)

	init {
		initEntityTypeBoundingBoxes()
	}

	fun getBoundingBox(base: Entity): AbstractBoundingBox? {
		return if (entityTypeBoundingBox.containsKey(base.type)) entityTypeBoundingBox[base.type] else HUMANOID
	}

	private fun setEntityTypeBoundingBox(type: EntityType, box: AbstractBoundingBox) {
		entityTypeBoundingBox[type] = box
	}

	private fun initEntityTypeBoundingBoxes() {
		try {
			setEntityTypeBoundingBox(EntityType.PLAYER, PLAYER)
			setEntityTypeBoundingBox(EntityType.ENDER_DRAGON, ENDERDRAGON)
			setEntityTypeBoundingBox(EntityType.ARMOR_STAND, HUMANOID)
			setEntityTypeBoundingBox(EntityType.SKELETON, HUMANOID)
			setEntityTypeBoundingBox(EntityType.ZOMBIE, HUMANOID)
			setEntityTypeBoundingBox(EntityType.BLAZE, HUMANOID)
			setEntityTypeBoundingBox(EntityType.ZOMBIFIED_PIGLIN, HUMANOID)
			setEntityTypeBoundingBox(EntityType.SNOWMAN, HUMANOID)
			setEntityTypeBoundingBox(EntityType.ENDERMAN, ENDERMAN)
			setEntityTypeBoundingBox(EntityType.WITHER_SKELETON, WITHER_SKELETON)
			setEntityTypeBoundingBox(EntityType.IRON_GOLEM, IRON_GOLEM)
			setEntityTypeBoundingBox(EntityType.CREEPER, CREEPER)
			setEntityTypeBoundingBox(EntityType.SQUID, CREEPER)
			setEntityTypeBoundingBox(EntityType.BAT, BAT)
			setEntityTypeBoundingBox(EntityType.RABBIT, BAT)
			setEntityTypeBoundingBox(EntityType.SPIDER, SPIDER)
			setEntityTypeBoundingBox(EntityType.CAVE_SPIDER, CAVE_SPIDER)
			setEntityTypeBoundingBox(EntityType.SILVERFISH, SILVERFISH)
			setEntityTypeBoundingBox(EntityType.VILLAGER, VILLAGER)
			setEntityTypeBoundingBox(EntityType.ZOMBIE_VILLAGER, VILLAGER)
			setEntityTypeBoundingBox(EntityType.WITCH, VILLAGER)
			setEntityTypeBoundingBox(EntityType.ILLUSIONER, VILLAGER)
			setEntityTypeBoundingBox(EntityType.EVOKER, VILLAGER)
			setEntityTypeBoundingBox(EntityType.PIG, PIG)
			setEntityTypeBoundingBox(EntityType.OCELOT, CAT)
			setEntityTypeBoundingBox(EntityType.CHICKEN, CHICKEN)
			setEntityTypeBoundingBox(EntityType.WOLF, WOLF)
			setEntityTypeBoundingBox(EntityType.COW, COW)
			setEntityTypeBoundingBox(EntityType.MUSHROOM_COW, COW)
			setEntityTypeBoundingBox(EntityType.SHEEP, COW)
			setEntityTypeBoundingBox(EntityType.HORSE, HORSE)
			setEntityTypeBoundingBox(EntityType.DONKEY, HORSE)
			setEntityTypeBoundingBox(EntityType.SKELETON_HORSE, HORSE)
			setEntityTypeBoundingBox(EntityType.ZOMBIE_HORSE, HORSE)
			setEntityTypeBoundingBox(EntityType.MULE, HORSE)
			setEntityTypeBoundingBox(EntityType.GHAST, GHAST)

			setEntityTypeBoundingBox(EntityType.LLAMA, HORSE)
			setEntityTypeBoundingBox(EntityType.WITHER, WITHER)
			setEntityTypeBoundingBox(EntityType.GUARDIAN, GUARDIAN)
			setEntityTypeBoundingBox(EntityType.ELDER_GUARDIAN, GUARDIAN)
			setEntityTypeBoundingBox(EntityType.VEX, BAT)
			setEntityTypeBoundingBox(EntityType.PARROT, BAT)

			setEntityTypeBoundingBox(EntityType.ENDERMITE, SILVERFISH)
			setEntityTypeBoundingBox(EntityType.STRAY, HUMANOID)
			setEntityTypeBoundingBox(EntityType.DROWNED, HUMANOID)
			setEntityTypeBoundingBox(EntityType.HUSK, HUMANOID)
			setEntityTypeBoundingBox(EntityType.POLAR_BEAR, COW)

			setEntityTypeBoundingBox(EntityType.SALMON, FISH)
			setEntityTypeBoundingBox(EntityType.TROPICAL_FISH, FISH)
			setEntityTypeBoundingBox(EntityType.PUFFERFISH, FISH)
			setEntityTypeBoundingBox(EntityType.COD, FISH)
			setEntityTypeBoundingBox(EntityType.DOLPHIN, WOLF)
			setEntityTypeBoundingBox(EntityType.PHANTOM, PHANTOM)
			setEntityTypeBoundingBox(EntityType.TURTLE, WOLF)

			setEntityTypeBoundingBox(EntityType.TRADER_LLAMA, HORSE)
			setEntityTypeBoundingBox(EntityType.RAVAGER, RAVAGER)
			setEntityTypeBoundingBox(EntityType.PANDA, COW)
			setEntityTypeBoundingBox(EntityType.FOX, WOLF)
			setEntityTypeBoundingBox(EntityType.CAT, CAT)
			setEntityTypeBoundingBox(EntityType.WANDERING_TRADER, VILLAGER)
			setEntityTypeBoundingBox(EntityType.PILLAGER, VILLAGER)
			setEntityTypeBoundingBox(EntityType.VINDICATOR, VILLAGER)

			setEntityTypeBoundingBox(EntityType.BEE, BAT)
		} catch (_: Error) {
		} catch (_: Exception) {
		}

		for (type in EntityType.values()) {
			if (!entityTypeBoundingBox.containsKey(type) && type.isAlive) {
				setEntityTypeBoundingBox(type, HUMANOID);
			}
		}
	}
}
