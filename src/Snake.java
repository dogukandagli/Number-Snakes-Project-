
public class Snake {
	private Position pos;
	private Position targetPos;
	public Boolean haveTarget=false;
	public Boolean randomMode=false;
	public int randomDirection=(int)(Math.random()*4);
	public int randomMoveCounter=0;
	public Node head;
	public SingleLinkedList positionLinkedList;
	public SingleLinkedList collactibleLinkedList;
	 int currentDirection;
	public Snake(Position newPos) {
		this.pos= new Position(newPos.x, newPos.y);
		positionLinkedList = new SingleLinkedList();
		collactibleLinkedList = new SingleLinkedList();
	}

	public void setTarget(Position targetPos) {
    this.targetPos = new Position(targetPos.x, targetPos.y);
    haveTarget = true;
	}

	public Position getPos() {
		return pos;
	}

	public void setPos(Position pos) {
		this.pos = new Position(pos.x, pos.y);
	}

	public Position getTargetPos() {
		return targetPos;
	}
}
