import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * The class provides a frame for the menu and panel of the game. It is also
 * responsible for managing the game difficulty and game status.
 * 
 * @author Guohong Yang
 *
 */
public class Game {

	private final JFrame WINDOW = new JFrame();
	private final int WIDTH;
	private final int HEIGHT;
	private final GameCore GAME_CORE;
	private final GameMenu GAME_MENU;
	private Thread gameLoop;
	private String gameStatus;
	private String difficulty;
	private boolean reverse;
	private int expectFrequency;
	private int frequency;

	public Game(String title, int width, int height, String difficulty) {
		gameStatus = "NEW";
		reverse = false;
		WIDTH = width - width % 10;
		HEIGHT = height - height % 10;
		GAME_CORE = new GameCore();
		setDifficulty(difficulty);
		GAME_MENU = new GameMenu();
		GAME_CORE.setPreferredSize(new Dimension(WIDTH, HEIGHT));

		WINDOW.setJMenuBar(GAME_MENU);
		WINDOW.setContentPane(GAME_CORE);
		WINDOW.setResizable(false);
		WINDOW.pack();
		WINDOW.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		WINDOW.setFocusable(true);
		WINDOW.setLocationRelativeTo(null);
		WINDOW.setTitle(title);
		WINDOW.setVisible(true);
		WINDOW.addKeyListener(GAME_CORE);

		/*
		 * show help message when game is launched
		 */
		JOptionPane
				.showMessageDialog(
						WINDOW,
						"Press direction arrows to start instantly."
								+ "\n"
								+ "Hold direction arrows for acceleration."
								+ "\n"
								+ "Press white space to pause/unpause, and to reset when game is stopped."
								+ "\n" + "Press ESC to exit.", "Help",
						JOptionPane.INFORMATION_MESSAGE);
	}

	public JFrame getWindow() {
		return WINDOW;
	}

	public int getWidth() {
		return WIDTH;
	}

	public int getHeight() {
		return HEIGHT;
	}

	public GameCore getGameCore() {
		return GAME_CORE;
	}

