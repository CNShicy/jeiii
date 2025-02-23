package cn.shicy.jeiii.network;

import cn.shicy.jeiii.JeiiPlugin;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record PacketSyncJeiInfo(List<Entry> entries) {
    public static void encode(PacketSyncJeiInfo msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entries.size());
        for (Entry entry : msg.entries) {
            buf.writeUtf(entry.itemId());
            buf.writeUtf(entry.content());
        }
    }

    public static PacketSyncJeiInfo decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<Entry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entries.add(new Entry(
                    buf.readUtf(),
                    buf.readUtf()
            ));
        }
        return new PacketSyncJeiInfo(entries);
    }

    public static void handle(PacketSyncJeiInfo msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            for (Entry entry : msg.entries) {
                Item item = ForgeRegistries.ITEMS.getValue(
                        new ResourceLocation(entry.itemId())
                );
                if (item != null) {
                    JeiiPlugin.registerClientInfo(item, entry.content());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public record Entry(String itemId, String content) {}
}