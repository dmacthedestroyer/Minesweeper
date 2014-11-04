import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleSolver {
	private MinesweeperBoard board;
	private Set<Point> flaggedTiles;
	private List<Constraint> constraints;

	public SimpleSolver(MinesweeperBoard board) {
		this.board = board;

		flaggedTiles = new HashSet<>();
		constraints = new ArrayList<>();
	}

	public void solve(){
		while(!isGameFinished())
			doMove();
	}

	public void doMove() {
		Constraint solvedConstraint = findSolvedConstraint();
		if (solvedConstraint == null) {
			makeGuess();
		} else {
			flaggedTiles.addAll(solvedConstraint.getMineTiles());
			solvedConstraint.getSafeTiles().forEach(this::revealTile);
		}
	}

	private Constraint findSolvedConstraint() {
		for (int i = 0; i < constraints.size(); i++)
			if (constraints.get(i).isSolved())
				return constraints.remove(i);

		return null;
	}

	private void makeGuess() {
		for (int x = 0; x < board.getWidth(); x++)
			for (int y = 0; y < board.getHeight(); y++) {
				Point p = new Point(x, y);
				if (getTile(p) == MinesweeperBoard.HIDDEN) {
					revealTile(p);
					return;
				}
			}
	}

	private void revealTile(Point p) {
		board.revealTile(p.y, p.x);
		if (!board.hasRevealedMine())
			constraints.add(new Constraint(getNeighbors(p), board.getTile(p.y, p.x)));
	}

	public MinesweeperBoard getBoard() {
		return board;
	}

	public Set<Point> getFlaggedTiles() {
		return flaggedTiles;
	}

	private class Constraint {
		private Set<Point> points;
		private int sum;

		private Constraint(Set<Point> points, int sum) {
			this.points = points;
			this.sum = sum;
		}

		private boolean isSolved() {
			for (Point p : points.stream().collect(Collectors.toList())) {
				if (flaggedTiles.contains(p)) {
					points.remove(p);
					sum--;
				}
				else if (getTile(p) != MinesweeperBoard.HIDDEN)
					points.remove(p);
			}

			return sum == 0 || points.size() == sum;
		}

		private Set<Point> getMineTiles() {
			if (points.size() != sum)
				return new HashSet<>();
			return points;
		}

		private Set<Point> getSafeTiles() {
			if (sum != 0)
				return new HashSet<>();
			return points;
		}

		public String toString() {
			String pString = points.stream().map(p -> String.format("<%s,%s>", p.y, p.x)).collect(Collectors.joining("+", "(", ")"));

			return String.format("%s = %d", pString, sum);
		}
	}

	private int getTile(Point p) {
		return board.getTile(p.y, p.x);
	}

	private Set<Point> getNeighbors(Point p) {
		Set<Point> neighbors = new HashSet<>();
		for (int x = Math.max(0, p.x - 1); x < Math.min(board.getWidth(), p.x + 2); x++)
			for (int y = Math.max(0, p.y - 1); y < Math.min(board.getHeight(), p.y + 2); y++)
				neighbors.add(new Point(x, y));

		return neighbors;
	}

	public boolean isGameFinished() {
		return board.hasRevealedMine() || isWin();
	}

	private int countRevealedTiles(){
		int revealedTileCount = 0;
		for (int x = 0; x < board.getWidth(); x++)
			for (int y = 0; y < board.getHeight(); y++)
				if (getTile(new Point(x, y)) != MinesweeperBoard.HIDDEN)
					revealedTileCount++;

		return revealedTileCount;
	}

	public Boolean isWin() {
		int revealedTileCount = countRevealedTiles();

		if (revealedTileCount == 0 && board.hasRevealedMine())
			return null; //what the hell kinda game lets you lose on the first move?!

		return !board.hasRevealedMine() && ((board.getHeight() * board.getWidth()) - board.getMines() == revealedTileCount);
	}

	public String toString() {
		return String.format("-------------------Constraints-------------------\n%s\n-------------------------------------------------\n%s",
				constraints.stream().map(Constraint::toString).collect(Collectors.joining("\n", "\t", "")), board);
	}
}
