
public class CRobot {
	private Position pos;
	private Position targetPos;
	private char symbol = 'C';
	
	public CRobot(Position newPos) {
		this.pos = newPos;
	}
	
	public void calcTarget() {
		
	}

	public Position getPos() {
		return pos;
	}

	public void setPos(Position pos) {
		this.pos = pos;
	}

	public Position getTargetTreasure() {
		return targetPos;
	}

	public void setTargetTreasure(Position targetTreasure) {
		this.targetPos = targetTreasure;
	}
	
	public char getSymbol() {
		return this.symbol;
	}

	public void setSymbol(char newSymbol) {
		this.symbol = newSymbol;
	}
	
}
