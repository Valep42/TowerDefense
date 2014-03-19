package engine;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.fills.GradientFill;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;

import engine.graphics.Sprite;
import engine.gui.Button;
import engine.gui.GUI;
import engine.gui.InterfaceBackground;
import engine.gui.StaticText;
import engine.gui.TowerButton;

public class Game extends BasicGame {
	private List<Drawable> drawables;
	private List<GUI> guiElements;
	private List<Button> buttons;
	private boolean showFPS;
	private boolean mouseWasClicked;

	private MapLayout currentMapLayout;
	private int currentTileLength;
	private Button button1, button2;
	private Tower[][] towers;
	private TowerButton towerButton1;
	private Tower currentTower;
	private Enemy1 e;
	private static Player player;
	private StaticText lives;
	private static StaticText numberLives;
	private InterfaceBackground interfaceBackground;
	//hallo
	// Constants:
	public static int INTERFACE_START_X;
	public static int STANDARD_TEXT_SCALE = 15;

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
				new Sprite("roteBlutk_klein.png"), this));
		Game.player = new Player();
		this.lives = new StaticText(Game.INTERFACE_START_X + 5, 200, Color.black, "Lives:");

		this.towers = new Tower[12][13];

		this.drawables = new ArrayList<Drawable>();

		this.e = new Enemy1(this.currentMapLayout.getWaypoints());

		this.mouseWasClicked = false;
		this.showFPS = false;

		button1 = new Button(300, 300, "button1.png", "button2.png");
		button2 = new Button(200, 300, "button1.png", "button2.png");

		// add all objects that need to be drawn to the respectable arrays
		// entities
		this.drawables.add(new TestEntity(10, 10, 180, "A.bmp"));

		// GUI
		this.guiElements = new ArrayList<GUI>();
		numberLives = new StaticText(Game.INTERFACE_START_X + 50, 200, Color.black, "" + player.getLives());

		this.guiElements.add(this.interfaceBackground);
		this.guiElements.add(this.button1);
		this.guiElements.add(this.button2);
		this.guiElements.add(numberLives);
		this.guiElements.add(this.towerButton1);
		this.guiElements.add(this.lives);

		// Buttons; this has nothing to do with the draw sequence
		this.buttons = new ArrayList<Button>();
		this.buttons.add(this.button1);
		this.buttons.add(this.button2);
		this.buttons.add(this.towerButton1);

		//

		container.setShowFPS(this.showFPS);

	}

	@Override
	public void render(GameContainer container, Graphics graphics) throws SlickException {
		this.currentMapLayout.drawBackground();

		this.e.draw();
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
				graphics.draw(new Rectangle(newX * this.currentTileLength, newY * this.currentTileLength, this.currentTileLength,
						this.currentTileLength),
						new GradientFill(0, 0, Color.green, currentTileLength, currentTileLength, Color.green));
			} else {
				graphics.draw(new Rectangle(newX * this.currentTileLength, newY * this.currentTileLength, this.currentTileLength,
						this.currentTileLength),
						new GradientFill(0, 0, Color.red, currentTileLength, currentTileLength, Color.red));
			}
		}
		for (GUI guiElement : this.guiElements) {
			guiElement.draw();
		}

	}

	@Override
	public void update(GameContainer container, int delta) throws SlickException {

		this.e.update(delta);
		for(int i = 0; i < this.towers.length; ++i) {
			for(int j = 0; j < this.towers[0].length; ++j) {
				if(this.towers[i][j] != null) {
					this.towers[i][j].shoot();
				}
			}
		}

		this.mouseEvents(container, delta);

		if (Game.player.getLives() <= 0) {
			System.out.println("Game Over!");
		}

	}

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

	private void debugPath() {
		Sprite s = new Sprite("Unbenannt-2.png");
		int[][] path = this.currentMapLayout.getPath();
		for (int i = 0; i < path.length; ++i) {
			for (int j = 0; j < path[0].length; ++j) {
				if (path[i][j] == 5) {// for now th epath has not the value 0 in the array path, but 5
					s.draw(j * this.currentTileLength, i * this.currentTileLength);
				}
			}

		}
	}

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

	public Enemy getEnemy() {
		return this.e;
	}
	public static void reduceLives() {
		Game.player.reduceLives();
		numberLives.setText("" + Game.player.getLives());
	}

}
