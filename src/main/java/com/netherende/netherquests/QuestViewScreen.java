package com.netherende.netherquests;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class QuestViewScreen extends Screen {
    private final Quest quest;
    private final Screen parentScreen;

    public QuestViewScreen(Quest quest, Screen parentScreen) {
        super(Component.literal(quest.title));
        this.quest = quest;
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int buttonWidth = 120;

        Component pinText = Component.translatable(QuestManager.pinnedQuestId.equals(quest.id) ? "gui.netherquests.pinned" : "gui.netherquests.pin");
        this.addRenderableWidget(Button.builder(pinText, button -> {
            boolean isPinned = QuestManager.pinnedQuestId.equals(quest.id);
            QuestManager.pinnedQuestId = isPinned ? "" : quest.id;
            button.setMessage(Component.translatable(isPinned ? "gui.netherquests.pin" : "gui.netherquests.pinned"));
        }).bounds(centerX - 125, this.height - 65, buttonWidth, 20).build());

        Component claimText = Component.translatable(quest.isCompleted ? "gui.netherquests.completed_status" : "gui.netherquests.claim");
        Button claimBtn = Button.builder(claimText, button -> {
            PacketDistributor.sendToServer(new CompleteQuestPayload(quest.id));
            this.minecraft.setScreen(parentScreen);
        }).bounds(centerX + 5, this.height - 65, buttonWidth, 20).build();
        claimBtn.active = !quest.isCompleted;
        this.addRenderableWidget(claimBtn);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.netherquests.close"), button -> {
            this.minecraft.setScreen(parentScreen);
        }).bounds(centerX - 50, this.height - 35, 100, 20).build());
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x90000000);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        guiGraphics.fill(this.width / 2 - 140, 10, this.width / 2 + 140, this.height - 10, 0xD0000000);
        
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFAA00);

        int textX = this.width / 2 - 120;
        int textY = 50;
        int maxWidth = 240;

        if (quest.description != null && !quest.description.isEmpty()) {
            guiGraphics.drawWordWrap(this.font, Component.literal(quest.description), textX, textY, maxWidth, 0xCCCCCC);
        } else {
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.netherquests.no_lore").getString(), this.width / 2, textY, 0x888888);
        }

        Component reqText = Component.translatable("gui.netherquests.requires", quest.requiredAmount, quest.requiredItem);
        Component rewText = Component.translatable("gui.netherquests.reward_label", quest.rewardAmount, quest.rewardItem);

        guiGraphics.drawString(this.font, reqText, textX, this.height - 110, 0xFF5555, false);
        guiGraphics.drawString(this.font, rewText, textX, this.height - 95, 0x55FF55, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return false;
    }
}