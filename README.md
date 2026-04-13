# 🎮 The Last Roar - RPG Game

Um jogo RPG de batalha por turnos desenvolvido em **Java 17** com **JavaFX 21**, apresentando combate estratégico, múltiplos mapas, sistema de inventário e gerenciamento de personagem.

## 🎯 Visão Geral

**The Last Roar** é um jogo RPG com mecânicas clássicas de turnos onde você:
- Controla um herói em um mundo de 3 mapas
- Enfrenta inimigos progressivamente mais desafiadores
- Coleta ouro, itens e ganha experiência
- Equipa armas e armaduras para ficar mais forte
- Salva/carrega seu progresso em 3 slots diferentes

## 📋 Requisitos

- **Java**: 17 ou superior
- **Maven**: 3.6+
- **Sistema Operacional**: Windows, Linux, macOS (testado em Windows)

## 🚀 Como Executar

### Via Maven

```bash
# Compilar
mvn clean compile

# Executar
mvn javafx:run

# Empacotar
mvn package
```

### Via IDE (VS Code/IntelliJ)

1. Importe o projeto como Maven project
2. Configure o JDK 17
3. Execute: `App.main()` ou use a configuração de launch

## 🎮 Controles

| Tecla | Ação |
|-------|------|
| **WASD** | Movimento do herói |
| **I** | Abrir/Fechar inventário |
| **H** | Usar primeira poção rapidamente |
| **ESC** | Pausa / Menu principal |
| **Mouse** | Botões e interações |

## 🗺️ Mapas

### Mapa 1 - Planície Inicial
- **Inimigos**: 2x Goblin (básico)
- **Dificuldade**: ⭐ Fácil
- **NPC**: Loja do neguciante
- **Moedas**: 3 por inimigo

### Mapa 2 - Vale Intermediário
- **Inimigos**: 2x GoblinExp (intermediário - CUIDADO: dano 99!)
- **Dificuldade**: ⭐⭐⭐ Difícil
- **Moedas**: 5 por inimigo

### Mapa 3 - Pico da Montanha
- **Inimigos**: 1x GoblinBoss (boss final)
- **Dificuldade**: ⭐⭐⭐⭐ Muito Difícil
- **Moedas**: 15 por inimigo
- **XP**: 20 (prêmio da vitória)

## 💼 Sistema de Inventário

- **Capacidade**: 20 slots (expandível em níveis 5 e 10)
- **Itens**: Armas, Armaduras, Poções
- **Equipamento**: 1 arma + 1 armadura (ativa por vez)

### Itens Disponíveis

#### Armas
| Nome | Dano | Custo | Efeito Especial |
|------|------|-------|-----------------|
| Madeira (inicial) | 3-6 | - | Comum |
| Adaga | 2-5 | 10 | 2 ataques/turno |
| Katana | 5-8 | 25 | Crítico: 3x-4x |
| Espada Longa | 4-8 | 20 | Crítico moderado |

#### Armaduras
| Nome | Defesa | Custo |
|------|--------|-------|
| Cota de Malha | 2 | 15 |
| Couro Reforçado | 3 | 20 |
| Aço Pesado | 5 | 35 |

#### Poções
| Nome | Cura | Custo |
|------|------|-------|
| Pequena | 5 | 5 |
| Média | 10 | 10 |
| Grande | 20 | 20 |

## 📊 Sistema de Combate

O combate funciona em **turnos**:

### Turno do Jogador
1. **LUTAR**: Ataca com sua arma
2. **MOCHILA**: Usa poção para recuperar HP
3. **FUGIR**: Tenta escapar da batalha

### Turno do Inimigo
- Inimigo ataca automaticamente após seu turno
- Dano = dano base - sua resistência
- A resistência vem da armadura equipada

### Cálculo de Dano
```
Dano Final = max(0, Dano Atacante - Resistência Defensor)
```

### Crítico
Cada arma tem seu próprio sistema de crítico:
- **Adaga**: Pode critar 2 vezes por turno (d20)
- **Katana**: Crítico com multiplicador (raridade dependente)
- **Espada Longa**: Crítico com limiar variável (raridade dependente)

## 📈 Sistema de Progressão

### XP e Níveis
- **Nível inicial**: 1
- **XP inicial para level up**: 5
- **Multiplicador a cada nível**: 1.5x

### Expansão de Inventário
- **Nível 5**: +5 slots (20 → 25)
- **Nível 10**: +5 slots (25 → 30)

### Recompensas de Combate
| Vitória | Ouro | XP |
|---------|------|-----|
| Goblin | 3 | 2 |
| GoblinExp | 5 | 4 |
| GoblinBoss | 15 | 20 |

## 💾 Sistema de Save/Load

- **3 Slots**: Save1, Save2, Save3
- **Dados Salvos**: Personagem, Inventário, Posição no mapa, Inimigos derrotados
- **Formato**: JSON em disco local
- **Localização**: Projeto raiz (save1.json, save2.json, save3.json)

## 🏗️ Arquitetura do Projeto

