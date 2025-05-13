
public class Player {
	private Position pos;
	private int trapCount = 0;
	private char symbol = 'P';
	private String name = "Player";
	private int score = 0;
	private int energy = 500;
	private int lifePoint = 1000;
	
	public Player(Position newPos) {
		this.pos = newPos;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	
	public Position getPos() {
		return pos;
	}

	public void setPos(Position pos) {
		this.pos = pos;
	}

	public int getTrapCount() {
		return trapCount;
	}

	public void setTrapCount(int trapCount) {
		this.trapCount = trapCount;
	}

	public char getSymbol() {
		return symbol;
	}

	public void setSymbol(char symbol) {
		this.symbol = symbol;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getEnergy() {
		return energy;
	}

	public void setEnergy(int energy) {
		this.energy = energy;
	}

	public int getLifePoint() {
		return lifePoint;
	}

	public void setLifePoint(int lifePoint) {
		this.lifePoint = lifePoint;
	}

}
