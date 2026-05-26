package net.countered.smoothf5.compat;

import dev.ryanhcode.sable.companion.impl.DefaultSableCompanion;
import net.minecraft.client.Minecraft;

public class SableCompat {

    public static boolean disableSmoothF5() {
        return DefaultSableCompanion.INSTANCE.getVehicleSubLevel(Minecraft.getInstance().player) != null;
    }
}
