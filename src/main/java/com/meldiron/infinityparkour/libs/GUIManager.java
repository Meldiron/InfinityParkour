package com.meldiron.infinityparkour.libs;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class GUIManager implements Listener {
    public static HashMap<UUID, GUIManager> inventoriesByUUID = new HashMap<>();
    public static HashMap<UUID, UUID> openInventories = new HashMap<>();

    private UUID uuid;
    private Inventory inv;
    private HashMap<Integer, GUIAction> actions;

    public GUIManager(int lines, String invName) {
        uuid = UUID.randomUUID();
        inv = Bukkit.createInventory(null, lines * 9, invName);
        inventoriesByUUID.put(getUuid(), this);
        actions = new HashMap<>();
    }

    public GUIManager() {

    }

    public interface GUIAction {
        void click(Player player);
    }

    public Inventory getInventory() {
        return inv;
    }

    public void setItem(int slot, ItemStack stack, GUIAction action){
        inv.setItem(slot, stack);
        if (action != null){
            actions.put(slot, action);
        }
    }

    public void setItem(int slot, ItemStack stack){
        setItem(slot, stack, null);
    }

    public void open(Player p){
        p.openInventory(inv);
        openInventories.put(p.getUniqueId(), getUuid());
    }

    public void delete(){
        for (Player p : Bukkit.getOnlinePlayers()){
            UUID u = openInventories.get(p.getUniqueId());
            if (u != null && u.equals(getUuid())){
                p.closeInventory();
            }
        }
        inventoriesByUUID.remove(getUuid());
    }


    public UUID getUuid() {
        return uuid;
    }

    public static HashMap<UUID, GUIManager> getInventoriesByUUID() {
        return inventoriesByUUID;
    }

    public static HashMap<UUID, UUID> getOpenInventories() {
        return openInventories;
    }

    public HashMap<Integer, GUIAction> getActions() {
        return actions;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if (!(e.getWhoClicked() instanceof Player)){
            return;
        }
        Player player = (Player) e.getWhoClicked();
        UUID playerUUID = player.getUniqueId();

        UUID inventoryUUID = GUIManager.openInventories.get(playerUUID);
        if (inventoryUUID != null){
            e.setCancelled(true);
            GUIManager gui = GUIManager.getInventoriesByUUID().get(inventoryUUID);
            GUIManager.GUIAction action = gui.getActions().get(e.getSlot());

            if (action != null){
                action.click(player);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        Player player = (Player) e.getPlayer();
        UUID playerUUID = player.getUniqueId();

        GUIManager.openInventories.remove(playerUUID);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        UUID playerUUID = player.getUniqueId();

        GUIManager.openInventories.remove(playerUUID);
    }
}
