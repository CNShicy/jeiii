package cn.shicy.jeiii;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@JeiPlugin
public class JeiiPlugin implements IModPlugin {
    private static final Map<Item, Component[]> ITEM_INFO = new HashMap<>();
    private static final ResourceLocation ID = new ResourceLocation(JeiiiMod.MODID, "main");

    public static void registerInfo(Item item, String description) {
        // 将字符串转换为Component数组
        Component[] components = Arrays.stream(description.split("\n"))
                .map(Component::literal)
                .toArray(Component[]::new);
        ITEM_INFO.put(item, components);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ITEM_INFO.forEach((item, components) -> {
            ItemStack stack = new ItemStack(item);
            registration.addIngredientInfo(
                    stack,
                    VanillaTypes.ITEM_STACK,
                    components // 使用Component数组
            );
        });
    }
}