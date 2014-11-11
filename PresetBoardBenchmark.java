import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Prints the total number of randomly generated games out of 100 that were winnable in at least 3 attempts at each of
 * the three preset Minesweeper difficulties
 */
public class PresetBoardBenchmark {
	public static void main(String[] args) throws IOException {
		final int runs = 100;
		final int attemptsPerRun = 3;

		for (Difficulty d : Difficulty.values()) {
			List<Boolean> games = IntStream.range(0, runs)
					.parallel()
					.mapToObj(i -> getOutcome(d, attemptsPerRun))
					.collect(Collectors.toList());

			int totalGames = games.size();
			long totalWins = games.stream().filter(b -> b).count();
			double winPercentage = 100 * totalWins / (double) games.size();
			System.out.println(String.format("%12s Win pct: %5d/%-5d (%.3f%%)", d, totalWins, totalGames, winPercentage));
		}
	}

	private static boolean getOutcome(Difficulty d, final int attempts) {
		for (int i = 0; i < attempts; i++) {
			MinesweeperSolver ss = new MinesweeperSolver(d.buildBoard());
			ss.solve();
			Boolean isWin = ss.isWin();

			if (isWin == null)
				i--;//don't count games we lose on the first attempt
			else if (isWin)
				return true;
		}
		return false;
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