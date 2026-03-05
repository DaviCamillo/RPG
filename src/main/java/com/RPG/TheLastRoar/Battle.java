import java.util.List;
import java.util.Scanner;

public class Battle {

    public static void lutaPadrao(Character player, List<Monsters> inimigos) {

        Scanner sc = new Scanner(System.in);

        System.out.println("A batalha começou!");

        while (player.getLife() > 0 && !inimigos.isEmpty()) {

            System.out.println("\nSua vida: " + player.getLife() + "/" + player.getMaxLife());
            System.out.println("1 - Atacar");
            System.out.println("2 - Fugir");
            System.out.println("3 - Usar Poção");

            int escolha = sc.nextInt();

            if (escolha == 2) {
                if (player.leave()) {
                    return;
                }
            }

            if (escolha == 3) {

                List<Item> itens = player.getInventory().getItems();
                boolean encontrou = false;

                for (int i = 0; i < itens.size(); i++) {
                    if (itens.get(i) instanceof Potion) {
                        Potion p = (Potion) itens.get(i);
                        System.out.println(i + " - " + p.getName() +
                                " (Cura: " + p.getHealedLife() + ")");
                        encontrou = true;
                    }
                }

                if (!encontrou) {
                    System.out.println("Você não tem poções!");
                    continue;
                }

                int indice = sc.nextInt();

                if (indice >= 0 && indice < itens.size() &&
                        itens.get(indice) instanceof Potion) {

                    Potion p = (Potion) itens.get(indice);

                    player.setLife(player.getLife() + p.getHealedLife());

                    System.out.println("Você usou " + p.getName() +
                            " e recuperou " + p.getHealedLife() + " de vida!");

                    player.getInventory().removeItem(p);
                }

                continue;
            }

            if (escolha == 1) {

                System.out.println("1 - Ataque único");
                System.out.println("2 - Ataque em área");

                int tipoAtaque = sc.nextInt();

                if (tipoAtaque == 1) {

                    for (int i = 0; i < inimigos.size(); i++) {
                        Monsters m = inimigos.get(i);
                        System.out.println(i + " - " +
                                m.getName() +
                                " (" + m.getLife() + " HP)");
                    }

                    int indice = sc.nextInt();

                    if (indice >= 0 && indice < inimigos.size()) {

                        Monsters alvo = inimigos.get(indice);
                        player.attack(alvo);

                        if (alvo.getLife() <= 0) {

                            System.out.println("Você ganhou " + alvo.getDropXp() + " XP!");
                            System.out.println("Você ganhou " + alvo.getDropCoin() + " moedas!");

                            player.earnXp(alvo.getDropXp());
                            player.addCoin(alvo.getDropCoin());

                            inimigos.remove(indice);
                        }
                    }

                } else if (tipoAtaque == 2) {

                    player.attackArea(inimigos);

                    for (int i = 0; i < inimigos.size(); i++) {
                        Monsters m = inimigos.get(i);

                        if (m.getLife() <= 0) {

                            System.out.println(m.getName() + " foi derrotado!");
                            System.out.println("Você ganhou " + m.getDropXp() + " XP!");
                            System.out.println("Você ganhou " + m.getDropCoin() + " moedas!");

                            player.earnXp(m.getDropXp());
                            player.addCoin(m.getDropCoin());

                            inimigos.remove(i);
                            i--;
                        }
                    }
                }
            }

            // TURNO DOS MONSTROS
            for (Monsters m : inimigos) {
                m.attack(player);
                if (player.getLife() <= 0) break;
            }
        }

        if (player.getLife() > 0) {
            System.out.println("Você venceu!");
        } else {
            System.out.println("Você foi derrotado...");
        }
    }
}