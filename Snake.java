import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Snake {

	private final Game GAME;
	private final Food food;
	/*
	 * All the squares excepts for the ones that are occupied by the snake's
	 * body. This is used to get a valid location for the food.
	 */
	private ArrayList<Point> availableLocs = new ArrayList<Point>();
	private ConcurrentLinkedQueue<Point> body;
	private Point head;
	public final int[] UP = { 0, -10 };
	public final int[] DOWN = { 0, 10 };
	public final int[] LEFT = { -10, 0 };
	public final int[] RIGHT = { 10, 0 };
	public final int[] STOP = { 0, 0 };
	private int[] velocity;
	/* A queue which stores the movement direction vectors from the key inputs */
	private ConcurrentLinkedQueue<int[]> velocityBuffer;
	private Color snakeColor;

	/**
	 * This class simulates a snake which can move up, right, down, and left,
	 * one square a time; grows length by one when one unit of food is eaten;
	 * dies when the head touches its body or the walls. The snake can turn left
	 * or right, 90 degrees each turn.
	 * 
	 * @param game
	 */
	public Snake(Game game) {
		this.GAME = game;
		body = new ConcurrentLinkedQueue<Point>();
		body.add(new Point((int) (GAME.getWidth() / 20) * 10 + 10, (int) (GAME
				.getHeight() / 20) * 10));
		head = new Point((int) (GAME.getWidth() / 20) * 10,
				(int) (GAME.getHeight() / 20) * 10);
		body.add(head);
		velocity = LEFT;
		velocityBuffer = new ConcurrentLinkedQueue<int[]>();
		for (int i = 0; i < GAME.getWidth() * GAME.getHeight() / 10; i += 10)
			availableLocs.add(new Point(i % GAME.getWidth(), i
					/ GAME.getWidth() * 10));
		availableLocs.removeAll(body);
		food = new Food();
		snakeColor = Color.yellow;
	}

	public int getScore() {
		return body.size() - 2;
	}

	public int[] getVelocity() {
		return velocity;
	}

	/**
	 * Instead of just changing the velocity parameter of the snake, the new
	 * velocity is added to the buffer queue, which pops out new velocities once
	 * at a time, so that the control experience of the game would be better.
	 * 
	 * @param newVelocity
	 */
	public void setVelocity(int[] newVelocity) {
		velocityBuffer.add(newVelocity);
	}

	public void flushVelocityBuffer() {
		velocityBuffer.clear();
	}

	/**
	 * Move the snake one square forward and check whether the snake eats or
	 * dies. If neither happens, the snake moves by adding one unit to the head
	 * and removing the last unit.
	 */
	public void move() {
		/* Only care about the last 3 velocities input to the snake */
		while (velocityBuffer.size() > 3)
			velocityBuffer.poll();
		/*
		 * Omit any illegal velocity value in the buffer until none exists or
		 * buffer is empty. New velocity that is the reverse of the current
		 * velocity is illegal.
		 */
		while (!velocityBuffer.isEmpty()
				&& ((velocityBuffer.peek()[0] + velocity[0] == 0 && velocityBuffer
						.peek()[1] + velocity[1] == 0) || velocityBuffer.peek()
						.equals(velocity)))
			velocityBuffer.poll();
		if (!velocityBuffer.isEmpty())
			velocity = velocityBuffer.poll();

		Point next = new Point((int) head.getX() + velocity[0],
				(int) head.getY() + velocity[1]);

		/*
		 * Check if the snake is going to be dead: the snake will stop right in
		 * front of the wall or itself and reminds the player that game is over.
		 */
		if (isDead(next)) {
			System.out.println("Ooops!");
			GAME.getGameCore().updateBestScore();
			GAME.stop();
			return;
		}

		if (next.equals(food.getLocation())) {
			availableLocs.remove(next);
			food.updateLocation();
			head = next;
			body.add(next);
			return;
		}

		head = next;
		body.add(next);
		availableLocs.remove(next);
		availableLocs.add(body.poll());
	}

	public boolean isDead(Point next) {
		if (body.contains(next))
			return true;
		int x = (int) next.getX();
		int y = (int) next.getY();
		if (x < 0 || x > GAME.getWidth() - 10 || y < 0
				|| y > GAME.getHeight() - 10)
			return true;
		return false;
	}

	public void drawSnake(Graphics g) {
		food.drawFood(g);
		Iterator<Point> itr = body.iterator();
		Point last = null;
		/* Draw a snake with connection between each body unit and shadow effect */
		while (itr.hasNext()) {
			Point next = itr.next();
			g.setColor(snakeColor);
			g.fillRect((int) next.getX(), (int) next.getY(), 9, 9);
			if (last != null) {
				g.setColor(Color.orange);
				if (last.getX() != next.getX())
					g.fillRect((int) Math.max(last.getX(), next.getX()) - 1,
							(int) next.getY(), 2, 9);
				else
					g.fillRect((int) next.getX(),
							(int) Math.max(last.getY(), next.getY()) - 1, 9, 2);
			}
			last = next;
		}
		g.setColor(Color.white);
		g.drawString("Score: " + getScore(), 10, 20);
	}

	/**
	 * This class simulate the food for the snake. When the food is eaten by the
	 * snake, the snake grows.
	 * 
	 * @author Administrator
	 *
	 */
	class Food {

		private Point location;
		private Color foodColor;

		public Food() {
			updateLocation();
			foodColor = Color.green;
		}

		/**
		 * Choose a random location from the available locations.
		 */
		public void updateLocation() {
			int size = availableLocs.size();
			location = availableLocs.get((int) (Math.random() * size));
		}

		public Point getLocation() {
			return location;
		}

		public Color getColor() {
			return foodColor;
		}

		public void changeColor() {
			Random random = new Random();
			int red = random.nextInt(256);
			int green = random.nextInt(256);
			int blue = random.nextInt(256);
			foodColor = new Color(red, green, blue);
		}

		public void drawFood(Graphics g) {
			g.setColor(foodColor.darker());
			g.fillRect((int) location.getX(), (int) location.getY(), 10, 10);
			g.setColor(foodColor);
			g.fillRect((int) location.getX(), (int) location.getY(), 8, 8);

		}
	}
}
