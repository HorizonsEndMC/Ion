package net.horizonsend.ion.server.features.ai.module.steering.context

import java.util.Arrays
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class LinearContext {
	val bins = DoubleArray(NUMLINBINS)
	companion object {

		const val NUMLINBINS = 12
	}

	val binloc = Array(NUMLINBINS) { i ->
		val spacing = 1.0 /(NUMLINBINS - 3).toDouble()
		(i - 1) * spacing
	}
	constructor() {
		bins.fill(0.0)
	}

	constructor(other: LinearContext) {
		for (i in 0 until NUMLINBINS) {
			bins[i] = other.bins[i]
		}
	}

	/**
	 * Sets all bins to 0.0
	 */
	fun clearContext() {
		Arrays.fill(bins, 0.0)
	}

	fun addContext(other: LinearContext) {
		for (i in 0 until NUMLINBINS) {
			bins[i] += other.bins[i]
		}
	}

	inline fun apply(func : (i : Int) -> Double, clear : Boolean = true) {
		if (clear) clearContext()
		for (i in 0 until NUMLINBINS) {
			bins[i] += func(i)
		}
	}

	fun populatePeak(scalar : Double, weight : Double, edgepoints: Boolean = true, spread : Double = 0.25, power : Double = 2.0): (Int) -> Double {
		var scaled = max(0.0, min(scalar, 1.0))
		if (edgepoints) {
			val range = binloc.last() - binloc.first()
			scaled = scaled * range + binloc.first()
		}
		val lamb  = {i : Int ->
			val loc = binloc[i]
			val dist = abs(scaled - loc)
			max(0.0, 1.0 - dist/spread).pow(power)*weight
		}
		return lamb
	}

	fun interpolotedMax(): Double {
		val i = bins.withIndex().maxBy { it.value }.index
		var j = i-1
		var maxval = 0.0
		for (k in i-1 until i+2){
			if (k == i || k < 0 || k == NUMLINBINS) {
				continue
			}
			if (bins[k] > maxval) {
				maxval = bins[k]
				j = k
			}
		}
		val interpolated = (bins[i]*binloc[i] + bins[j]*binloc[j])/(bins[i]+bins[j])
		return max(0.0, min(interpolated, 1.0))
	}
}
