import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {
		final int runs = 10000;
		int wins = 0;
		for (int i = 0; i < runs; i++) {
			SimpleSolver ss = new SimpleSolver(new MinesweeperBoard(9,9,10));
			ss.solve();
			Boolean isWin = ss.isWin();
			if (isWin == null)
				i--;//redo if we lost on the first play
			else if (isWin) wins++;
		}

		System.out.println(String.format("Win pct: %s/%d (%s%%)", wins, runs, wins / (double) runs));

//		MinesweeperBoard board;
		// Two ways to create boards.  Create a random board...
//		board = new MinesweeperBoard(9, 9, 10);
		// ...or create a specific board.
/*		board = new MinesweeperBoard(new int[][]{{0, 0, 1, 0, 0},
		                                         {0, 1, 0, 0, 0},
		                                         {1, 0, 0, 0, 1}});*/
//		MinesweeperPlayer.solve(board);
	}
}