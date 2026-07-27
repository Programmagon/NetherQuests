package com.netherende.netherquests;
import net.minecraft.client.resources.language.I18n;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class QuestScreen extends Screen {

    private boolean isEditMode = false;
    private final int panelWidth = 160; 

    private Quest draggedQuest = null;
    private int dragOffsetX = 0, dragOffsetY = 0;
    private double lastMouseX, lastMouseY;
    public static Quest questToLink = null; 

    private static class ChapterHitbox {
        Chapter chapter; int x, yTop, yBottom;
        ChapterHitbox(Chapter c, int x, int yT, int yB) { this.chapter = c; this.x = x; this.yTop = yT; this.yBottom = yB; }
    }
    private final List<ChapterHitbox> chapterHitboxes = new ArrayList<>();

    public QuestScreen() {
        super(Component.translatable("gui.netherquests.title"));
        QuestManager.init();
    }

    @Override
    protected void init() {
        super.init();
        
        this.addRenderableWidget(Button.builder(Component.translatable(isEditMode ? "gui.netherquests.edit_mode.on" : "gui.netherquests.edit_mode.off"), 
            button -> { this.isEditMode = !this.isEditMode; this.rebuildWidgets(); }
        ).bounds(10, this.height - 30, panelWidth - 20, 20).build());

        if (isEditMode) {
            this.addRenderableWidget(Button.builder(Component.translatable("gui.netherquests.add_main_chapter"), button -> {
                QuestManager.addChapter(new Chapter("chap_" + System.currentTimeMillis(), Component.translatable("gui.netherquests.new_chapter").getString(), ""));
            }).bounds(10, this.height - 55, panelWidth - 20, 20).build());
        }
    }

    private Quest getHoveredQuest(double mouseX, double mouseY) {
        for (int i = QuestManager.getQuests().size() - 1; i >= 0; i--) {
            Quest q = QuestManager.getQuests().get(i);
            if (!q.chapterId.equals(QuestManager.currentChapterId)) continue; 
            if (mouseX >= q.x && mouseX <= q.x + 24 && mouseY >= q.y && mouseY <= q.y + 24) return q;
        }
        return null;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isEditMode && keyCode == 261) { 
            Quest hovered = getHoveredQuest(this.lastMouseX, this.lastMouseY);
            if (hovered != null) {
                QuestManager.getQuests().remove(hovered);
                for (Quest q : QuestManager.getQuests()) q.dependencies.remove(hovered.id);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX <= panelWidth) {
            for (ChapterHitbox box : chapterHitboxes) {
                if (mouseY >= box.yTop && mouseY <= box.yBottom) {
                    if (button == 1 && isEditMode) { 
                        if (this.minecraft != null) this.minecraft.setScreen(new ChapterEditScreen(this, box.chapter));
                        return true;
                    } else if (button == 0) { 
                        if (mouseX >= box.x && mouseX <= box.x + 15) {
                            box.chapter.isExpanded = !box.chapter.isExpanded;
                        }
                        QuestManager.currentChapterId = box.chapter.id;
                        return true;
                    }
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (QuestManager.currentChapterId == null || QuestManager.currentChapterId.isEmpty()) return super.mouseClicked(mouseX, mouseY, button);

        if (questToLink != null) {
            if (button == 1) { 
                if (this.minecraft != null) this.minecraft.setScreen(new QuestEditScreen(this, questToLink));
                questToLink = null;
                return true;
            }
            if (button == 0) {
                Quest hovered = getHoveredQuest(mouseX, mouseY);
                if (hovered != null && hovered != questToLink && !questToLink.dependencies.contains(hovered.id)) {
                    questToLink.dependencies.add(hovered.id);
                }
                Quest tmp = questToLink; questToLink = null;
                if (this.minecraft != null) this.minecraft.setScreen(new QuestEditScreen(this, tmp));
                return true;
            }
        }

        if (isEditMode) {
            Quest hoveredQuest = getHoveredQuest(mouseX, mouseY);
            if (hoveredQuest != null) {
                if (button == 0) {
                    draggedQuest = hoveredQuest;
                    dragOffsetX = (int) (mouseX - hoveredQuest.x); dragOffsetY = (int) (mouseY - hoveredQuest.y);
                    return true;
                } else if (button == 1) {
                    if (this.minecraft != null) this.minecraft.setScreen(new QuestEditScreen(this, hoveredQuest));
                    return true;
                }
            } else if (button == 0) { 
                String randomId = "quest_" + System.currentTimeMillis();
                int spawnX = Math.max(panelWidth + 30, (int) mouseX - 12);
                int spawnY = Math.max(30, (int) mouseY - 12);
                String newQuestTitle = I18n.get("gui.netherquests.new_quest");
QuestManager.addQuest(new Quest(randomId, QuestManager.currentChapterId, newQuestTitle, "", spawnX, spawnY, "ITEM", "minecraft:dirt", 1, "ITEM", "minecraft:apple", 1, new ArrayList<>(), new ArrayList<>(), "minecraft:dirt"));
            }
        }

        if (!isEditMode && button == 0) {
            Quest hoveredQuest = getHoveredQuest(mouseX, mouseY);
            if (hoveredQuest != null) {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new QuestViewScreen(hoveredQuest, this));
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isEditMode && draggedQuest != null) {
            draggedQuest.x = (int) Math.max(panelWidth + 10, mouseX - dragOffsetX);
            draggedQuest.y = (int) Math.max(10, mouseY - dragOffsetY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggedQuest != null) { draggedQuest = null; return true; }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private int renderChapterTree(GuiGraphics guiGraphics, Chapter c, int x, int y) {
        chapterHitboxes.add(new ChapterHitbox(c, x, y, y + 15));
        
        boolean hasChildren = false;
        for (Chapter sub : QuestManager.getChapters()) {
            if (sub.parentId.equals(c.id)) { hasChildren = true; break; }
        }
        
        String prefix = hasChildren ? (c.isExpanded ? "[-] " : "[+] ") : "  ";
        int color = c.id.equals(QuestManager.currentChapterId) ? 0xFF5555 : 0xAAAAAA;
        
        guiGraphics.drawString(this.font, prefix + c.title, x, y, color, false);
        
        int nextY = y + 15;
        if (c.isExpanded) {
            for (Chapter sub : QuestManager.getChapters()) {
                if (sub.parentId.equals(c.id)) {
                    nextY = renderChapterTree(guiGraphics, sub, x + 10, nextY);
                }
            }
        }
        return nextY;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x80000000); 
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.lastMouseX = mouseX; this.lastMouseY = mouseY;
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        for (Quest q : QuestManager.getQuests()) {
            if (!q.chapterId.equals(QuestManager.currentChapterId)) continue;
            
            for (String depId : q.dependencies) {
                Quest dep = QuestManager.getQuestById(depId);
                if (dep != null && dep.chapterId.equals(QuestManager.currentChapterId)) {
                    int startX = dep.x + 12; int startY = dep.y + 12;
                    int endX = q.x + 12; int endY = q.y + 12;
                    int lineColor = dep.isCompleted ? 0xFF00FF00 : 0xFF00AA00;
                    
                    guiGraphics.fill(Math.min(startX, endX), startY - 1, Math.max(startX, endX), startY + 1, lineColor);
                    guiGraphics.fill(endX - 1, Math.min(startY, endY), endX + 1, Math.max(startY, endY), lineColor);

                    if (startY < endY) {
                        guiGraphics.fill(endX - 3, q.y - 3, endX + 3, q.y - 2, lineColor);
                        guiGraphics.fill(endX - 2, q.y - 2, endX + 2, q.y - 1, lineColor);
                        guiGraphics.fill(endX - 1, q.y - 1, endX + 1, q.y, lineColor);
                    } else if (startY > endY) {
                        guiGraphics.fill(endX - 3, q.y + 26, endX + 3, q.y + 27, lineColor);
                        guiGraphics.fill(endX - 2, q.y + 25, endX + 2, q.y + 26, lineColor);
                        guiGraphics.fill(endX - 1, q.y + 24, endX + 1, q.y + 25, lineColor);
                    } else {
                        if (startX < endX) {
                            guiGraphics.fill(q.x - 3, endY - 3, q.x - 2, endY + 3, lineColor);
                            guiGraphics.fill(q.x - 2, endY - 2, q.x - 1, endY + 2, lineColor);
                            guiGraphics.fill(q.x - 1, endY - 1, q.x, endY + 1, lineColor);
                        } else if (startX > endX) {
                            guiGraphics.fill(q.x + 26, endY - 3, q.x + 27, endY + 3, lineColor);
                            guiGraphics.fill(q.x + 25, endY - 2, q.x + 26, endY + 2, lineColor);
                            guiGraphics.fill(q.x + 24, endY - 1, q.x + 25, endY + 1, lineColor);
                        }
                    }
                }
            }
        }

        guiGraphics.fill(0, 0, panelWidth, this.height, 0xFF202020);
        guiGraphics.fill(panelWidth, 0, panelWidth + 2, this.height, NetherQuestsConfig.getBarColor());
        guiGraphics.drawString(this.font, Component.translatable("gui.netherquests.chapters").getString(), 10, 10, 0xFFFFFF, false);
        
        chapterHitboxes.clear();
        int yPos = 30;
        for (Chapter c : QuestManager.getChapters()) {
            if (c.parentId.isEmpty()) { 
                yPos = renderChapterTree(guiGraphics, c, 10, yPos);
            }
        }

        if (questToLink != null) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.netherquests.link_hint").getString(), this.width / 2 + panelWidth / 2, 10, 0x55FF55);
        } else if (isEditMode) {
            guiGraphics.drawString(this.font, Component.translatable("gui.netherquests.editor_hint").getString(), panelWidth + 20, 10, 0xFFAA00, false);
        }

        for (Quest q : QuestManager.getQuests()) {
            if (!q.chapterId.equals(QuestManager.currentChapterId)) continue;

            int borderColor = (q == draggedQuest) ? 0xFFFFAA00 : (q.isCompleted ? 0xFF00FF00 : 0xFFAA0000);
            guiGraphics.fill(q.x - 1, q.y - 1, q.x + 25, q.y + 25, borderColor);
            guiGraphics.fill(q.x, q.y, q.x + 24, q.y + 24, 0xFF333333);

            try {
                String iconStr = (q.iconItem != null && !q.iconItem.isEmpty()) ? q.iconItem : "minecraft:book";
                if ("ITEM".equals(q.reqType) && (q.iconItem == null || q.iconItem.isEmpty())) {
                    iconStr = q.requiredItem;
                }
                Item iconItemObj = BuiltInRegistries.ITEM.get(ResourceLocation.parse(iconStr));
                guiGraphics.renderItem(new ItemStack(iconItemObj), q.x + 4, q.y + 4);
            } catch (Exception ignored) {}

            guiGraphics.drawCenteredString(this.font, q.title, q.x + 12, q.y - 12, 0xFFFFFF);
        }

        Quest hovered = getHoveredQuest(mouseX, mouseY);
        if (hovered != null && questToLink == null) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("§l" + hovered.title));
            
            if (hovered.isCompleted) {
                tooltip.add(Component.translatable("gui.netherquests.completed"));
            } else {
                switch (hovered.reqType) {
                    case "LEVEL" -> tooltip.add(Component.translatable("gui.netherquests.required_level", hovered.requiredAmount));
                    case "ADVANCEMENT" -> tooltip.add(Component.translatable("gui.netherquests.required_adv", hovered.requiredItem));
                    default -> {
                        Item reqItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(hovered.requiredItem));
                        Component reqItemName = new ItemStack(reqItem).getHoverName();
                        tooltip.add(Component.translatable("gui.netherquests.required", hovered.requiredAmount, reqItemName));
                    }
                }

                switch (hovered.rewardType) {
                    case "LEVEL" -> tooltip.add(Component.translatable("gui.netherquests.reward_level", hovered.rewardAmount));
                    case "ADVANCEMENT" -> tooltip.add(Component.translatable("gui.netherquests.reward_adv", hovered.rewardItem));
                    case "COMMAND" -> tooltip.add(Component.translatable("gui.netherquests.reward_cmd"));
                    default -> {
                        Item rewItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(hovered.rewardItem));
                        Component rewItemName = new ItemStack(rewItem).getHoverName();
                        tooltip.add(Component.translatable("gui.netherquests.reward", hovered.rewardAmount, rewItemName));
                    }
                }
            }
            guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}