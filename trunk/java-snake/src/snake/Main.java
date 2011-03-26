
package snake;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Peuch
 */
public class Main implements KeyListener, WindowListener { // , Runnable {

	// KEYS MAP
	public final static int UP = 0;
	public final static int DOWN = 1;
	public final static int LEFT = 2;
	public final static int RIGHT = 3;
	// GRID CONTENT
	public final static int EMPTY = 0;
	public final static int FOOD_BONUS = 1;
	public final static int FOOD_MALUS = 2;
	public final static int BIG_FOOD_BONUS = 3;
	public final static int SNAKE = 4;
	private int[][] grid = null;
	private int[][] snake = null;
	private int direction = -1;
	private int next_direction = -1;
	private int height = 600;
	private int width = 600;
	private int gameSize = 40;
	private long speed = 50;
	private Frame frame = null;
	private Canvas canvas = null;
	private Graphics graph = null;
	private BufferStrategy strategy = null;
	// private int lastx = 0;
	// private int lasty = 0;
	private boolean game_over = false;
	private boolean paused = false;

	private long cycleTime = 0;
	private long sleepTime = 0;

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		Main game = new Main();
		game.init();
		game.mainLoop();
	}

	public Main() {
		super();
		frame = new Frame();
		canvas = new Canvas();

		grid = new int[gameSize][gameSize];
		snake = new int[gameSize * gameSize][2];
	}

	public void init() {

		frame.setSize(width + 7, height + 27);
		frame.setResizable(false);
		frame.setLocationByPlatform(true);
		canvas.setSize(width + 7, height + 27);
		frame.add(canvas);
		canvas.addKeyListener(this);
		frame.addWindowListener(this);
		frame.dispose();
		frame.validate();
		frame.setTitle("Snake");
		frame.setVisible(true);

		canvas.setIgnoreRepaint(true);
		canvas.setBackground(Color.WHITE);
		/*
		 * try { canvas.createBufferStrategy(2, new BufferCapabilities( new
		 * ImageCapabilities(true), new ImageCapabilities(true),
		 * BufferCapabilities.FlipContents.BACKGROUND)); } catch (AWTException
		 * ex) { Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null,
		 * ex); }
		 */

		canvas.createBufferStrategy(2);
		/*
		 * BufferCapabilities buffCapa = canvas.getGraphicsConfiguration()
		 * .getBufferCapabilities(); System.out.println("Multibuff = " +
		 * buffCapa.isMultiBufferAvailable() + ", Page flipping = " +
		 * buffCapa.isPageFlipping() + ", FullScreen required = " +
		 * buffCapa.isFullScreenRequired() + ", FlipContent = " +
		 * buffCapa.getFlipContents());
		 */
		strategy = canvas.getBufferStrategy();
		graph = strategy.getDrawGraphics();

		initGame();

		// Start rendering in a separate dedicated thread
		renderGame();
		// new Thread(this).start();
	}

	public void mainLoop() {

		while (!game_over) {
			cycleTime = System.currentTimeMillis();
			if(!paused)
			{
				direction = next_direction;
				moveSnake();
			}
			renderGame();
			cycleTime = System.currentTimeMillis() - cycleTime;
			sleepTime = speed - cycleTime;
			if (sleepTime < 0)
				sleepTime = 0;
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException ex) {
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null,
						ex);
			}
		}
	}

	private void initGame() {

		// Initialise tabs
		for (int i = 0; i < gameSize; i++) {
			for (int j = 0; j < gameSize; j++) {
				grid[i][j] = EMPTY;
			}
		}

		for (int i = 0; i < gameSize * gameSize; i++) {
			snake[i][0] = -1;
			snake[i][1] = -1;
		}

		snake[0][0] = gameSize/2;
		snake[0][1] = gameSize/2;
		grid[gameSize/2][gameSize/2] = SNAKE;
		placeBonus();

	}

	/*
	 * public void run() {
	 * 
	 * while (true) { renderGame(); try { Thread.sleep(10); } catch
	 * (InterruptedException ex) {
	 * Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex); } } }
	 * 
	 * //
	 */
	private void renderGame() {
		int gridUnit = height / gameSize;
		canvas.paint(graph);

		do {
			do {
				graph = strategy.getDrawGraphics();

				// Draw Background
				graph.setColor(Color.WHITE);
				graph.fillRect(0, 0, width, height);

				// Draw snake, bonus ...
				int gridCase = EMPTY;

				for (int i = 0; i < gameSize; i++) {
					for (int j = 0; j < gameSize; j++) {
						gridCase = grid[i][j];

						switch (gridCase) {
						case SNAKE:
							graph.setColor(Color.BLUE);
							graph.fillOval(i * gridUnit, j * gridUnit,
									gridUnit, gridUnit);
							break;
							
						case FOOD_BONUS:
							graph.setColor(Color.darkGray);
							graph.fillOval(i * gridUnit + gridUnit / 4, j
									* gridUnit + gridUnit / 4, gridUnit / 2,
									gridUnit / 2);
							break;
						case FOOD_MALUS:
							graph.setColor(Color.RED);
							graph.fillOval(i * gridUnit + gridUnit / 4, j
									* gridUnit + gridUnit / 4, gridUnit / 2,
									gridUnit / 2);
							break;
						case BIG_FOOD_BONUS:
							graph.setColor(Color.GREEN);
							graph.fillOval(i * gridUnit + gridUnit / 4, j
									* gridUnit + gridUnit / 4, gridUnit / 2,
									gridUnit / 2);
							break;
						default:
							break;
						}
					}
				}

				graph.setFont(new Font(Font.SANS_SERIF, Font.BOLD, height / 40));

				if (game_over) {
					graph.setColor(Color.RED);
					graph.drawString("GAME OVER", height / 2 - 30, height / 2);
				}
				else if (paused) {
					graph.setColor(Color.RED);
					graph.drawString("PAUSED", height / 2 - 30, height / 2);
				}

				graph.setColor(Color.BLACK);
				graph.drawString("SCORE = " + getScore(), 10, 20);

				graph.dispose();
			} while (strategy.contentsRestored());

			// Draw image from buffer
			strategy.show();
			Toolkit.getDefaultToolkit().sync();
		} while (strategy.contentsLost());
	}

	private int getScore() {
		int score = 0;
		for (int i = 0; i < gameSize * gameSize; i++) {
			if ((snake[i][0] < 0) || (snake[i][1] < 0)) {
				break;
			}
			score++;
		}
		return score;
	}

	private void moveSnake() {
		boolean grow = false;

		if (direction < 0) {
			return;
		}
		int ymove = 0;
		int xmove = 0;
		switch (direction) {
		case UP:
			xmove = 0;
			ymove = -1;
			break;
		case DOWN:
			xmove = 0;
			ymove = 1;
			break;
		case RIGHT:
			xmove = 1;
			ymove = 0;
			break;
		case LEFT:
			xmove = -1;
			ymove = 0;
			break;
		default:
			xmove = 0;
			ymove = 0;
			break;
		}

		int tempx = snake[0][0];
		int tempy = snake[0][1];

		int fut_x = snake[0][0] + xmove;
		int fut_y = snake[0][1] + ymove;

//		if ((fut_x < 0) || (fut_y < 0) || (fut_x >= gameSize)
//				|| (fut_y >= gameSize)) {
//			gameOver();
//			return;
//		}
		
		if(fut_x < 0)
			fut_x = gameSize - 1;
		if(fut_y < 0)
			fut_y = gameSize - 1;
		if(fut_x >= gameSize)
			fut_x = 0;
		if(fut_y >= gameSize)
			fut_y = 0;
		
		if (grid[fut_x][fut_y] == FOOD_BONUS) {
			grow = true;
		}

		snake[0][0] = fut_x;
		snake[0][1] = fut_y;

		if ((grid[snake[0][0]][snake[0][1]] == SNAKE)) { // && ((snake[0][0] !=
															// lastx) &&
															// (snake[0][1] !=
															// lasty))) {
			gameOver();
			return;
		}
		grid[tempx][tempy] = EMPTY;

		int snakex, snakey, i;

		for (i = 1; i < gameSize * gameSize; i++) {
			if ((snake[i][0] < 0) || (snake[i][1] < 0)) {
				// lastx = tempx;// snake[i - 1][0];
				// lasty = tempy;// snake[i - 1][0];
				break;
			}
			grid[snake[i][0]][snake[i][1]] = EMPTY;
			snakex = snake[i][0];
			snakey = snake[i][1];
			snake[i][0] = tempx;
			snake[i][1] = tempy;
			tempx = snakex;
			tempy = snakey;
		}

		for (i = 0; i < gameSize * gameSize; i++) {
			if ((snake[i][0] < 0) || (snake[i][1] < 0)) {
				break;
			}
			grid[snake[i][0]][snake[i][1]] = SNAKE;
		}

		if (grow) {
			snake[i][0] = tempx;
			snake[i][1] = tempy;
			// lastx = tempx;
			// lasty = tempy;
			grid[snake[i][0]][snake[i][1]] = SNAKE;
			placeBonus();
		}
	}

	private void placeBonus() {
		int x = (int) (Math.random() * 1000) % gameSize;
		int y = (int) (Math.random() * 1000) % gameSize;
		if (grid[x][y] == EMPTY) {
			grid[x][y] = FOOD_BONUS;
		} else {
			placeBonus();
		}
	}

	private void gameOver() {
		game_over = true;
	}

	// / IMPLEMENTED FUNCTIONS
	public void keyPressed(KeyEvent ke) {
		int code = ke.getKeyCode();
		Dimension dim;
		// System.out.println("Key pressed" + ke.toString());

		switch (code) {
		case KeyEvent.VK_UP:
			// System.out.println("UP");
			if (direction != DOWN) {
				next_direction = UP;
			}

			break;

		case KeyEvent.VK_DOWN:
			// System.out.println("DOWN");
			if (direction != UP) {
				next_direction = DOWN;
			}

			break;

		case KeyEvent.VK_LEFT:
			// System.out.println("LEFT");
			if (direction != RIGHT) {
				next_direction = LEFT;
			}

			break;

		case KeyEvent.VK_RIGHT:
			// System.out.println("RIGHT");
			if (direction != LEFT) {
				next_direction = RIGHT;
			}

			break;

		case KeyEvent.VK_F11:
			dim = Toolkit.getDefaultToolkit().getScreenSize();
			if ((height != dim.height - 50) || (width != dim.height - 50)) {
				height = dim.height - 50;
				width = dim.height - 50;
			} else {
				height = 600;
				width = 600;
			}
			frame.setSize(width + 7, height + 27);
			canvas.setSize(width + 7, height + 27);
			canvas.validate();
			frame.validate();
			break;

		/*
		 * case KeyEvent.VK_F12: dim =
		 * Toolkit.getDefaultToolkit().getScreenSize(); if((height !=
		 * dim.height) || (width != dim.width)) {
		 * 
		 * frame.setVisible(false); //frame.setUndecorated(true);
		 * frame.setVisible(true);
		 * GraphicsEnvironment.getLocalGraphicsEnvironment
		 * ().getDefaultScreenDevice().setFullScreenWindow(frame);
		 * 
		 * } break;
		 */

		case KeyEvent.VK_ESCAPE:
			System.exit(0);
			break;
			
		case KeyEvent.VK_SPACE:
			if(!game_over)
				paused = !paused;
			break;

		default:
			// Unsupported key
			break;
		}
	}

	public void windowClosing(WindowEvent we) {
		// throw new UnsupportedOperationException("Not supported yet.");
		System.exit(0);

	}

	// UNNUSED IMPLEMENTED FUNCTIONS
	public void keyTyped(KeyEvent ke) {
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	public void keyReleased(KeyEvent ke) {
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	public void windowOpened(WindowEvent we) {
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	public void windowClosed(WindowEvent we) {
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	public void windowIconified(WindowEvent we) {
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	public void windowDeiconified(WindowEvent we) {
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	public void windowActivated(WindowEvent we) {
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	public void windowDeactivated(WindowEvent we) {
		// throw new UnsupportedOperationException("Not supported yet.");
	}
}
