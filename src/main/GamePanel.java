package main;

import components.background.Background;
import components.mario.Mario;
import components.ground.Ground;
import components.obstacles.Obstacles;
import components.ui.*;
import interfaces.GameSettings;
import interfaces.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GamePanel extends JPanel implements Runnable, KeyListener, GameSettings, SoundManager {

    private Thread mainThread = new Thread(this);

    public static boolean debugMode = false;
    public static int gameSpeed = game_start_speed;
    public static boolean isGameSpeedChanged = false;

    public boolean running = false;
    public boolean paused = false;
    public boolean gameOver = false;
    public boolean intro = true;
    private boolean isMuted = false;
    isMute mute = new isMute();
    final Object PAUSE_LOCK = new Object();

    Mario mario = new Mario();
    Ground ground = new Ground();
    Obstacles obstacles = new Obstacles();
    Background background = new Background();
    Score score = new Score();

    Score scoreUI = new Score();
    GameOver gameOverUI = new GameOver();
    Paused pausedUI = new Paused();
    Intro introUI = new Intro();

    public GamePanel() {
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLayout(null);
        setVisible(true);

        add(introUI.introLabel);
        mainThread.start();
        System.out.println("FPS: " + game_fps);
    }

    /**
     * Toggles the Audio on and off.
     *
     * This function checks the current state of the microphone and toggles it to
     * the opposite state.
     * If the microphone is currently off, it will be turned on and the intro music
     * will start playing.
     * If the microphone is currently on, it will be turned off and the intro music
     * will stop playing.
     * The function also prints a message to the console indicating whether the
     * microphone is on or off.
     */
    public void toggleMic() {
        isMuted = !isMuted;
        if (isMuted) {
            introUI.overworld.stop();
            System.out.println("Audio: Mic Off");
        } else {
            if (!running || paused) {
                introUI.overworld.playInLoop();
            }
            System.out.println("Audio: Mic On");
        }
    }

    /**
     * Starts the game by setting the running flag to true, stopping the intro
     * music, and hiding the intro UI.
     *
     * @param None
     * @return None
     */
    public void startGame() {
        System.out.println("\nGame log");
        System.out.println("-----------------------------------------------------");

        running = true;
        intro = false;
        introUI.overworld.stop(); // stop the intro music
        if (running == true) {
            System.out.println("Running...");
        }
    }

    /**
     * Resets the game state and starts a new game.
     *
     * This function sets the gameOver flag to false, the running flag to true,
     * and resets the game speed to the initial value. It also resets the score,
     * mario, obstacles, ground, and background objects. Finally, it starts a new
     * thread to run the game loop.
     *
     * @param None
     * @return None
     */
    public void resetGame() {
        gameOver = false;
        running = true;

        gameSpeed = game_start_speed;

        scoreUI.reset();
        mario.reset();
        obstacles.reset();
        ground.reset();
        background.reset();
        mainThread = new Thread(this);
        mainThread.start();
    }

    /**
     * Pauses the game by setting the `paused` flag to true and playing the
     * `playInLoop` method of the `introUI.overworld` object.
     * Prints "Paused" to the console.
     */
    public void pauseGame() {
        paused = true;
        if (!isMuted) {
            introUI.overworld.playInLoop();
        }
        System.out.println("Paused");
    }

    /**
     * Resumes the game by setting the 'paused' flag to false, stopping the
     * 'introUI.overworld' and notifying the 'PAUSE_LOCK' to wake up any waiting
     * threads. Also prints "Resumed" to the console.
     */
    public void resumeGame() {
        synchronized (PAUSE_LOCK) {
            paused = false;
            introUI.overworld.stop();
            PAUSE_LOCK.notify();
            System.out.println("Resumed");
        }
    }

    /**
     * @Experimental!
     *                Changes the game speed if the score is greater than 0 and is a
     *                multiple of 260, and the game speed has not already been
     *                changed
     *                and is less than the maximum game speed.
     *
     * @return void
     */
    private void changeGameSpeed() {
        if (Score.score > 0 && Score.score % 260 == 0 && !isGameSpeedChanged && gameSpeed < game_max_speed) {
            isGameSpeedChanged = true;
            gameSpeed += 1;
        }
    }

    /**
     * MAIN PAINT METHOD
     * --------------------------------------------------------
     * 
     * Paints the components using the provided Graphics object.
     *
     * @param g the Graphics object to paint with
     * @return void
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        background.draw(g);

        if (isMuted) {
            mute.draw(g);
        }

        if (paused)
            pausedUI.draw(g);
        if (gameOver)
            gameOverUI.draw(g);
        if (!intro)
            scoreUI.draw(g);

        ground.draw(g);
        mario.draw(g);
        obstacles.draw(g);

        if (intro)
            introUI.draw(g);
    }

    /**
     * MAIN GAME LOOP
     * It is probably the simplest version
     * ------------------------------------------------------------------------
     * Good resources:
     * -
     * https://gamedev.stackexchange.com/questions/160329/java-game-loop-efficiency
     * - https://stackoverflow.com/questions/18283199/java-main-game-loop
     */
    @Override
    public void run() {
        // INTRO LOOP FOR EASTER EGG
        while (intro) {
            try {
                int msPerFrame = 5000 / game_fps;
                Thread.sleep(msPerFrame);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            repaint();
        }

        // MAIN GAME LOOP
        while (running) {
            // GAME TIMING
            try {
                int msPerFrame = 1000 / game_fps;
                Thread.sleep(msPerFrame);
                if (paused) {
                    synchronized (PAUSE_LOCK) {
                        repaint();
                        PAUSE_LOCK.wait();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // GAME LOGIC
            changeGameSpeed();
            scoreUI.update();
            background.update();
            mario.update();
            ground.update();
            obstacles.update();

            if (obstacles.isCollision()) {
                mario.die();
                if (Mario.isMario)
                    introUI.overworld.stop();
                scoreUI.writeHighScore();
                gameOver = true;
                running = false;
                System.out.println("Game over");
            }
            // RENDER OUTPUT
            repaint();
        }
    }

    /**
     * KEY BINDINGS
     *
     * -------------------------------------------
     * Debug mode: '`'
     * Jump: ' ', 'w', 'ARROW UP'
     * Fall: 's', 'ARROW DOWN'
     * Pause: 'p', 'ESC'
     * -------------------------------------------
     * 
     * @param e KeyEvent
     */
    @Override
    public void keyPressed(KeyEvent e) {
        // DEBUG
        if (DEBUGGER)
            if (e.getKeyChar() == '`') {
                debugMode = !debugMode;
            }

        // Mic
        if (e.getKeyChar() == 'm' || e.getKeyChar() == 'M') {
            toggleMic();
            mario.toggleMic();
            score.toggleMic();
        }

        // JUMP
        if (e.getKeyChar() == ' ' || e.getKeyChar() == 'w' || e.getKeyChar() == 'W'
                || e.getKeyCode() == KeyEvent.VK_UP) {
            if (!paused && running) {
                mario.jump();
            } else if (paused && running) {
                resumeGame();
            }

            if (!running && !gameOver) {
                startGame();
                mario.run();
                mario.jump();
                introUI.overworld.stop();
            } else if (gameOver) {
                resetGame();
                introUI.overworld.stop();
            }
        }

        // FALL
        if (e.getKeyChar() == 's' || e.getKeyChar() == 'S' || e.getKeyCode() == KeyEvent.VK_DOWN) {
            if (!paused && running) {
                mario.fall();
            }
        }

        // PAUSE
        if (e.getKeyChar() == 'p' || e.getKeyChar() == 'P' || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (!paused && running) {
                pauseGame();
            } else if (paused && running) {
                resumeGame();
            }
        }
    }

    /**
     * Just checking if someone change mind to jump
     * right after hitting ground
     * --------------------------------------------------------
     * 
     * @param e KeyEvent
     */
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyChar() == ' ' || e.getKeyChar() == 'w' || e.getKeyChar() == 'W' || e.getKeyCode() == KeyEvent.VK_UP)
            mario.jumpRequested = false;
    }

    /**
     * Abstract method from KeyListener.
     * 
     * @param e KeyEvent
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    @Override
    public void keyTyped(KeyEvent e) {
        //
    }
}
