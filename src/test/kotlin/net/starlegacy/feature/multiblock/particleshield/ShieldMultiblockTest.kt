package net.starlegacy.feature.multiblock.particleshield

import io.mockk.verify
import net.starlegacy.feature.multiblock.MultiblockTest
import net.starlegacy.util.colorize
import org.junit.jupiter.api.Test

internal abstract class ShieldMultiblockTest : MultiblockTest() {
    override fun getExpectedName(): String = "shield"

    override fun getExpectedLines(): Array<String> = arrayOf(
        "&3Particle Shield".colorize(),
        "&7Generator".colorize(),
        "&duncolored shield name",
        "&8Class ${getClassText()}".colorize()
    )

    override fun getExpectedInputLines(): Array<String> {
        val expectedInputLines = super.getExpectedInputLines()
        expectedInputLines[1] = "&duncolored shield name"
        return expectedInputLines
    }

    protected abstract fun getClassText(): String

    @Test
    fun setupSignRequiresSecondLine() {
        val player = mockPlayer(hasAdvancement = false)
        val world = mockWorld()
        val sign = mockSign(world)
        setSignToInputLines(sign)
        sign.setLine(1, "")
        getMultiblock().setupSign(player = player, sign = sign)
        verify { player.sendMessage("&cThe second line must be the shield's name.".colorize()) }
    }
}
