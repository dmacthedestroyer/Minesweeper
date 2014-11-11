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

	private List<ArrayList<Constraint>> getIntersectingConstraints() {
		ArrayList<ArrayList<Constraint>> groups = new ArrayList<>();

		for (Constraint constraint : constraints) {
			ArrayList<Constraint> group;
			Optional<ArrayList<Constraint>> existingGroup = groups.stream()
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

	public List<Map<Point, Boolean>> findSatisfyingConfigurations() {
		List<Map<Point, Boolean>> satisfyingConfigurations = new ArrayList<>();
		for (List<Constraint> group : getIntersectingConstraints())
			for (Map<Point, Boolean> c : findSatisfyingConfigurations(group))
				satisfyingConfigurations.add(c);

		return satisfyingConfigurations;
	}

	private List<Map<Point, Boolean>> findSatisfyingConfigurations(List<Constraint> constraints) {
		List<Map<Point, Boolean>> satisfyingConfigurations = new ArrayList<>();
		for (Point point : constraints.stream().flatMap(c -> c.getPoints().stream()).collect(Collectors.toSet()))
			satisfyingConfigurations = findSatisfyingConfigurations(satisfyingConfigurations, point);

		return satisfyingConfigurations;
	}

	private List<Map<Point, Boolean>> findSatisfyingConfigurations(List<Map<Point, Boolean>> existingConfigurations, Point newPoint) {
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