package com.kotori316.marker.render;

import net.minecraft.util.math.AxisAlignedBB;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BoxTest {
    private Box box(double x, double y, double z) {
        return Box.apply(new AxisAlignedBB(1, 2, 3, 1 + x, 2 + y, 3 + z), .125, .25, .5, true, false);
    }

    @Test void retainsAxisSpecializations() {
        assertEquals("BoxX", box(4, 0, 0).getClass().getSimpleName());
        assertEquals("BoxY", box(0, 4, 0).getClass().getSimpleName());
        assertEquals("BoxZ", box(0, 0, 4).getClass().getSimpleName());
        assertEquals("BoxXZ", box(4, 0, 4).getClass().getSimpleName());
        assertEquals("Box", box(4, 4, 4).getClass().getSimpleName());
    }

    @Test void retainsScalaStyleAccessors() {
        Box box = box(4, 5, 6);
        assertEquals(1, box.startX()); assertEquals(2, box.startY()); assertEquals(3, box.startZ());
        assertEquals(5, box.endX()); assertEquals(7, box.endY()); assertEquals(9, box.endZ());
        assertEquals(.125, box.sizeX()); assertEquals(.25, box.sizeY()); assertEquals(.5, box.sizeZ());
        assertTrue(box.firstSide()); assertFalse(box.endSide());
    }

    @Test void retainsEqualityAndHashCode() {
        Box first = box(4, 5, 6), equal = box(4, 5, 6);
        assertEquals(first, equal); assertEquals(first.hashCode(), equal.hashCode());
        assertNotEquals(first, box(4, 5, 7)); assertNotEquals(first, null); assertNotEquals(first, "box");
    }

    @Test void acceptsPointBoxesLikeOriginalFactory() {
        Box point = box(0, 0, 0);
        assertEquals("BoxZ", point.getClass().getSimpleName());
        assertEquals(point.startZ(), point.endZ());
    }
}
