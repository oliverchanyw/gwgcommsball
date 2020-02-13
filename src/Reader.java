import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Reader {

    final static String CSVFILE = "/Users/oliverchan/gwg/GWG Commissioning Ball Survey.csv";
    public final static int LIKE = 10;
    public final static int DISLIKE = -30;
    public final static int SECTION = 0;
    public final static int PLATOON = 0;

    public static Preference read() {
        BufferedReader br = null;
        String line;
        Preference prefs = new Preference();

        try {

            br = new BufferedReader(new FileReader(CSVFILE));
            while ((line = br.readLine()) != null) {

                String[] splitLine = line.split(","); // name, section, likes, dislikes
                String name = splitLine[0];
                int section = Integer.parseInt(splitLine[1]);
                Set<String> likes = Arrays.stream(splitLine[2].split(";")).collect(Collectors.toSet());
                Set<String> dislikes = Arrays.stream(splitLine[3].split(";")).collect(Collectors.toSet());

                prefs.parse(name, section, likes, dislikes);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return prefs;
    }

    public static class Preference {
        public Map<String, Integer> names;
        public Map<Integer, String> revNames;
        public Map<Integer, Integer> sect; // section
        public int[][] prefs; // people preference
        private int counter;

        public Preference() {
            names = new HashMap<>();
            prefs = new int[45][45];
            revNames = new HashMap<>();
            sect = new HashMap<>();
            counter = 1;
        }

        public void parse(String name, int section, Set<String> likes, Set<String> dislikes) {
            int idx = recognize(name);
            sect.put(idx, section);

            for (String likeable : likes) {
                if (likeable.equals("Rezo")) continue;
                addPref(idx, recognize(likeable), LIKE);
            }

            for (String dislikeable : dislikes) {
                if (dislikeable.equals("Rezo")) continue;
                if (dislikeable.equals("No Preference")) continue;
                addPref(idx, recognize(dislikeable), DISLIKE);
            }
        }

        public int getPref(int i, int j) {
            boolean section = sect.get(i).equals(sect.get(j));
            boolean platoon = ((3.5 - sect.get(i)) * (3.5 - sect.get(j))) > 0;
            return (prefs[i-1][j-1] != 0) ? prefs[i-1][j-1] : section ? SECTION : platoon ? PLATOON : 0;
        }

        private int recognize(String name) {
            if (names.containsKey(name)) return names.get(name);
            else {
                names.put(name, counter);
                revNames.put(counter, name);
                return counter++;
            }
        }

        private void addPref(int i, int j, int strength) {
            prefs[i-1][j-1] = prefs[i-1][j-1] + strength;
            prefs[j-1][i-1] = prefs[i-1][j-1];
        }
    }

    public static class IntPair {
        int i, j;

        public IntPair(int i, int j) {
            this.i = i;
            this.j = j;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof IntPair) {
                IntPair ip2 = (IntPair) o;
                return (this.i == ip2.i && this.j == ip2.j) || (this.i == ip2.j && this.j == ip2.i);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (127 * (i + j)) + (i * j);
        }
    }
}
