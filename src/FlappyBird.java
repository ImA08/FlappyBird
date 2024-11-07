import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    enum GameState { MENU, PLAYING, GAME_OVER }
    GameState gameState = GameState.MENU;

    int boardWidth = 360;
    int boardHeight = 640;

    // images
    Image backgroundImg,
          birdImg,
          topPipeImg,
          bottomPipeImg;

    // bird
    int birdX = boardWidth / 8,
        birdY = boardHeight / 2,
        birdWidth = 34,
        birdHeight = 24;

    class Bird {
        int x = birdX,
            y = birdY,
            width = birdWidth,
            height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // pipes
    int pipeX = boardWidth,
        pipeY = 0,
        pipeWidth = 64,
        pipeHeight = 512;

    class Pipe {
        int x = pipeX,
            y = pipeY,
            width = pipeWidth,
            height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // game logic
    Bird bird;
    int velocityX = -4,
        velocityY = 0,
        gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipesTimer;
    boolean gameOver = false;
    double score = 0;

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // load images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        // place pipes timer
        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });

        // game timer
        gameLoop = new Timer(1000 / 60, this);
    }

    public void placePipes() {
        int[] possibleSpaces = { boardHeight / 4, boardHeight / 7, boardHeight / 8 };
        int openingSpace = possibleSpaces[random.nextInt(possibleSpaces.length)];

        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

        if (gameState == GameState.MENU) {
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("START", boardWidth / 2 - 60, boardHeight / 2 - 30);

            g.setFont(new Font("Arial", Font.PLAIN, 24));
            g.drawString("PRESS SPACE TO START PLAY", boardWidth / 2 - 130, boardHeight / 2 + 30);
        } else if (gameState == GameState.PLAYING) {
            // Draw bird
            g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

            // Draw pipes
            for (Pipe pipe : pipes) {
                g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
            }

            // Draw score
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.PLAIN, 32));
            g.drawString("Score: " + (int) score, 10, 35);
        } else if (gameState == GameState.GAME_OVER) {
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("GAME OVER", boardWidth / 2 - 120, boardHeight / 2 - 30);

            g.setFont(new Font("Arial", Font.PLAIN, 24));
            g.drawString("PRESS SPACE TO RETURN TO MENU", boardWidth / 2 - 130, boardHeight / 2 + 30);
        }
    }

    public void resetGame() {
        bird.y = birdY;
        pipes.clear();
        score = 0;
        velocityY = 0;
        gameOver = false;
        placePipesTimer.start();
        gameLoop.start();
    }

    public void move() {
        if (gameState == GameState.PLAYING) {
            // Bird movement
            velocityY += gravity;
            bird.y += velocityY;
            bird.y = Math.max(bird.y, 0);

            // Pipe movement
            for (Pipe pipe : pipes) {
                pipe.x += velocityX;

                if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                    pipe.passed = true;
                    score += 0.5;
                }

                if (collision(bird, pipe)) {
                    gameOver = true;
                    gameState = GameState.GAME_OVER;
                    placePipesTimer.stop();
                    gameLoop.stop();
                }
            }

            if (bird.y > boardHeight) {
                gameOver = true;
                gameState = GameState.GAME_OVER;
                placePipesTimer.stop();
                gameLoop.stop();
            }
        }
    }

    public boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameState == GameState.MENU && e.getKeyCode() == KeyEvent.VK_SPACE) {
            gameState = GameState.PLAYING;
            resetGame();
        } else if (gameState == GameState.PLAYING && e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;
        } else if (gameState == GameState.GAME_OVER && e.getKeyCode() == KeyEvent.VK_SPACE) {
            gameState = GameState.MENU;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}
