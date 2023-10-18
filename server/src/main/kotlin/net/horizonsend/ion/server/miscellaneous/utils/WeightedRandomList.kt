package net.horizonsend.ion.server.miscellaneous.utils

import java.util.Random
import java.util.concurrent.ThreadLocalRandom

// This was fun to learn
class WeightedRandomList<T : Any>() {
	private val weightedEntryList = arrayListOf<WeightedEntry<T>>()

	constructor(vararg constructorEntries: Pair<T, Int>): this() {
		addMany(constructorEntries.toList())
	}

	constructor(map: Map<T, Int>): this() {
		addMany(map)
	}

	var rollingWeight = 0
	val size = weightedEntryList.size

	private data class WeightedEntry<T>(
		val parent: T,
		val weight: Int
	)

	fun addEntry(entry: T, weight: Int): WeightedRandomList<T> {
		rollingWeight += weight

		weightedEntryList += WeightedEntry(
			entry,
			rollingWeight
		)

		return this
	}

	fun addMany(map: Map<T, Int>): WeightedRandomList<T> {
		for ((entry, weight) in map) {
			addEntry(entry, weight)
		}
		return this
	}

	fun addMany(map: List<Pair<T, Int>>): WeightedRandomList<T> {
		for ((entry, weight) in map) {
			addEntry(entry, weight)
		}
		return this
	}

	/**
	 * Provided a double 0..1, it returns an entry T based on its weight and index.
	 **/
	fun getEntry(pos: Double): T {
		check((0.0..1.0).contains(pos)) {
			val exception = IndexOutOfBoundsException("Values provided must be between 0 and 1! Provided: $pos")
			exception.printStackTrace()
			throw exception
		}

		for ((parent, weight) in weightedEntryList) {
			if ((weight.toDouble() / (rollingWeight)) >= pos) return parent
		}

		throw NoSuchElementException("Weighted random list is empty!")
	}

	/**
	 * Gets a random weighted entry.
	 **/
	fun random(): T {
		val selection = Random().nextInt(0, rollingWeight)
		for ((parent, weight) in weightedEntryList) {
			if (weight >= selection) return parent
		}

		throw NoSuchElementException("Weighted random list is empty!")
	}

	fun random(random: Random): T {
		val selection = random.nextInt(0, rollingWeight)
		for ((parent, weight) in weightedEntryList) {
			if (weight >= selection) return parent
		}

		throw NoSuchElementException("Weighted random list is empty!")
	}

	fun randomOrNull(random: Random = ThreadLocalRandom.current()): T? {
		if (weightedEntryList.isEmpty()) return null
		val selection = random.nextInt(0, rollingWeight)
		for ((parent, weight) in weightedEntryList) {
			if (weight >= selection) return parent
		}

		return null
	}

	fun randomOrNull(random: Random = ThreadLocalRandom.current(), predicate: (T) -> Boolean): T? {
		if (weightedEntryList.isEmpty()) return null
		val filtered = weightedEntryList.filter { predicate(it.parent) }

		val selection = random.nextInt(0, filtered.sumOf { it.weight })

		var localRollingWeight = 0
		for ((parent, weight) in filtered) {
			localRollingWeight += weight

			if (localRollingWeight >= selection) return parent
		}

		return null
	}

	fun entries(): List<T> {
		return weightedEntryList.map { it.parent }
	}

	operator fun get(index: Int): T = weightedEntryList[index].parent

	override fun toString(): String {
		return "WeightedRandomList $weightedEntryList"
	}
}
