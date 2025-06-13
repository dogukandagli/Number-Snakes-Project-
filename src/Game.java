
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


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.io.BufferedReader;
import java.io.FileReader;

public class Game {
	enigma.console.Console cn = Enigma.getConsole("Mouse and Keyboard", 75, 25, 16);
	TextMouseListener tmlis;
	KeyListener klis;

	private char[][] board = readBoardFromFile("src/maze.txt");
	private Player player = spawnPlayer();
	private CRobot cRobot = spawnCRobot();
	private Snake[] snakes = new Snake[100];
	private String computerName = "Computer";
	private int computerScore = 0;
	private char[] collectables;
	private int timeUnit = 100;
	private int totalTime = 0;
	private CircularQueue inputQueue = new CircularQueue(15);
	private Position[] traps = new Position[100];
	private int trapsMaxCount = 0;
	private int[] trapsTimes = new int[100];
	int[] oppositeDirection = { 1, 0, 3, 2 };
	//
	public SingleLinkedList rememberedHitSnakeTailElements = new SingleLinkedList();
	public boolean continueAddingHitelements = false;
	public int counterForHit = 0;
	public int counterForHowManyTimesCont = 0;
	//
	private int count;// haci neyin count u

	Stack stack;// neyin stack ı
	Stack stack2;
	private int lesnekocounte = 0;// bu ne demek:D

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
			
			printMap();
			checkSnakeCollision();
			addTrapsToBoard();

