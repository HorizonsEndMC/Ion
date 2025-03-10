package net.horizonsend.ion.server.features.starship.movement


import net.horizonsend.ion.common.utils.miscellaneous.i
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.util.Vector
import java.lang.IndexOutOfBoundsException
import kotlin.math.absoluteValue
import kotlin.math.pow

class KinematicEstimator(
	val ship : ActiveStarship,
	val expireTime : Long = 5000L,
	val numTerms : Int = 5,
	val regulationfactor: Double = 0.2,
	val dataWeightFactor: Double = 0.85
	) {
	var xCoefficients = DoubleArray(numTerms + 1)
	var yCoefficients = DoubleArray(numTerms + 1)
	var zCoefficients = DoubleArray(numTerms + 1)

	val minN = numTerms * 2

	var needsUpdate = true
	var referenceTime = 0L
	var referncePos = Vector()

	var movements = mutableListOf<TranslationMovements>()


	data class TranslationMovements(val time : Long, val movement: TranslateMovement, val origin : Vector)

	private fun estimateCoeffs() {
		if (!needsUpdate) return //dont do matrix operations until data changes
		xCoefficients = DoubleArray(numTerms + 1)
		yCoefficients = DoubleArray(numTerms + 1)
		zCoefficients = DoubleArray(numTerms + 1)
		if (movements.isEmpty()) {//ship hasnt moved in the time interval
			referncePos = ship.centerOfMass.toVector()
			referenceTime = System.currentTimeMillis()
			return
		}
		//println("# of movements : ${movements.size}")
		needsUpdate = false
		referenceTime = movements[0].time
		referncePos = movements[0].origin.clone().add(
			Vector(-movements[0].movement.dx,
				   -movements[0].movement.dy,
				   -movements[0].movement.dz))

		val N = movements.size
		val timesRaw = movements.map { it.time }
		val times = timesRaw.map { (it - referenceTime - expireTime).toDouble() / 1000.0}.toMutableList()
		val xData = movements.map { it.movement.dx.toDouble() }.toMutableList()
		val yData = movements.map { it.movement.dy.toDouble() }.toMutableList()
		val zData = movements.map { it.movement.dz.toDouble() }.toMutableList()

		if (N < minN) {
			val padN = minN - N
			val timePad = DoubleArray(padN){it.i() * expireTime.toDouble() / padN.toDouble() - expireTime}.toList()
			val xPad = DoubleArray(padN).toList()
			xData.addAll(xPad)
			yData.addAll(xPad)
			zData.addAll(xPad)
			times.addAll(timePad)
			val timeSorted = times.withIndex().sortedBy { it.value }
			val temp1 = xData.toList()
			val temp2 = yData.toList()
			val temp3 = zData.toList()
			val temp4 = times.toList()
			for (i in timeSorted.withIndex()) {
				xData[i.index] = temp1[i.value.index]
				yData[i.index] = temp2[i.value.index]
				zData[i.index] = temp3[i.value.index]
				times[i.index] = temp4[i.value.index]
			}
		}

		val xCum = xData.runningReduce {sum, el -> sum+el}
		val yCum = yData.runningReduce {sum, el -> sum+el}
		val zCum = zData.runningReduce {sum, el -> sum+el}


		//println("first time: ${times[0]}, last time: ${times.last()}")
		//println("first x: ${xCum[0]}, last x: ${xCum.last()}")
		//println("first y: ${yCum[0]}, last y: ${yCum.last()}")
		//println("first z: ${zCum[0]}, last z: ${zCum.last()}")

		val resultX = polynomialRegression(times.zip(xCum),numTerms)
		val resultY = polynomialRegression(times.zip(yCum),numTerms)
		val resultZ = polynomialRegression(times.zip(zCum),numTerms)

		for (i in resultX.indices) {
			xCoefficients[i] = resultX[i]
			yCoefficients[i] = resultY[i]
			zCoefficients[i] = resultZ[i]
		}
		//println("xCoeffs: ${xCoefficients.contentToString()}")
		//println("yCoeffs: ${yCoefficients.contentToString()}")
		//println("zCoeffs: ${zCoefficients.contentToString()}")
	}

	fun getDerivative(time: Long, order: Int): Vector {
		if (order >= numTerms) throw IndexOutOfBoundsException("Derivative order exceeds available terms")
		estimateCoeffs()
		val timeOffset = (time - referenceTime - expireTime).toDouble() / 1000 // place time in relation to data
		//println(timeOffset)
		return evaluatePolynomial(timeOffset, order)[order]
	}

	fun getKinematics(time: Long): List<Vector> {
		estimateCoeffs()
		val timeOffset = (time - referenceTime - expireTime).toDouble() / 1000 // Normalize time
		return evaluatePolynomial(timeOffset, numTerms)
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

	// Function to perform polynomial regression for one coordinate with degree-weighted regularization
	private fun polynomialRegression(data: List<Pair<Double, Double>>, degree: Int,
									 lambda: Double = regulationfactor, s: Double = dataWeightFactor): DoubleArray {
		val n = data.size
		val adjustedDegree = if (n <= degree) n - 1 else degree
		// to solve for the coefficients a we need to solve system (X_T@W@X +L)@a = X_T@W@y
		//(X_T@W@X +L)
		val X = Array(adjustedDegree + 1) { DoubleArray(adjustedDegree + 1) { 0.0 } }
		//X_t@W@y
		val Y = DoubleArray(adjustedDegree + 1) { 0.0 }


		for ((index, entry) in data.withIndex()) {
			val (t, value) = entry
			val weight = weightFunction(index, n, s)  // Compute weight for this data point

			for (i in 0..adjustedDegree) {
				for (j in 0..adjustedDegree) {
					X[i][j] += weight * t.pow(i + j)  // Normal equation
				}
				Y[i] += weight * value * t.pow(i)
			}
		}

		// Apply degree-based regularization only to diagonal elements
		for (i in 1..adjustedDegree) {  // Start from 1 to avoid penalizing the constant term
			X[i][i] += lambda * i.toDouble().pow(3)  // Power scaling on higher-degree terms
		}

		return solveLinearSystem(X, Y)
	}

	// Define weight function: More recent points get higher weight
	private fun weightFunction(index: Int, totalPoints: Int, s: Double): Double {
		return s.pow(totalPoints - 1 - index)
	}

	// Function to solve a linear system using Gaussian elimination
	private fun solveLinearSystem(matrix: Array<DoubleArray>, vector: DoubleArray): DoubleArray {
		val n = vector.size
		val augmentedMatrix = Array(n) { i -> matrix[i] + doubleArrayOf(vector[i]) }

		for (i in 0 until n) {
			var maxRow = i
			for (k in i + 1 until n) {
				if (augmentedMatrix[k][i].absoluteValue > augmentedMatrix[maxRow][i].absoluteValue) {
					maxRow = k
				}
			}
			augmentedMatrix[i] = augmentedMatrix[maxRow].also { augmentedMatrix[maxRow] = augmentedMatrix[i] }

			for (k in i + 1 until n) {
				val factor = augmentedMatrix[k][i] / augmentedMatrix[i][i]
				for (j in i until n + 1) {
					augmentedMatrix[k][j] -= factor * augmentedMatrix[i][j]
				}
			}
		}

		val solution = DoubleArray(n)
		for (i in n - 1 downTo 0) {
			solution[i] = augmentedMatrix[i][n] / augmentedMatrix[i][i]
			for (k in 0 until i) {
				augmentedMatrix[k][n] -= augmentedMatrix[k][i] * solution[i]
			}
		}

		return solution
	}

	// Function to evaluate polynomial and its derivatives up to the highest order needed
	private fun evaluatePolynomial(t: Double, maxOrder: Int): List<Vector> {
		fun evaluate(coefficients: DoubleArray, t: Double, maxOrder: Int): List<Double> {
			val results = MutableList(maxOrder+1) { 0.0 }

			for (i in coefficients.indices) {
				for (j in 0..minOf(i, maxOrder)) { // Compute up to maxOrder derivatives
					val factor = (i downTo (i - j + 1)).fold(1.0) { acc, k -> acc * k }
					results[j] += factor * coefficients[i] * t.pow(i - j)
				}
			}

			return results
		}

		val xResults = evaluate(xCoefficients, t, maxOrder)
		val yResults = evaluate(yCoefficients, t, maxOrder)
		val zResults = evaluate(zCoefficients, t, maxOrder)

		return List(maxOrder + 1) { order ->
			Vector(xResults[order], yResults[order], zResults[order])
		}
	}
}
