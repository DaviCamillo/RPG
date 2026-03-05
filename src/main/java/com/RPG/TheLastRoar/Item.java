public class Item {

    private String name;
    private int value;
    private int size;

    // Construtor
    public Item(String name, int value, int size) {
        this.name = name;
        this.value = value;
        this.size = size;
    }

    // GETTERS
    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public int getSize() {
        return size;
    }

    // SETTERS
    public void setName(String name) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
        }
    }

    public void setValue(int value) {
        if (value >= 0) {
            this.value = value;
        }
    }

    public void setSize(int size) {
        if (size > 0) {
            this.size = size;
        }
    }
}