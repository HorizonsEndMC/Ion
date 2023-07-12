package net.horizonsend.ion.common.utils

typealias IntLocation = Triple<Int, Int, Int>
typealias DoubleLocation = Triple<Double, Double, Double>

fun DoubleLocation.int() = IntLocation(first.toInt(), second.toInt(), third.toInt())
