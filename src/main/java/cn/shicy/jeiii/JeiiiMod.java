package cn.shicy.jeiii;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.*;
import java.nio.file.*;

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
        checkAndCreateDir();
        processSmpFiles();
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

    private void processSmpFiles() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(CONFIG_DIR, "*.smp")) {
            for (Path file : stream) {
                processSingleFile(file);
            }
        } catch (IOException e) {
            // 异常处理
        }
    }

    private void processSingleFile(Path file) {
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (root.isJsonArray()) {
                for (JsonElement element : root.getAsJsonArray()) {
                    processJsonObject(element.getAsJsonObject());
                }
            } else if (root.isJsonObject()) {
                processJsonObject(root.getAsJsonObject());
            }
        } catch (IOException | JsonParseException e) {
            // 异常处理
        }
    }

    // 修改processJsonObject方法中的调用
    private void processJsonObject(JsonObject obj) {
        if (validateJson(obj)) {
            String itemId = obj.get("item").getAsString();
            String content = obj.get("content").getAsString();

            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
            if (item != null) {
                JeiiPlugin.registerInfo(item, content);
            }
        }
    }

    private boolean validateJson(JsonObject obj) {
        return obj.has("item") && obj.has("content") && obj.has("type");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // 服务器端初始化逻辑
    }
}