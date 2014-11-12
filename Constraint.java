import java.awt.Point;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * represents a constraint such that the sum of all mines in points is equal to sum
 */
public class Constraint {
	private final Set<Point> points;
	private int sum;

	public Constraint(Set<Point> points, int sum) {
		this.points = points;
		this.sum = sum;
	}

	public Set<Point> getPoints(){
		return points;
	}

	public String toString() {
		String pString = points.stream()
				.sorted((p1, p2) -> {
					int dx = p1.x - p2.x;
					return dx != 0 ? dx : (p1.y - p2.y);
				})
				.map(p -> String.format("<%s,%s>", p.y, p.x))
				.collect(Collectors.joining("+", "(", ")"));

		return String.format("%s = %d", pString, sum);
	}

	/**
	 * Simplify this constraint based on the knowledge provided in knownTiles
	 * @param knownTiles
	 */
	public void reduce(Map<Point, Boolean> knownTiles) {
		for (Map.Entry<Point, Boolean> entry : knownTiles.entrySet())
			if (points.remove(entry.getKey()))
				sum -= entry.getValue() ? 1 : 0;
	}

	/**
	 * returns a configuration of either all mines or all empty tiles, if such a configuration is possible based on this
	 * constraint.  Otherwise returns null
	 * @return
	 */
	public Map<Point, Boolean> getTriviallySatisfiableConfiguration() {
		if (sum != 0 && sum != points.size())
			return null;

		return points.stream().collect(Collectors.toMap(p -> p, p -> sum != 0));
	}

	public Boolean isTriviallySatisfied() {
		return sum == 0 || sum == points.size();
	}

	/**
	 * returns true if the given tile configuration fully resolves this constraint, false if it does not, and null if the
	 * provided configuration is inconclusive
	 * @param tiles
	 * @return
	 */
	public Boolean isSatisfied(Map<Point, Boolean> tiles) {
		int count = 0, sum = 0;
		for (Point p : this.points)
			if (tiles.containsKey(p)) {
				count++;
				sum += (tiles.get(p) ? 1 : 0);

				if (sum > this.sum)
					return false;
			}

		if (count == points.size())
			return sum == this.sum;

		return null;
	}

	/**
	 * returns whether the given constraint shares any tiles with this constraint
	 * @param constraint
	 * @return
	 */
	public boolean intersects(Constraint constraint) {
		return this.points.stream().anyMatch(constraint.points::contains);
	}
}