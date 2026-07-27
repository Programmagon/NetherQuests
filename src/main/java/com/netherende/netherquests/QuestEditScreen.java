package com.netherende.netherquests;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;

public class QuestEditScreen extends Screen {

    private final Screen parentScreen;
    private final Quest quest;

    private EditBox titleBox;
    private EditBox iconItemBox;
    
    private Button reqTypeBtn;
    private EditBox reqItemBox;
    private EditBox reqAmountBox;
    
    private Button rewardTypeBtn;
    private EditBox rewardItemBox;
    private EditBox rewardAmountBox;
    
    private EditBox depsBox;

    private String currentReqType;
    private String currentRewardType;

    public QuestEditScreen(Screen parentScreen, Quest quest) {
        super(Component.translatable("gui.netherquests.edit_quest"));
        this.parentScreen = parentScreen;
        this.quest = quest;
        this.currentReqType = quest.reqType != null ? quest.reqType : "ITEM";
        this.currentRewardType = quest.rewardType != null ? quest.rewardType : "ITEM";
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int boxWidth = 200;

        // 1. Titel (Box-Y: 45)
        titleBox = new EditBox(this.font, centerX - boxWidth / 2, 45, boxWidth, 20, Component.translatable("gui.netherquests.quest_title"));
        titleBox.setValue(quest.title);
        this.addRenderableWidget(titleBox);

        // 2. Icon Item (Box-Y: 85)
        iconItemBox = new EditBox(this.font, centerX - boxWidth / 2, 85, boxWidth, 20, Component.translatable("gui.netherquests.icon_item"));
        iconItemBox.setValue(quest.iconItem != null ? quest.iconItem : "");
        this.addRenderableWidget(iconItemBox);

        // 3. Bedingung (Typ-Button, Item/Adv-ID, Anzahl) (Box-Y: 125)
        reqTypeBtn = Button.builder(getReqTypeLabel(), button -> {
            cycleReqType();
            button.setMessage(getReqTypeLabel());
        }).bounds(centerX - boxWidth / 2, 125, 60, 20).build();
        this.addRenderableWidget(reqTypeBtn);

        reqItemBox = new EditBox(this.font, centerX - boxWidth / 2 + 65, 125, 85, 20, Component.translatable("gui.netherquests.req_item"));
        reqItemBox.setValue(quest.requiredItem);
        this.addRenderableWidget(reqItemBox);

        reqAmountBox = new EditBox(this.font, centerX + 60, 125, 40, 20, Component.translatable("gui.netherquests.amount"));
        reqAmountBox.setValue(String.valueOf(quest.requiredAmount));
        this.addRenderableWidget(reqAmountBox);

        // 4. Belohnung (Typ-Button, Item/Adv/Command, Anzahl) (Box-Y: 165)
        rewardTypeBtn = Button.builder(getRewardTypeLabel(), button -> {
            cycleRewardType();
            button.setMessage(getRewardTypeLabel());
        }).bounds(centerX - boxWidth / 2, 165, 60, 20).build();
        this.addRenderableWidget(rewardTypeBtn);

        rewardItemBox = new EditBox(this.font, centerX - boxWidth / 2 + 65, 165, 85, 20, Component.translatable("gui.netherquests.reward_item"));
        rewardItemBox.setValue(quest.rewardItem);
        this.addRenderableWidget(rewardItemBox);

        rewardAmountBox = new EditBox(this.font, centerX + 60, 165, 40, 20, Component.translatable("gui.netherquests.amount"));
        rewardAmountBox.setValue(String.valueOf(quest.rewardAmount));
        this.addRenderableWidget(rewardAmountBox);

        // 5. Abhängigkeiten (Box-Y: 205)
        depsBox = new EditBox(this.font, centerX - boxWidth / 2, 205, boxWidth, 20, Component.translatable("gui.netherquests.dependencies"));
        depsBox.setValue(String.join(",", quest.dependencies));
        this.addRenderableWidget(depsBox);

        // Buttons
        this.addRenderableWidget(Button.builder(Component.translatable("gui.netherquests.link_dependency"), button -> {
            saveQuestData();
            QuestScreen.questToLink = this.quest;
            if (this.minecraft != null) this.minecraft.setScreen(parentScreen);
        }).bounds(centerX - 100, 232, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.netherquests.save"), button -> {
            saveQuestData();
            if (this.minecraft != null) this.minecraft.setScreen(parentScreen);
        }).bounds(centerX - 105, this.height - 30, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.netherquests.cancel"), button -> {
            if (this.minecraft != null) this.minecraft.setScreen(parentScreen);
        }).bounds(centerX + 5, this.height - 30, 100, 20).build());
    }

    private void cycleReqType() {
        switch (currentReqType) {
            case "ITEM" -> currentReqType = "LEVEL";
            case "LEVEL" -> currentReqType = "ADVANCEMENT";
            default -> currentReqType = "ITEM";
        }
    }

    private void cycleRewardType() {
        switch (currentRewardType) {
            case "ITEM" -> currentRewardType = "LEVEL";
            case "LEVEL" -> currentRewardType = "ADVANCEMENT";
            case "ADVANCEMENT" -> currentRewardType = "COMMAND";
            default -> currentRewardType = "ITEM";
        }
    }

    private Component getReqTypeLabel() {
        return Component.translatable("gui.netherquests.type." + currentReqType.toLowerCase());
    }

    private Component getRewardTypeLabel() {
        return Component.translatable("gui.netherquests.type." + currentRewardType.toLowerCase());
    }

    private void saveQuestData() {
        quest.title = titleBox.getValue();
        quest.iconItem = iconItemBox.getValue();
        quest.reqType = currentReqType;
        quest.requiredItem = reqItemBox.getValue();
        quest.rewardType = currentRewardType;
        quest.rewardItem = rewardItemBox.getValue();
        
        try { quest.requiredAmount = Integer.parseInt(reqAmountBox.getValue()); } catch (Exception e) { quest.requiredAmount = 1; }
        try { quest.rewardAmount = Integer.parseInt(rewardAmountBox.getValue()); } catch (Exception e) { quest.rewardAmount = 1; }

        String depsText = depsBox.getValue().replace(" ", "");
        if (depsText.isEmpty()) quest.dependencies = new ArrayList<>();
        else quest.dependencies = new ArrayList<>(Arrays.asList(depsText.split(",")));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.fill(0, 0, this.width, this.height, 0xD0000000);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;

        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.netherquests.edit_quest"), centerX, 15, 0xFFFFAA00);

        guiGraphics.drawString(this.font, Component.translatable("gui.netherquests.quest_title"), centerX - 100, 34, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.netherquests.icon_item"), centerX - 100, 74, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.netherquests.req_item"), centerX - 100, 114, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.netherquests.reward_item"), centerX - 100, 154, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.netherquests.dependencies"), centerX - 100, 194, 0xFFFFFF, false);
    }
}