
import enigma.core.Enigma;
import enigma.event.TextMouseEvent;
import enigma.event.TextMouseListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import enigma.console.TextAttributes;
import java.awt.Color;

public class Game {
	enigma.console.Console cn = Enigma.getConsole("Mouse and Keyboard", 75, 25, 16);
	TextMouseListener tmlis;
	KeyListener klis;

	private char[][] board = readBoardFromFile("src/maze.txt");
	private Player player = spawnPlayer();
	private CRobot cRobot = spawnCRobot();
	private Snake[] snakes = new Snake[15];
	private String computerName = "Computer";
	private int computerScore = 0;
	private char[] collectables;
	private int timeUnit = 100;
	private int totalTime = 0;
	private CircularQueue inputQueue = new CircularQueue(15);
	private Position[] traps = new Position[100];
	private int trapsMaxCount = 0;
	private int[] trapsTimes = new int[100];
	
	private int count;//haci neyin count u

	Stack stack;//neyin stack ı
	Stack stack2;
	private int lesnekocounte = 0;//bu ne demek:D

	// ------ Standard variables for mouse and keyboard ------
	public int mousepr; // mouse pressed?
	public int mousex, mousey; // mouse text coords.
	public int keypr; // key pressed?
	public int rkey; // key (for press/release)
	// -------------------------------------------------------

