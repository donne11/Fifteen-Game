import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// represents a tile
class Tile {
  // The number on the tile. Use 0 to represent the space
  int value;

  // constructor for tile
  Tile(int value) {
    this.value = value;
  }

  // draws the tile onto the background at the specified logical coordinates
  WorldScene drawAt(int col, int row, WorldScene background) {
    // EXTRA CREDIT IN DRAWAT
    // change the color of tiles that are in correct location into green
    Color tileColor = (this.value == (row * 4 + col + 1) || this.value == 0) ? Color.GREEN
        : Color.GRAY;

    WorldImage tileImage = new RectangleImage(30, 30, OutlineMode.SOLID, tileColor);
    WorldImage space = new RectangleImage(30, 30, OutlineMode.SOLID, Color.white);
    if (this.value != 0) {
      tileImage = new OverlayImage(new TextImage(Integer.toString(this.value), 15, Color.BLACK),
          tileImage);
    }
    else {
      tileImage = space;
    }
    // mutate the background with the tile drawn
    background.placeImageXY(tileImage, col * 30 + 15, row * 30 + 15);
    return background;
  }
}

// represents a world class of Fifteen Game
class FifteenGame extends World {
  ArrayList<ArrayList<Tile>> board;
  Stack<ArrayList<ArrayList<Tile>>> previousStates;
  ArrayList<Integer> valuesForTestWin = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7,
      8, 9, 10, 11, 12, 13, 14, 15));

  // constructor
  FifteenGame(ArrayList<ArrayList<Tile>> board) {
    this.board = board;
    this.previousStates = new Stack<ArrayList<ArrayList<Tile>>>();
  }

  // constructor for testing swap
  FifteenGame() {
    this.board = this.initBoard();
    this.previousStates = new Stack<ArrayList<ArrayList<Tile>>>();
  }

  // constructor for testing with seeded random
  FifteenGame(Random r) {
  }

  // constructor for testing onKeyEvent
  FifteenGame(boolean foo) {
    this.board = this.solvedBoard();
  }

  // Randomized the values of tiles
  public ArrayList<ArrayList<Tile>> initBoard() {
    Random rand = new Random();
    ArrayList<ArrayList<Tile>> startTile = new ArrayList<ArrayList<Tile>>();
    ArrayList<Integer> possibleValues = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7,
        8, 9, 10, 11, 12, 13, 14, 15));
    for (int i = 0; i < 4; i++) {
      ArrayList<Tile> inner = new ArrayList<Tile>();
      for (int j = 0; j < 4; j++) {
        int count = rand.nextInt(possibleValues.size());
        inner.add(new Tile(possibleValues.get(count)));
        possibleValues.remove(count);
      }
      startTile.add(inner);
    }
    return startTile;
  }

  // helper method for swap
  public ArrayList<ArrayList<Tile>> cloneBoard(ArrayList<ArrayList<Tile>> original) {
    ArrayList<ArrayList<Tile>> clone = new ArrayList<>();

    for (ArrayList<Tile> row : original) {
      ArrayList<Tile> newRow = new ArrayList<>();
      for (Tile tile : row) {
        newRow.add(new Tile(tile.value));
      }
      clone.add(newRow);
    }
    return clone;
  }

  // initial solved board for testing
  public ArrayList<ArrayList<Tile>> solvedBoard() {
    ArrayList<ArrayList<Tile>> start = new ArrayList<ArrayList<Tile>>();
    ArrayList<Integer> possibleValues = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7,
        8, 9, 10, 11, 12, 13, 14, 15));

    for (int i = 0; i < 4; i++) {
      ArrayList<Tile> inner = new ArrayList<Tile>();
      for (int j = 0; j < 4; j++) {
        inner.add(new Tile(possibleValues.remove(0)));
      }
      start.add(inner);
    }
    return start;
  }

  // draws the game
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(120, 120);

    for (int i = 0; i < this.board.size(); i++) {
      ArrayList<Tile> row = board.get(i);
      for (int j = 0; j < row.size(); j++) {
        Tile t = row.get(j);
        scene = t.drawAt(i, j, scene);
      }
    }
    return scene;
  }

  // finds the row and column of the given string in 2d array list and returns
  // them as an ArrayList
  public ArrayList<Integer> find(int value) {
    for (int i = 0; i < this.board.size(); i++) {
      ArrayList<Tile> curr = this.board.get(i);
      for (int j = 0; j < curr.size(); j++) {
        if (curr.get(j).value == value) {
          return new ArrayList<Integer>(Arrays.asList(i, j));
        }
      }
    }
    throw new IllegalStateException("There is no such Tile");
  }

  // determine id this move is legal
  public boolean canMove(String s) {
    int row = this.find(0).get(0);
    int col = this.find(0).get(1);

    if (s.equals("up")) {
      return ((row <= this.board.size() - 1) && row >= 0);
    }
    if (s.equals("down")) {
      return (row >= 0 && (row <= this.board.size() - 1));
    }
    if (s.equals("left")) {
      return ((col <= this.board.size() - 1) && col >= 0);
    }
    if (s.equals("right")) {
      return (col >= 0 && (col <= this.board.size() - 1));
    }
    throw new IllegalArgumentException("The move is illegal");
  }

  // swaps the tiles if they can be swapped
  public void swap(String key) {
    int mtRow = this.find(0).get(0);
    int mtCol = this.find(0).get(1);

    // Calculate the position to swap with based on the keyMove
    int thisRow = mtRow;
    int thisCol = mtCol;

    if (key.equals("up")) {
      thisCol += 1;
    }
    else if (key.equals("down")) {
      thisCol -= 1;
    }
    else if (key.equals("left")) {
      thisRow += 1;
    }
    else if (key.equals("right")) {
      thisRow -= 1;
    }

    // Check if the calculated position is valid
    if (isValidPosition(thisRow, thisCol)) {
      // Save the current state before making a move
      this.previousStates.push(cloneBoard(this.board));

      // Swap the tiles
      Tile temp = this.board.get(thisRow).get(thisCol);
      this.board.get(thisRow).set(thisCol, this.board.get(mtRow).get(mtCol));
      this.board.get(mtRow).set(mtCol, temp);
    }
  }

  // determine if the given position is within the bounds of the board
  public boolean isValidPosition(int row, int col) {
    return row >= 0 && row < this.board.size() && col >= 0 && col < this.board.get(0).size();
  }

  // handles keystrokes and does the given movement if possible
  public void onKeyEvent(String key) {
    if (key.equals("up") || key.equals("down") || key.equals("left") || key.equals("right")) {
      if (this.canMove(key)) {
        // Save the current state before making a move
        this.previousStates.push(cloneBoard(this.board));
        // If the move is possible, invoke swap method
        this.swap(key);
      }
    }

    // EXTRA CREDIT!!
    // Undo moves with the "u" key
    if (key.equals("u")) {
      if (!this.previousStates.isEmpty()) {
        // If there are previous states in the stack, pop and restore the last state
        this.board = this.previousStates.pop();
      }
    }
  }

  // determine if all the tiles in the right place
  public boolean hasGameEnded() {
    boolean result = true;
    // only for numbers 1-15, not the sixteenth blank tile
    for (int i = 0; i < this.board.size(); i++) {
      ArrayList<Tile> curr = this.board.get(i);
      for (int j = 0; j < curr.size(); j++) {
        // if the current index does not equal the number
        // of the tile, set flag to false.
        if ((curr.get(j).value != ((i * this.board.size()) + j))) {
          result = false;
        }
      }
    }
    // will return true if all tiles are in correct place
    return result;
  }

  // represent a WorldEnd
  public WorldEnd worldEnds() {
    // current world scene
    WorldScene current = new WorldScene(120, 120);

    if (this.hasGameEnded()) {
      // if game has ended, place image over current world scene
      current.placeImageXY(new TextImage("You Win!", 50, Color.red), 60, 60);
    }
    return new WorldEnd(true, current);
  }

}

