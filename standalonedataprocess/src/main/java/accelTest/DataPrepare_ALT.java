package accelTest;

import configWork.ConfigManager;
import org.nd4j.linalg.io.ClassPathResource;

import java.io.*;
import java.util.*;

public class DataPrepare_ALT {

    static HashMap<Integer, FileWriter> writers;
    static HashMap<Integer, Long> latestTimecodes;
    static HashMap<Integer, Double[]> latestCoords;

    static int counter = 0;
    static long fixedDelay = Integer.parseInt(ConfigManager.loadProperty("fixed-time-interval"));

    private static String baseDir = ConfigManager.loadProperty("net-in");

    private static Double[] normalize(Double[] in, Long delay, Double[] out) {
        if (delay < fixedDelay * 0.7f)
            return null;
        Double[] grad = new Double[3];
        for (int i = 0; i < 3; i++) {
            grad[i] = (out[i] - in[i]) / delay;

            //count predictinon
            grad[i] *= fixedDelay;
        }

        return grad;
    }

    private static int countMinimal() {
        int min = Integer.MAX_VALUE;
        for (int i = 1; i <= 36; i++) {
            try (BufferedReader br = new BufferedReader(new FileReader(baseDir + i + ".csv"))) {
                int k = 0;
                while (br.readLine() != null)
                    k++;

                if (k < min)
                    min = k;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return min;
    }

    static int fileNum = 0;

    public static int generateEvalData(File rawData, float ratio, int batch, int offset, boolean deleteExisting) {
        if (deleteExisting)
            deleteAll();

        int clustersCount = 0;

        File[] csvs = rawData.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("csv");
            }
        });

        for (int i = offset; i < csvs.length; i++) {
            try {
                File f = new File(rawData, i + ".csv");
                BufferedReader reader = new BufferedReader(new FileReader(f));

                long lines = countLines(f);

                int limit = batch;
                while (limit < lines && clustersCount < clusterLimit) {
                    File ft = new File(baseDir, "t\\features\\" + (fileNum) + ".csv");
                    File fe = new File(baseDir, "e\\features\\" + (fileNum) + ".csv");

                    FileWriter fwt = new FileWriter(ft);
                    FileWriter fwe = new FileWriter(fe);

                    writeLabel(fileNum, i - offset, new File(baseDir, "e" + "\\labels"));
                    writeLabel(fileNum, i - offset, new File(baseDir, "t" + "\\labels"));

                    fileNum++;

                    long _size = batch;

                    long l = 0;
                    for (; l < _size * ratio; l++) {
                        fwt.write(reader.readLine() + "\n");
                    }

                    for (; l < _size; l++) {
                        fwe.write(reader.readLine() + "\n");
                    }

                    fwt.close();
                    fwe.close();

                    limit += batch;
                    clustersCount++;
                }
                reader.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(fileNum);
        return clustersCount;
    }

    private static void deleteAll() {
        File t = new File(baseDir, "t");
        File e = new File(baseDir, "e");

        File[] dirs = {
                new File(t, "features"),
                new File(t, "labels"),
                new File(e, "features"),
                new File(e, "labels")
        };

        for (File dir : dirs) {
            dir.mkdirs();
            File[] childFiles = dir.listFiles();

            if (childFiles == null)
                continue;

            for (File f : childFiles)
                f.delete();
        }
    }

    static long countLines(File f) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(f));
        long count = 0;
        while (reader.readLine() != null)
            count++;
        reader.close();
        return count;
    }

    static void writeLabel(int filenum, int i, File path) {
        try {
            FileWriter fw = new FileWriter(new File(path, filenum + ".csv"));
            fw.write(Integer.toString(label));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param path path to database in one csv file with all records in format [id,time,x,y,z]
     * @param out  split database, file per id. Deletes timestamp and normalizes data time intervals
     */
    public static void convert(String path, String out) {
        BufferedReader br = null;
        writers = new HashMap<>();
        latestTimecodes = new HashMap<>();
        latestCoords = new HashMap<>();

        try {
            br = new BufferedReader(new FileReader(path));

            String line;
            Scanner s;
            String delim = "[,;]";
            while ((line = br.readLine()) != null) {
                counter++;
                s = new Scanner(line);
                s.useLocale(Locale.ENGLISH);
                s.useDelimiter(delim);

                write(s, out);
                s.close();
            }

        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println(counter);
        } finally {
            try {
                for (FileWriter _fw : writers.values())
                    _fw.close();

                br.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private static void write(Scanner sc, String outPrefix) throws IOException {
        try {
            //id
            int h = sc.nextInt();

            FileWriter concreteWriter;

            if (!writers.containsKey(h)) {
                writers.put(h, new FileWriter(outPrefix + h + ".csv"));
            }
            concreteWriter = writers.get(h);


            //activity
            String activity = sc.next();
            if (!activity.equals("Walking"))
                return;

            //time in ms
            Long timeNow = (long) (sc.nextLong() / 1e+6);

            if (timeNow == 0)
                return;

            Long timePrev = latestTimecodes.put(h, timeNow);
            Long delay = timePrev == null ? 0 : timeNow - timePrev;

            if (timePrev == null)
                System.out.println("zero time: " + h + ", " + counter);
            else if (delay == 0) {
                //System.out.println("duplication: " + h + ", " + counter);
                return;
            }


            //xyz
            Double[] xyz = new Double[3];
            for (int i = 0; i < 3; i++) {
                xyz[i] = sc.nextDouble();
            }

            Double[] prev = latestCoords.put(h, xyz);

            boolean normalizedWell = true;
            if (prev != null) {
                normalizedWell = false;
                Double[] norm = normalize(prev, delay, xyz);
                if (norm != null) {
                    normalizedWell = true;
                    delay = fixedDelay;
                    xyz = norm;
                }
            }

            if (normalizedWell)
                for (int i = 0; i < 3; i++) {
                    concreteWriter.write("" + xyz[i]);
                    if (i < 2)
                        concreteWriter.write(",");
                }

            concreteWriter.write("\n");
        } catch (NoSuchElementException e) {
            System.out.println(sc.hasNext());
            System.out.println("mm: " + counter);
        }
    }

    public static void reset() {
        fileNum = 0;
    }

    static int label = 0;
    static int clusterLimit = 2;

    public static void main(String[] args) {
//        convert("C:\\Users\\Nosp\\IdeaProjects\\NetworkTest\\standalonedataprocess\\src\\main\\resources\\dataFromServer\\0.csv",
//                "C:\\Users\\Nosp\\IdeaProjects\\NetworkTest\\standalonedataprocess\\src\\main\\resources\\dataFromServer\\sas");

        label = 0;
        generateEvalData(new File("C:\\Users\\Nosp\\IdeaProjects\\NetworkTest\\standalonedataprocess\\src\\main\\resources\\dataFromServer"), .8f, 500, 0, true);

        label = 1;
        generateEvalData(new File("C:\\Users\\Nosp\\IdeaProjects\\NetworkTest\\standalonedataprocess\\src\\main\\resources\\downloadedDB"), .8f, 500, 1, false);

    }
}