	Game() throws Exception {
		// ------ Standard code for mouse and keyboard ------
		tmlis = new TextMouseListener() {
			public void mouseClicked(TextMouseEvent arg0) {
			}

			public void mousePressed(TextMouseEvent arg0) {
				if (mousepr == 0) {
					mousepr = 1;
					mousex = arg0.getX();
					mousey = arg0.getY();
				}
			}

			public void mouseReleased(TextMouseEvent arg0) {
			}
		};
		cn.getTextWindow().addTextMouseListener(tmlis);

		klis = new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				if (keypr == 0) {
					keypr = 1;
					rkey = e.getKeyCode();
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		};
		cn.getTextWindow().addKeyListener(klis);
		// ---------------------------------------------------

		// first 30 element throw to the maze
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 15; j++) {
				enqueueInput();
				Position rPos = giveUniqPos();
				if ((char) inputQueue.peek() == 'S') {
					snakes[lesnekocounte] = new Snake(rPos);
					lesnekocounte++;
				}
				this.board[rPos.y][rPos.x] = (char) inputQueue.dequeue();
			}
		}

		this.board[cRobot.getPos().y][cRobot.getPos().x] = cRobot.getSymbol();

		// fill 15
		for (int j = 0; j < 15; j++) {
			enqueueInput();
		}

		// main game loop
		pathLine();
		while (true) {
			addTrapsToBoard();
			printMap();
			if ((stack2.isEmpty()))
				pathLine();

			if (totalTime % (4 * timeUnit) == 0) {
				//check snake pos for trap
				boolean hardBreak = false;
				for (int i = 0; i < this.trapsMaxCount; i++) {
					if (this.traps[i] != null) {
						for (int j = 0; j < this.snakes.length; j++) {
							if (this.snakes[j] != null) {
								if (Math.abs(this.traps[i].x - this.snakes[j].getPos().x) <= 1 && Math.abs(this.traps[i].y - this.snakes[j].getPos().y) <= 1) {
									removeTrap(i);
									removeSnake(j);
									this.player.setEnergy(this.player.getEnergy()+500);
									this.player.setScore(this.player.getScore()+200);
									hardBreak = true;
									break;
								}
							}
						}
						if (hardBreak) {
							break;
						}
					}
				}
				updateSposition();
			}
			if (mousepr == 1) {

				mousepr = 0;
			}

			if (keypr == 1) {
				if (rkey == KeyEvent.VK_SPACE) {
					putTrap();
				} else {
					if (player.getEnergy() > 0) {
						if (totalTime % timeUnit == 0) {
							updatePlayerPos();
						}
					} else {
						if (totalTime % (2 * timeUnit) == 0) {
							updatePlayerPos();
						}
					}
				}

				keypr = 0;
			}

			if (totalTime % (4 * timeUnit) == 0) {
				movementCrobot();
			}

			if (totalTime % (20 * timeUnit) == 0) {
				Position rPos = giveUniqPos();
				if ((char) inputQueue.peek() == 'S') {
					snakes[lesnekocounte] = new Snake(rPos);
					lesnekocounte++;
				}
				this.board[rPos.y][rPos.x] = (char) inputQueue.dequeue();
				enqueueInput();
			}

			if(totalTime % (10*timeUnit) == 0) {
				for(int i = 0; i < this.trapsMaxCount; i++) {
					if (this.traps[i] != null) {
						if (this.trapsTimes[i]-- == 0) {
							this.board[this.traps[i].y][this.traps[i].x] = ' ';
							this.traps[i] = null;
						}
					}
				}
			}
			
			if (player.getEnergy() > 0) {
				player.setEnergy(player.getEnergy() - 1);
			}

			Thread.sleep(timeUnit);
			totalTime += timeUnit;
		}
	}

	public Position createRandomPos() {
		int yPos = (int) ((Math.random() * (22 - 0)) + 0);
		int xPos = (int) ((Math.random() * (55 - 0)) + 0);
		Position rPos = new Position(xPos, yPos);
		return rPos;
	}

	public Player spawnPlayer() {
		Position rPos = giveUniqPos();
		Player player = new Player(rPos);
		this.board[rPos.y][rPos.x] = 'P';
		// drawThatPos(player.getPos(),'P');
		// updateGame(player.getPos(),player.getSymbol());
		return player;
	}

	public CRobot spawnCRobot() {
		Position rPos = createRandomPos();
		while (checkWall(rPos)) {
			rPos = createRandomPos();
		}
		CRobot cRobot = new CRobot(rPos);
		return cRobot;
	}

	public void removeSnake(int entityNo) {
		Snake bumSnake = this.snakes[entityNo];
		this.board[bumSnake.getPos().y][bumSnake.getPos().x] = ' ';
		Node temp = bumSnake.positionLinkedList.getHead();
		while (temp != null) {
			Position partPos = (Position)temp.getData();
			this.board[partPos.y][partPos.x] = ' ';
			temp = temp.getLink();
		}
		this.snakes[entityNo] = null;
	}

	public void removeTrap(int entityNo) {
		this.board[this.traps[entityNo].y][this.traps[entityNo].x] = ' ';
		this.traps[entityNo] = null;
		this.trapsTimes[entityNo] = 0;
	}
	
	public char[][] readBoardFromFile(String fileName) {
		char[][] board = new char[23][55];
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(fileName));
			for (int i = 0; i < 23; i++) {
				String line = scanner.nextLine();
				for (int j = 0; j < 55; j++) {
					board[i][j] = line.charAt(j);
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("Dosya bulunamadı: " + fileName);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		return board;
	}

	public Position giveUniqPos() {
		Position rPos = createRandomPos();
		while (checkWall(rPos) || checkWalkers(rPos) || checkActiveTraps(rPos)) {
			rPos = createRandomPos();
		}
		return rPos;
	}

	public int check3Wall(Position pos) {
		int count = 0;
		int i = 0;
		int y = pos.y;
		int x = pos.x;
		if (checkWall(new Position(x, y + 1)) || checkWalkers3(new Position(x, y + 1)))
			count++;
		i += 3;
		if (checkWall(new Position(x, y - 1)) || checkWalkers3(new Position(x, y - 1)))
			count++;
		i += 2;
		if (checkWall(new Position(x + 1, y)) || checkWalkers3(new Position(x + 1, y)))
			count++;
		if (checkWall(new Position(x - 1, y)) || checkWalkers3(new Position(x - 1, y)))
			count++;
		i += 1;

		if (count == 3) {
			if (i == 6)
				return 0;
			if (i == 5)
				return 1;
			if (i == 4)
				return 2;
			if (i == 3)
				return 3;
		}
		return 5;
	}

	public boolean checkWall(Position pos) {
		if (this.board[pos.y][pos.x] == '#') {
			return true;
		} else {
			return false;
		}
	}

	public boolean checkWalkers(Position pos) {
		if (this.board[pos.y][pos.x] == 'P' || this.board[pos.y][pos.x] == 'C' || this.board[pos.y][pos.x] == 'S') {
			return true;
		} else {
			return false;
		}
	}

	public boolean checkWalkers2(Position pos) {
		if (this.board[pos.y][pos.x] == 'P' || this.board[pos.y][pos.x] == 'S') {
			return true;
		} else {
			return false;
		}
	}

	public boolean checkWalkers3(Position pos) {
		if (this.board[pos.y][pos.x] == 'P' || this.board[pos.y][pos.x] == 'C') {
			return true;
		} else {
			return false;
		}
	}

	public boolean checkActiveTraps(Position pos) {
		if (this.board[pos.y][pos.x] == '=') {
			return true;
		} else {
			return false;
		}
	}

	public char checkCollectible(Position pos) {
		if (pos != null) {
			if (this.board[pos.y][pos.x] == '1' || this.board[pos.y][pos.x] == '2' || this.board[pos.y][pos.x] == '3'
					|| this.board[pos.y][pos.x] == '@') {
				return this.board[pos.y][pos.x];
			} else {
				return 0;
			}
		}
		return 0;

	}

	public char checkCollectible2(Position pos) {
		if (pos != null) {
			if (this.board[pos.y][pos.x] == '1' || this.board[pos.y][pos.x] == '2' || this.board[pos.y][pos.x] == '3') {
				return this.board[pos.y][pos.x];
			} else {
				return 0;
			}
		}
		return 0;

	}
	
	public boolean checkTail(Position pos) {
		for (int i = 0; i< this.snakes.length; i++) {
			if (this.snakes[i] != null) {
				Snake nextSnake = this.snakes[i];
				Node temp = nextSnake.positionLinkedList.getHead();
				while (temp != null) {
					Position partPos = (Position)temp.getData();
					if (partPos.x == pos.x && partPos.y == pos.y) {
						return true;
					}
					temp = temp.getLink();
				}
			}
		}
		return false;
	}

	public void updatePlayerPos() {
		int xDirection = 0, yDirection = 0;
		if (rkey == KeyEvent.VK_LEFT)
			xDirection = -1;
		else if (rkey == KeyEvent.VK_RIGHT)
			xDirection = 1;
		else if (rkey == KeyEvent.VK_UP)
			yDirection = -1;
		else if (rkey == KeyEvent.VK_DOWN)
			yDirection = 1;
		else {
			return;
		}

		this.board[player.getPos().y][player.getPos().x] = ' ';
		Position newPlayerPos = new Position(player.getPos().x + xDirection, player.getPos().y + yDirection);
		if (!checkWall(newPlayerPos) && !checkWalkers(newPlayerPos) && !checkTail(newPlayerPos)) {
			char collectible = checkCollectible(newPlayerPos);
			if (collectible != 0) {
				if (collectible == '@') {
					player.setTrapCount(player.getTrapCount() + 1);
				} else {
					int treasure = Character.getNumericValue(collectible);
					player.setScore(player.getScore() + (int) Math.pow(treasure, 2));
					player.setEnergy(player.getEnergy() + 50 + (treasure - 1) * (100));
				}
			}
			player.setPos(newPlayerPos);
		}
		this.board[player.getPos().y][player.getPos().x] = 'P';
	}

	public void putTrap() {
		if (player.getTrapCount() > 0) {
			player.setTrapCount(player.getTrapCount()-1);
			this.traps[this.trapsMaxCount] = player.getPos();
			this.trapsTimes[this.trapsMaxCount] = 10;
			this.trapsMaxCount++;
		}
	}

	public void addTrapsToBoard() {
		for (int i = 0; i < this.trapsMaxCount; i++) {
			if (this.traps[i] != null) {// for sign deactived traps, they still in memory cuz i lazy to write basic
										// garbage collector
				if (this.board[this.traps[i].y][this.traps[i].x] != 'P') {
					this.board[this.traps[i].y][this.traps[i].x] = '=';
				}
			}
		}
	}

	public void enqueueInput() {
		int a = (int) (Math.random() * 100);
		if (a <= 50) {
			inputQueue.enqueue('1');
		} else if (50 < a && a <= 75) {
			inputQueue.enqueue('2');
		} else if (75 < a && a <= 88) {
			inputQueue.enqueue('3');
		} else if (88 < a && a <= 97) {
			inputQueue.enqueue('@');
		} else if (97 < a && a <= 100) {
			inputQueue.enqueue('S');
		}
	}

	public void drawThatPos(Position pos, char symbol) {// depreceted for now
		int oldX = cn.getTextWindow().getCursorX();
		int oldY = cn.getTextWindow().getCursorY();
		cn.getTextWindow().setCursorPosition(pos.x, pos.y);
		System.out.print(symbol);
		cn.getTextWindow().setCursorPosition(oldY, oldX);
	}

	public void movementCrobot() {

		if (!stack2.isEmpty()) {

			Position position = (Position) stack2.pop();
			if (!stack2.isEmpty()) {

				this.board[position.y][position.x] = ' ';
				Position newPosition = (Position) stack2.peek();

				char collectible = checkCollectible(newPosition);
				if (collectible != 0) {
					if (collectible == '@') {
						computerScore += 50;
					} else {

						int treasure = Character.getNumericValue(collectible);
						computerScore += (int) Math.pow(treasure, 2);
					}
				}

				cRobot.setPos(newPosition);
				this.board[newPosition.y][newPosition.x] = 'C';
			}
		}

	}

	public void pathLine() {
		int count = 0;

		cRobot.setTargetTreasure(createRandomPos());

		while (checkCollectible(cRobot.getTargetTreasure()) == 0) {
			cRobot.setTargetTreasure(createRandomPos());
		}

		Stack stack = pathFinding();

		while (count < 3500) {
			int a = stack.size();
			Stack stack2 = pathFinding();
			if (stack2.size() < a) {
				stack = stack2;
				a = stack2.size();
			}

			count++;
		}

		stack2 = new Stack(stack.size());
		while (!stack.isEmpty()) {
			Position position = (Position) stack.pop();
			stack2.push(position);

			if (!position.equals(cRobot.getPos()) && !position.equals(cRobot.getTargetTreasure())
					&& checkCollectible(position) == 0 && !checkWalkers2(position)) {
				board[position.y][position.x] = '.';
			}

		}
	}

	public Stack pathFinding() {
		Stack stack = new Stack(1265);
		boolean[][] visited = new boolean[23][55];
		int initialX = cRobot.getPos().x;
		int initialY = cRobot.getPos().y;
		Position position = new Position(initialX, initialY);

		stack.push(cRobot.getPos());
		visited[initialY][initialX] = true;

		while (!stack.isEmpty()) {
			position = (Position) stack.peek();
			if (position.x == cRobot.getTargetTreasure().x && position.y == cRobot.getTargetTreasure().y) {
				break;
			}
			Position newPosition = unvisitedNeighbor(position, visited);
			if (newPosition != null) {
				visited[newPosition.y][newPosition.x] = true;
				stack.push(newPosition);

			} else {
				stack.pop();
			}

		}
		return stack;
	}

	public Position unvisitedNeighbor(Position position, boolean visited[][]) {
		Position[] position2 = new Position[4];
		count = 0;

		while (count < 4) {

			int dir = (int) (Math.random() * 4);
			int newX = position.x;
			int newY = position.y;

			switch (dir) {
			case 0:
				newX += 1;
				break;
			case 1:
				newX -= 1;
				break;
			case 2:
				newY += 1;
				break;
			case 3:
				newY -= 1;
				break;
			}

			Position newPosition = new Position(newX, newY);
			if (isNeighbor(newPosition, position2)) {
				position2[count] = newPosition;
				count++;
			}

		}

		for (int i = 0; i < position2.length; i++) {
			if (!visited[position2[i].y][position2[i].x] && !checkWall(position2[i]) && !checkWalkers2(position2[i])) {
				return position2[i];

			}
		}
		return null;

	}

	public boolean isNeighbor(Position position, Position[] position2) {
		for (int i = 0; i < position2.length; i++) {
			if (position2[i] != null) {
				if (position.x == position2[i].x && position.y == position2[i].y) {
					return false;
				}
			}
		}
		return true;
	}

	public void updateSposition() {

		for (int i = 0; i < snakes.length; i++) {
			if (snakes[i] != null && !snakes[i].haveTarget) {
				Position rPos;
				do {
					rPos = createRandomPos();
				} while (checkCollectible2(rPos) == 0 || checkWall(rPos) || checkWalkers(rPos));
				snakes[i].setTarget(rPos);
			}
		}
		for (int i = 0; i < snakes.length; i++) {

			if (snakes[i] != null && snakes[i].haveTarget) {
				if (check3Wall(snakes[i].getPos()) != 5) {
					snakeReserving(snakes[i]);
					snakes[i].currentDirection = (snakes[i].currentDirection + 2) % 4;
					snakes[i].randomMode=true;
				}

				if (snakes[i].positionLinkedList != null) {
					board[snakes[i].getPos().y][snakes[i].getPos().x] = ' ';
				}
				if (!snakes[i].randomMode) {
					if (snakes[i].getPos().x < snakes[i].getTargetPos().x
							&& !(board[snakes[i].getPos().y][snakes[i].getPos().x + 1] == '#'
									|| board[snakes[i].getPos().y][snakes[i].getPos().x + 1] == 'P'
									|| board[snakes[i].getPos().y][snakes[i].getPos().x + 1] == 'C')) {
						Position newSnakePos = new Position(snakes[i].getPos().x + 1, snakes[i].getPos().y);
						boolean controlEating = eating(newSnakePos.y, newSnakePos.x, snakes[i].collactibleLinkedList,
								snakes[i].positionLinkedList);
						if (!controlEating) {
							updateTailPositions(snakes[i].getPos(), snakes[i].positionLinkedList);
						} else {
							updateTailPositionsEating(snakes[i].getPos(), snakes[i].positionLinkedList);
						}
						snakes[i].setPos(newSnakePos);
						snakes[i].currentDirection = 0;
					} else if (snakes[i].getPos().x > snakes[i].getTargetPos().x
							&& !(board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == '#'
									|| board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == 'P'
									|| board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == 'C')) {
						Position newSnakePos = new Position(snakes[i].getPos().x - 1, snakes[i].getPos().y);
						boolean controlEating = eating(newSnakePos.y, newSnakePos.x, snakes[i].collactibleLinkedList,
								snakes[i].positionLinkedList);
						if (!controlEating) {
							updateTailPositions(snakes[i].getPos(), snakes[i].positionLinkedList);
						} else {
							updateTailPositionsEating(snakes[i].getPos(), snakes[i].positionLinkedList);
						}
						snakes[i].setPos(newSnakePos);	
						snakes[i].currentDirection = 1;
					} else if (snakes[i].getPos().y < snakes[i].getTargetPos().y
							&& !(board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == '#'
									|| board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == 'P'
									|| board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == 'C')) {
						Position newSnakePos = new Position(snakes[i].getPos().x, snakes[i].getPos().y + 1);
						boolean controlEating = eating(newSnakePos.y, newSnakePos.x, snakes[i].collactibleLinkedList,
								snakes[i].positionLinkedList);
						if (!controlEating) {
							updateTailPositions(snakes[i].getPos(), snakes[i].positionLinkedList);
						} else {
							updateTailPositionsEating(snakes[i].getPos(), snakes[i].positionLinkedList);
						}
						snakes[i].setPos(newSnakePos);
						snakes[i].currentDirection = 2;
					} else if (snakes[i].getPos().y > snakes[i].getTargetPos().y
							&& !(board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == '#'
									|| board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == 'P'
									|| board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == 'C')) {
						Position newSnakePos = new Position(snakes[i].getPos().x, snakes[i].getPos().y - 1);
						boolean controlEating = eating(newSnakePos.y, newSnakePos.x, snakes[i].collactibleLinkedList,
								snakes[i].positionLinkedList);
						if (!controlEating) {
							updateTailPositions(snakes[i].getPos(), snakes[i].positionLinkedList);
						} else {
							updateTailPositionsEating(snakes[i].getPos(), snakes[i].positionLinkedList);
						}
						snakes[i].setPos(newSnakePos);
						snakes[i].currentDirection = 3;

					} else
						snakes[i].randomMode = true;
				}

				if (snakes[i].randomMode) {

					int changeDirect = (int) (Math.random() * 6);

					snakes[i].randomDirection = snakes[i].currentDirection;
					if (snakes[i].randomDirection == 0 && (board[snakes[i].getPos().y][snakes[i].getPos().x + 1] == '#'
							|| board[snakes[i].getPos().y][snakes[i].getPos().x + 1] == 'P'
							|| board[snakes[i].getPos().y][snakes[i].getPos().x + 1] == 'C')) {
						changeDirect = 0;
					} else if (snakes[i].randomDirection == 1
							&& (board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == '#'
									|| board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == 'P'
									|| board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == 'C')) {
						changeDirect = 0;
					} else if (snakes[i].randomDirection == 2
							&& (board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == '#'
									|| board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == 'P'
									|| board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == 'C')) {
						changeDirect = 0;
					} else if (snakes[i].randomDirection == 3
							&& (board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == '#'
									|| board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == 'P'
									|| board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == 'C')) {
						changeDirect = 0;
					}

					if (changeDirect == 0) {

						int newDirection = 0;
						do {
							newDirection = (int) (Math.random() * 4);
						} while ((newDirection == 0 && snakes[i].randomDirection == 1)
								|| (newDirection == 1 && snakes[i].randomDirection == 0)
								|| (newDirection == 2 && snakes[i].randomDirection == 3)
								|| (newDirection == 3 && snakes[i].randomDirection == 2)
								|| (newDirection == 0 && (board[snakes[i].getPos().y][snakes[i].getPos().x + 1] == '#'
										|| board[snakes[i].getPos().y][snakes[i].getPos().x + 1] == 'P'
										|| board[snakes[i].getPos().y][snakes[i].getPos().x + 1] == 'C'))
								|| newDirection == 1 && (board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == '#'
										|| board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == 'P'
										|| board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == 'C')
								|| (newDirection == 2 && (board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == '#'
										|| board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == 'P'
										|| board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == 'C'))
								|| (newDirection == 3 && (board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == '#'
										|| board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == 'P'
										|| board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == 'C')));
						snakes[i].randomDirection = newDirection;
					}

					if (snakes[i].randomDirection == 0 && !(board[snakes[i].getPos().y][snakes[i].getPos().x + 1] == '#'
							|| board[snakes[i].getPos().y][snakes[i].getPos().x + 1] == 'P'
							|| board[snakes[i].getPos().y][snakes[i].getPos().x + 1] == 'C')) {
						Position newSnakePos = new Position(snakes[i].getPos().x + 1, snakes[i].getPos().y);
						boolean controlEating = eating(newSnakePos.y, newSnakePos.x, snakes[i].collactibleLinkedList,
								snakes[i].positionLinkedList);
						if (!controlEating) {
							updateTailPositions(snakes[i].getPos(), snakes[i].positionLinkedList);
						} else {
							updateTailPositionsEating(snakes[i].getPos(), snakes[i].positionLinkedList);
						}
						snakes[i].setPos(newSnakePos);
						snakes[i].currentDirection = 0;
					} else if (snakes[i].randomDirection == 1
							&& !(board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == '#'
									|| board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == 'P'
									|| board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == 'C')) {
						Position newSnakePos = new Position(snakes[i].getPos().x - 1, snakes[i].getPos().y);
						boolean controlEating = eating(newSnakePos.y, newSnakePos.x, snakes[i].collactibleLinkedList,
								snakes[i].positionLinkedList);
						if (!controlEating) {
							updateTailPositions(snakes[i].getPos(), snakes[i].positionLinkedList);
						} else {
							updateTailPositionsEating(snakes[i].getPos(), snakes[i].positionLinkedList);
						}
						snakes[i].setPos(newSnakePos);
						snakes[i].currentDirection = 1;
					} else if (snakes[i].randomDirection == 2
							&& !(board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == '#'
									|| board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == 'P'
									|| board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == 'C')) {
						Position newSnakePos = new Position(snakes[i].getPos().x, snakes[i].getPos().y + 1);
						boolean controlEating = eating(newSnakePos.y, newSnakePos.x, snakes[i].collactibleLinkedList,
								snakes[i].positionLinkedList);
						if (!controlEating) {
							updateTailPositions(snakes[i].getPos(), snakes[i].positionLinkedList);
						} else {
							updateTailPositionsEating(snakes[i].getPos(), snakes[i].positionLinkedList);
						}
						snakes[i].setPos(newSnakePos);
						snakes[i].currentDirection = 2;
					} else if (snakes[i].randomDirection == 3
							&& !(board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == '#'
									|| board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == 'P'
									|| board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == 'C')) {
						Position newSnakePos = new Position(snakes[i].getPos().x, snakes[i].getPos().y - 1);
						boolean controlEating = eating(newSnakePos.y, newSnakePos.x, snakes[i].collactibleLinkedList,
								snakes[i].positionLinkedList);
						if (!controlEating) {
							updateTailPositions(snakes[i].getPos(), snakes[i].positionLinkedList);
						} else {
							updateTailPositionsEating(snakes[i].getPos(), snakes[i].positionLinkedList);
						}
						snakes[i].setPos(newSnakePos);
						snakes[i].currentDirection = 3;
					}
					if (snakes[i].randomMoveCounter == 25) {
						snakes[i].randomMode = false;
						snakes[i].randomMoveCounter = 0;
					} else {
						snakes[i].randomMoveCounter++;
					}
				}
				board[snakes[i].getPos().y][snakes[i].getPos().x] = 'S';
				showSnakeTail(snakes[i].collactibleLinkedList, snakes[i].positionLinkedList);

			}

		}

	}

	public boolean eating(int y, int x, SingleLinkedList list, SingleLinkedList list2) {
		char point = this.board[y][x];

		if (point == '1' || point == '2' || point == '3') {
			if (list2 != null) {
				Node current = list2.getHead();
				while (current != null) {
					Position position2 = (Position) current.getData();
					if (position2.x == x && position2.y == y) {
						return false;
					}
					current = current.getLink();
				}
			}
			list.add(point);
			list2.add(new Position(y, x));
			return true;
		}
		return false;
	}

	public void updateTailPositions(Position newTailPos, SingleLinkedList coordinates) {
		if (coordinates.getHead() == null)
			return;

		if (coordinates != null) {
			Node current = coordinates.getHead();

			Position temp = new Position(newTailPos.x, newTailPos.y);
			Position nextTemp;

			while (current != null) {
				nextTemp = (Position) current.getData();
				current.setData(new Position(temp.x, temp.y));

				temp = nextTemp;
				current = current.getLink();
			}

			board[temp.y][temp.x] = ' ';
		}
	}

	public void updateTailPositionsEating(Position newTailPos, SingleLinkedList coordinates) {
		if (coordinates.getHead() == null)
			return;

		Node current = coordinates.getHead();
		current.setData(newTailPos);

	}

	public void showSnakeTail(SingleLinkedList kuyruk, SingleLinkedList coordinates) {
		Node coordNode = coordinates.getHead();
		Node dataNode = kuyruk.getHead();
		while (coordNode != null && dataNode != null) {
			Position pos = (Position) coordNode.getData();
			if (pos.x >= 0 && pos.x < board[0].length && pos.y >= 0 && pos.y < board.length) {
				board[pos.y][pos.x] = (char) dataNode.getData(); // Kuyruktaki karakteri tahtaya yerleştir
			}

			coordNode = coordNode.getLink();
			dataNode = dataNode.getLink();
		}
	}

	public void snakeReserving(Snake snake) {

		if (snake.positionLinkedList != null) {
			Node current = snake.positionLinkedList.getHead();

			Position temp = new Position(snake.getPos().x, snake.getPos().y);
			Position nextTemp;

			while (current != null) {
				nextTemp = (Position) current.getData();
				current.setData(new Position(temp.x, temp.y));

				temp = nextTemp;
				current = current.getLink();
			}

			snake.setPos(temp);
			board[temp.y][temp.x] = 'S';
		}

		if (snake.collactibleLinkedList != null) {
			Node prev = null;
			Node current = snake.collactibleLinkedList.getHead();
			Node next = null;

			while (current != null) {
				next = current.getLink();
				current.setLink(prev);
				prev = current;
				current = next;
			}

			snake.collactibleLinkedList.setHead(prev);

		}
		if (snake.positionLinkedList != null) {
			Node prev = null;
			Node current = snake.positionLinkedList.getHead();
			Node next = null;

			while (current != null) {
				next = current.getLink();
				current.setLink(prev);
				prev = current;
				current = next;
			}

			snake.positionLinkedList.setHead(prev);

		}
		showSnakeTail(snake.collactibleLinkedList, snake.positionLinkedList);

	}

	public void printMap() {
		cn.getTextWindow().setCursorPosition(0, 0);
		for (int i = 0; i < 23; i++) {
			for (int j = 0; j < 55; j++) {
				System.out.print(this.board[i][j]);
			}
			System.out.println();
		}
		cn.getTextWindow().setCursorPosition(57, 0);
		System.out.print("Input");
		cn.getTextWindow().setCursorPosition(57, 1);
		System.out.print("<<<<<<<<<<<<<<<");
		cn.getTextWindow().setCursorPosition(57, 2);
		for (int i = 0; i < 15; i++) {
			System.out.print(this.inputQueue.peek());
			inputQueue.enqueue(inputQueue.dequeue());
		}
		cn.getTextWindow().setCursorPosition(57, 3);
		System.out.print("<<<<<<<<<<<<<<<");
		cn.getTextWindow().setCursorPosition(57, 5);
		System.out.print("Time    :   " + totalTime / 1000);
		cn.getTextWindow().setCursorPosition(57, 7);
		System.out.print("--- " + player.getName() + " ---");
		cn.getTextWindow().setCursorPosition(57, 8);
		System.out.print(String.format(" %-7s:%5d", "Energy", player.getEnergy()));
		cn.getTextWindow().setCursorPosition(57, 9);
		System.out.print(String.format(" %-7s:%5d", "Life", player.getLifePoint()));
		cn.getTextWindow().setCursorPosition(57, 10);
		System.out.print(String.format(" %-7s:%5d", "Trap", player.getTrapCount()));
		cn.getTextWindow().setCursorPosition(57, 11);
		System.out.print(String.format(" %-7s:%5d", "Score", player.getScore()));
		cn.getTextWindow().setCursorPosition(57, 15);
		System.out.print("---" + this.computerName + "---");
		cn.getTextWindow().setCursorPosition(57, 16);
		System.out.print(String.format(" %-7s:%5d", "S Robot", this.lesnekocounte));
		cn.getTextWindow().setCursorPosition(57, 17);
		System.out.print(String.format(" %-7s:%5d", "Score", this.computerScore));

	}
}

/*
 * for(int i=0;i<23;i++) { for(int j=0;j<55;j++) {
 * 
 * } }
 */
