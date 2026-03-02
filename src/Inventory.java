import java.util.ArrayList;

public class Inventory {

    int maxSpace = 20;
    int usedSpace = 0;

    ArrayList<Item> items = new ArrayList<>();

    public boolean addItem(Item item) {

        if (usedSpace + item.size <= maxSpace) {
            items.add(item);
            usedSpace += item.size;
            System.out.println(item.name + " foi adicionado!");
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
            System.out.println("- " + item.name + " (Espaço: " + item.size + ")");
        }

        System.out.println("Espaço usado: " + usedSpace + "/" + maxSpace);
    }

    public void increaseSpace(int quantidade) {
        maxSpace += quantidade;
    }
    
}