// represents a example world of Fifteen Game
class ExampleFifteenGame {
  Tile t1 = new Tile(1);
  Tile t2 = new Tile(2);
  Tile t3 = new Tile(3);
  Tile t4 = new Tile(4);
  Tile t5 = new Tile(5);
  Tile t6 = new Tile(6);
  Tile t7 = new Tile(7);
  Tile t8 = new Tile(8);
  Tile t9 = new Tile(9);
  Tile t10 = new Tile(10);
  Tile t11 = new Tile(11);
  Tile t12 = new Tile(12);
  Tile t13 = new Tile(13);
  Tile t14 = new Tile(14);
  Tile t15 = new Tile(15);
  Tile t16 = new Tile(0);

  ArrayList<Tile> row1 = new ArrayList<Tile>(Arrays.asList(this.t1, this.t5, this.t9, this.t13));
  ArrayList<Tile> row2 = new ArrayList<Tile>(Arrays.asList(this.t2, this.t6, this.t10, this.t14));
  ArrayList<Tile> row3 = new ArrayList<Tile>(Arrays.asList(this.t3, this.t7, this.t11, this.t15));
  ArrayList<Tile> row4 = new ArrayList<Tile>(Arrays.asList(this.t4, this.t8, this.t12, this.t16));

  ArrayList<ArrayList<Tile>> board1 = new ArrayList<ArrayList<Tile>>(Arrays.asList(this.row4,
      this.row3, this.row2, this.row1));

  FifteenGame game1 = new FifteenGame();
  FifteenGame game2 = new FifteenGame(true);
  FifteenGame game3 = new FifteenGame(board1);

  WorldScene bk;
  FifteenGame game;
  WorldImage tileImage = new RectangleImage(30, 30, OutlineMode.SOLID, Color.gray);

