import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MinesweeperSolver {
	private MinesweeperBoard board;
	private Map<Point, Boolean> knownTiles;
	private ConstraintSet constraints;

	public MinesweeperSolver(MinesweeperBoard board) {
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
			for (Map.Entry<Point, Boolean> entry : solvedConstraint.getTriviallySatisfiableConfiguration().entrySet()) {
				if (entry.getValue())
					flagTile(entry.getKey());
				else revealTile(entry.getKey());
			}
			debug(printDivider());
		}
	}

	private void makeGuess() {
		ConstraintSet.MineProbabilities probabilities = constraints.calculateProbabilities();
		boolean revealedOrFlaggedTile = false;
		List<Point> unexploredNonFrontierTiles = getUnexploredTiles().stream().filter(p -> !probabilities.getProbabilities().containsKey(p)).collect(Collectors.toList());
		double unexploredMineProbability = unexploredNonFrontierTiles.isEmpty() ? 1.0 : (board.getMines() - getFlaggedTiles().size() - probabilities.getAvgMines()) / unexploredNonFrontierTiles.size();

		debug(String.format("%s\nMaking best guess from following choices:\n%s\n%s\n%s", printDivider(), printProbabilities(probabilities.getProbabilities()), printDivider(), printBoard()));

		for (Map.Entry<Point, Double> e : probabilities.getProbabilities().entrySet())
			if (e.getValue() <= 0) {
				revealTile(e.getKey());
				revealedOrFlaggedTile = true;
			} else if (e.getValue() >= 1) {
				flagTile(e.getKey());
				revealedOrFlaggedTile = true;
			}

		if (!revealedOrFlaggedTile) {
			Optional<Map.Entry<Point, Double>> safestMove = probabilities.getProbabilities().entrySet().stream()
					.sorted((e1, e2) -> ((int) (e1.getValue() * 10000)) - ((int) (e2.getValue() * 10000)))
					.findFirst();

			if (safestMove.isPresent() && safestMove.get().getValue() <= unexploredMineProbability) {
				debug(String.format("Chose %s:%s", printPoint(safestMove.get().getKey()), safestMove.get().getValue()));
				revealTile(safestMove.get().getKey());
				debug(printDivider());
			} else {
				Optional<Point> tileToReveal = unexploredNonFrontierTiles.stream()
						.sorted((p1, p2) -> (int) (getNeighbors(p1).stream().filter(p -> probabilities.getProbabilities().containsKey(p)).count() - getNeighbors(p2).stream().filter(p -> probabilities.getProbabilities().containsKey(p)).count()))
						.findFirst();
				debug(String.format("%s\nNo probabilistic choices, choosing random hidden tile from list of %s", printDivider(), board.getHeight() * board.getWidth() - knownTiles.size()));
				revealTile(tileToReveal.get()); //ignoring this possible exception
				debug(printDivider());
			}
		}
	}

	/**
	 * flags a tile as a mine
	 * @param p
	 */
	private void flagTile(Point p) {
		debug("Flagging " + printPoint(p));
		knownTiles.put(p, true);
	}

	/**
	 * reveals a tile
	 * @param p
	 */
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

	public Set<Point> getUnexploredTiles() {
		Set<Point> unexploredTiles = new HashSet<>();
		for (int row = 0; row < board.getHeight(); row++)
			for (int col = 0; col < board.getWidth(); col++) {
				Point p = getPoint(row, col);
				if (!knownTiles.containsKey(p))
					unexploredTiles.add(p);
			}

		return unexploredTiles;
	}

	/**
	 * convert a (row,column) coordinate into an (x,y) coordinate
	 * @param row
	 * @param col
	 * @return
	 */
	private Point getPoint(int row, int col){
		return new Point(col, row);
	}

	/**
	 * return the value exposed on MinesweeperBoard.getTile(row, column) for a point in (x, y) format
	 * @param p
	 * @return
	 */
	private int getTile(Point p) {
		return board.getTile(p.y, p.x);
	}

	/**
	 * returns all points touching the given point, including itself
	 * @param p
	 * @return
	 */
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

	/**
	 * returns true if all non-mine tiles have been revealed, false if a mine was hit and null if a mine was hit on the
	 * first move
	 * @return
	 */
	public Boolean isWin() {
		int revealedTileCount = knownTiles.size();

		if (revealedTileCount == 0 && board.hasRevealedMine())
			return null; //what the hell kinda game lets you lose on the first move?!

		return !board.hasRevealedMine() && ((board.getHeight() * board.getWidth()) == revealedTileCount);
	}

	/********* Bunch of debug stuff for debugging stuff *********/

	private String printPoint(Point p) {
		return String.format("<%d,%d>", p.y, p.x);
	}

	private String printDivider(){
		return "------------------------------------------";
	}

	private String printProbabilities(Map<Point, Double> probabilities) {
		return probabilities.entrySet().stream()
				.sorted((e1, e2) -> ((int) (e1.getValue() * 10000)) - ((int) (e2.getValue() * 10000)))
				.map(e -> String.format("%s:%s", printPoint(e.getKey()), e.getValue()))
				.collect(Collectors.joining("\n"));
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