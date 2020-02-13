import net.sf.javailp.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Solution {

    final static int REPS = 100;
    final static int TIMEOUT = 10;
    Problem problem;
    Reader.Preference prefs;

    public void solveSeating() {

        // Read csv
        prefs = Reader.read();

        Result best = null;

        for (int rep = 0; rep < REPS; rep++)  {
            Result result = solveOnce(TIMEOUT);
            if (best == null || result.getObjective().intValue() > best.getObjective().intValue()) {
                best = result;
                displayResult(result);
            }
        }
        System.out.println("Overall best solution: utility of " + best.getObjective());
        displayResult(best);
    }

    private void displayResult(Result result) {
        Map<Integer, Set<String>> allocation = new HashMap<>();
        IntStream.range(1, 10).forEach(t -> {
            allocation.put(t, new HashSet<>());
            IntStream.range(1, 46).forEach(i -> {
                if (result.getBoolean(varName("x", t, i))) allocation.get(t).add(prefs.revNames.get(i));
            });
        });
        System.out.println("Solution of utility " + result.getObjective().intValue() + " found");
        for (Set<String> table : allocation.values()) System.out.println(table);
    }

    private Result solveOnce(int timeout) {
        // Choose solver
        SolverFactory factory = new SolverFactorySAT4J();
        factory.setParameter(Solver.TIMEOUT, timeout);
        Solver solver = factory.get();

        // werk
        setUpObjective();
        setUpY();
        setUpX();
        return solver.solve(problem);
    }

    private static Set<Reader.IntPair> allPairs() {
        Set<Reader.IntPair> pairs = new HashSet<>();
        for (int i = 1; i <= 45; i++) {
            for (int j = i + 1; j <= 45; j++) {
                pairs.add(new Reader.IntPair(i, j));
            }
        }
        return pairs;
    }

    private static String varName(String prefix, int a, int b, int t) {
        return prefix + String.format("%02d", a) + String.format("%02d", b) + t;
    }

    private static String varName(String prefix, int t, int i) {
        return prefix + t + String.format("%02d", i);
    }

    private static IntStream shuffleStream(int n) {
        List<Integer> list = IntStream.range(1, n + 1).boxed().collect(Collectors.toList());
        Collections.shuffle(list);
        return list.stream().mapToInt(i -> i);
    }

    // Set up objective - maximize sum(y_abt)
    private void setUpObjective() {
        problem = new Problem();
        Linear objective = new Linear();
        for (Reader.IntPair pair : allPairs()) {
            IntStream.range(1, 10)
                    .forEach(t -> objective.add(prefs.getPref(pair.i, pair.j), varName("y", pair.i, pair.j, t)));
        }
        problem.setObjective(objective, OptType.MAX);
    }

    private void setUpY() {
        for (int t = 1; t <= 9; t++) {
            for (Reader.IntPair pair : allPairs()) {
                // y_abt >= x_ta + x_tb - 1
                Linear yesyes = new Linear();
                yesyes.add(1, varName("y", pair.i, pair.j, t));
                yesyes.add(-1, varName("x", t, pair.i));
                yesyes.add(-1, varName("x", t, pair.j));
                problem.add(yesyes, ">=", -1);

                // y_abt <= x_ta && y_abt <= x_tb
                Linear no1 = new Linear();
                no1.add(1, varName("y", pair.i, pair.j, t));
                no1.add(-1, varName("x", t, pair.i));
                problem.add(no1, "<=", 0);

                Linear no2 = new Linear();
                no2.add(1, varName("y", pair.i, pair.j, t));
                no2.add(-1, varName("x", t, pair.j));
                problem.add(no2, "<=", 0);

                // boolean 0-1, integer constraint
                problem.setVarType(varName("y", pair.i, pair.j, t), Boolean.class);
            }
        }
    }

    private void setUpX() {
        // table capacity = 5
        shuffleStream(9).forEach(t -> {
            Linear full = new Linear();
            shuffleStream(45).forEach(i -> full.add(1, varName("x", t, i)));
            problem.add(full, "=", 5);
        });


        // all cadets seated
        shuffleStream(45).forEach(i -> {
            Linear unique = new Linear();
            shuffleStream(9).forEach(t -> unique.add(1, varName("x", t, i)));
            problem.add(unique, "=", 1);
        });

        // boolean 0-1, integer
        shuffleStream(45).forEach(i -> {
            shuffleStream(9).forEach(t -> problem.setVarType(varName("x", t, i), Boolean.class));
        });
    }

    public static void main(String[] args) {
        (new Solution()).solveSeating();
    }
}
