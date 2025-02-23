package cn.shicy.jeiii.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkManager {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("jeiii", "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(
                id++,
                PacketSyncJeiInfo.class,
                PacketSyncJeiInfo::encode,
                PacketSyncJeiInfo::decode,
                PacketSyncJeiInfo::handle
        );
    }
}