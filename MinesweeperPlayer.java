import java.awt.*;
import java.util.*;

public class MinesweeperPlayer {
	/**
	 * Attempts to identify the locations of all mine tiles on a
	 * Minesweeper board.  If the method can deduce the locations of all the
	 * hidden mines without revealing any mine tiles, it should return a
	 * collection of all the hidden mine locations.  If the method reveals a
	 * mine tile during the course of its operation, it should return a
	 * collection of all the hidden mine locations deduced up to that point.
	 *
	 * @param board A Minesweeper board.
	 * @return A collection of locations on the specified board that contain
	 * mine tiles. Note: {@link MinesweeperBoard} uses a row/column
	 * coordinate system while {@link Point} uses an x/y coordinate system.
	 */
	public static Collection<Point> solve(MinesweeperBoard board) {
		MinesweeperSolver s = new MinesweeperSolver(board);
		s.solve();

		return s.getFlaggedTiles();
	}
}