```
TheLastRoar/
├── App.java                          ⭐ Entry Point
├── backend/
│   ├── models/                       # Dados puros (sem JavaFX)
│   │   ├── Character.java
│   │   ├── Monsters.java
│   │   ├── Goblin.java
│   │   ├── GoblinExp.java
│   │   ├── GoblinBoss.java
│   │   ├── Item.java
│   │   ├── Sword.java
│   │   ├── Armor.java
│   │   └── Potion.java
│   └── managers/                     # Gerenciadores de lógica
│       ├── Inventory.java
│       ├── EnemyManager.java
│       └── SaveManager.java
└── frontend/
    ├── core/                         # Sistema principal
    │   ├── AppGameLoop.java
    │   ├── AppKeyboardControls.java
    │   ├── AppGameState.java
    │   ├── AppUIElements.java
    │   └── AppInitializer.java
    ├── screens/                      # Telas/UIs
    │   ├── StartScreen.java
    │   ├── Battle.java
    │   ├── BattleUI.java
    │   ├── HudManager.java
    │   ├── InventoryScreen.java
    │   ├── PauseMenu.java
    │   └── IntroScreen.java
    ├── controllers/                  # Controladores de ação
    │   ├── BattleAnimations.java
    │   ├── MapTransitionManager.java
    │   ├── PlayerMovementController.java
    │   └── InputController.java
    └── npc/
        └── ShopNPC.java              # Vendedor
```

### Princípios de Design

- **Frontend → Backend**: ✅ Pode acessar
- **Backend → Frontend**: ❌ Nunca conhece
- **Backend**: Lógica pura, sem dependência de JavaFX
- **Frontend**: Renderização e entrada do usuário

Isso permite reusar Backend em outros projetos/interfaces.

## 📖 Fluxo Principal

```
main()
  ↓
App.start(stage)
  ├─ Exibe StartScreen (menu inicial)
  ↓
Escolha do jogador
  ├─ Novo Jogo → IntroScreen → AppInitializer.startGame()
  ├─ Continuar → carrega último save
  └─ Carregar → escolhe slot
  ↓
AppGameLoop inicia 2 timers
  ├─ playerMovementTimer (movimento + sprite)
  ├─ enemyAITimer (IA inimigos + colisões)
  ↓
Colisão com inimigo
  └─ Battle.startBattle() → sistema de turnos
  ↓
ESC → PauseMenu (salvar/carregar/sair)
```

## 🔧 Dependências Maven

```xml
<!-- JavaFX (Interface gráfica) -->
javafx-controls
javafx-graphics
javafx-base
javafx-fxml
javafx-media
```

## 🎨 Recursos de Arte

O projeto espera os seguintes arquivos em `src/main/resources/images/`:

### Sprites
- `sprite_personagem.png` (128x128, sprite sheet 4x4)
- `sprite_goblin.png`
- `sprite_goblinexperiente.png`
- `sprite_Baginga.png` (boss)
- `personagem_battle.png` (posição de batalha)
- `goblin.png` (sprite de batalha dos inimigos)

### Mapas
- `mapa_padrao.png`
- `mapa_padrao2.png`
- `mapa_padrao3.png`

### Ícone
- `logo.png`

## 🐛 Troubleshooting

### "Cannot find image resource"
- Verifique se os arquivos estão em `src/main/resources/images/`
- Use `getClass().getResource()` ou `getClass().getResourceAsStream()`

### "Compilação falha com JavaFX"
```bash
# Use o plugin maven-javafx-plugin
mvn javafx:run
```

### "Jogo não inicia"
- Verifique se Java 17+ está instalado: `java -version`
- Compile primeiro: `mvn clean compile`
- Execute: `mvn javafx:run`

## 📝 Estrutura de Código

### Backend/Models
**Responsabilidade**: Dados puros, sem efeitos colaterais

```java
// Character.java
public class Character {
    private int life, maxLife, xp;
    private Inventory inventory;
    
    public void heal(int amount) { /* cura sem UI */ }
    public int attack(Monsters target) { /* calcula dano */ }
}
```

### Backend/Managers
**Responsabilidade**: Orquestrar Models

```java
// EnemyManager.java
public void configureForMap(int mapIndex) {
    // Cria inimigos do mapa
    enemies.clear();
    switch(mapIndex) {
        case 0: enemies.add(new Goblin()); break;
        case 1: enemies.add(new GoblinExp()); break;
        case 2: enemies.add(new GoblinBoss()); break;
    }
}
```

### Frontend/Screens
**Responsabilidade**: Renderizar dados do Backend

```java
// BattleUI.java
public void update(Character player, Monsters enemy) {
    hpLabel.setText("HP: " + player.getLife());
    enemyLabel.setText(enemy.getName() + " - " + enemy.getLife() + "HP");
}
```

## 🎯 Próximas Melhorias Sugeridas

- [ ] Mais tipos de inimigos
- [ ] Sistema de quests
- [ ] Loja mais dinâmica (venda de itens)
- [ ] Efeitos sonoros
- [ ] Mais mapas
- [ ] Boss rush mode
- [ ] Leaderboard
- [ ] Achievements

## 📄 Licença

Este projeto é fornecido como está para fins educacionais.

## 🤝 Contribuição

1. Faça um fork
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 👨‍💻 Autor

**Davi Camillo**

---

**Status**: ✅ Compilação sucesso | 🎮 Funcional | 🏗️ Bem Organizado

**Última atualização**: 10 de Abril de 2026
