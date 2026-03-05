import java.util.Random;

public class Sword extends Item {

    private int damage;
    private String type;
    private static final Random random = new Random();

    // Construtor
    public Sword(String name, int value, int damage, String type, int size) {
        super(name, value, size);
        this.damage = damage;
        this.type = type;
    }

    // GETTERS
    public int getDamage() {
        return damage;
    }

    public String getType() {
        return type;
    }

    // SETTERS
    public void setDamage(int damage) {
        if (damage > 0) {
            this.damage = damage;
        }
    }

    public void setType(String type) {
        if (type != null && !type.isEmpty()) {
            this.type = type;
        }
    }

    // Método que calcula o dano
    public int calculateDamage() {

        int danoTotal = 0;
        int dado = random.nextInt(20) + 1;
        String weaponName = getName(); // agora usa getter

        switch (weaponName) {

            case "Adaga" -> {
                for (int i = 0; i < 2; i++) {

                    int danoFinal = damage;

                    if (dado == 20) {
                        System.out.println("CRÍTICO!");
                        danoFinal *= 2;
                    }

                    danoTotal += danoFinal;
                }
            }

            case "Katana" -> {
                int danoFinal = damage;

                int multiplicadorCritico = switch (type) {
                    case "Rara" -> 3;
                    case "Lendaria" -> 4;
                    default -> 2;
                };

                if (dado == 20) {
                    System.out.println("CRÍTICO!");
                    danoFinal *= multiplicadorCritico;
                }

                danoTotal = danoFinal;
            }

            case "Espada Longa" -> {
                int danoFinal = damage;

                int chanceCritico = switch (type) {
                    case "Rara" -> 18;
                    case "Lendaria" -> 15;
                    default -> 20;
                };

                if (dado >= chanceCritico) {
                    System.out.println("CRÍTICO!");
                    danoFinal *= 2;
                }

                danoTotal = danoFinal;
            }

            default -> {
                int danoFinal = damage;

                if (dado == 20) {
                    System.out.println("CRÍTICO!");
                    danoFinal *= 2;
                }

                danoTotal = danoFinal;
            }
        }

        return danoTotal;
    }
}