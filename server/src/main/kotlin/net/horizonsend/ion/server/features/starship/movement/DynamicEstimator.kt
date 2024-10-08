package net.horizonsend.ion.server.features.starship.movement


import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.util.Vector
import org.jetbrains.kotlinx.multik.api.arange
import org.jetbrains.kotlinx.multik.api.identity
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.linalg.inv
import org.jetbrains.kotlinx.multik.api.linspace
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.ones
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.DataType
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import org.jetbrains.kotlinx.multik.ndarray.operations.append
import org.jetbrains.kotlinx.multik.ndarray.operations.div
import org.jetbrains.kotlinx.multik.ndarray.operations.minus
import org.jetbrains.kotlinx.multik.ndarray.operations.plus
import org.jetbrains.kotlinx.multik.ndarray.operations.repeat
import org.jetbrains.kotlinx.multik.ndarray.operations.sorted
import org.jetbrains.kotlinx.multik.ndarray.operations.times
import org.jetbrains.kotlinx.multik.ndarray.operations.toList
import kotlin.math.pow

class DynamicEstimator(
	val ship : ActiveStarship,
	val expireTime : Long = 5000L,
	val numTerms : Int = 4) {

	var coeffMatrix  = mk.zeros<Double>(numTerms,3)
	val regulationfactor = 0.5
	val minN = numTerms * 2

	var needsUpdate = true
	var referenceTime = 0L
	var referncePos = Vector()

	var movements = mutableListOf<TranslationMovements>()


	data class TranslationMovements(val time : Long, val movement: TranslateMovement, val origin : Vector)

	private fun estimateCoeffs() {
		println("boop1")
		if (!needsUpdate) return //dont do matrix operations until data changes
		needsUpdate = false
		referenceTime = System.currentTimeMillis()
		if (movements.isEmpty()) {//ship hasnt moved in the time interval
			referncePos = ship.centerOfMass.toVector()
			coeffMatrix  = mk.zeros<Double>(numTerms,3)
			needsUpdate = false
			return
		}

		referncePos = movements[0].origin

		val N = movements.size
		var timeMatrixRaw = mk.ndarray(movements.map { it.time })
		var posMatrix = mk.zeros<Double>(3, N)
		posMatrix[0] = mk.ndarray(movements.map { it.movement.dx.toDouble() })
		posMatrix[1] = mk.ndarray(movements.map { it.movement.dy.toDouble() })
		posMatrix[2] = mk.ndarray(movements.map { it.movement.dz.toDouble() })
		posMatrix = posMatrix.transpose()

		timeMatrixRaw = timeMatrixRaw - referenceTime + expireTime
		val timeMatrix = timeMatrixRaw.asType<Double>(DataType.DoubleDataType) / 1000.0 //convert to double and seconds

		if (N < minN) {
			val padN = minN - N
			val timePad = mk.linspace<Double>(0.0,expireTime.toDouble(),padN)
			val posPad = mk.zeros<Double>(padN, 3)
			timeMatrix.append(timePad)
			posMatrix.append(posPad)
			val timeMatrixList = timeMatrix.toList().withIndex().sortedBy { it.value }
			val temp1 = timeMatrix.copy()
			val temp2 = posMatrix.copy()
			for (i in timeMatrixList.withIndex()) {
				timeMatrix[i.index] = temp1[i.value.index]
				posMatrix[i.index] = temp2[i.value.index]
			}

		}

		posMatrix = mk.math.cumSum(posMatrix, 0) // since these are dxs instead of raw pos might need to check later

		val X = mk.ones<Double>(N, numTerms) // there is no element power function :/ so have to do it by hand
		for (i in 1 until numTerms) {
			var slice = X[i..<numTerms]
			slice *= timeMatrix.asD2Array()
		}

		val solution = (mk.linalg.inv(X.transpose().dot(X) + (mk.identity<Double>(numTerms) * regulationfactor))
			.dot(X.transpose()).dot(posMatrix))

		coeffMatrix = solution
		println(solution)
	}

	fun getDerivative(time : Long, order : Int) : Vector{
		estimateCoeffs()
		val timeOffset = (time - referenceTime + expireTime).toDouble() / 1000// place time in relation to the data
		println(timeOffset)
		var tempCoeffs = coeffMatrix.copy()
		val orgMask = mk.arange<Int>(numTerms).repeat(3).reshape(3,numTerms).transpose()
		val mask = mk.ones<Int>(numTerms, 3)
		for (i in 0 until order) {
			for (j in 0 until  numTerms) {
				val k = j-i
				if (k < 0) {
					mask[j] = mask[j] * 0
				}
				mask[j] = mask[j] * orgMask[j-i]
			}
		}

		val finalMask = mask.asType<Double>(DataType.DoubleDataType)
		println(finalMask)
		tempCoeffs *= finalMask

		val t = mk.ones<Double>(numTerms) // there is no element power function :/ so have to do it by hand
		for (i in 1 until numTerms) {
			t[i] *= timeOffset.pow(i)
		}
		println(tempCoeffs)
		println(t)
		val transposed = tempCoeffs.transpose()
		val result = mk.linalg.dot(transposed, t)
		return Vector(result[0],result[1],result[2])
	}

	fun addData(origin : Vector, movement: TranslateMovement) {
		val time = System.currentTimeMillis()
		movements.add(TranslationMovements(time,movement,origin))
		needsUpdate = true
	}

	fun removeData() {
		val oldSize = movements.size
		val time = System.currentTimeMillis()
		movements = movements.filter { (time - it.time) < expireTime }.toMutableList()
		if (movements.size != oldSize) {
			needsUpdate = true
		}
	}
}
