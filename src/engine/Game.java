package engine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

import engine.graphics.SlickRectangle;
import engine.graphics.SlickUnfilledEllipse;
import engine.graphics.SlickUnfilledRectangle;
import engine.graphics.Sprite;
import engine.gui.Button;
import engine.gui.GUI;
import engine.gui.InterfaceBackground;
import engine.gui.SlickHealthbar;
import engine.gui.StaticText;
import engine.gui.TowerButton;

/**
 * @author Pavel
 */
public class Game extends BasicGame {
	private List<Drawable> drawables;
	private List<GUI> guiElements;
	private List<Button> buttons;
	private ConcurrentLinkedQueue<Enemy> enemies;
	private WaveHandler waveHandler;
	private boolean mouseWasClicked = false;
	private boolean debugMode = false;

	private MapLayout currentMapLayout;
	private int currentTileLength;
	private Tower[][] towers;
	private TowerButton towerButton1;
	private Tower currentTower;
	// private Enemy1 e;
	private static Player player;
	private StaticText lives;
	private static StaticText numberLives;
	private InterfaceBackground interfaceBackground;
	// Constants:
	public static int INTERFACE_START_X;
	public static int STANDARD_TEXT_SCALE = 15;
	private float speed = 1f;

	// Tests:

	//

	public Game() {
		super("Tower Defense");
	}

	@Override
	public void init(GameContainer container) throws SlickException {

		this.currentMapLayout = new MapLayout("/maps/map.png", "/maps/background.jpg", 50);
		this.currentTileLength = this.currentMapLayout.getTileLength();

		// Set Constants:

		Game.INTERFACE_START_X = this.currentMapLayout.getNumberTilesWidth() * this.currentTileLength;
		//
		this.interfaceBackground = new InterfaceBackground("Interface1.png");
		this.towerButton1 = new TowerButton(13 * this.currentTileLength, 0, "button1.png", "button2.png", new ShootingTower(0, 0,
				new Sprite("tower/t1.png", 0.05f), this));
		Game.player = new Player();
		this.lives = new StaticText(Game.INTERFACE_START_X + 5, 200, Color.white, "Lives:");

		this.towers = new Tower[12][13];
		this.drawables = new ArrayList<Drawable>();
		this.enemies = new ConcurrentLinkedQueue<Enemy>();
		this.waveHandler = new WaveHandler(this, 5000);
		waveHandler.addWave(new Wave(3, new int[] { 100 }));
		waveHandler.addWave(new Wave(2, new int[] { 100 }));
		waveHandler.addWave(new Wave(1, new int[] { 100 }));


		// add all objects that need to be drawn to the respectable arrays
		// entities

		// GUI
		this.guiElements = new ArrayList<GUI>();
		numberLives = new StaticText(Game.INTERFACE_START_X + 50, 200, Color.white, "" + player.getLives());

		this.guiElements.add(this.interfaceBackground);
		this.guiElements.add(numberLives);
		this.guiElements.add(this.towerButton1);
		this.guiElements.add(this.lives);

		// Buttons; this has nothing to do with the draw sequence
		this.buttons = new ArrayList<Button>();
		this.buttons.add(this.towerButton1);

		//
		container.setShowFPS(this.debugMode);
	}

	@Override
	public void render(GameContainer container, Graphics graphics) throws SlickException {
		this.currentMapLayout.drawBackground();
		for (Enemy enemy : this.enemies) {
			if (enemy != null)
				enemy.draw();
		}
		for (Tower[] towerArray : this.towers) {
			for (Tower tower : towerArray) {
				if (tower != null) {
					tower.draw();
				}
			}
		}
		for (Drawable entity : this.drawables) {
			entity.draw();
		}

		this.renderTowerShadow(container, graphics);

		for (GUI guiElement : this.guiElements) {
			guiElement.draw();
		}
		for (Enemy enemy : this.enemies) {
			int barLength = 30;
			int barHeight = 7;
			SlickHealthbar h = new SlickHealthbar(graphics, enemy.getX() - barLength / 2, enemy.getY() - 25 - barHeight,
					enemy.getMaxHealth(), barLength, barHeight);
			h.setHealth(enemy.getHealth());
			h.setBordered(true);
			h.draw();
		}
		this.renderDebug(container, graphics);

	}

	/**
	 * renders the transparent version of the tower's sprite when choosing a place
	 * 
	 * @param container
	 * @param graphics
	 */
	private void renderTowerShadow(GameContainer container, Graphics graphics) {

		if (this.currentTower != null) {
			Sprite sprite = this.currentTower.getSprite();
			Input input = container.getInput();
			float x = input.getMouseX();
			float y = input.getMouseY();
			sprite.draw(x - sprite.getWidth() / 2, y - sprite.getHeight() / 2);
			int newX = (int) x / this.currentTileLength;
			int newY = (int) y / this.currentTileLength;
			int[][] path = this.currentMapLayout.getPath();
			if (x < Game.INTERFACE_START_X && path[newY][newX] == 1 && towers[newY][newX] == null) {
				new SlickUnfilledRectangle(graphics, 50, 50, Color.green).draw(newX * this.currentTileLength, newY
						* this.currentTileLength);
			} else {
				new SlickUnfilledRectangle(graphics, 50, 50, Color.red).draw(newX * this.currentTileLength, newY
						* this.currentTileLength);
			}
		}
	}

