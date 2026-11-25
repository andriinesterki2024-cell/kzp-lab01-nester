package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Перевіряє enum {@link DeviceKind}: розпізнавання, OTHER, ознаку побутової техніки. */
class DeviceKindTest {

    @Test
    void recognizesKnownDevicesByFirstWords() {
        assertSame(DeviceKind.LAPTOP, DeviceKind.fromDevice("Ноутбук Lenovo IdeaPad"));
        assertSame(DeviceKind.WASHER, DeviceKind.fromDevice("Пральна машина Bosch"));
        assertSame(DeviceKind.MICROWAVE, DeviceKind.fromDevice("Мікрохвильова піч"));
        assertSame(DeviceKind.CONSOLE, DeviceKind.fromDevice("Ігрова консоль PS5"));
    }

    @Test
    void ignoresCaseAndExtraSpaces() {
        assertSame(DeviceKind.WASHER, DeviceKind.fromDevice("  ПРАЛЬНА   машина  LG "));
        assertSame(DeviceKind.PHONE, DeviceKind.fromDevice("смартфон"));
    }

    @Test
    void unknownOrNullDeviceIsOther() {
        assertSame(DeviceKind.OTHER, DeviceKind.fromDevice("Електросамокат Xiaomi"));
        assertSame(DeviceKind.OTHER, DeviceKind.fromDevice(null));
    }

    @Test
    void applianceFlagAndLabels() {
        assertTrue(DeviceKind.FRIDGE.isAppliance());
        assertTrue(DeviceKind.TV.isAppliance());
        assertFalse(DeviceKind.LAPTOP.isAppliance());
        assertFalse(DeviceKind.OTHER.isAppliance());
        assertEquals("Пральна машина", DeviceKind.WASHER.label());
    }
}
