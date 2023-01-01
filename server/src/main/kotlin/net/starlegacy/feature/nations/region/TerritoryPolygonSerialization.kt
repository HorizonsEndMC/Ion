package net.starlegacy.feature.nations.region

import com.daveanthonythomas.moshipack.MoshiPack
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import java.awt.Polygon

private val moshi = MoshiPack({ add(PolygonAdapter()) })

fun unpackTerritoryPolygon(byteArray: ByteArray): Polygon = moshi.unpack(byteArray)

fun packTerritoryPolygon(polygon: Polygon): ByteArray = moshi.packToByteArray(polygon)

private class PolygonAdapter : JsonAdapter<Polygon>() {
	@FromJson
	override fun fromJson(reader: JsonReader): Polygon {
		var npoints = 0
		var xpoints = intArrayOf()
		var ypoints = intArrayOf()
		reader.beginObject()
		while (reader.hasNext()) {
			when (reader.nextName()) {
				"npoints" -> npoints = reader.nextInt()
				"xpoints" -> xpoints = reader.nextString().split(",").map { it.toInt() }.toIntArray()
				"ypoints" -> ypoints = reader.nextString().split(",").map { it.toInt() }.toIntArray()
			}
		}
		reader.endObject()
		return Polygon(xpoints, ypoints, npoints)
	}

	@ToJson
	override fun toJson(writer: JsonWriter, value: Polygon?) {
		if (value == null) return
		writer.beginObject()
		writer.name("npoints").value(value.npoints)
		writer.name("xpoints").value(value.xpoints.joinToString(","))
		writer.name("ypoints").value(value.ypoints.joinToString(","))
		writer.endObject()
	}
}
