package com.sumirelabs.flexiblemarker.gui.mui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MarkerSettingsTest {
    @Test void retainsYModifierAmounts() {
        assertEquals(1, MarkerSettings.delta(false, false));
        assertEquals(4, MarkerSettings.delta(false, true));
        assertEquals(16, MarkerSettings.delta(true, false));
        assertEquals(64, MarkerSettings.delta(true, true));
    }
    @Test void chunkLimitCannotOverflowBlockDistance() {
        assertTrue((long) MarkerSettings.MAX_CHUNKS * 16 + 1 <= Integer.MAX_VALUE);
    }
}
