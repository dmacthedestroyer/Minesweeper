import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {
		final int runs = 500;


		for (Difficulty d : Difficulty.values()) {
			long start = System.currentTimeMillis(), totalWins = 0, totalFairGames = 0;
			for (int i = 0; i < runs; i++) {
				SimpleSolver ss = new SimpleSolver(d.buildBoard());
				try{
					ss.solve();
				}catch (IllegalStateException ise){
					System.out.println(ss.dumpDebugInfo());
					ise.printStackTrace();
				}
				Boolean isWin = ss.isWin();
				if (isWin != null) {
					totalFairGames++;
					if (isWin)
						totalWins++;
				}
			}

			System.out.println(String.format("%12s Win pct: %5d/%-5d (%.3f%%) %.3f games/second", d, totalWins, totalFairGames, 100 * totalWins / (double) totalFairGames, (1000.0 * runs) / (System.currentTimeMillis() - start)));
		}
	}

	private enum Difficulty {
		Beginner,
		Intermediate,
		Expert;

		public MinesweeperBoard buildBoard() {
			switch (this) {
				case Beginner:
					return new MinesweeperBoard(9, 9, 10);
				case Intermediate:
					return new MinesweeperBoard(16, 16, 40);
				case Expert:
					return new MinesweeperBoard(16, 30, 99);
			}

			throw new IllegalStateException();
		}
	}
}