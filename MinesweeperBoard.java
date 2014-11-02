public class MinesweeperBoard {
	/**
	 * Constant representing a revealed mine on the board.
	 */
	public static final int MINE = -1;
	/**
	 * Constant representing a hidden tile.
	 */
	public static final int HIDDEN = -2;

	private int[][] state;
	private int mines;
	private boolean revealedMine;

	/**
	 * Initializes a new <code>MinesweeperBoard</code> object with specific
	 * dimensions and number of mines.  The locations of the mines are chosen
	 * randomly.  All tiles are initially hidden.
	 *
	 * @param width The desired width of the board.
	 * @param height The desired height of the board.
	 * @param mines The desired number of mines on the board.
	 * @throws IllegalArgumentException If the desired width or height of the
	 * board is non-positive, if the desired number of mines is negative, or if
	 * the desired number of mines is larger than desired number of tiles on
	 * the board.
	 */
	public MinesweeperBoard(int width, int height, int mines) {
		if (width < 1 || height < 1 || mines < 0 || mines > width * height)
			throw new IllegalArgumentException();

		state = new int[height][width];
		this.mines = mines;

		int[] indices = new int[width * height];
		for (int i = 0; i < indices.length; i++)
			indices[i] = i;
		for (int i = 0; i < mines; i++) {
			int rand = (int)(Math.random() * (indices.length - i));
			state[indices[rand] / width][indices[rand] % width] = -10;
			indices[rand] = indices[indices.length - i - 1];
		}
		calculateAdjacentCounts();
	}

	/**
	 * Initializes a new <code>MinesweeperBoard</code> object with mines at
	 * specified locations.  The <code>mines</code> array should be a
	 * rectangular (non-ragged) array in row-major order.  All non-zero
	 * elements are interpreted as locations that should contain mines.
	 * All tiles are initially hidden.
	 *
	 * @param mines An array of mine location information.
	 * @throws IllegalArgumentException If the implied width or height of the
	 * board is non-positive, or if the <code>mines</code> array is not
	 * rectangular.
	 */
	public MinesweeperBoard(int[][] mines) {
		if (mines == null || mines.length < 1 || mines[0].length < 1)
			throw new IllegalArgumentException();

		state = new int[mines.length][mines[0].length];

		for (int row = 0; row < getHeight(); row++) {
			if (mines[row].length != getWidth())
				throw new IllegalArgumentException();
			for (int col = 0; col < getWidth(); col++)
				if (mines[row][col] != 0) {
					state[row][col] = -10;
					this.mines++;
				}
		}

		calculateAdjacentCounts();
	}

	/**
	 * Returns the width of this board.
	 *
	 * @return The width of this board.
	 */
	public int getWidth() {
		return state[0].length;
	}

	/**
	 * Returns the height of this board.
	 *
	 * @return The height of this board.
	 */
	public int getHeight() {
		return state.length;
	}

	/**
	 * Returns the number of mines on this board.
	 *
	 * @return The number of mines on this board.
	 */
	public int getMines() {
		return mines;
	}

	/**
	 * Indicates whether a mine tile has been revealed, signifying that the
	 * game for this board is over (a loss).
	 *
	 * @return <code>true</code> if a mine tile has been revealed, or
	 * <code>false</code> otherwise.
	 */
	public boolean hasRevealedMine() {
		return revealedMine;
	}

	/**
	 * Returns the visible state of a specified tile.  The visible state of a
	 * tile is {@link #HIDDEN} if the tile is hidden, {@link #MINE} if the
	 * tile is a mine, or a number between 0 and 8 (inclusive) indicating the
	 * number of mine tiles adjacent (8-way) to the specified tile.
	 *
	 * @param row The row of the tile to examine.
	 * @param column The column of the tile to examine.
	 * @return The visible state of the specified tile, either {@link #HIDDEN},
	 * {@link #MINE}, or an integer in the range [0, 8].
	 * @throws IllegalArgumentException If the coordinates of the specified
	 * tile do not exist on this board.
	 */
	public int getTile(int row, int column) {
		if (row < 0 || column < 0 || row >= getHeight() || column >= getWidth())
			throw new IllegalArgumentException();

		if (state[row][column] < 0)
			return HIDDEN;
		if (state[row][column] == 10)
			return MINE;
		return state[row][column] - 1;
	}

	/**
	 * Reveals a specified tile.  The visible state of the tile is changed and
	 * may be examined via {@link #getTile(int,int)}.  Note: once a mine tile
	 * is revealed, no further tiles may be revealed (unless the board is reset).
	 *
	 * @param row The row of the tile to reveal.
	 * @param column The column of the tile to reveal.
	 * @throws IllegalStateException If a mine has been revealed previously on
	 * this board or if the specified tile has been revealed previously.
	 * @throws IllegalArgumentException If the coordinates of the specified
	 * tile do not exist on this board.
	 */
	public void revealTile(int row, int column) {
		if (revealedMine || state[row][column] >= 0)
			throw new IllegalStateException();
		if (row < 0 || column < 0 || row >= getHeight() || column >= getWidth())
			throw new IllegalArgumentException();

		state[row][column] = -state[row][column];
		revealedMine = state[row][column] == 10;
	}

	/**
	 * Resets this board to its initial configuration where all tiles are
	 * hidden.
	 */
	public void reset() {
		for (int row = 0; row < getHeight(); row++)
			for (int col = 0; col < getWidth(); col++)
				if (state[row][col] > 0)
					state[row][col] = -state[row][col];
		revealedMine = false;
	}

	/**
	 * Returns a string representation of this board based on the current set
	 * of revealed tiles.
	 *
	 * @return A string representation of this board.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int row = 0; row < getHeight(); row++) {
			for (int col = 0; col < getWidth(); col++)
				if (state[row][col] < 0)
					sb.append(' ');
				else if (state[row][col] == 10)
					sb.append('*');
				else
					sb.append(state[row][col] - 1);
			sb.append('\n');
		}
		return sb.toString();
	}

	private void calculateAdjacentCounts() {
		for (int row = 0; row < getHeight(); row++)
			for (int col = 0; col < getWidth(); col++) {
				if (state[row][col] == -10)
					continue;
				for (int drow = -1; drow <= 1; drow++)
					for (int dcol = -1; dcol <= 1; dcol++)
						if (col + dcol >= 0 && col + dcol < getWidth() && row + drow >= 0 && row + drow < getHeight() && state[row+drow][col+dcol] == -10)
							state[row][col]++;
				state[row][col] = -(1 + state[row][col]);
			}
	}
}
