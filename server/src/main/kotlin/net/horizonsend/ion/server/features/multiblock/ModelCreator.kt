package net.horizonsend.ion.server.features.multiblock

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.averageBy
import net.minecraft.core.component.DataComponents
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.util.CraftMagicNumbers
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.InputStreamReader
import kotlin.system.measureTimeMillis

@CommandPermission("ion.nope")
@CommandAlias("modelcreator")
object ModelCreator : SLCommand() {
	val packDir = IonServer.dataFolder.resolve("generated/pack").apply { mkdirs() }
	val itemModelPath = packDir.resolve("assets/horizonsend/models/item/multiblock").apply { mkdirs() }
	val itemDefinitionPath = packDir.resolve("assets/horizonsend/items/multiblock").apply { mkdirs() }

	@Default
	fun createModels(sender: Player) {
		Tasks.async {
			val time = measureTimeMillis {
				val multiblocks = MultiblockRegistration.getAllMultiblocks()

				for (multiblock in multiblocks) {
					makeModelFiles(multiblock)
				}
			}

			sender.success("success, $time ms")
		}
	}

	fun makeModelFiles(multiblock: Multiblock) {
		val parent = if (multiblock is SignlessStarshipWeaponMultiblock<*>) "weapon" else multiblock.name.lowercase()
		val name = multiblock.javaClass.simpleName.lowercase()

		val modelPath = itemModelPath.resolve(parent).apply { mkdirs() }.resolve("${name}.json")

		val modelWriter = BufferedWriter(FileWriter(modelPath))
		val model = createModel(multiblock.shape)
		modelWriter.write(model)
		modelWriter.close()

		val definitionPath = itemDefinitionPath.resolve(parent).apply { mkdirs() }.resolve("$name.json")

		val defintitionWriter = BufferedWriter(FileWriter(definitionPath))
		val definition = """
			{
			    "model": {
			        "type": "minecraft:model",
			        "model": "horizonsend:item/multiblock/$parent/$name"
			    }
			}
		""".trimIndent()
		defintitionWriter.write(definition)
		defintitionWriter.close()
	}

	fun createModel(from: MultiblockShape): String {
		val requirements = from.getRequirementMap(BlockFace.NORTH)
		val examples: List<String> = requirements.mapTo(mutableListOf()) { getMaterialTexture(it.value.example) }.distinct()

		val averageX = requirements.keys.averageBy { vec3i -> vec3i.x.toDouble() }.toInt()
		val averageY = requirements.keys.averageBy { vec3i -> vec3i.y.toDouble() }.toInt()
		val averageZ = requirements.keys.averageBy { vec3i -> vec3i.z.toDouble() }.toInt()

		val center = Vector(7.5 - averageX, 7.5 - averageY, 7.5 - averageZ)

		val builder = StringBuilder()

		builder.append("{\n")
		builder.append("\t${getQuoted("credit")}: ${getQuoted(("GOOTAN"))},\n")
		builder.append("\t${getQuoted("textures")}: {\n")

		examples
			.withIndex()
			.map { (index, example) ->
				"\t\t${getQuoted("$index")}: ${getQuoted(example)},\n"
			}
			.forEach(builder::append)

		builder.append("\t\t${getQuoted("particle")}: ${getQuoted("block/iron_block")}\n")

		builder.append("\t},\n")
		builder.append("\t${getQuoted("elements")}: [\n")

		val cubes = requirements.entries.joinToString(",\n") { (offset, requirement) ->
			val requirementIndex = examples.indexOf(getMaterialTexture(requirement.example))
			val offsetVector = center.clone().add(offset.toVector())

			createBlockString(offsetVector.x, offsetVector.y, offsetVector.z, requirementIndex)
		}

		builder.append(cubes)

		builder.append("\n\t],\n")
		builder.append("\t${getQuoted("display")}: {\n") // Display
		builder.append("\t\t${getQuoted("fixed")}: {\n") // Fixed

		builder.append("\t\t\t${getQuoted("scale")}: [2.0, 2.0, 2.0]\n")

		builder.append("\t\t},\n") // Close fixed

		val maxExtent = maxOf(
			requirements.keys.maxOf { it.x } - requirements.keys.minOf { it.x },
			requirements.keys.maxOf { it.y } - requirements.keys.minOf { it.y },
			requirements.keys.maxOf { it.z } - requirements.keys.minOf { it.z },
		)

		val scale = (-(2.0 / 7.0) * maxExtent) + 4

		builder.append("\t\t${getQuoted("gui")}: {\n") // gui
		builder.append("\t\t\t${getQuoted("rotation")}: [45, -45, 0],\n")
		builder.append("\t\t\t${getQuoted("scale")}: [$scale, $scale, $scale]\n")

		builder.append("\t\t}\n") // Close gui
		builder.append("\t}\n") // Close display

		builder.append("}\n") // Close model

		return builder.toString()
	}

