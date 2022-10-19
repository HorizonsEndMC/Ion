package net.starlegacy.feature.economy.collectors

import net.starlegacy.database.schema.economy.CollectedItem

/**
 * @property item The base item stack this mission is collecting
 * @property stacks The amount of full stacks of the item to require for the mission
 * @property reward The amount of credits to give for giving all of the required stacks of the item
 * @property xp The amount of XP to give in reward for this mission
 */
data class CollectionMission(val item: CollectedItem, val stacks: Int, val reward: Int, val xp: Int)