			if ((stack2.isEmpty()))
				pathLine();
			if (totalTime % (4 * timeUnit) == 0) {
				// check snake pos for trap
				boolean hardBreak = false;
				for (int i = 0; i < this.trapsMaxCount; i++) {
					if (this.traps[i] != null) {
						for (int j = 0; j < this.snakes.length; j++) {
							if (this.snakes[j] != null) {
								if (Math.abs(this.traps[i].x - this.snakes[j].getPos().x) <= 1
										&& Math.abs(this.traps[i].y - this.snakes[j].getPos().y) <= 1) {
									removeTrap(i);
									removeSnake(j);
									this.player.setEnergy(this.player.getEnergy() + 500);
									this.player.setScore(this.player.getScore() + 200);
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
				printMap();
			}

			if (mousepr == 1) {

				mousepr = 0;
			}

			if (keypr == 1) {
				if (rkey == KeyEvent.VK_SPACE) {
					putTrap();
				}
				if (player.getEnergy() > 0) {
					if (totalTime % timeUnit == 0) {
						updatePlayerPos();
					}
				} else {
					if (totalTime % (2 * timeUnit) == 0) {
						updatePlayerPos();
					}
				}

				keypr = 0;
			}


			if (totalTime % (4 * timeUnit) == 0) {
				movementCrobot();
				checkNeighborHarming();
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

			if (totalTime % (8 * timeUnit) == 0) {
				for (int i = 0; i < this.trapsMaxCount; i++) {
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

			if (player.getLifePoint() <= 0) {
				cn.getTextWindow().setCursorPosition(0, 500);
				System.out.println("You died. if you want add score to highscore table give me nickname otherwise just enter 'E'.");
				Scanner s = new Scanner(System.in);
				String input = s.next();
				if (input == "E") {
					System.exit(31);
				}
				
				try (BufferedWriter writer = new BufferedWriter(new FileWriter("highscore.txt", true))) {
		            writer.write(input+" "+player.getScore()+"\n");
		        } catch (IOException e) {
		            System.err.println("Error write file: " + e.getMessage());
		        }
				
				DoubleLinkedList dll = new DoubleLinkedList();
				
				int count = 0;
		        try (BufferedReader reader = new BufferedReader(new FileReader("highscore.txt"))) {
		            String line;
		            while ((line = reader.readLine()) != null) {
		                dll.add(line);
		                count++;
		            }
		        } catch (IOException e) {
		            System.err.println("Error read file: " + e.getMessage());
		        }
		        
		        DoubleLinkedList dll2 = new DoubleLinkedList();
		        Node2 head = dll.head;
		        Node2 temp = head;
		        Node2 maxNode = null;
		        int max = -1;
		        for (int i = 0; i < count; i++) {
		        	temp = head;
		        	max = -1;
		        	maxNode = null;
			        while(temp != null) {
			        	int score = Integer.parseInt(((String)temp.data).split(" ")[1]);
			        	if (score >= max) {
			        		maxNode = temp;
			        		max = score;
			        	}
			        	temp = temp.next;
			        }
			        
			        
			        dll2.add(maxNode.data);
			        if (maxNode.prev == null) {
			        	head = head.next;
			        }else if (maxNode.next == null) {
			        	maxNode.prev.next = null;
			        }else {
			        	maxNode.prev.next = maxNode.next;
			        	maxNode.next.prev = maxNode.prev;
			        }
			        
		        }
		        temp = dll2.head;
		        System.out.println();
		        while (temp != null) {
		        	System.out.println(temp.data);
		        	temp = temp.next;
		        }
		        s.next();
		        System.exit(0);
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
			Position partPos = (Position) temp.getData();
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

	public boolean check3Wall(Snake snake) {
		int count = 0;
		int y = snake.getPos().y;
		int x = snake.getPos().x;
		if (checkWall(new Position(x, y + 1)) || checkWalkers(new Position(x, y + 1)) || checkSnake(snake, 2))
			count++;
		if (checkWall(new Position(x, y - 1))|| checkWalkers(new Position(x, y - 1))  || checkSnake(snake, 3))
			count++;
		if (checkWall(new Position(x + 1, y)) || checkWalkers(new Position(x+1, y)) || checkSnake(snake, 0))
			count++;
		if (checkWall(new Position(x - 1, y)) || checkWalkers(new Position(x-1, y)) || checkSnake(snake, 1))
			count++;

		if (count == 4 && snake.positionLinkedList.getHead() != null) {
			return true;}

		if (count == 3 && snake.positionLinkedList.getHead() == null) {
			return true;}

		return false;
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

	public boolean checkWalkers10(Position pos) {
		if (this.board[pos.y][pos.x] == 'P' || this.board[pos.y][pos.x] == 'C' || this.board[pos.y][pos.x] == 'S'|| this.board[pos.y][pos.x] == '#') {
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
		for (int i = 0; i < this.snakes.length; i++) {
			if (this.snakes[i] != null) {
				Snake nextSnake = this.snakes[i];
				Node temp = nextSnake.positionLinkedList.getHead();
				while (temp != null) {
					Position partPos = (Position) temp.getData();
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
			player.setTrapCount(player.getTrapCount() - 1);
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
		if (a <= 55) {
			inputQueue.enqueue('1');
		} else if (55 < a && a <= 75) {
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

				board[snakes[i].getPos().y][snakes[i].getPos().x] = ' ';

				if (!snakes[i].randomMode) {
					if (snakes[i].getPos().x < snakes[i].getTargetPos().x
							&& !(board[snakes[i].getPos().y][snakes[i].getPos().x + 1] == '#'
									|| board[snakes[i].getPos().y][snakes[i].getPos().x + 1] == 'P'
									|| board[snakes[i].getPos().y][snakes[i].getPos().x + 1] == 'C'
									|| checkSnake(snakes[i], 0))) {
						Position newSnakePos = new Position(snakes[i].getPos().x + 1, snakes[i].getPos().y);
						boolean controlEating = eating(newSnakePos.y, newSnakePos.x, snakes[i].collactibleLinkedList,
								snakes[i].positionLinkedList);
						if (!controlEating) {
							if (continueAddingHitelements && counterForHowManyTimesCont != 0) {
								updateTailPositionsAndTailForCollision1(snakes[i].getPos(),
										snakes[i].positionLinkedList,
										snakes[i].collactibleLinkedList/* ,rememberedHitSnakeTailElements */);
								counterForHowManyTimesCont--;
							} else {
								updateTailPositions(snakes[i].getPos(), snakes[i].positionLinkedList);

							}
							if (counterForHowManyTimesCont == 0) {
								continueAddingHitelements = false;
							}
						} else {
							updateTailPositionsEating(snakes[i].getPos(), snakes[i].positionLinkedList);
						}
						snakes[i].setPos(newSnakePos);
						snakes[i].currentDirection = 0;
					} else if (snakes[i].getPos().x > snakes[i].getTargetPos().x
							&& !(board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == '#'
									|| board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == 'P'
									|| board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == 'C'
									|| checkSnake(snakes[i], 1))) {
						Position newSnakePos = new Position(snakes[i].getPos().x - 1, snakes[i].getPos().y);
						boolean controlEating = eating(newSnakePos.y, newSnakePos.x, snakes[i].collactibleLinkedList,
								snakes[i].positionLinkedList);
						if (!controlEating) {
							if (continueAddingHitelements && counterForHowManyTimesCont != 0) {
								updateTailPositionsAndTailForCollision1(snakes[i].getPos(),
										snakes[i].positionLinkedList,
										snakes[i].collactibleLinkedList/* ,rememberedHitSnakeTailElements */);
								counterForHowManyTimesCont--;
							} else {
								updateTailPositions(snakes[i].getPos(), snakes[i].positionLinkedList);

							}
							if (counterForHowManyTimesCont == 0) {
								continueAddingHitelements = false;
							}
						} else {
							updateTailPositionsEating(snakes[i].getPos(), snakes[i].positionLinkedList);
						}
						snakes[i].setPos(newSnakePos);
						snakes[i].currentDirection = 1;
					} else if (snakes[i].getPos().y < snakes[i].getTargetPos().y
							&& !(board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == '#'
									|| board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == 'P'
									|| board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == 'C'
									|| checkSnake(snakes[i], 2))) {
						Position newSnakePos = new Position(snakes[i].getPos().x, snakes[i].getPos().y + 1);
						boolean controlEating = eating(newSnakePos.y, newSnakePos.x, snakes[i].collactibleLinkedList,
								snakes[i].positionLinkedList);
						if (!controlEating) {
							if (continueAddingHitelements && counterForHowManyTimesCont != 0) {
								updateTailPositionsAndTailForCollision1(snakes[i].getPos(),
										snakes[i].positionLinkedList,
										snakes[i].collactibleLinkedList/* ,rememberedHitSnakeTailElements */);
								counterForHowManyTimesCont--;
							} else {
								updateTailPositions(snakes[i].getPos(), snakes[i].positionLinkedList);

							}
							if (counterForHowManyTimesCont == 0) {
								continueAddingHitelements = false;
							}
						} else {
							updateTailPositionsEating(snakes[i].getPos(), snakes[i].positionLinkedList);
						}
						snakes[i].setPos(newSnakePos);
						snakes[i].currentDirection = 2;
					} else if (snakes[i].getPos().y > snakes[i].getTargetPos().y
							&& !(board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == '#'
									|| board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == 'P'
									|| board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == 'C'
									|| checkSnake(snakes[i], 3))) {
						Position newSnakePos = new Position(snakes[i].getPos().x, snakes[i].getPos().y - 1);
						boolean controlEating = eating(newSnakePos.y, newSnakePos.x, snakes[i].collactibleLinkedList,
								snakes[i].positionLinkedList);
						if (!controlEating) {
							if (continueAddingHitelements && counterForHowManyTimesCont != 0) {
								updateTailPositionsAndTailForCollision1(snakes[i].getPos(),
										snakes[i].positionLinkedList,
										snakes[i].collactibleLinkedList/* ,rememberedHitSnakeTailElements */);
								counterForHowManyTimesCont--;
							} else {
								updateTailPositions(snakes[i].getPos(), snakes[i].positionLinkedList);

							}
							if (counterForHowManyTimesCont == 0) {
								continueAddingHitelements = false;
							}
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
										|| board[snakes[i].getPos().y][snakes[i].getPos().x + 1] == 'C'
										|| checkSnake(snakes[i], 0)))
								|| newDirection == 1 && (board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == '#'
										|| board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == 'P'
										|| board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == 'C'
										|| checkSnake(snakes[i], 1))
								|| (newDirection == 2 && (board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == '#'
										|| board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == 'P'
										|| board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == 'C'
										|| checkSnake(snakes[i], 2)))
								|| (newDirection == 3 && (board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == '#'
										|| board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == 'P'
										|| board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == 'C'
										|| checkSnake(snakes[i], 3))));
						snakes[i].randomDirection = newDirection;
					}

					if (snakes[i].randomDirection == 0 && !(board[snakes[i].getPos().y][snakes[i].getPos().x + 1] == '#'
							|| board[snakes[i].getPos().y][snakes[i].getPos().x + 1] == 'P'
							|| board[snakes[i].getPos().y][snakes[i].getPos().x + 1] == 'C'
							|| checkSnake(snakes[i], 0))) {
						Position newSnakePos = new Position(snakes[i].getPos().x + 1, snakes[i].getPos().y);
						boolean controlEating = eating(newSnakePos.y, newSnakePos.x, snakes[i].collactibleLinkedList,
								snakes[i].positionLinkedList);
						if (!controlEating) {
							if (continueAddingHitelements && counterForHowManyTimesCont != 0) {
								updateTailPositionsAndTailForCollision1(snakes[i].getPos(),
										snakes[i].positionLinkedList,
										snakes[i].collactibleLinkedList/* ,rememberedHitSnakeTailElements */);
								counterForHowManyTimesCont--;
							} else {
								updateTailPositions(snakes[i].getPos(), snakes[i].positionLinkedList);

							}
							if (counterForHowManyTimesCont == 0) {
								continueAddingHitelements = false;
							}
						} else {
							updateTailPositionsEating(snakes[i].getPos(), snakes[i].positionLinkedList);
						}
						snakes[i].setPos(newSnakePos);
						snakes[i].currentDirection = 0;
					} else if (snakes[i].randomDirection == 1
							&& !(board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == '#'
									|| board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == 'P'
									|| board[snakes[i].getPos().y][snakes[i].getPos().x - 1] == 'C'
									|| checkSnake(snakes[i], 1))) {
						Position newSnakePos = new Position(snakes[i].getPos().x - 1, snakes[i].getPos().y);
						boolean controlEating = eating(newSnakePos.y, newSnakePos.x, snakes[i].collactibleLinkedList,
								snakes[i].positionLinkedList);
						if (!controlEating) {
							if (continueAddingHitelements && counterForHowManyTimesCont != 0) {
								updateTailPositionsAndTailForCollision1(snakes[i].getPos(),
										snakes[i].positionLinkedList,
										snakes[i].collactibleLinkedList/* ,rememberedHitSnakeTailElements */);
								counterForHowManyTimesCont--;
							} else {
								updateTailPositions(snakes[i].getPos(), snakes[i].positionLinkedList);

							}
							if (counterForHowManyTimesCont == 0) {
								continueAddingHitelements = false;
							}
						} else {
							updateTailPositionsEating(snakes[i].getPos(), snakes[i].positionLinkedList);
						}
						snakes[i].setPos(newSnakePos);
						snakes[i].currentDirection = 1;
					} else if (snakes[i].randomDirection == 2
							&& !(board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == '#'
									|| board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == 'P'
									|| board[snakes[i].getPos().y + 1][snakes[i].getPos().x] == 'C'
									|| checkSnake(snakes[i], 2))) {
						Position newSnakePos = new Position(snakes[i].getPos().x, snakes[i].getPos().y + 1);
						boolean controlEating = eating(newSnakePos.y, newSnakePos.x, snakes[i].collactibleLinkedList,
								snakes[i].positionLinkedList);
						if (!controlEating) {
							if (continueAddingHitelements && counterForHowManyTimesCont != 0) {
								updateTailPositionsAndTailForCollision1(snakes[i].getPos(),
										snakes[i].positionLinkedList,
										snakes[i].collactibleLinkedList/* ,rememberedHitSnakeTailElements */);
								counterForHowManyTimesCont--;
							} else {
								updateTailPositions(snakes[i].getPos(), snakes[i].positionLinkedList);

							}
							if (counterForHowManyTimesCont == 0) {
								continueAddingHitelements = false;
							}
						} else {
							updateTailPositionsEating(snakes[i].getPos(), snakes[i].positionLinkedList);
						}
						snakes[i].setPos(newSnakePos);
						snakes[i].currentDirection = 2;
					} else if (snakes[i].randomDirection == 3
							&& !(board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == '#'
									|| board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == 'P'
									|| board[snakes[i].getPos().y - 1][snakes[i].getPos().x] == 'C'
									|| checkSnake(snakes[i], 3))) {
						Position newSnakePos = new Position(snakes[i].getPos().x, snakes[i].getPos().y - 1);
						boolean controlEating = eating(newSnakePos.y, newSnakePos.x, snakes[i].collactibleLinkedList,
								snakes[i].positionLinkedList);
						if (!controlEating) {
							if (continueAddingHitelements && counterForHowManyTimesCont != 0) {
								updateTailPositionsAndTailForCollision1(snakes[i].getPos(),
										snakes[i].positionLinkedList,
										snakes[i].collactibleLinkedList/* ,rememberedHitSnakeTailElements */);
								counterForHowManyTimesCont--;
							} else {
								updateTailPositions(snakes[i].getPos(), snakes[i].positionLinkedList);

							}
							if (counterForHowManyTimesCont == 0) {
								continueAddingHitelements = false;
							}
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

				if (check3Wall(snakes[i]) == true) {
					snakeReserving(snakes[i]);
					snakes[i].currentDirection = (snakes[i].currentDirection + 2) % 4;
					snakes[i].randomMode = true;
				}

			}

		}

	}

	public boolean eating(int y, int x, SingleLinkedList list, SingleLinkedList list2) {
		char point = this.board[y][x];

		if (point == '1'  || point == '2' || point == '3') {
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
			Position nextTemp = null;

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

	public void checkNeighborHarming() {
		if (cRobot != null) {
			int[] dx = { 0, 0, 1, -1 };
			int[] dy = { 1, -1, 0, 0 };

			for (int i = 0; i < 4; i++) {
				int neighborX = cRobot.getPos().x + dx[i];
				int neighborY = cRobot.getPos().y + dy[i];
				Position neighborPosition = new Position(neighborX, neighborY);
				if (player.getPos().x == neighborPosition.x && player.getPos().y == neighborPosition.y) {
					player.setLifePoint(player.getLifePoint() - 30);
					break;
				}
			}
		}
		for (int i = 0; i < snakes.length; i++) {
			if (snakes[i] != null) {
				int[] dx = { 0, 0, 1, -1 };
				int[] dy = { 1, -1, 0, 0 };
				for (int j = 0; j < 4; j++) {
					int neighborX = snakes[i].getPos().x + dx[j];
					int neighborY = snakes[i].getPos().y + dy[j];
					Position neighborPosition = new Position(neighborX, neighborY);
					if (player.getPos().x == neighborPosition.x && player.getPos().y == neighborPosition.y) {
						player.setLifePoint(player.getLifePoint() - 1);
						break;
					}
				}
				Node current = snakes[i].positionLinkedList.getHead();

				while (current != null) {
					Position currentPosition = (Position) current.getData();
					for (int j = 0; j < 4; j++) {
						int neighborX = currentPosition.x + dx[j];
						int neighborY = currentPosition.y + dy[j];
						Position neighborPosition = new Position(neighborX, neighborY);
						if (player.getPos().x == neighborPosition.x && player.getPos().y == neighborPosition.y) {
							player.setLifePoint(player.getLifePoint() - 1);
						}

					}
					current = current.getLink();
				}
			}

		}
	}

	public Position position1(Snake snake, int direction) {
		Position Position = null;
		Position snakesHead = snake.getPos();
		if (direction == 0)
			Position = new Position(snakesHead.x + 1, snakesHead.y);
		else if (direction == 1)
			Position = new Position(snakesHead.x - 1, snakesHead.y);
		else if (direction == 2)
			Position = new Position(snakesHead.x, snakesHead.y + 1);
		else if (direction == 3)
			Position = new Position(snakesHead.x, snakesHead.y - 1);

		return Position;
	}

	//
	public SingleLinkedList deepCopyList(SingleLinkedList original) {
		SingleLinkedList copy = new SingleLinkedList();
		Node current = original.getHead();
		while (current != null) {
			copy.add(current.getData());
			current = current.getLink();
		}
		return copy;
	}

	//
	public void checkSnakeCollision() {//
		boolean[] processed = new boolean[snakes.length];

		for (int i = 0; i < snakes.length; i++) {
			if (snakes[i] == null || processed[i])
				continue;

			Position collisionPosition = position1(snakes[i], snakes[i].currentDirection);

			for (int j = 0; j < snakes.length; j++) {
				if (snakes[j] == null ||snakes[i]==null|| i == j || processed[j])
					continue;
				Snake hitSnake = snakes[i];
				Position otherHead = snakes[j].getPos();
				if (otherHead.x == collisionPosition.x && otherHead.y == collisionPosition.y) {
					removeSnake(i);
					removeSnake(j);
					
					processed[i] = true;
					processed[j] = true;
					break;
				}
				if (snakes[j] == null ||snakes[i]==null|| i == j || processed[j])
					continue;
				if (snakes[i].getPos().x == otherHead.x && snakes[i].getPos().y == otherHead.y) {
					removeSnake(i);
					removeSnake(j);
					processed[i] = true;
					processed[j] = true;
					break;
				}
				if (snakes[j] == null ||snakes[i]==null|| i == j || processed[j])
					continue;

				Node tailNode = snakes[j].positionLinkedList.getHead();
				Node collactibleNode = snakes[j].collactibleLinkedList.getHead();

				while (tailNode != null && collactibleNode != null) {
					Position tailPos = (Position) tailNode.getData();
					char value = (char) collactibleNode.getData();

					if (tailPos.x == collisionPosition.x && tailPos.y == collisionPosition.y) {
						if (value == '1') {
							collision1(snakes[j], snakes[i], collisionPosition, tailPos.x, tailPos.y);
							//
							rememberedHitSnakeTailElements = deepCopyList(snakes[i].collactibleLinkedList);
							processed[i] = true;
							processed[j] = true;
							continueAddingHitelements = true;
							removeSnake(snakes[i]);
							i++;
							break;
						}
						if (value == '2' || value == '3') {
							int newSnakeInt = collision2or3(snakes[j], hitSnake, collisionPosition);
							processed[i] = true;
							processed[j] = true;
							processed[newSnakeInt] = true;
							break;
						}
					}

					tailNode = tailNode.getLink();
					collactibleNode = collactibleNode.getLink();
				}
			}
			
		}

	}


	public void collision1(Snake crashedSnake, Snake hitSnake, Position collisionPosition, int coordX, int coordY) {
		SingleLinkedList crashedSnakeTailPosition = crashedSnake.positionLinkedList;
		SingleLinkedList hitSnakeTailElements = hitSnake.collactibleLinkedList;
		// bu kısmı kaçıncı eleman olduğunu bulmak için yaptım
		int counter = 0;
		Node crashedSnakePositionNode = crashedSnakeTailPosition.getHead();
		while (true) {
			Position pos = (Position) crashedSnakePositionNode.getData();
			if (pos.x == coordX && pos.y == coordY) {
				break;
			}
			counter++;
			crashedSnakePositionNode = crashedSnakePositionNode.getLink();
		}
		// global değişkenle çartığı noktanın bir yanını tuttum
		counterForHit = counter + 1;
		// global değişkenle yılanın kaç hareket yapması gerektiğini tuttum tuttum.
		counterForHowManyTimesCont = hitSnakeTailElements.size();
		
	}

	public void updateTailPositionsAndTailForCollision1(Position newTailPos, SingleLinkedList coordinates,
			SingleLinkedList snakeTail) {
		  Node willBeAdded = rememberedHitSnakeTailElements.getHead();
		    
		    if (willBeAdded == null) {
		        return;
		    }

		    Object dataToAddTail = willBeAdded.getData();
		    int size = snakeTail.size();
		    Node newNode = new Node(dataToAddTail);
		    if (counterForHit >= size) {
		        Node temp = snakeTail.getHead();
		        if(temp!=null) {
		        while (temp.getLink() != null) {
		            temp = temp.getLink();
		        }
		        temp.setLink(newNode);
		        }
		    } else {
		        Node temp = snakeTail.getHead();
		        int count = 0;
		        while (temp != null) {
		            if (count == counterForHit - 1) {
		                newNode.setLink(temp.getLink());
		                temp.setLink(newNode);
		                break;
		            }
		            temp = temp.getLink();
		            count++;
		        }
		    }
		    int controlSize=snakeTail.size();
			rememberedHitSnakeTailElements.setHead(willBeAdded.getLink());
			//
			// kordinat kısmı burda düzgün bir şekilde eklemiyor olasılık
			if (coordinates != null) {
				int size2 = coordinates.size();
				if (counterForHit > size2) {
					Node current = coordinates.getHead();
					if (current == null) {
						coordinates.setHead(new Node(new Position(newTailPos.x, newTailPos.y)));
					} else {
						while (current.getLink() != null) {
							current = current.getLink();
						}
						current.setLink(new Node(new Position(newTailPos.x, newTailPos.y)));
					}
				} else {
					if (coordinates.getHead() == null)
						return;
					Node current = coordinates.getHead();
					Position temp = new Position(newTailPos.x, newTailPos.y); // ilk pozisyon
					Position prevTemp;

					int i = 0;
					while (i < counterForHit && current != null) {
						prevTemp = (Position) current.getData();
						current.setData(temp);
						temp = prevTemp;

						current = current.getLink();
						i++;
					}
					Node insertAt = coordinates.getHead();
					int j = 0;
					while (j < counterForHit - 1 && insertAt != null) {
						insertAt = insertAt.getLink();
						j++;
					}

					if (insertAt != null) {
						Node newNode2 = new Node(temp);
						newNode2.setLink(insertAt.getLink());
						insertAt.setLink(newNode2);
					}
				}
			}	
			int controlSize2=coordinates.size();
			//
		}	

	public int collision2or3(Snake crashedSnake, Snake hitSnake, Position collisionPosition) {
		Snake newSnake = new Snake(collisionPosition);

		int newSnakeInt = 0;
		SingleLinkedList crashedSnakeTailPosition = crashedSnake.positionLinkedList;
		SingleLinkedList crashedSnakeTailCollactible = crashedSnake.collactibleLinkedList;

		Node crashedSnakeCurrentPositonNode = crashedSnakeTailPosition.getHead();
		Node crashedSnakeCurrentCollactibleNode = crashedSnakeTailCollactible.getHead();
		Node prevCutPos = null;
		Node prevCutColl = null;

		while (crashedSnakeCurrentPositonNode != null && crashedSnakeCurrentCollactibleNode != null) {
			Position crashedSnakeCurrentPositon = (Position) crashedSnakeCurrentPositonNode.getData();
			prevCutPos = crashedSnakeCurrentPositonNode;
			prevCutColl = crashedSnakeCurrentCollactibleNode;

			if (crashedSnakeCurrentPositon.x == collisionPosition.x
					&& crashedSnakeCurrentPositon.y == collisionPosition.y) {
				break;
			}

			crashedSnakeCurrentPositonNode = crashedSnakeCurrentPositonNode.getLink();
			crashedSnakeCurrentCollactibleNode = crashedSnakeCurrentCollactibleNode.getLink();
		}

		Node posIter = crashedSnakeCurrentPositonNode.getLink();
		Node collIter = crashedSnakeCurrentCollactibleNode.getLink();

		while (posIter != null && collIter != null) {
			newSnake.positionLinkedList.add((Position) posIter.getData());
			newSnake.collactibleLinkedList.add((char) collIter.getData());
			posIter = posIter.getLink();
			collIter = collIter.getLink();
		}

		if (crashedSnakeCurrentPositonNode != null && crashedSnakeCurrentCollactibleNode != null) {
			if (prevCutPos != null && prevCutColl != null) {
				prevCutColl.setLink(null);
				prevCutPos.setLink(null);
			}
		}

		updateTailPositions(crashedSnake.getPos(), crashedSnake.positionLinkedList);
		int[] dx = { 1, -1, 0, 0 };
		int[] dy = { 0, 0, 1, -1 };
		Position crashedSnakeHeadTail = crashedSnake.getPos();
		int direction = crashedSnake.currentDirection;
		if (board[crashedSnakeHeadTail.y + dy[direction]][crashedSnakeHeadTail.x + dx[direction]] == ' ') {
			crashedSnake.setPos(
					new Position(crashedSnakeHeadTail.x + dx[direction], crashedSnakeHeadTail.y + dy[direction]));
			board[crashedSnakeHeadTail.y + dy[direction]][crashedSnakeHeadTail.x + dx[direction]] = 'S';
		} else {

			for (int i = 0; i < 4; i++) {
				if (board[crashedSnakeHeadTail.y + dy[i]][crashedSnakeHeadTail.x + dx[i]] == ' ') {
					crashedSnake.setPos(new Position(crashedSnakeHeadTail.x + dx[i], crashedSnakeHeadTail.y + dy[i]));
					board[crashedSnakeHeadTail.y + dy[i]][crashedSnakeHeadTail.x + dx[i]] = 'S';
					break;
				}
			}
		}
		updateTailPositions(crashedSnake.getPos(), crashedSnake.positionLinkedList);
		crashedSnakeHeadTail = crashedSnake.getPos();
		direction = crashedSnake.currentDirection;

		if (board[crashedSnakeHeadTail.y + dy[direction]][crashedSnakeHeadTail.x + dx[direction]] == ' ') {
			crashedSnake.setPos(
					new Position(crashedSnakeHeadTail.x + dx[direction], crashedSnakeHeadTail.y + dy[direction]));
			board[crashedSnakeHeadTail.y + dy[direction]][crashedSnakeHeadTail.x + dx[direction]] = 'S';
		} else {
			for (int i = 0; i < 4; i++) {
				if (board[crashedSnakeHeadTail.y + dy[i]][crashedSnakeHeadTail.x + dx[i]] == ' ') {
					crashedSnake.setPos(new Position(crashedSnakeHeadTail.x + dx[i], crashedSnakeHeadTail.y + dy[i]));
					board[crashedSnakeHeadTail.y + dy[i]][crashedSnakeHeadTail.x + dx[i]] = 'S';
					break;
				}
			}
		}
		showSnakeTail(crashedSnake.collactibleLinkedList, crashedSnake.positionLinkedList);

		for (int i = 0; i < snakes.length; i++) {
			if (snakes[i] == null) {
				snakes[i] = newSnake;
				newSnakeInt = i;
				break;
			}
		}

		// newSnakereverse yapmadin onu yap
//bazen hitsnake reverse olduktan sonra s kismi silinmiyor sebepsiz
		// newsnake nasil eklendigibelli degil
		newSnake.currentDirection = oppositeDirection[crashedSnake.currentDirection];
		Reverse(snakes[newSnakeInt]);
		snakeReserving(snakes[newSnakeInt]);

		showSnakeTail(newSnake.collactibleLinkedList, newSnake.positionLinkedList);
		if (hitSnake.positionLinkedList.getHead() != null) {

			Reverse(hitSnake);
			Position hitSnakeHeadTail = (Position) hitSnake.positionLinkedList.getHead().getData();
			int direction2 = oppositeDirection[hitSnake.currentDirection];
			if (board[hitSnakeHeadTail.y + dy[direction2]][hitSnakeHeadTail.x + dx[direction2]] == ' ') {
				board[hitSnake.getPos().y][hitSnake.getPos().x] = ' ';
				hitSnake.setPos(new Position(hitSnakeHeadTail.x + dx[direction2], hitSnakeHeadTail.y + dy[direction2]));
				board[hitSnakeHeadTail.y + dy[direction2]][hitSnakeHeadTail.x + dx[direction2]] = 'S';
			} else {
				for (int i = 0; i < 4; i++) {
					if (board[hitSnakeHeadTail.y + dy[i]][hitSnakeHeadTail.x + dx[i]] == ' ') {
						board[hitSnake.getPos().y][hitSnake.getPos().x] = ' ';
						hitSnake.setPos(new Position(hitSnakeHeadTail.x + dx[i], hitSnakeHeadTail.y + dy[i]));
						board[hitSnakeHeadTail.y + dy[i]][hitSnakeHeadTail.x + dx[i]] = 'S';
						break;
					}
				}
			}
		}

		hitSnake.currentDirection = oppositeDirection[hitSnake.currentDirection];
		hitSnake.randomMode = true;
		showSnakeTail(hitSnake.collactibleLinkedList, hitSnake.positionLinkedList);
		return newSnakeInt;
	}

	public void Reverse(Snake snake) {

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

	public void removeSnake(Snake snake) {

		if (snake == null)
			return;

		Node pos = snake.positionLinkedList != null ? snake.positionLinkedList.getHead() : null;

		while (pos != null) {
			Position p = (Position) pos.getData();
			board[p.y][p.x] = ' ';
			pos = pos.getLink();
		}
		Position head = snake.getPos();

		if (head != null) {
			board[head.y][head.x] = ' ';
		}
		if (snake.positionLinkedList != null)
			snake.positionLinkedList.setHead(null);
		if (snake.collactibleLinkedList != null)
			snake.collactibleLinkedList.setHead(null);
		for (int i = 0; i < snakes.length; i++) {
			if (snakes[i] == snake) {
				snakes[i] = null;
				break;
			}
		}
	}

	public boolean checkSnake(Snake snake, int direction) {

		Position position = position1(snake, direction);

		for (int i = 0; i < snakes.length; i++) {
			if (snakes[i] == null)
				continue;

			SingleLinkedList positionLinkedList = snakes[i].positionLinkedList;
			Node positionNode = positionLinkedList.getHead();

			while (positionNode != null) {
				Position position2 = (Position) positionNode.getData();
				if (position.x == position2.x && position.y == position2.y) {
					return true;
				}
				positionNode = positionNode.getLink();
			}

		}
		return false;
	}

	public void printMap() {
		cn.getTextWindow().setCursorPosition(0, 0);
		for (int y = 0; y < 23; y++) {
			for (int x = 0; x < 55; x++) {
				if (board[y][x] == 'S') {
					cn.getTextWindow().output(x, y, 'S', new TextAttributes(Color.GREEN));
				}
				else if (board[y][x] == 'P') {
					cn.getTextWindow().output(x, y, 'P', new TextAttributes(Color.CYAN));
				} else if (board[y][x] == '#') {
					cn.getTextWindow().output(x, y, '#', new TextAttributes(Color.GRAY));
				} else if (board[y][x] == 'C') {
					cn.getTextWindow().output(x, y, 'C', new TextAttributes(Color.YELLOW));
				}else if(board[y][x] == '.') {
					cn.getTextWindow().output(x, y, '.', new TextAttributes(Color.RED));
				}
				else {
					cn.getTextWindow().output(x, y, board[y][x]);
				}
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
}

/*
 * for(int i=0;i<23;i++) { for(int j=0;j<55;j++) {
 * 
 * } }
 */