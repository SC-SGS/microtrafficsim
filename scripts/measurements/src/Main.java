import java.io.File;
import java.util.*;


/**
 * @author Dominic Parga Cacheiro
 */
public class Main {
    private static final String sep                 = System.getProperty("file.separator");
    private static final String PATH_TO_DATA_INPUT  = "data" + sep + "input";
    private static final String PATH_TO_DATA_OUTPUT = "data" + sep + "csv";
    private static FileManager  fileManager;

    private enum VisType {
        age("age", "age_when_despawning.csv"),
        anger("anger", "anger_per_timestep.csv"),
        linear_distance("linear_distance", "linear_distance_per_despawn_age.csv"),
        spawned_count("spawned_count", "spawned_count_per_timestep.csv"),
        total_anger("total_anger", "total_anger_when_despawning.csv");

        private final String pathToInputData;
        private final String pathToOutputData;
        private final String avgFilename;

        private VisType(String typename, String avgFilename) {
            this.pathToInputData = PATH_TO_DATA_INPUT + sep + typename;
            pathToOutputData     = PATH_TO_DATA_OUTPUT;
            this.avgFilename     = avgFilename;
        }
    }

    public static void main(String[] args) {
        File file;
        fileManager = new FileManager();

        if (args.length == 1) {
            switch (args[0]) {
            case "-h":
            case "--help": printUsage(); return;
            case "age": age();
            case "anger": anger();
            case "linear_distance": linearDistance();
            case "spawned_count": spawnedCount();
            case "total_anger": totalAnger();
            case "all":
                age();
                anger();
                linearDistance();
                spawnedCount();
                totalAnger();
            }
        }
        System.out.println("age() started");
        age();
        System.out.println("anger() started");
        anger();
        System.out.println("linearDistance() started");
        linearDistance();
        System.out.println("spawnedCount() started");
        spawnedCount();
        System.out.println("totalAnger() started");
        totalAnger();
    }

    public static void printUsage() {
        System.out.println("MicroTrafficSim - precalc average data");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("  precalcAvg                Nothing happens.");
        System.out.println("  precalcAvg (type)         Run this example with the specified map-file.");
        System.out.println("  simulation --help | -h    Show this help message.");
        System.out.println("");
        System.out.println("  type");
        System.out.println("  age                       output: age_when_despawning.csv");
        System.out.println("  anger                     SimStep -> avg anger(spawned)");
        System.out.println("  linear_distance           TODO");
        System.out.println("  spawned_count             SimStep -> # spawned vehicles");
        System.out.println("  total_anger               total anger when despawning -> # vehicles");
        System.out.println("  all                       all cases");
        // (anger | total_anger | spawned_count | t_despawn | t_lifetime) [options]
    }

    private static ArrayList<File> getdirectoryList(String path) {
        File dir                      = new File(path);
        File[] protodirectoryList     = dir.listFiles();
        ArrayList<File> directoryList = new ArrayList<>();
        if (directoryList != null)
            for (File file : protodirectoryList)
                if (file.getName().endsWith(".csv")) directoryList.add(file);

        return directoryList;
    }

