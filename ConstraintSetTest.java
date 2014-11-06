import org.junit.Assert;
import org.junit.Test;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ConstraintSetTest {

	@Test
	public void testCalculateProbabilities1() throws Exception {
		Point a = new Point(0, 1), b = new Point(1, 0), c = new Point(1, 1), d = new Point(2, 1);

		ConstraintSet constraints = buildConstraintSet(buildConstraint(2, a, b, c), buildConstraint(2, b, c, d));

		Map<Point, Double> probs = constraints.calculateProbabilities();
		Assert.assertEquals(2 / (double) 3, probs.get(a), 0.0);
		Assert.assertEquals(2 / (double) 3, probs.get(b), 0.0);
		Assert.assertEquals(2 / (double) 3, probs.get(c), 0.0);
		Assert.assertEquals(2 / (double) 3, probs.get(d), 0.0);
	}

	@Test
	public void testCalculateProbabilities2() throws Exception {
		Point a = new Point(0, 1), b = new Point(1, 0), c = new Point(1, 1), d = new Point(2, 1);

		ConstraintSet constraints = buildConstraintSet(buildConstraint(2, a, b, c), buildConstraint(1, b, c, d));

		Map<Point, Double> probs = constraints.calculateProbabilities();
		Assert.assertEquals(1.0, probs.get(a), 0.0);
		Assert.assertEquals(0.5, probs.get(b), 0.0);
		Assert.assertEquals(0.5, probs.get(c), 0.0);
		Assert.assertEquals(0.0, probs.get(d), 0.0);
	}

	@Test
	public void testFailedConstraintGuess() throws Exception {
		ConstraintSet constraints = buildConstraintSet(
				buildConstraint(1, new Point(2, 0), new Point(2, 1)),
				buildConstraint(1, new Point(2, 0), new Point(2, 1)),
				buildConstraint(2, new Point(0, 3), new Point(1, 3), new Point(2, 1), new Point(2, 3)),
				buildConstraint(1, new Point(0, 3), new Point(1, 3)),
				buildConstraint(2, new Point(1, 3), new Point(2, 1), new Point(2, 3), new Point(3, 1), new Point(3, 2)),
				buildConstraint(2, new Point(2, 3), new Point(2, 4), new Point(3, 2), new Point(3, 4), new Point(4, 2), new Point(4, 3), new Point(4, 4)));
		System.out.println("Constraints:\n" + constraints);
		System.out.println("-------------------------------------");
		System.out.println("Satisfying Configurations:\n" + debugSatisfyingConfigurations(constraints.findSatisfyingConfigurations()));
		System.out.println("-------------------------------------");
		System.out.println("Probabilities:\n" + debugProbabilities(constraints.calculateProbabilities()));
	}

	private static String printPoint(Point p) {
		return String.format("<%d,%d>", p.y, p.x);
	}

	private static String debugProbabilities(Map<Point, Double> probabilities){
		StringBuilder sb = new StringBuilder();

		probabilities.entrySet().stream()
				.sorted((e1, e2)->{
					int dp = (int)(((e1.getValue()*10000))-((e2.getValue()*10000)));
					if(dp != 0)
						return dp;
					int dx = e1.getKey().x - e2.getKey().x;
					return dx != 0 ? dx : e1.getKey().y - e2.getKey().y;
				}).forEach(e -> sb.append(String.format("%s:%s\n", printPoint(e.getKey()), e.getValue())));

		return sb.toString();
	}

	private static String debugSatisfyingConfigurations(List<Map<Point, Boolean>> configurations){
		StringBuilder sb = new StringBuilder();

		for (Map<Point, Boolean> configuration : configurations) {
			configuration.keySet().stream()
					.sorted((p1, p2) -> {
						int dx = p1.x - p2.x;
						return dx != 0 ? dx : p1.y - p2.y;
					}).forEach(p -> sb.append(configuration.get(p) ? "1" : "0"));
			sb.append("\n");
		}

		return sb.toString();
	}


	private static ConstraintSet buildConstraintSet(Constraint... constraints){
		ConstraintSet constraintSet = new ConstraintSet();
		for (Constraint constraint : constraints) constraintSet.add(constraint);

		return constraintSet;
	}

	private static Constraint buildConstraint(int sum, Point... points){
		return new Constraint(new HashSet<>(Arrays.asList(points)), sum);
	}
}