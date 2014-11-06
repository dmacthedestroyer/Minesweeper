import java.awt.Point;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

	public void reduce(Map<Point, Boolean> knownTiles) {
		for (Map.Entry<Point, Boolean> entry : knownTiles.entrySet())
			if (points.remove(entry.getKey()))
				sum -= entry.getValue() ? 1 : 0;
	}

	public Map<Point, Boolean> getSatisfiableConfiguration() {
		if (sum != 0 && sum != points.size())
			return null;

		return points.stream().collect(Collectors.toMap(p -> p, p -> sum != 0));
	}

	public Boolean isSatisfied() {
		return sum == 0 || sum == points.size();
	}

	public boolean isEmpty() {
		return points.size() == 0;
	}

	public Boolean isSatisfied(Map<Point, Boolean> m) {
		int count = 0, sum = 0;
		for (Point p : points)
			if (m.containsKey(p)) {
				count++;
				sum += (m.get(p) ? 1 : 0);
			}

		if (count == points.size())
			return sum == this.sum;

		if (sum > this.sum)
			return false;

		return null;
	}

	public boolean intersects(Constraint constraint) {
		return points.stream().anyMatch(constraint.points::contains);
	}
}