	public String getGameStatus() {
		return gameStatus;
	}

	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
		switch (difficulty) {
		case "Easy":
			expectFrequency = 5;
			frequency = expectFrequency;
			reverse = false;
			break;
		case "Medium":
			expectFrequency = 10;
			frequency = expectFrequency;
			reverse = false;
			break;
		case "Hard":
			expectFrequency = 15;
			frequency = expectFrequency;
			reverse = false;
			break;
		case "Extreme":
			expectFrequency = 15;
			frequency = expectFrequency;
			reverse = true;
			break;
		default:
			break;
		}
		GAME_CORE.bestScore = new int[3];
	}

	/**
	 * Change the update frequency of the game to have the effect of boost.
	 */
	public void boost() {
		frequency = (int) (2.5 * expectFrequency);
	}

	/**
	 * Change the update frequency of the game back to origin value.
	 */
	public void deboost() {
		frequency = expectFrequency;
	}

	/**
	 * Start the game by initializing the game loop thread and setting it to
	 * start.
	 */
	public void start() {
		gameStatus = "RUNNING";
		gameLoop = new Thread() {
			public void run() {
				while (gameStatus.equals("RUNNING")
						|| gameStatus.equals("PAUSED")) {
					double now = System.nanoTime();
					double lastUpdate = now;
					while (gameStatus.equals("RUNNING")) {
						while (now - lastUpdate < (1000000000 / frequency)) {
							if (!gameStatus.equals("RUNNING"))
								break;
							Thread.yield();
							try {
								Thread.sleep(1);
							} catch (Exception e) {
							}
							now = System.nanoTime();
						}
						/*
						 * render if updated. multiple updates are made if
						 * necessary
						 */
						while (now - lastUpdate > (1000000000 / frequency)) {
							GAME_CORE.repaint();
							GAME_CORE.updateGame();
							lastUpdate += (1000000000 / frequency);
						}
					}
					while (gameStatus.equals("PAUSED")) {
						Thread.yield();
						try {
							Thread.sleep(1);
						} catch (Exception e) {
						}
						;
					}
				}
			}
		};
		gameLoop.start();

	}

	public void pause() {
		gameStatus = "PAUSED";
		System.out.println("PAUSED");
	}

	public void unpause() {
		gameStatus = "RUNNING";
		System.out.println("UNPAUSED");
	}

	public void stop() {
		gameStatus = "STOPPED";
	}

	public void renew() {
		gameStatus = "NEW";
		GAME_CORE.resetGame();
		GAME_CORE.repaint();
	}

	public void exit() {
		System.out.println("Exit");
		stop();
		WINDOW.removeKeyListener(GAME_CORE);
		WINDOW.dispose();
	}

	class GameMenu extends JMenuBar implements ActionListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5995961800376482109L;
		private final JMenu M_GAME, M_DIFFICULTY, M_SCORE, M_ABOUT;
		private final JMenuItem MI_RESTART, MI_EXIT, MI_PAUSE_UNPAUSE, MI_EASY,
				MI_MEDIUM, MI_HARD, MI_EXTREME, MI_SCOREBOARD, MI_CREDITS,
				MI_HELP;

		public GameMenu() {

			M_GAME = new JMenu("Game");
			M_DIFFICULTY = new JMenu("Difficulty");
			M_SCORE = new JMenu("Score");
			M_ABOUT = new JMenu("About");

			MI_RESTART = new JMenuItem("Restart");
			MI_PAUSE_UNPAUSE = new JMenuItem("Pause/Unpause");
			MI_EXIT = new JMenuItem("Exit");

			MI_SCOREBOARD = new JMenuItem("Scoreboard");

			MI_EASY = new JMenuItem("Easy");
			MI_MEDIUM = new JMenuItem("Medium");
			MI_HARD = new JMenuItem("Hard");
			MI_EXTREME = new JMenuItem("Extreme");

			MI_HELP = new JMenuItem("Help");
			MI_CREDITS = new JMenuItem("Credits");

			add(M_GAME);
			add(M_DIFFICULTY);
			add(M_SCORE);
			add(M_ABOUT);
			M_GAME.add(MI_RESTART);
			M_GAME.add(MI_PAUSE_UNPAUSE);
			M_GAME.add(MI_EXIT);
			M_DIFFICULTY.add(MI_HARD);
			M_DIFFICULTY.add(MI_MEDIUM);
			M_DIFFICULTY.add(MI_EASY);
			M_DIFFICULTY.add(MI_EXTREME);
			M_SCORE.add(MI_SCOREBOARD);
			M_ABOUT.add(MI_HELP);
			M_ABOUT.add(MI_CREDITS);

			MI_RESTART.addActionListener(this);
			MI_EXIT.addActionListener(this);
			MI_PAUSE_UNPAUSE.addActionListener(this);
			MI_EASY.addActionListener(this);
			MI_MEDIUM.addActionListener(this);
			MI_HARD.addActionListener(this);
			MI_EXTREME.addActionListener(this);
			MI_SCOREBOARD.addActionListener(this);
			MI_CREDITS.addActionListener(this);
			MI_HELP.addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource().equals(MI_RESTART)) {
				stop();
				renew();
			}
			if (e.getSource().equals(MI_EXIT)) {
				exit();
			}
			if (e.getSource().equals(MI_PAUSE_UNPAUSE)) {
				if (gameStatus.equals("RUNNING"))
					pause();
				else if (gameStatus.equals("PAUSED"))
					unpause();
			}
			if (e.getSource().equals(MI_EASY)) {
				stop();
				renew();
				setDifficulty("Easy");
			}
			if (e.getSource().equals(MI_MEDIUM)) {
				stop();
				renew();
				setDifficulty("Medium");
			}
			if (e.getSource().equals(MI_HARD)) {
				stop();
				renew();
				setDifficulty("Hard");
			}
			if (e.getSource().equals(MI_EXTREME)) {
				stop();
				renew();
				setDifficulty("Extreme");
			}
			if (e.getSource().equals(MI_SCOREBOARD)) {
				JOptionPane.showMessageDialog(WINDOW, "Difficulty: "
						+ difficulty + "\n" + "First Place: "
						+ GAME_CORE.bestScore[0] + "\n" + "Second Place: "
						+ GAME_CORE.bestScore[1] + "\n" + "Third Place: "
						+ GAME_CORE.bestScore[2] + "\n", "Scoreboard",
						JOptionPane.INFORMATION_MESSAGE);
			}
			if (e.getSource().equals(MI_CREDITS)) {
				JOptionPane.showMessageDialog(WINDOW, "by Leon Yang" + "\n"
						+ "leonyang1994@gmail.com", "Credits",
						JOptionPane.INFORMATION_MESSAGE);
			}
			if (e.getSource().equals(MI_HELP)) {
				JOptionPane
						.showMessageDialog(
								WINDOW,
								"Press direction arrows to start instantly."
										+ "\n"
										+ "Hold direction arrows for acceleration."
										+ "\n"
										+ "Press white space to pause/unpause, and to reset when game is stopped."
										+ "\n" + "Press ESC to exit.", "Help",
								JOptionPane.INFORMATION_MESSAGE);
			}
			GAME_CORE.repaint();

		}
	}

	class GameCore extends JPanel implements KeyListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = 999723864191480636L;
		private boolean[] keys = new boolean[256];
		private Snake snake;
		private int[] bestScore = new int[3];

		public GameCore() {
			resetGame();
		}

		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() > 255)
				return;

			if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				exit();

			if (!gameStatus.equals("PAUSED")) {
				/*
				 * check if the game is in the reversed mode. If so, snake
				 * should move to reverse direction of the key pressed
				 */
				if (!reverse) {
					switch (e.getKeyCode()) {
					case KeyEvent.VK_UP:
						if (!snake.getVelocity().equals(snake.DOWN))
							if (keys[KeyEvent.VK_UP])
								boost();
						snake.setVelocity(snake.UP);
						break;
					case KeyEvent.VK_DOWN:
						if (!snake.getVelocity().equals(snake.UP))
							if (keys[KeyEvent.VK_DOWN])
								boost();
						snake.setVelocity(snake.DOWN);
						break;
					case KeyEvent.VK_LEFT:
						if (!snake.getVelocity().equals(snake.RIGHT))
							if (keys[KeyEvent.VK_LEFT])
								boost();
						snake.setVelocity(snake.LEFT);
						break;
					case KeyEvent.VK_RIGHT:
						if (!snake.getVelocity().equals(snake.LEFT))
							if (keys[KeyEvent.VK_RIGHT])
								boost();
						snake.setVelocity(snake.RIGHT);
						break;
					default:
						break;
					}
				} else {
					switch (e.getKeyCode()) {
					case KeyEvent.VK_UP:
						if (!snake.getVelocity().equals(snake.UP))
							if (keys[KeyEvent.VK_UP])
								boost();
						snake.setVelocity(snake.DOWN);
						break;
					case KeyEvent.VK_DOWN:
						if (!snake.getVelocity().equals(snake.DOWN))
							if (keys[KeyEvent.VK_DOWN])
								boost();
						snake.setVelocity(snake.UP);
						break;
					case KeyEvent.VK_LEFT:
						if (!snake.getVelocity().equals(snake.LEFT))
							if (keys[KeyEvent.VK_LEFT])
								boost();
						snake.setVelocity(snake.RIGHT);
						break;
					case KeyEvent.VK_RIGHT:
						if (!snake.getVelocity().equals(snake.RIGHT))
							if (keys[KeyEvent.VK_RIGHT])
								boost();
						snake.setVelocity(snake.LEFT);
						break;
					default:
						break;
					}
				}
			}

			/*
			 * Launch the game if any direction key is pressed when the game is
			 * newly initialized
			 */
			if (gameStatus.equals("NEW"))
				if (e.getKeyCode() == KeyEvent.VK_UP
						|| e.getKeyCode() == KeyEvent.VK_DOWN
						|| e.getKeyCode() == KeyEvent.VK_LEFT
						|| e.getKeyCode() == KeyEvent.VK_RIGHT)
					start();

			/* Pressing space to pause or resume the game */
			if (e.getKeyCode() == KeyEvent.VK_SPACE && !keys[KeyEvent.VK_SPACE]) {
				if (gameStatus.equals("RUNNING")) {
					pause();
					keys[KeyEvent.VK_SPACE] = true;
				} else

				if (gameStatus.equals("PAUSED")) {
					unpause();
					keys[KeyEvent.VK_SPACE] = true;
				}
			}

			keys[e.getKeyCode()] = true;

			if (gameStatus.equals("STOPPED"))
				if (keys[KeyEvent.VK_SPACE]) {
					renew();
				}
		}

		/*
		 * the speed in the direction of the key is loss when that key is
		 * released
		 */
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() > 255)
				return;

			if (!reverse) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
					if (!snake.getVelocity().equals(snake.DOWN))
						if (keys[KeyEvent.VK_UP])
							deboost();
					break;
				case KeyEvent.VK_DOWN:
					if (!snake.getVelocity().equals(snake.UP))
						if (keys[KeyEvent.VK_DOWN])
							deboost();
					break;
				case KeyEvent.VK_LEFT:
					if (!snake.getVelocity().equals(snake.RIGHT))
						if (keys[KeyEvent.VK_LEFT])
							deboost();
					break;
				case KeyEvent.VK_RIGHT:
					if (!snake.getVelocity().equals(snake.LEFT))
						if (keys[KeyEvent.VK_RIGHT])
							deboost();
					break;
				default:
					break;
				}
			} else {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
					if (!snake.getVelocity().equals(snake.UP))
						if (keys[KeyEvent.VK_UP])
							deboost();
					break;
				case KeyEvent.VK_DOWN:
					if (!snake.getVelocity().equals(snake.DOWN))
						if (keys[KeyEvent.VK_DOWN])
							deboost();
					break;
				case KeyEvent.VK_LEFT:
					if (!snake.getVelocity().equals(snake.LEFT))
						if (keys[KeyEvent.VK_LEFT])
							deboost();
					break;
				case KeyEvent.VK_RIGHT:
					if (!snake.getVelocity().equals(snake.RIGHT))
						if (keys[KeyEvent.VK_RIGHT])
							deboost();
					break;
				default:
					break;
				}
			}

			keys[e.getKeyCode()] = false;

		}

		public void keyTyped(KeyEvent e) {
		}

		/**
		 * Update the game by moving the snake forward. The snake itself checks
		 * whether it should grow (eat the food) or die (touch itself or the
		 * wall) or just move. It notifies the game when it dies.
		 */
		public void updateGame() {
			snake.move();
		}

		public void updateBestScore() {
			int score = snake.getScore();
			if (score > bestScore[0]) {
				JOptionPane.showMessageDialog(WINDOW, "New Best Score!" + "\n"
						+ score);
				bestScore[2] = bestScore[1];
				bestScore[1] = bestScore[0];
				bestScore[0] = score;
				System.out.println("New Best Score: " + score);
			} else if (score > bestScore[1]) {
				bestScore[2] = bestScore[1];
				bestScore[1] = score;
			} else if (score > bestScore[2]) {
				bestScore[2] = score;
			}
		}

		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(Color.darkGray);
			g.fillRect(0, 0, Game.this.getWidth(), Game.this.getHeight());
			snake.drawSnake(g2d);
		}

		public void resetGame() {
			snake = new Snake(Game.this);
		}
	}

}
