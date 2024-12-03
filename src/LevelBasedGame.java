import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

public class LevelBasedGame extends JPanel implements KeyListener {
    private static final int TILE_SIZE = 32;
    private static final int MAP_WIDTH = 20;
    private static final int MAP_HEIGHT = 15;
    private static final int PLAYER_SIZE = 28;
    private static final int PRODUCTS_PER_LEVEL = 10;

    private int playerX, playerY; // Pozycja gracza
    private int animationFrame = 0;
    private int score = 0;
    private int currentLevel = 1;

    private ArrayList<Product> products = new ArrayList<>();
    private final Random random = new Random();

    private Image grassTexture, wallTexture, gateTexture;
    private Image[] playerSprites;
    private Image healthyProductTexture, unhealthyProductTexture;

    private boolean isPaused = false;
    private boolean inMainMenu = true;

    public LevelBasedGame() {
        this.setPreferredSize(new Dimension(MAP_WIDTH * TILE_SIZE, MAP_HEIGHT * TILE_SIZE));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(this);
        loadTextures();
    }

    private void loadTextures() {
        try {
            grassTexture = new ImageIcon("resources/grass.png").getImage();
            wallTexture = new ImageIcon("resources/wall.png").getImage();
            gateTexture = new ImageIcon("resources/gate.png").getImage();
            healthyProductTexture = new ImageIcon("resources/healthy_product.png").getImage();
            unhealthyProductTexture = new ImageIcon("resources/unhealthy_product.png").getImage();
            playerSprites = new Image[] {
                    new ImageIcon("resources/player_1.png").getImage(),
                    new ImageIcon("resources/player_2.png").getImage()
            };
        } catch (Exception e) {
            System.err.println("Błąd podczas ładowania tekstur: " + e.getMessage());
            System.exit(1);
        }
    }

    private void startLevel(int level) {
        currentLevel = level;
        playerX = (MAP_WIDTH - 1) * TILE_SIZE; // Pozycja startowa (prawa krawędź)
        playerY = MAP_HEIGHT / 2 * TILE_SIZE;
        score = 0;
        generateLevel();
        inMainMenu = false;
        isPaused = false;
        repaint();
    }

    private void generateLevel() {
        products.clear();
        for (int i = 0; i < PRODUCTS_PER_LEVEL; i++) {
            boolean isHealthy = random.nextBoolean();
            int x, y;
            do {
                x = random.nextInt(MAP_WIDTH - 2) + 1; // Unikaj krawędzi
                y = random.nextInt(MAP_HEIGHT - 2) + 1;
                int finalX = x; // Kopia wartości x
                int finalY = y; // Kopia wartości y
                if (products.stream().noneMatch(p -> p.x == finalX * TILE_SIZE && p.y == finalY * TILE_SIZE)) {
                    break; // Znaleziono unikalne współrzędne
                }
            } while (true);
            products.add(new Product(x * TILE_SIZE, y * TILE_SIZE, isHealthy,
                    isHealthy ? random.nextInt(10) + 10 : -random.nextInt(5) - 1));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (inMainMenu) {
            drawMainMenu(g);
        } else if (isPaused) {
            drawPauseMenu(g);
        } else {
            drawGame(g);
        }
    }

    private void drawMainMenu(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Healthy Products Game", 100, 100);

        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Press 1 for Level 1", 150, 200);
        g.drawString("Press 2 for Level 2", 150, 250);
        g.drawString("Press 3 for Level 3", 150, 300);
    }

    private void drawPauseMenu(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Game Paused", 150, 150);

        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Press R to Resume", 150, 200);
        g.drawString("Press M for Main Menu", 150, 250);
    }

    private void drawGame(Graphics g) {
        // Rysowanie mapy
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                if (x == 0 || y == 0 || x == MAP_WIDTH - 1 || y == MAP_HEIGHT - 1) {
                    g.drawImage(wallTexture, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                } else {
                    g.drawImage(grassTexture, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                }
            }
        }

        // Rysowanie bramy
        g.drawImage(gateTexture, 0, MAP_HEIGHT / 2 * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);

        // Rysowanie produktów
        for (Product product : products) {
            g.drawImage(product.isHealthy ? healthyProductTexture : unhealthyProductTexture, product.x, product.y,
                    TILE_SIZE, TILE_SIZE, null);
        }

        // Rysowanie gracza
        g.drawImage(playerSprites[animationFrame], playerX, playerY, PLAYER_SIZE, PLAYER_SIZE, null);

        // Wyświetlanie punktów i poziomu
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Level: " + currentLevel, 10, 40);
    }

    private void checkCollisionWithProducts() {
        products.removeIf(product -> {
            if (playerX == product.x && playerY == product.y) {
                score += product.points;
                return true; // Produkt został zebrany
            }
            return false;
        });

        // Jeśli wszystkie produkty zebrano, pozwól przejść przez bramę
        if (products.isEmpty()) {
            System.out.println("Wszystkie produkty zebrane! Możesz przejść przez bramę.");
        }
    }

    private boolean isWallCollision(int x, int y) {
        // Jeśli gracz próbuje wyjść poza krawędzie mapy, zwróć true
        if (x < TILE_SIZE || x >= (MAP_WIDTH - 1) * TILE_SIZE ||
                y < TILE_SIZE || y >= (MAP_HEIGHT - 1) * TILE_SIZE) {
            return true;
        }
        return false;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (inMainMenu) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_1 -> startLevel(1);
                case KeyEvent.VK_2 -> startLevel(2);
                case KeyEvent.VK_3 -> startLevel(3);
            }
        } else if (isPaused) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_R -> {
                    isPaused = false;
                    repaint();
                }
                case KeyEvent.VK_M -> {
                    inMainMenu = true;
                    repaint();
                }
            }
        } else {
            int nextX = playerX;
            int nextY = playerY;

            switch (e.getKeyCode()) {
                case KeyEvent.VK_W -> nextY -= TILE_SIZE;
                case KeyEvent.VK_A -> nextX -= TILE_SIZE;
                case KeyEvent.VK_S -> nextY += TILE_SIZE;
                case KeyEvent.VK_D -> nextX += TILE_SIZE;
                case KeyEvent.VK_P -> isPaused = true;
            }

            // Sprawdzenie kolizji ze ścianą
            if (!isWallCollision(nextX, nextY)) {
                playerX = nextX;
                playerY = nextY;
            }

            // Sprawdzanie kolizji z produktami
            checkCollisionWithProducts();

            // Sprawdzenie przejścia przez bramę
            if (products.isEmpty() && playerX == 0 && playerY == MAP_HEIGHT / 2 * TILE_SIZE) {
                System.out.println("Przechodzisz na kolejny poziom!");
                startLevel(currentLevel + 1);
            }

            repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Healthy Products Game");
        LevelBasedGame game = new LevelBasedGame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(game);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static class Product {
        int x, y;
        boolean isHealthy;
        int points;

        public Product(int x, int y, boolean isHealthy, int points) {
            this.x = x;
            this.y = y;
            this.isHealthy = isHealthy;
            this.points = points;
        }
    }
}