    private static void processInts(VisType vistype, boolean withTotalDespawnCounter) {
        ArrayList<File> directoryList = getdirectoryList(vistype.pathToInputData);
        if (!directoryList.isEmpty()) {
            HashMap<Integer, Number> buxtehude = new HashMap<>();
            StringBuilder data           = new StringBuilder();
            int           totalDespawned = 0;

            for (File file : directoryList) {
                // get data from file
                Scanner scanner = fileManager.openScanner(file);
                while (scanner.hasNext()) {
                    totalDespawned++;
                    int key = Integer.parseInt(scanner.next());

                    Number value = buxtehude.get(key);
                    if (value == null)
                        buxtehude.put(key, 1);
                    else
                        buxtehude.put(key, value.intValue() + 1);
                }
                fileManager.closeScanner(file);
            }

            // process data
            float n = directoryList.size();
            if (withTotalDespawnCounter)
                data.append((totalDespawned / n) + ";0;0;");
            else
                data.append("0;0;");

            LinkedList<Integer> list = new LinkedList<>(buxtehude.keySet());
            Collections.sort(list);
            float             lastPercent = 0f;
            float             curPercent  = 0f;
            float             delta       = 0.05f;
            Iterator<Integer> keys        = list.iterator();
            while (keys.hasNext()) {
                int key      = keys.next();
                int rawvalue = buxtehude.get(key).intValue();

                curPercent = curPercent + rawvalue / (float) totalDespawned;
                if (curPercent >= lastPercent + delta || !keys.hasNext()) {
                    data.append(key + ";" + curPercent);
                    if (keys.hasNext()) {
                        data.append(";");
                        lastPercent = curPercent;
                    }
                }
            }
            fileManager.writeDataToFile(vistype.pathToOutputData, vistype.avgFilename, data.toString(), true);
        } else {
            // Handle the case where dir is not really a directory.
            // Checking dir.isDirectory() above would not be sufficient
            // to avoid race conditions with another process that deletes
            // directories.
        }
    }

    private static void processFloatsWithTotalDespawnCounter(VisType vistype) {

        int roundFactor = 10;

        ArrayList<File> directoryList = getdirectoryList(vistype.pathToInputData);
        if (!directoryList.isEmpty()) {
            HashMap<Integer, Number> buxtehude = new HashMap<>();
            StringBuilder data           = new StringBuilder();
            int           totalDespawned = 0;

            for (File file : directoryList) {
                // get data from file
                Scanner scanner = fileManager.openScanner(file);
                while (scanner.hasNext()) {
                    totalDespawned++;
                    int key = Math.round(roundFactor * Float.parseFloat(scanner.next()));

                    Number value = buxtehude.get(key);
                    if (value == null)
                        buxtehude.put(key, 1);
                    else
                        buxtehude.put(key, value.intValue() + 1);
                }
                fileManager.closeScanner(file);
            }

            // process data
            float n = directoryList.size();
            data.append((totalDespawned / n) + ";0;0;");

            LinkedList<Integer> list = new LinkedList<>(buxtehude.keySet());
            Collections.sort(list);
            float             lastPercent = 0f;
            float             curPercent  = 0f;
            float             delta       = 0.05f;
            Iterator<Integer> keys        = list.iterator();
            while (keys.hasNext()) {
                int key      = keys.next();
                int rawvalue = buxtehude.get(key).intValue();

                curPercent = curPercent + rawvalue / (float) totalDespawned;
                if (curPercent >= lastPercent + delta || !keys.hasNext()) {
                    float x = key / (float) roundFactor;
                    data.append(x + ";" + curPercent);
                    if (keys.hasNext()) {
                        data.append(";");
                        lastPercent = curPercent;
                    }
                }
            }
            fileManager.writeDataToFile(vistype.pathToOutputData, vistype.avgFilename, data.toString(), true);
        } else {
            // Handle the case where dir is not really a directory.
            // Checking dir.isDirectory() above would not be sufficient
            // to avoid race conditions with another process that deletes
            // directories.
        }
    }

    /*
    |=======|
    | cases |
    |=======|
    */
    /**
     * filename:<br>
     * age_when_despawning.csv
     * <p>
     * type (in order):<br>
     * int; ...
     * <p>
     * order:<br>
     * age_0; age_1; ...
     */
    private static void age() {
        processInts(VisType.age, true);
    }

