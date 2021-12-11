package net.starlegacy.feature.multiblock

import be.seeseemelk.mockbukkit.inventory.ItemFactoryMock
import io.mockk.*
import net.md_5.bungee.api.chat.BaseComponent
import net.starlegacy.StarLegacy
import net.starlegacy.feature.progression.advancement.Advancements
import net.starlegacy.feature.progression.advancement.SLAdvancement
import net.starlegacy.util.*
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.WallSign
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.function.Consumer

internal abstract class MultiblockTest {
    protected abstract fun getMultiblock(): Multiblock
    protected abstract fun getExpectedAdvancement(): SLAdvancement
    protected abstract fun getExpectedLines(): Array<String>
    protected abstract fun getExpectedName(): String

    protected open fun getExpectedInputLines(): Array<String> = arrayOf(
        "[${getExpectedName()}]",
        "",
        "",
        ""
    )

    @BeforeEach
    fun beforeTests() {
        setup()
    }

    protected open fun setup() {
        mockBukkit()
        mockPlugin()
    }

    protected lateinit var plugin: StarLegacy

    private val originX = 0
    private val originY = 10
    private val originZ = 0

    private fun mockBukkit() {
        mockkStatic("org.bukkit.Bukkit")

        val materialSlot = CapturingSlot<Material>()
        every { Bukkit.createBlockData(capture(materialSlot)) } answers {
            val material = materialSlot.captured
            val data = null
            return@answers CBBlockData.newData(material, data)
        }

        val dataSlot = CapturingSlot<String>()
        every { Bukkit.createBlockData(capture(materialSlot), capture(dataSlot)) } answers {
            val material = materialSlot.captured
            val data = dataSlot.captured
            return@answers CBBlockData.newData(material, data)
        }

        val consumerSlot = CapturingSlot<Consumer<BlockData>>()
        every { Bukkit.createBlockData(capture(materialSlot), capture(consumerSlot)) } answers {
            return@answers Bukkit.createBlockData(materialSlot.captured).also { consumerSlot.captured.accept(it) }
        }

        every { Bukkit.getItemFactory() } returns ItemFactoryMock()
    }

    private fun mockPlugin() {
        plugin = mockk()
        every { plugin.name } returns "StarLegacy"
        val namespaceKeyParam = CapturingSlot<String>()
        every { plugin.namespacedKey(capture(namespaceKeyParam)) }.answers {
            NamespacedKey(plugin, namespaceKeyParam.captured)
        }
        StarLegacy.PLUGIN = plugin
    }

    protected fun mockSign(world: World): Sign {
        val sign = mockk<Sign>()
        val signLines = arrayListOf("", "", "", "")
        every { sign.world } returns world
        every { sign.location } answers {
            Location(
                world,
                originX.toDouble(),
                originY.toDouble(),
                originZ.toDouble() - 1
            )
        }
        val getLineIndex = CapturingSlot<Int>()
        every { sign.getLine(capture(getLineIndex)) } answers { signLines[getLineIndex.captured] }
        val setLineIndex = CapturingSlot<Int>()
        val setLineValue = CapturingSlot<String>()
        every { sign.setLine(capture(setLineIndex), capture(setLineValue)) } answers {
            signLines[setLineIndex.captured] = setLineValue.captured
        }
        every { sign.update() } returns true

        mockkStatic("net.starlegacy.util.BlocksKt")
        every { getBlockIfLoaded(world, any(), any(), any()) } returns null
        every { sign.getFacing() } returns BlockFace.NORTH
        val blockData = Material.OAK_WALL_SIGN.createBlockData()
        (blockData as WallSign).facing = sign.getFacing()
        mockBlockAt(world, 0, 0, -1, blockData)
        return sign
    }

    protected fun mockWorld(): World {
        val world = mockk<World>()
        val x = CapturingSlot<Int>()
        val y = CapturingSlot<Int>()
        val z = CapturingSlot<Int>()
        every { world.getBlockAt(capture(x), capture(y), capture(z)) } answers {
            val dx = x.captured - originX
            val dy = y.captured - originY
            val dz = z.captured - originZ
            val blockData = Material.AIR.createBlockData()
            return@answers mockBlockAt(world, dx, dy, dz, blockData)
        }
        return world
    }

    protected fun setSignToInputLines(sign: Sign) {
        for ((index, line) in getExpectedInputLines().withIndex()) {
            sign.setLine(index, line)
        }
    }

    private fun mockBlocks(world: World) {
        // TODO: Make an easy way to do this per-class
        mockBlockAt(world, -1, -1, +0, Material.STONE_BRICK_STAIRS.createBlockData())
        mockBlockAt(world, +0, -1, +0, Material.NOTE_BLOCK.createBlockData())
        mockBlockAt(world, +1, -1, +0, Material.STONE_BRICK_STAIRS.createBlockData())
        mockBlockAt(world, -1, +0, +0, Material.GLASS_PANE.createBlockData())
        mockBlockAt(world, +0, +0, +0, Material.GLASS.createBlockData())
        mockBlockAt(world, +1, +0, +0, Material.GLASS_PANE.createBlockData())
        mockBlockAt(world, +0, -1, +1, Material.STONE_BRICKS.createBlockData())
        mockBlockAt(world, +0, +0, +1, Material.GLASS_PANE.createBlockData())
    }

