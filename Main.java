import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
	public static void main(String[] args) throws IOException {
		final int runs = 1000;


		for (Difficulty d : Difficulty.values()) {
			long start = System.currentTimeMillis();

			List<Boolean> games = IntStream.range(0, runs)
					.parallel()
					.mapToObj(i -> getOutcome(d))
					.filter(b -> b != null)
					.collect(Collectors.toList());

			int totalFairGames = games.size();
			long totalWins = games.stream().filter(b -> b).count();
			System.out.println(String.format("%12s Win pct: %5d/%-5d (%.3f%%) %.3f games/second", d, totalWins, totalFairGames, 100 * totalWins / (double) totalFairGames, (1000.0 * runs) / (System.currentTimeMillis() - start)));
		}
	}

	private static Boolean getOutcome(Difficulty d) {
		for (int i = 0; i < 5; i++) {
			SimpleSolver ss = new SimpleSolver(d.buildBoard());
			ss.solve();
			Boolean isWin = ss.isWin();

			if (isWin == null)
				i--;
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