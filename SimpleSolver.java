import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;

public class SimpleSolver {
	private MinesweeperBoard board;
	private Map<Point, Boolean> knownTiles;
	private ConstraintSet constraints;

	public SimpleSolver(MinesweeperBoard board) {
		this.board = board;

		knownTiles = new HashMap<>();
		constraints = new ConstraintSet();
	}

	public void solve() {
		while (!isGameFinished())
			doMove();
	}

	public void doMove() {
		Constraint solvedConstraint = constraints.findTriviallySatisfiedConstraint(knownTiles);
		if (solvedConstraint == null) {
			makeGuess();
		} else {
			debug(printDivider() + "\nFound trivially satisfied constraint: " + solvedConstraint);
			for (Map.Entry<Point, Boolean> entry : solvedConstraint.getSatisfiableConfiguration().entrySet()) {
				if (entry.getValue())
					flagTile(entry.getKey());
				else revealTile(entry.getKey());
			}
			debug(printDivider());
		}
	}

	private void makeGuess() {
		Map<Point, Double> probabilities = constraints.calculateProbabilities();

		if (probabilities.size() > 0) {
			boolean revealedOrFlaggedTile = false;

			String foo = probabilities.entrySet().stream()
					.sorted((e1, e2) -> ((int) (e1.getValue() * 10000)) - ((int) (e2.getValue() * 10000)))
					.map(e -> String.format("%s:%s", printPoint(e.getKey()), e.getValue()))
					.collect(Collectors.joining("\n"));
			debug(printDivider());
			debug("Making best guess from following choices:\n" + foo);
			debug(printDivider());
			debug(printBoard());

			for (Map.Entry<Point, Double> e : probabilities.entrySet()) {
				if (e.getValue() <= 0) {
					revealTile(e.getKey());
					revealedOrFlaggedTile = true;
				}
				if (e.getValue() >= 1) {
					flagTile(e.getKey());
					revealedOrFlaggedTile = true;
				}
			}

			if (!revealedOrFlaggedTile) {
				Optional<Map.Entry<Point, Double>> safestMove = probabilities.entrySet().stream()
						.sorted((e1, e2) -> ((int) (e1.getValue() * 10000)) - ((int) (e2.getValue() * 10000)))
						.findFirst();
				if (safestMove.isPresent()) {
					debug(String.format("Chose %s:%s", printPoint(safestMove.get().getKey()), safestMove.get().getValue()));
					revealTile(safestMove.get().getKey());
					debug(printDivider());
				} else {
					throw new IllegalStateException("Should have revealed or flagged something here, but didn't");
				}
			}
		} else {
			for (int r = 0; r < board.getHeight(); r++)
				for (int c = 0; c < board.getWidth(); c++) {
					Point p = getPoint(r, c);
					if (!knownTiles.containsKey(p)) {
						debug(printDivider());
						debug("No probabilistic choices, choosing random hidden tile from list of " + ((board.getHeight() * board.getWidth()) - knownTiles.size()));
						revealTile(p);
						debug(printDivider());
						return;
					}
				}
		}
	}

	private void flagTile(Point p) {
		debug("Flagging " + printPoint(p));
		knownTiles.put(p, true);
	}

	private void revealTile(Point p) {
		debug("Revealing " + printPoint(p));
		board.revealTile(p.y, p.x);
		if (!board.hasRevealedMine()) {
			knownTiles.put(p, false);
			constraints.add(new Constraint(getNeighbors(p), board.getTile(p.y, p.x)));
		} else
			debug(String.format("%s\nLose!\nConstraints:\n%s\nBoard:\n%s", printDivider(), constraints, printBoard()));
	}

	public Set<Point> getFlaggedTiles() {
		return knownTiles.entrySet().stream()
				.filter(Map.Entry::getValue)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
	}

	private Point getPoint(int row, int col){
		return new Point(col, row);
	}

	private int getTile(Point p) {
		return board.getTile(p.y, p.x);
	}

	private Set<Point> getNeighbors(Point p) {
		Set<Point> neighbors = new HashSet<>();
		for (int col = Math.max(0, p.x - 1); col < Math.min(board.getWidth(), p.x + 2); col++)
			for (int row = Math.max(0, p.y - 1); row < Math.min(board.getHeight(), p.y + 2); row++)
				neighbors.add(getPoint(row, col));

		return neighbors;
	}

	public boolean isGameFinished() {
		return board.hasRevealedMine() || isWin();
	}

	public Boolean isWin() {
		int revealedTileCount = knownTiles.size();

		if (revealedTileCount == 0 && board.hasRevealedMine())
			return null; //what the hell kinda game lets you lose on the first move?!

		return !board.hasRevealedMine() && ((board.getHeight() * board.getWidth()) == revealedTileCount);
	}

	private String printPoint(Point p) {
		return String.format("<%d,%d>", p.y, p.x);
	}

	private String printDivider(){
		return "------------------------------------------";
	}

	private String printBoard() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < board.getWidth()+2; i++) sb.append("-");
		sb.append("\n");
		for (int row = 0; row < board.getHeight(); row++) {
			sb.append("|");
			for (int col = 0; col < board.getWidth(); col++) {
				Point p = getPoint(row, col);
				if (knownTiles.containsKey(p)) {
					sb.append(knownTiles.get(p) ? "^" : getTile(p));
				} else if (getTile(p) == MinesweeperBoard.MINE)
					sb.append("*");
				else
					sb.append(" ");
			}
			sb.append("|\n");
		}
		for (int i = 0; i < board.getWidth()+2; i++) sb.append("-");
		return sb.toString();
	}

	public String toString() {
		return String.format("-------------------Constraints-------------------\n%s\n-------------------------------------------------\n%s",
				constraints, printBoard());
	}

	private StringBuilder debugInfo = new StringBuilder();

	public String dumpDebugInfo(){
		return debugInfo.toString();
	}

	private void debug(String msg) {
		debugInfo.append(msg).append("\n");
	}
}