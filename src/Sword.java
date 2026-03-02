public class Sword extends Item {
    
    int damage;
    String type;

    // Construtor da espada
    public Sword(String name,int valor ,int damage, String type,int size) {
        super(name,valor,size);
        this.damage = damage;
        this.type = type;
    }

    // Método responsável por calcular o dano final da arma
    public int calculateDamage() {

        // Gerador de número aleatório (simula dado RPG)
        java.util.Random random = new java.util.Random();

        // Variável que armazenará o dano total final
        int danoTotal = 0;

        // =========================
        // SE FOR ADAGA
        // =========================
        if (name.equals("Adaga")) {

            // A adaga ataca duas vezes
            for (int i = 0; i < 2; i++) {

                int danoFinal = this.damage;

                // Rola um dado de 1 a 20
                int dado = random.nextInt(20) + 1;

                // Se tirar 20, é crítico (dano dobra)
                if (dado == 20) {
                    System.out.println("CRÍTICO!");
                    danoFinal *= 2;
                }

                // Soma o dano das duas investidas
                danoTotal += danoFinal;
            }
        }

        // =========================
        // SE FOR KATANA
        // =========================
        else if (name.equals("Katana")) {

            int danoFinal = this.damage;

            // Rola dado de 1 a 20
            int dado = random.nextInt(20) + 1;

            // Multiplicador padrão de crítico
            int multiplicadorCritico = 2;

            // Se for rara, crítico mais forte
            if (type.equals("Rara")) {
                multiplicadorCritico = 3;
            }

            // Se for lendária, crítico ainda mais forte
            if (type.equals("Lendaria")) {
                multiplicadorCritico = 4;
            }

            // Só é crítico se tirar 20
            if (dado == 20) {
                System.out.println("CRÍTICO!");
                danoFinal *= multiplicadorCritico;
            }

            danoTotal = danoFinal;
        }

        // =========================
        // SE FOR ESPADA LONGA
        // =========================
        else if (name.equals("Espada Longa")) {

            int danoFinal = this.damage;

            // Rola dado de 1 a 20
            int dado = random.nextInt(20) + 1;

            // Chance padrão de crítico (só no 20)
            int chanceCritico = 20; 

            // Se for rara, crítico com 18+
            if (type.equals("Rara")) {
                chanceCritico = 18;
            }

            // Se for lendária, crítico com 15+
            if (type.equals("Lendaria")) {
                chanceCritico = 15; 
            }

            // Se o dado for maior ou igual à chance definida
            if (dado >= chanceCritico) {
                System.out.println("CRÍTICO!");
                danoFinal *= 2;
            }

            danoTotal = danoFinal;
        }

        // =========================
        // QUALQUER OUTRA ARMA
        // =========================
        else {

            int danoFinal = this.damage;

            int dado = random.nextInt(20) + 1;

            // Crítico padrão apenas no 20
            if (dado == 20) {
                System.out.println("CRÍTICO!");
                danoFinal *= 2;
            }

            danoTotal = danoFinal;
        }

        // Retorna o dano total calculado
        return danoTotal;
    }
}