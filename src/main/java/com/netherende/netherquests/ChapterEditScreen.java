package com.netherende.netherquests;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ChapterEditScreen extends Screen {

    private final Screen parentScreen;
    private final Chapter chapter;

    private EditBox titleBox;
    private EditBox iconItemBox;

    public ChapterEditScreen(Screen parentScreen, Chapter chapter) {
        super(Component.translatable("gui.netherquests.edit_chapter"));
        this.parentScreen = parentScreen;
        this.chapter = chapter;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int boxWidth = 200;

        titleBox = new EditBox(this.font, centerX - boxWidth / 2, 45, boxWidth, 20, Component.translatable("gui.netherquests.chapter_title"));
        titleBox.setValue(chapter.title);
        this.addRenderableWidget(titleBox);

        iconItemBox = new EditBox(this.font, centerX - boxWidth / 2, 85, boxWidth, 20, Component.translatable("gui.netherquests.icon_item"));
        iconItemBox.setValue(chapter.iconItem != null ? chapter.iconItem : "minecraft:book");
        this.addRenderableWidget(iconItemBox);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.netherquests.save"), button -> {
            saveChapterData();
            if (this.minecraft != null) this.minecraft.setScreen(parentScreen);
        }).bounds(centerX - 105, this.height - 30, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.netherquests.cancel"), button -> {
            if (this.minecraft != null) this.minecraft.setScreen(parentScreen);
        }).bounds(centerX + 5, this.height - 30, 100, 20).build());
    }

    private void saveChapterData() {
        chapter.title = titleBox.getValue();
        chapter.iconItem = iconItemBox.getValue();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.fill(0, 0, this.width, this.height, 0xD0000000);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;

        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.netherquests.edit_chapter"), centerX, 15, 0xFFFFAA00);

        guiGraphics.drawString(this.font, Component.translatable("gui.netherquests.chapter_title"), centerX - 100, 34, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.netherquests.icon_item"), centerX - 100, 74, 0xFFFFFF, false);
    }
}