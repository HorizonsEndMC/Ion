package net.starlegacy.feature.multiblock.areashield

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.verify
import net.starlegacy.feature.machine.AreaShields
import net.starlegacy.feature.multiblock.PowerStoringMultiblockTest
import net.starlegacy.util.colorize
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal abstract class AreaShieldTest : PowerStoringMultiblockTest() {
    abstract override fun getMultiblock(): AreaShield

    protected abstract fun getExpectedRadius(): Int

    override fun getExpectedName(): String = "areashield"

    override fun getExpectedLines() = arrayOf(
        "&6Area".colorize(),
        "&bParticle Shield".colorize(),
        getExpectedPowerLine(),
        "&8Radius: &a${getExpectedRadius()}".colorize()
    )

    @Test
    fun radiusIsCorrect() {
        val expected = getExpectedRadius()
        val actual = getMultiblock().radius
        Assertions.assertEquals(expected, actual)
    }

    override fun setup() {
        super.setup()
        mockAreaShieldsObject()
    }

    private fun mockAreaShieldsObject() {
        mockkObject(AreaShields)
        every { AreaShields.register(any(), any()) } just Runs
    }

    @Test
    fun setupSignRegistersAreaShield() {
        val world = mockWorld()
        val sign = mockSign(world)
        getMultiblock().setupSign(mockPlayer(), sign)
        // don't get this inside the verify block lest it think we're verifying sign.location was called
        val location = sign.location
        val radius = getMultiblock().radius
        verify { AreaShields.register(location, radius) }
    }

    @Test
    fun setupSignMessagesPlayer() {
        val player = mockPlayer()
        val world = mockWorld()
        val sign = mockSign(world)
        getMultiblock().setupSign(player, sign)
        verify { player.sendMessage("&aArea Shield created.".colorize()) }
    }
}
