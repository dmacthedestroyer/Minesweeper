public class Main {
	public static void main(String[] args) {
		MinesweeperBoard board;
		// Two ways to create boards.  Create a random board...
//		board = new MinesweeperBoard(8, 4, 10);
		// ...or create a specific board.
//		board = new MinesweeperBoard(new int[][]{{0, 0, 1, 0, 0},{0, 1, 0, 0, 0},{1, 0, 0, 0, 1}});
		MinesweeperBoard b = new MinesweeperBoard(16, 30, 99);
		int attempts = 0;
		Boolean isWin = null;
		while(isWin == null || !isWin) {
			b.reset();
			MinesweeperSolver s = new MinesweeperSolver(b);
			s.solve();
			isWin = s.isWin();
			attempts++;
			System.out.println(String.format("Attempt %d: %s (%d mines flagged)", attempts, isWin, s.getFlaggedTiles().size()));
		}

//		MinesweeperPlayer.solve(board);
	}
}
