package net.horizonsend.ion.server.networks

import com.fasterxml.jackson.annotation.JsonIgnore
import org.bukkit.Bukkit

abstract class Validatable {
	private var lastLoadCheck = Int.MIN_VALUE
	private var lastValidCheck = Int.MIN_VALUE

	private var cachedLoadCheck = false
	private var cachedValidCheck = true

	protected abstract fun checkIsLoaded(): Boolean
	protected abstract fun checkIsValid(): Boolean

	@Deprecated("Function should only be called by the node remover.")
	abstract fun remove()

	@get:JsonIgnore
	val isLoaded: Boolean get() {
		if (lastLoadCheck == Bukkit.getCurrentTick()) return cachedLoadCheck
		cachedLoadCheck = checkIsLoaded()
		lastLoadCheck = Bukkit.getCurrentTick()
		return cachedLoadCheck
	}

	@get:JsonIgnore
	val isValid: Boolean get() {
		if (!cachedValidCheck) return false
		if (!isLoaded) return true
		if (lastValidCheck == Bukkit.getCurrentTick()) return cachedValidCheck
		cachedValidCheck = checkIsValid()
		lastValidCheck = Bukkit.getCurrentTick()
		if (!cachedValidCheck) removalQueue.add(this)
		return cachedValidCheck
	}
}