    /**
     * filename:<br>
     * anger_per_timestep.csv
     * <p>
     * type (in order):<br>
     * int; int; int; ... int;<br>
     * ...
     * <p>
     * order:<br>
     * n; simulation_age; anger_1; ...; anger_n;<br>
     * m; simulation_age; anger_1; ...; anger_m;<br>
     * ...
     */
    private static void anger() {
        ArrayList<File> directoryList = getdirectoryList(VisType.anger.pathToInputData);
        if (!directoryList.isEmpty()) {
            StringBuilder data = new StringBuilder();
            data.append("0;0;0;0;0;0;0");

            ArrayList<Scanner> scanners = new ArrayList<>(directoryList.size());
            for (File file : directoryList) {
                scanners.add(fileManager.openScanner(file));
            }

            int timestep = 1;
            // while there is a timestep
            while (!scanners.isEmpty()) {

                // check if all scanner has next
                boolean allScannersHasNext = true;
                for (Scanner scanner : scanners) {
                    allScannersHasNext &= scanner.hasNext();
                }
                while (allScannersHasNext) {

                    // for each timestep:
                    long               sum             = 0;
                    long               totalValueCount = 0;
                    ArrayList<Integer> angers          = new ArrayList<>();
                    for (Scanner scanner : scanners) {
                        // for boxfake
                        int valueCount = Integer.parseInt(scanner.next());
                        scanner.next();    // timestep shit
                        totalValueCount += valueCount;
                        for (int i = 0; i < valueCount; i++) {
                            try {
                                int anger = Integer.parseInt(scanner.next());
                                angers.add(anger);
                                sum += anger;    // for avg
                            } catch (NoSuchElementException e) {
                                System.out.println(scanner.hashCode());
                                System.out.println(timestep);
                            }
                        }
                        // for scanner
                        allScannersHasNext &= scanner.hasNext();
                    }

                    // calculate this timestep
                    if (!angers.isEmpty()) {
                        data.append(";" + timestep++ + ";");
                        data.append(sum / (float) totalValueCount + ";");    // mean
                        Collections.sort(angers);
                        int n = angers.size() - 1;
                        data.append(angers.get(0) + ";");            // min
                        data.append(angers.get(n / 4) + ";");        // low quartile
                        data.append(angers.get(n / 2) + ";");        // median
                        data.append(angers.get(n * 3 / 4) + ";");    // high quartile
                        data.append(angers.get(n));                  // max
                    }
                }

                // remove finished scanners
                LinkedList<Scanner> finished = new LinkedList<>();
                for (Scanner scanner : scanners)
                    if (!scanner.hasNext()) {
                        finished.add(scanner);
                        fileManager.closeScanner(scanner);
                    }
                scanners.removeAll(finished);
            }

            // write to file
            fileManager.writeDataToFile(
                    VisType.anger.pathToOutputData, VisType.anger.avgFilename, data.toString(), true);

            // filter file
            Scanner scanner = fileManager.openScanner(
                    new File(VisType.anger.pathToOutputData + sep + VisType.anger.avgFilename));
            float lastAvg          = 0f;
            int   lastMin          = 0;
            int   lastLowQuartile  = 0;
            int   lastMedian       = 0;
            int   lastHighQuartile = 0;
            int   lastMax          = 0;
            data                   = new StringBuilder();
            // start filter
            float delta = 0.5f;
            while (scanner.hasNext()) {
                timestep           = Integer.parseInt(scanner.next());
                float avg          = Float.parseFloat(scanner.next());
                int   min          = Integer.parseInt(scanner.next());
                int   lowQuartile  = Integer.parseInt(scanner.next());
                int   median       = Integer.parseInt(scanner.next());
                int   highQuartile = Integer.parseInt(scanner.next());
                int   max          = Integer.parseInt(scanner.next());

                boolean newPoint = false;
                newPoint |= Math.abs(lastAvg - avg) >= delta * lastAvg;
                if (lastMin > 0) newPoint |= Math.abs(lastMin - min) >= delta * lastMin;
                if (lastLowQuartile > 0) newPoint |= Math.abs(lastLowQuartile - lowQuartile) >= delta * lastLowQuartile;
                if (lastMedian > 0) newPoint |= Math.abs(lastMedian - median) >= delta * lastMedian;
                if (lastHighQuartile > 0)
                    newPoint |= Math.abs(lastHighQuartile - highQuartile) >= delta * lastHighQuartile;
                if (lastMax > 0) newPoint |= Math.abs(lastMax - max) >= delta * lastMax;
                newPoint |= !scanner.hasNext();

                if (newPoint) {
                    data.append(timestep + ";");
                    data.append(avg + ";");
                    data.append(min + ";");
                    data.append(lowQuartile + ";");
                    data.append(median + ";");
                    data.append(highQuartile + ";");
                    data.append(max);

                    lastAvg          = avg;
                    lastMin          = min;
                    lastLowQuartile  = lowQuartile;
                    lastMedian       = median;
                    lastHighQuartile = highQuartile;
                    lastMax          = max;

                    if (scanner.hasNext()) data.append(";");
                }
            }

            // write to file
            fileManager.writeDataToFile(
                    VisType.anger.pathToOutputData, VisType.anger.avgFilename, data.toString(), true);
        }
    }