	/**
	 * renders circles for tower radius and enemy radius and black box for FPS
	 * 
	 * @param container
	 * @param graphics
	 */
	private void renderDebug(GameContainer container, Graphics graphics) {
		if (this.debugMode) {
			for (Enemy enemy : this.enemies) {
				new SlickUnfilledEllipse(graphics, enemy.getRadius() * 2, enemy.getRadius() * 2, Color.blue).draw(enemy.getX(),
						enemy.getY());
			}
			for (int i = 0; i < this.towers.length; ++i) {
				for (int j = 0; j < this.towers[0].length; ++j) {
					if (this.towers[i][j] != null) {
						Tower currentTower = this.towers[i][j];
						new SlickUnfilledEllipse(graphics, currentTower.getRadius() * 2, currentTower.getRadius() * 2, Color.red)
								.draw(currentTower.getX() * this.currentTileLength + 25, currentTower.getY() * this.currentTileLength
										+ 25);
					}
				}
			}
			// create a black box that the FPS are visible
			new SlickRectangle(graphics, 100, 20, Color.black).draw(5, 10);
		}
	}

	@Override
	public void update(GameContainer container, int originalDelta) throws SlickException {
		int delta = (int) (originalDelta * this.speed);
		for (Enemy enemy : this.enemies) {
			if (delta > 100) {
				delta = 0;
			}
			if (enemy != null)
				enemy.update(delta);
		}
		for (int i = 0; i < this.towers.length; ++i) {
			for (int j = 0; j < this.towers[0].length; ++j) {
				if (this.towers[i][j] != null) {
					this.towers[i][j].shoot();
				}
			}
		}

		this.waveHandler.update(delta);

		this.mouseEvents(container, delta);
		this.keyboardEvents(container, delta);

		if (Game.player.getLives() <= 0) {
			System.out.println("Game Over!");
		}

	}

	/**
	 * updates keyboard events i.e. button presses
	 * 
	 * @param container
	 * @param delta
	 */
	private void keyboardEvents(GameContainer container, int delta) {
		Input input = container.getInput();

		if (input.isKeyPressed(Input.KEY_I)) {
			this.debugMode = !this.debugMode;
			container.setShowFPS(this.debugMode);
			if (this.debugMode) {
				System.out.println("debug");
			} else {
				System.out.println("not debug");
				this.speed = 1f;
			}
		}
		if (this.debugMode) {
			this.debugKeyboardEvents(container, delta);
		}

	}

	/**
	 * updates the keyboard events(button presse) only occuring in debug mode
	 * 
	 * @param container
	 * @param delta
	 */
	private void debugKeyboardEvents(GameContainer container, int delta) {
		Input input = container.getInput();
		if (input.isKeyPressed(Input.KEY_ADD)) {
			this.speed *= 2f;
		}
		if (input.isKeyPressed(Input.KEY_SUBTRACT)) {
			this.speed /= 2f;
		}
		if (input.isKeyPressed(Input.KEY_L)) {
			enemies.add(new Enemy1(this.currentMapLayout.getWaypoints(), this));
		}
	}

	/**
	 * handles mouse events like clicks
	 * 
	 * @param container
	 * @param delta
	 */
	private void mouseEvents(GameContainer container, int delta) {
		Input input = container.getInput();
		if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {

			float x = input.getMouseX();
			float y = input.getMouseY();

			boolean buttonWasPressed = false;
			for (Button button : this.buttons) {
				if (button.checkCollision(x, y)) {
					buttonWasPressed = true;
					this.releaseAllButtons();
					button.onClick();
					this.currentTower = button.getTower();
					this.currentTower.getSprite().setAlpha(0.5f);
					if (this.currentTower != null) {
						this.towerButton1.onClick();
					}
				}
			}
			if (!buttonWasPressed) {
				int newX = (int) x / this.currentTileLength;
				int newY = (int) y / this.currentTileLength;
				if (this.currentTower != null) {
					int[][] path = this.currentMapLayout.getPath();
					if (x < Game.INTERFACE_START_X && path[newY][newX] == 1 && towers[newY][newX] == null) {
						Tower bufferTower = this.currentTower.clone();
						bufferTower.setX(newX);
						bufferTower.setY(newY);
						bufferTower.getSprite().setAlpha(1f);
						this.towers[newY][newX] = bufferTower;

						this.currentTower = null;
						this.releaseAllButtons();

					}
				}
			}
			this.mouseWasClicked = true;

		}
		// checks if mouse button was released again after being pressed
		if (this.mouseWasClicked && !input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {

			this.mouseWasClicked = false;
			this.releaseAllButtonsNotTowerButtons();

		}
	}

	/*
	 * private void debugPath() { Sprite s = new Sprite("Unbenannt-2.png"); int[][] path = this.currentMapLayout.getPath(); for (int i
	 * = 0; i < path.length; ++i) { for (int j = 0; j < path[0].length; ++j) { if (path[i][j] == 5) {// for now th epath has not the
	 * value 0 in // the array path, but 5 s.draw(j * this.currentTileLength, i * this.currentTileLength); } }
	 * 
	 * } }
	 */

	private void releaseAllButtons() {
		for (Button button : this.buttons) {
			button.onRelease();
		}
	}

	private void releaseAllButtonsNotTowerButtons() {
		for (Button button : this.buttons) {
			if (button.getTower() == null) {
				button.onRelease();
			}
		}
	}

	public ConcurrentLinkedQueue<Enemy> getEnemies() {
		return (ConcurrentLinkedQueue<Enemy>) enemies;
	}

	public Waypoint getWaypoints() {
		return currentMapLayout.getWaypoints();
	}

	public static void reduceLives() {
		Game.player.reduceLives();
		numberLives.setText("" + Game.player.getLives());
	}

}
