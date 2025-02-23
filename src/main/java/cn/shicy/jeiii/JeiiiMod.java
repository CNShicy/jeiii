package cn.shicy.jeiii;

import cn.shicy.jeiii.network.NetworkManager;
import cn.shicy.jeiii.network.PacketSyncJeiInfo;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Mod(JeiiiMod.MODID)
public class JeiiiMod {
    public static final String MODID = "jeiii";
    private static final Path CONFIG_DIR = Paths.get("jeiii");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public JeiiiMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkManager.register();
            checkAndCreateDir();
        });
    }

    private void checkAndCreateDir() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory", e);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        processSmpFiles();
    }

    private void processSmpFiles() {
        List<PacketSyncJeiInfo.Entry> entries = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(CONFIG_DIR, "*.smp")) {
            for (Path file : stream) {
                entries.addAll(processSingleFile(file));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!entries.isEmpty()) {
            NetworkManager.INSTANCE.send(
                    PacketDistributor.ALL.noArg(),
                    new PacketSyncJeiInfo(entries)
            );
        }
    }

    private List<PacketSyncJeiInfo.Entry> processSingleFile(Path file) {
        List<PacketSyncJeiInfo.Entry> entries = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (root.isJsonArray()) {
                for (JsonElement element : root.getAsJsonArray()) {
                    processJsonObject(element.getAsJsonObject(), entries);
                }
            } else if (root.isJsonObject()) {
                processJsonObject(root.getAsJsonObject(), entries);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entries;
    }

    private void processJsonObject(JsonObject obj, List<PacketSyncJeiInfo.Entry> entries) {
        if (validateJson(obj)) {
            entries.add(new PacketSyncJeiInfo.Entry(
                    obj.get("item").getAsString(),
                    obj.get("content").getAsString()
            ));
        }
    }

    private boolean validateJson(JsonObject obj) {
        return obj.has("item") && obj.has("content") && obj.has("type");
    }

    @SubscribeEvent
    public void onPlayerJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof ServerPlayer player) {
            processSmpFiles();
        }
    }
}