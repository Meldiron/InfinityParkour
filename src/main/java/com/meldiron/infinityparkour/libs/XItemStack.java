package com.meldiron.infinityparkour.libs;

import com.meldiron.infinityparkour.Main;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class XItemStack {
    private static XItemStack ourInstance = new XItemStack();

    public static XItemStack getInstance() {
        return ourInstance;
    }

    private Main main;

    private XItemStack() {
        main = Main.getInstance();
    }

    public ItemStack createItem(String material, String title, List<String> lore) {
        ItemStack item = XMaterial.fromString(material).parseItem();
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(main.color(title));
        itemMeta.setLore(lore.stream().map(n -> main.color(n)).collect(Collectors.toList()));
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(itemMeta);

        return item;
    }
}