    private fun mockBlockAt(world: World, dx: Int, dy: Int, dz: Int, blockData: BlockData): Block {
        val x = originX + dx
        val y = originY + dy
        val z = originZ + dz
        val block = mockk<Block>()
        every { block.world } returns world
        every { block.x } returns x
        every { block.y } returns y
        every { block.z } returns z
        every { block.blockData } returns blockData
        every { block.type } returns blockData.material
        every { getBlockTypeSafe(world, x, y, z) } returns block.type
        val relativeX = CapturingSlot<Int>()
        val relativeY = CapturingSlot<Int>()
        val relativeZ = CapturingSlot<Int>()
        every { block.getRelative(capture(relativeX), capture(relativeY), capture(relativeZ)) } answers {
            world.getBlockAt(x + relativeX.captured, y + relativeY.captured, z + relativeZ.captured)
        }
        every { world.getBlockAt(x, y, z) } returns block
        return block
    }

    protected fun mockPlayer(hasAdvancement: Boolean = true, hasPermission: Boolean = false): Player {
        val player = mockk<Player>()
        every { player.sendMessage(any<String>()) } just Runs
        val component = CapturingSlot<BaseComponent>()
        every { player.sendMessage(capture(component)) } answers {
            player.sendMessage(component.captured.toLegacyText()) // default behavior
        }
        every { player.hasPermission(any<String>()) } returns hasPermission
        mockkObject(Advancements)
        every { Advancements.has(player, any()) } returns hasAdvancement
        return player
    }

    @Test
    fun matchesStructureDoesNotMatchJustSign() {
        val world = mockWorld()
        val sign = mockSign(world)
        val multiblock = getMultiblock()
        Assertions.assertFalse(multiblock.signMatchesStructure(sign)) { "Multiblock structure matches without placing any blocks" }
    }

    @Test
    fun matchesStructureFailsOnChunkUnloadedAndLoadChunksFalse() {
        val world = mockWorld()
        val sign = mockSign(world)
        val multiblock = getMultiblock()
        Assertions.assertFalse(multiblock.signMatchesStructure(sign, loadChunks = false)) { "Avoid chunk load fail" }
    }

    @Test
    fun matchesUndetectedSignMatchesName() {
        val world = mockWorld()
        val sign = mockSign(world)
        setSignToInputLines(sign)
        Assertions.assertTrue(getMultiblock().matchesUndetectedSign(sign))
    }

    @Test
    fun matchesSignMatchesAlreadyDetectedSign() {
        val multiblock = getMultiblock()
        val expectedLines = getExpectedLines()
        Assertions.assertTrue(multiblock.matchesSign(expectedLines))
    }

    @Test
    fun matchesSignDoesNotMatchEmptySign() {
        val multiblock = getMultiblock()
        val unexpectedLines = arrayOf("", "", "", "")
        Assertions.assertFalse(multiblock.matchesSign(unexpectedLines))
    }

    @Test
    fun setupSignGeneratesCorrectLines() {
        val world = mockWorld()
        val sign = mockSign(world)
        setSignToInputLines(sign)
        getMultiblock().setupSign(mockPlayer(), sign)
        assertLineMatches(sign, 0)
        assertLineMatches(sign, 1)
        assertLineMatches(sign, 2)
        assertLineMatches(sign, 3)
    }

    private fun assertLineMatches(sign: Sign, lineIndex: Int) {
        val actual = sign.getLine(lineIndex)
        val expected = getExpectedLines()[lineIndex]
        Assertions.assertEquals(expected, actual) { "signLines[$lineIndex] should be $expected but is $actual" }
    }

    @Test
    fun setupSignEnsuresAdvancement() {
        val player = mockPlayer(hasAdvancement = false)
        val world = mockWorld()
        val sign = mockSign(world)
        setSignToInputLines(sign)
        getMultiblock().setupSign(player = player, sign = sign)
        val expectedMessage = ("&7You don't have access to this multiblock! " +
                "To detect it, you need the advancement ${getExpectedAdvancement()}.").colorize()
        verify { player.sendMessage(expectedMessage) }
    }

    @Test
    fun setupSignBypassesAdvancementWhenPlayerHasDutymode() {
        val player = mockPlayer(hasAdvancement = false, hasPermission = true)
        val world = mockWorld()
        val sign = mockSign(world)
        setSignToInputLines(sign)
        getMultiblock().setupSign(player = player, sign = sign)
        val expectedMessage = "&eBypassed advancement ${getExpectedAdvancement()} " +
                "for multiblock ${getMultiblock().javaClass.simpleName}"
        verify { player.sendMessage(expectedMessage.colorize()) }
    }
}