  FifteenGame init = new FifteenGame();
  FifteenGame g = new FifteenGame();
  ArrayList<ArrayList<Tile>> initialState = g.cloneBoard(g.board);
  ArrayList<ArrayList<Tile>> og = g.initBoard();
  ArrayList<ArrayList<Tile>> cloned = g.cloneBoard(og);
  ArrayList<ArrayList<Tile>> d = g.initBoard();

  WorldImage text1 = new TextImage("1", Color.black);
  WorldImage text2 = new TextImage("2", Color.black);
  WorldImage text3 = new TextImage("3", Color.black);

  WorldImage tileBackground1 = new OverlayImage(text1, tileImage);
  WorldImage tileBackground2 = new OverlayImage(text2, tileImage);
  WorldImage tileBackground3 = new OverlayImage(text3, tileImage);

  // create the inital data
  void initData() {
    bk = new WorldScene(120, 120);
    game = new FifteenGame();
  }

  // creates a test scene
  void initScene() {
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        this.bk = this.game.board.get(j).get(i).drawAt(i, j, bk);
      }
    }
    this.bk.placeImageXY(this.tileImage, 120, 120);
  }

  void testDrawAt(Tester t) {
    WorldScene worldSceneEx = new WorldScene(120, 120);
    Tile tile = new Tile(1);

    // tile at (0, 0) in WorldScene
    worldSceneEx = tile.drawAt(0, 0, worldSceneEx);

    // expected WorldScene with the tile
    WorldScene expected = new WorldScene(120, 120);
    expected.placeImageXY(new OverlayImage(new TextImage("1", 15, Color.BLACK), new RectangleImage(
        30, 30, OutlineMode.SOLID, Color.GREEN)), 15, 15);

    // matches world scene w/ tile
    t.checkExpect(worldSceneEx, expected);
  }

  // tests for method find
  void testFind(Tester t) {
    t.checkExpect(game2.find(1), new ArrayList<Integer>(Arrays.asList(0, 1)));
    t.checkExpect(game2.find(2), new ArrayList<Integer>(Arrays.asList(0, 2)));
    t.checkExpect(game2.find(3), new ArrayList<Integer>(Arrays.asList(0, 3)));
  }

  // tests for method canMove
  void testCanMove(Tester t) {
    t.checkExpect(game2.canMove("up"), true);
    t.checkExpect(game2.canMove("down"), true);
    t.checkExpect(game2.canMove("left"), true);
    t.checkExpect(game2.canMove("right"), true);
  }

  // tests for method cloneBoard
  void testCloneBoard(Tester t) {
    t.checkExpect(cloned, og);
    t.checkExpect(cloned.get(0).get(0) == og.get(0).get(0), false);
  }

  // tests for method isValidPosition
  void testIsValidPos(Tester t) {
    t.checkExpect(g.isValidPosition(0, 0), true);
    t.checkExpect(g.isValidPosition(4, 0), false);
    t.checkExpect(g.isValidPosition(0, 4), false);
    t.checkExpect(g.isValidPosition(-1, 0), false);
    t.checkExpect(g.isValidPosition(0, -1), false);
  }

  // tests for method onKeyEvent
  public void testOnKeyEvent(Tester t) {
    // "up" key
    g.onKeyEvent("up");
    t.checkExpect(g.board.equals(g.cloneBoard(g.initBoard())), false);
    // "down" key
    g.onKeyEvent("down");
    t.checkExpect(g.board.equals(g.cloneBoard(g.initBoard())), false);
    // "left" key
    g.onKeyEvent("left");
    t.checkExpect(g.board.equals(g.cloneBoard(g.initBoard())), false);
    // "right" key
    g.onKeyEvent("right");
    t.checkExpect(g.board.equals(g.cloneBoard(g.initBoard())), false);
  }

  // tests for method hasGameEnded
  void testHasGameEnded(Tester t) {
    // Test with an unsolved board
    t.checkExpect(game3.hasGameEnded(), false);
    // Test with a solved board
    t.checkExpect(game2.hasGameEnded(), true);
  }

  // tests for method makeScene
  void testMakeScene(Tester t) {
    this.initData();
    this.game.initBoard();
    this.initScene();
    t.checkExpect(this.game.makeScene(), this.game.makeScene());
  }

  // tests for method swap
  void testSwap(Tester t) {
    this.initData();
    t.checkExpect(this.game2.board.get(3).get(1), t13);
    t.checkExpect(this.game2.board.get(3).get(0), t12);
    this.game2.swap("down");
    t.checkExpect(this.game2.board.get(3).get(1), t13);
    t.checkExpect(this.game2.board.get(3).get(0), t12);
  }

  // big-bang
  void testGame(Tester t) {
    FifteenGame g = new FifteenGame();
    g.bigBang(120, 120);
  }
}