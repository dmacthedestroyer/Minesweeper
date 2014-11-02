public class Main {
	public static void main(String[] args) {
		MinesweeperBoard board;
		// Two ways to create boards.  Create a random board...
		board = new MinesweeperBoard(8, 4, 10);
		// ...or create a specific board.
/*		board = new MinesweeperBoard(new int[][]{{0, 0, 1, 0, 0},
		                                         {0, 1, 0, 0, 0},
		                                         {1, 0, 0, 0, 1}});*/
		MinesweeperPlayer.solve(board);
	}
}
