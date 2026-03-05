import java.util.ArrayList;
import java.util.List;

public class Inventory {

    private int maxSpace = 20;
    private int usedSpace = 0;
    private List<Item> items = new ArrayList<>();

    public boolean addItem(Item item) {

        if (item == null) return false;

        if (usedSpace + item.getSize() <= maxSpace) {
            items.add(item);
            usedSpace += item.getSize();
            System.out.println(item.getName() + " foi adicionado!");
            return true;
        } else {
            System.out.println("Inventário cheio!");
            return false;
        }
    }

    public void showInventory() {

        System.out.println("\n=== INVENTÁRIO ===");

        if (items.isEmpty()) {
            System.out.println("Inventário vazio.");
            return;
        }

        for (Item item : items) {
            System.out.println("- " + item.getName() + 
                               " (Espaço: " + item.getSize() + ")");
        }

        System.out.println("Espaço usado: " + usedSpace + "/" + maxSpace);
    }

    public void increaseSpace(int quantidade) {
        if (quantidade > 0) {
            maxSpace += quantidade;
        }
    }

    public boolean removeItem(Item item) {
        if (items.remove(item)) {
            usedSpace -= item.getSize();
            return true;
        }
        return false;
    }

    // GETTERS
    public int getMaxSpace() {
        return maxSpace;
    }

    public int getUsedSpace() {
        return usedSpace;
    }

    public List<Item> getItems() {
        return items;
    }
}