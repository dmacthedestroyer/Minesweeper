import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {
		final int runs = 10000;
		int totalWins = 0, totalFairGames = 0;
		long start = System.currentTimeMillis();

		for (int i = 1; i <= runs; i++) {
			SimpleSolver ss = new SimpleSolver(expert());
			ss.solve();
			Boolean isWin = ss.isWin();
			if (isWin != null) {
				totalFairGames++;
				if (isWin)
					totalWins++;
			}

			if (i % 100 == 0 && i != 0)
				System.out.println(String.format("*%3s%% complete* Win pct: %5d/%-5d (%.3f%%) %.3f games/second", i*100/runs, totalWins, totalFairGames, 100 * totalWins / (double) totalFairGames, (1000.0 * i) / (System.currentTimeMillis() - start)));
		}

//		SimpleSolver ss;
//		Boolean isWin;
//		do{
//			ss = new SimpleSolver(new MinesweeperBoard(9,9,10));
//			ss.solve();
//			isWin = ss.isWin();
//		} while (isWin == null || isWin);
//
//		System.out.println(ss.dumpDebugInfo());

//		MinesweeperBoard board;
		// Two ways to create boards.  Create a random board...
//		board = new MinesweeperBoard(9, 9, 10);
		// ...or create a specific board.
/*		board = new MinesweeperBoard(new int[][]{{0, 0, 1, 0, 0},
		                                         {0, 1, 0, 0, 0},
		                                         {1, 0, 0, 0, 1}});*/
//		MinesweeperPlayer.solve(board);
	}

	private static MinesweeperBoard beginner() {
		return new MinesweeperBoard(9, 9, 10);
	}

	private static MinesweeperBoard intermediate() {
		return new MinesweeperBoard(16, 16, 40);
	}

	private static MinesweeperBoard expert() {
		return new MinesweeperBoard(16, 30, 99);
	}
}