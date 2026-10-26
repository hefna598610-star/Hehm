package com.example

import com.example.model.FieldStatus
import com.example.model.MagnetReading
import com.example.model.TriggerAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class ExampleUnitTest {
    @Test
    fun testMagnitudeCalculation() {
        val x = 30f
        val y = 40f
        val z = 0f
        val magnitude = sqrt(x * x + y * y + z * z)
        assertEquals(50f, magnitude, 0.001f)
    }

    @Test
    fun testThresholdTriggerLogic() {
        val threshold = 120f
        val normalReading = 45f
        val magnetReading = 145f

        assertTrue(normalReading < threshold)
        assertTrue(magnetReading > threshold)
    }

    @Test
    fun testTriggerActionEnums() {
        assertEquals("firecracker_pop", TriggerAction.FIRECRACKER_POP.id)
        assertEquals("move_to_back", TriggerAction.MOVE_TO_BACK.id)
        assertEquals("haptic_alert", TriggerAction.HAPTIC_ALERT.id)
        assertEquals("audio_radar", TriggerAction.AUDIO_RADAR.id)
    }
}
