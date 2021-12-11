package net.starlegacy.feature.multiblock

import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockkObject
import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.util.Vec3i
import net.starlegacy.util.colorize
import net.starlegacy.util.getFacing
import org.bukkit.block.Sign
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal abstract class PowerStoringMultiblockTest : MultiblockTest() {
    abstract override fun getMultiblock(): PowerStoringMultiblock

    protected abstract fun getExpectedNoteBlockOffset(): Vec3i

    override fun setup() {
        super.setup()
        mockPowerMachinesObject()
    }

    private fun mockPowerMachinesObject() {
        mockkObject(PowerMachines)
        val sign = CapturingSlot<Sign>()
        every { PowerMachines.setPower(capture(sign), 0) } answers {
            sign.captured.setLine(2, "&eE: &a0".colorize())
            return@answers 0
        }
    }

    protected fun getExpectedPowerLine() = "&eE: &a0".colorize()

    @Test
    fun noteBlockLocationIsCorrect() {
        val world = mockWorld()
        val sign = mockSign(world)
        val expected = sign.location
            .add(sign.getFacing().oppositeFace.direction)
            .add(getExpectedNoteBlockOffset().toVector())
        val actual = getMultiblock().getNoteblockLocation(sign)
        Assertions.assertEquals(expected, actual)
    }
}
