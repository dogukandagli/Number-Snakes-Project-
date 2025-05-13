
public class Position {
	public int x;
	public int y;
	public boolean visited;
	
	public Position(int newX, int newY){
		this.x = newX;
		this.y = newY;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	
	
}