	/**
	 * Creates a json block representing a single cube at the offset provided.
	 * The material index is computed ahead of time.
	 **/
	fun createBlockString(offsetX: Double, offsetY: Double, offsetZ: Double, materialIndex: Int): String {
		val builder = StringBuilder()

		builder.append("\t\t{\n") // Cube block open {
		builder.append("\t\t\t${getQuoted("from")}: [$offsetX, $offsetY, $offsetZ],\n") // Cube start corner
		builder.append("\t\t\t${getQuoted("to")}: [${offsetX + 1}, ${offsetY + 1}, ${offsetZ + 1}],\n") // Cube close corner
		builder.append("\t\t\t${getQuoted("rotation")}: {${getQuoted("angle")}: 0, ${getQuoted("axis")}: ${getQuoted("y")}, ${getQuoted("origin")}: [${offsetX + 0.5}, $offsetY, ${offsetY + 0.5}]},\n") // Empty rotation & origin
		builder.append("\t\t\t${getQuoted("faces")}: {\n") // Faces {

		fun getUVString(face: BlockFace): String {
			return "\t\t\t\t${getQuoted(face.name.lowercase())}: {${getQuoted("uv")}: [0, 0, 16, 16], ${getQuoted("texture")}: ${getQuoted("#$materialIndex")}}"
		}

		val uvBlock = ADJACENT_BLOCK_FACES.joinToString(",\n", transform = ::getUVString)
		builder.append(uvBlock)

		builder.append("\n\t\t\t}\n") // Close UV block
		builder.append("\t\t}")   // Close cube block

		return builder.toString()
	}

	/** To ensure a lack of mistakes, handle the quotation of text in a separate function */
	private fun getQuoted(text: String): String = "\"$text\""

	fun getMaterialTexture(blockData: BlockData): String {
		val customBlock = CustomBlocks.getByBlockData(blockData)
		if (customBlock != null) {
			val item = customBlock.customItem
			return "horizonsend:block/${item.customModel}"
		}

		val material = blockData.material

		val nmsItem = CraftMagicNumbers.getBlock(material).asItem()
		val nmsModel = nmsItem.components().get(DataComponents.ITEM_MODEL)!!.path

		val heAssetsFolder = IonServer.dataFolder.resolve("assets/blocks").apply { mkdirs() }
		var withName = heAssetsFolder.resolve("$nmsModel.json")

		if (!withName.exists()) {
			val closest = heAssetsFolder.listFiles().firstOrNull { file -> file.name.startsWith(nmsModel) }

			if (closest != null) {
				withName = closest
			} else {
				return "blocks/iron_block"
			}
		}

		val reader = BufferedReader(InputStreamReader(withName.inputStream()))
		val jsonObject = JSONParser().parse(reader) as JSONObject
		val textures = jsonObject["textures"] as JSONObject
		val keys = textures.keys

		if (keys.isEmpty()) fail { "why are there no textures" }
		val textureString = textures[keys.first()]

		return textureString as String
	}
}
