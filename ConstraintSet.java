import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ConstraintSet {
	public class MineProbabilities {
		private final Map<Point, Double> probabilities;
		private final double avgMines;

		public MineProbabilities(Map<Point, Double> probabilities, double avgMines) {
			this.probabilities = probabilities;
			this.avgMines = avgMines;
		}

		public Map<Point, Double> getProbabilities() {
			return probabilities;
		}

		public double getAvgMines() {
			return avgMines;
		}
	}

	private final List<Constraint> constraints;

	public ConstraintSet() {
		constraints = new ArrayList<>();
	}

	/**
	 * returns the first constraint which is resolved to contain either all empty tiles or all mines, or null if no such
	 * constraint exists
	 * @param knownTiles
	 * @return
	 */
	public Constraint findTriviallySatisfiedConstraint(Map<Point, Boolean> knownTiles) {
		for (int i = 0; i < constraints.size(); i++) {
			constraints.get(i).reduce(knownTiles);
			if (constraints.get(i).getPoints().size() == 0)
				constraints.remove(i--);
			else if (constraints.get(i).isTriviallySatisfied())
				return constraints.remove(i);
		}

		return null;
	}

	public boolean add(Constraint c) {
		return constraints.add(c);
	}

	public String toString() {
		return constraints.stream().map(Constraint::toString).collect(Collectors.joining("\n"));
	}

	/**
	 * Finds the probability of any given tile having a mine.  This is done by:
	 *  1) combining all intersecting constraints (have at least one tile in common)
	 *  2) finding all satisfying configurations of those grouped constraints
	 *  3) calculating the occurrence of a mine for each tile in the set of satisfying configurations
	 * @return The list of tiles and their probabilities, as well as the average number of mines from all satisfying
	 * configurations
	 */
	public MineProbabilities calculateProbabilities() {
		List<Map<Point, Boolean>> configurations = findSatisfyingConfigurations();
		if (configurations.isEmpty())
			return new MineProbabilities(new HashMap<>(), 0);

		Map<Point, Integer> mineCounts = new HashMap<>();
		List<Long> mineTotals = new ArrayList<>();

		for (Map<Point, Boolean> configuration : configurations) {
			long mineTotal = 0;
			for (Point key : configuration.keySet()) {
				if (!mineCounts.containsKey(key))
					mineCounts.put(key, 0);
				if (configuration.get(key)) {
					mineTotal++;
					mineCounts.put(key, 1 + mineCounts.get(key));
				}
			}
			mineTotals.add(mineTotal);
		}

		Map<Point, Double> isMineProbabilities = mineCounts.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() / (double) configurations.size()));

		double avgMines = mineTotals.stream().mapToLong(i -> i).average().orElse(0.0);

		return new MineProbabilities(isMineProbabilities, avgMines);
	}

	/**
	 * Returns all groupings of constraints that intersect each other
	 * @return
	 */
	private List<List<Constraint>> getIntersectingConstraints() {
		ArrayList<List<Constraint>> groups = new ArrayList<>();

		for (Constraint constraint : this.constraints) {
			List<Constraint> group;
			Optional<List<Constraint>> existingGroup = groups.stream()
					.filter(g -> g.stream().anyMatch(constraint::intersects))
					.findFirst();

			if (existingGroup.isPresent())
				group = existingGroup.get();
			else
				groups.add(group = new ArrayList<>());

			group.add(constraint);
		}

		return groups;
	}

	/**
	 * returns all variations of mine configurations based on the constraints contained in this ConstraintSet
	 * @return
	 */
	public List<Map<Point, Boolean>> findSatisfyingConfigurations() {
		List<Map<Point, Boolean>> satisfyingConfigurations = new ArrayList<>();
		for (List<Constraint> group : getIntersectingConstraints())
			for (Map<Point, Boolean> c : findSatisfyingConfigurations(group))
				satisfyingConfigurations.add(c);

		return satisfyingConfigurations;
	}

	/**
	 * finds all variations of mine configurations that satisfy the given group of constraints
	 * @param constraints
	 * @return
	 */
	private List<Map<Point, Boolean>> findSatisfyingConfigurations(List<Constraint> constraints) {
		List<Map<Point, Boolean>> satisfyingConfigurations = new ArrayList<>();
		for (Point point : constraints.stream().flatMap(c -> c.getPoints().stream()).collect(Collectors.toSet()))
			satisfyingConfigurations = introduceNewPoint(satisfyingConfigurations, point);

		return satisfyingConfigurations;
	}

	/**
	 * Introduces the additional variable, newPoint, to the existing configurations such that doing so will provide a list
	 * of all configurations that match all points in existingConfigurations as well as with newPoint
	 * @param existingConfigurations
	 * @param newPoint
	 * @return
	 */
	private List<Map<Point, Boolean>> introduceNewPoint(List<Map<Point, Boolean>> existingConfigurations, Point newPoint) {
		List<Map<Point, Boolean>> satisfyingConfigurations = new ArrayList<>();
		if (existingConfigurations.size() == 0) {
			for (boolean isMine : new boolean[]{true, false}) {
				Map<Point, Boolean> rootConfiguration = new HashMap<>();
				rootConfiguration.put(newPoint, isMine);
				satisfyingConfigurations.add(rootConfiguration);
			}

			return satisfyingConfigurations;
		}

		for (boolean isMine : new boolean[]{true, false}) {
			for (Map<Point, Boolean> configuration : existingConfigurations) {
				Map<Point, Boolean> shallowCopy = new HashMap<>(configuration);
				shallowCopy.put(newPoint, isMine);
				if (this.constraints.stream()
						.map(c -> c.isSatisfied(shallowCopy))
						.allMatch(b -> b == null || b))
					satisfyingConfigurations.add(shallowCopy);
			}
		}

		return satisfyingConfigurations;
	}
}