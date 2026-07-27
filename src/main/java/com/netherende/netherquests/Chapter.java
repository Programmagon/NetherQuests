package com.netherende.netherquests;

import net.minecraft.nbt.CompoundTag;

public class Chapter {
    public String id;
    public String title;
    public String parentId;
    public boolean isExpanded;
    public String iconItem;

    public Chapter(String id, String title, String parentId) {
        this(id, title, parentId, "minecraft:book");
    }

    public Chapter(String id, String title, String parentId, String iconItem) {
        this.id = id;
        this.title = title;
        this.parentId = parentId;
        this.isExpanded = true;
        this.iconItem = iconItem != null && !iconItem.isEmpty() ? iconItem : "minecraft:book";
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", id);
        tag.putString("Title", title);
        tag.putString("ParentId", parentId);
        tag.putBoolean("IsExpanded", isExpanded);
        tag.putString("IconItem", iconItem != null ? iconItem : "minecraft:book");
        return tag;
    }

    public static Chapter load(CompoundTag tag) {
        Chapter c = new Chapter(
            tag.getString("Id"),
            tag.getString("Title"),
            tag.getString("ParentId"),
            tag.contains("IconItem") ? tag.getString("IconItem") : "minecraft:book"
        );
        c.isExpanded = tag.getBoolean("IsExpanded");
        return c;
    }
}