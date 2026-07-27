package com.netherende.netherquests;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = "netherquests", value = Dist.CLIENT)
public class QuestHUD {

    @SubscribeEvent
    public static void onRenderHUD(RenderGuiEvent.Post event) {
        if (QuestManager.pinnedQuestId.isEmpty()) return;

        Quest pinned = QuestManager.getQuestById(QuestManager.pinnedQuestId);
        if (pinned == null || pinned.isCompleted) {
            QuestManager.pinnedQuestId = ""; // Auto-Entpinnen wenn fertig oder gelöscht
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        // Items im Inventar zählen
        int currentAmount = 0;
        try {
            Item reqItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(pinned.requiredItem));
            for (ItemStack stack : player.getInventory().items) {
                if (stack.is(reqItem)) currentAmount += stack.getCount();
            }
        } catch (Exception e) {}

        boolean hasEnough = currentAmount >= pinned.requiredAmount;
        int color = hasEnough ? 0x55FF55 : 0xFFFFFF; // Grün wenn man genug hat!

        GuiGraphics gui = event.getGuiGraphics();
        
        // Simples dunkles Feld oben rechts
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        gui.fill(screenWidth - 150, 10, screenWidth - 10, 50, 0x80000000);
        
        gui.drawString(mc.font, "📌 " + pinned.title, screenWidth - 145, 15, 0xFFFFAA00, true);
        
        String progress = currentAmount + " / " + pinned.requiredAmount;
        gui.drawString(mc.font, pinned.requiredItem, screenWidth - 145, 30, 0xAAAAAA, true);
        gui.drawString(mc.font, progress, screenWidth - (mc.font.width(progress) + 15), 30, color, true);
        
        if (hasEnough) {
            gui.drawCenteredString(mc.font, "Bereit zum Abgeben!", screenWidth - 80, 45, 0x55FF55);
        }
    }
}