    /**
     * filename:<br>
     * linear_distance_per_despawn_age.csv
     * <p>
     * type (in order):<br>
     * float; ...
     * <p>
     * order:<br>
     * bla_0; bla_1; ...
     */
    private static void linearDistance() {
        processFloatsWithTotalDespawnCounter(VisType.linear_distance);
    }

    /**
     * filename:<br>
     * spawned_count_per_timestep.csv
     * <p>
     * type (in order):<br>
     * int; ...
     * <p>
     * order:<br>
     * count_0; count_1; ...
     */
    private static void spawnedCount() {
        ArrayList<File> directoryList = getdirectoryList(VisType.spawned_count.pathToInputData);
        if (!directoryList.isEmpty()) {
            StringBuilder data = new StringBuilder();

            ArrayList<Scanner> scanners = new ArrayList<>(directoryList.size());
            for (File file : directoryList) {
                scanners.add(fileManager.openScanner(file));
            }

            int timestep  = 1;
            int fileCount = directoryList.size();
            // while there is a timestep
            while (!scanners.isEmpty()) {

                // check if all scanner has next
                boolean allScannersHasNext = true;
                for (Scanner scanner : scanners) {
                    allScannersHasNext &= scanner.hasNext();
                }
                while (allScannersHasNext) {

                    // for each timestep:
                    long sum = 0;
                    for (Scanner scanner : scanners) {
                        sum += Integer.parseInt(scanner.next());
                        allScannersHasNext &= scanner.hasNext();
                    }

                    // calculate this timestep
                    data.append(";" + timestep++ + ";");
                    data.append(sum / (float) fileCount);    // avg
                }

                // remove finished scanners
                LinkedList<Scanner> finished = new LinkedList<>();
                for (Scanner scanner : scanners)
                    if (!scanner.hasNext()) {
                        finished.add(scanner);
                        fileManager.closeScanner(scanner);
                    }
                scanners.removeAll(finished);
            }

            // write to file
            fileManager.writeDataToFile(
                    VisType.spawned_count.pathToOutputData, VisType.spawned_count.avgFilename, data.toString(), true);

            // filter file
            Scanner scanner = fileManager.openScanner(
                    new File(VisType.spawned_count.pathToOutputData + sep + VisType.spawned_count.avgFilename));
            float lastAvg = 0f;
            data          = new StringBuilder();
            // start filter
            float delta = 0.05f;
            while (scanner.hasNext()) {
                timestep  = Integer.parseInt(scanner.next());
                float avg = Float.parseFloat(scanner.next());

                boolean newPoint = false;
                newPoint |= Math.abs(lastAvg - avg) >= delta * lastAvg;
                newPoint |= !scanner.hasNext();

                if (newPoint) {
                    data.append(timestep + ";");
                    data.append(avg);

                    lastAvg = avg;

                    if (scanner.hasNext()) data.append(";");
                }
            }

            // write to file
            fileManager.writeDataToFile(
                    VisType.spawned_count.pathToOutputData, VisType.spawned_count.avgFilename, data.toString(), true);
        }
    }

    /**
     * filename:<br>
     * total_anger_when_despawning.csv
     * <p>
     * type (in order):<br>
     * int; ...
     * <p>
     * order:<br>
     * ta_0; ta_1; ...
     */
    private static void totalAnger() {
        processInts(VisType.total_anger, true);
    }
}
