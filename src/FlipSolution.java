import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FlipSolution {

    public Reader.Preference prefs;
    public int utility;
    public Map<Integer, Integer> alloc; // cadet to table
    public Map<Integer, Set<Integer>> revAlloc; // table to cadet


    public FlipSolution() {
        utility = 0;
        alloc = new HashMap<>();
        revAlloc = new HashMap<>();
    }

    private void trySwaps() {
        shuffleStream(45).boxed()
                .sorted((Comparator.comparingInt(this::stayValue)))
                .forEachOrdered(a -> shuffleStream(45)
                            .filter(b -> !revAlloc.get(alloc.get(a)).contains(b))
                            .filter(b -> swapValue(a, b) >= 0)
                            .findAny().ifPresent(b -> swap(a, b))
                );
    }

    private void randomlyAllocate() {
        int[] ordering = shuffleStream(45).toArray();
        for (int i = 0; i < 45; i++) {
            alloc.put(ordering[i], i / 5);
            if (i % 5 == 0) revAlloc.put(i / 5, new HashSet<>());
            revAlloc.get(i / 5).add(ordering[i]);
        }
    }

    private void swap(int a, int b) {
        utility += swapValue(a, b);

        int ta = alloc.get(a);
        int tb = alloc.get(b);

        alloc.put(a, tb);
        alloc.put(b, ta);

        revAlloc.get(ta).remove(a);
        revAlloc.get(tb).remove(b);
        revAlloc.get(ta).add(b);
        revAlloc.get(tb).add(a);
    }

    private Set<Integer> table(int i) {
        return revAlloc.get(alloc.get(i));
    }
    private static IntStream shuffleStream(int n) {
        List<Integer> list = IntStream.range(1, n + 1).boxed().collect(Collectors.toList());
        Collections.shuffle(list);
        return list.stream().mapToInt(i -> i);
    }

    private int stayValue(int a) {
        return table(a).stream().reduce(0, (accum, i) -> accum + prefs.getPref(a, i)) - prefs.getPref(a, a);
    }

    private int swapValue(int a, int b) {
        return  - table(a).stream().mapToInt(i -> prefs.getPref(a, i)).sum()
                - table(b).stream().mapToInt(i -> prefs.getPref(b, i)).sum()
                + table(a).stream().mapToInt(i -> prefs.getPref(b, i)).sum()
                + table(b).stream().mapToInt(i -> prefs.getPref(a, i)).sum()
                - (2 * prefs.getPref(a, b)) + prefs.getPref(a, a) + prefs.getPref(b, b);
    }

    public int solveSeating() {
        prefs = Reader.read();
        randomlyAllocate();
        for (int i = 0; i < 150; i++) trySwaps();

        System.out.println("Solution of utility " + utility + " found");
        return utility;
    }

    public void printSolution() {
        revAlloc.values().stream().map(t -> t.stream().map(i -> prefs.revNames.get(i)).collect(Collectors.toSet()))
                .forEach(System.out::println);
    }

    public static void main(String[] args) {
        FlipSolution best = null;
        for (int i = 0; i < 100; i++) {
            FlipSolution sol = new FlipSolution();
            sol.solveSeating();

            if (best == null || sol.utility > best.utility) {
                best = sol;
                best.printSolution();
            }

            if (best.utility >= 1300) break;
        }

        System.out.println("All done, final solution of utility " + best.utility);
        best.printSolution();
    }
}
