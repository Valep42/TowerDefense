package engine.gui;

import engine.Gameplay;
import engine.Tower;
import engine.graphics.Sprite;

public class TowerButton extends Button {
	private Tower tower;

	public TowerButton(float x, float y, String unclickedButtonPath, String clickedButtonPath, Tower tower, Gameplay game) {
		super(x, y, unclickedButtonPath, clickedButtonPath, game);
		this.tower = tower;
	}

	@Override
	public void draw() {
		super.draw();
		Sprite s = this.tower.getSprite();
		s.setAlpha(0.8f);
		float scale = 0.4f;
		s.draw(this.x + (this.collisionWidth - s.getWidth() * scale) / 2, this.y + (this.collisionHeight - s.getHeight() * scale) / 2,
				0, scale);
	}

	@Override
	public void onClick() {
		super.onClick();
		this.game.setCurrentTower(this.tower);
		this.game.getCurrentTower().getSprite().setAlpha(0.5f);

	}

	public Tower getTower() {
		return this.tower;
	}

}
