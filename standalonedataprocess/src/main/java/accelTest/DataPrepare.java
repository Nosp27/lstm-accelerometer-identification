package accelTest;

import configWork.ConfigManager;
import configWork.ConfigType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.*;
import java.util.*;

public class DataPrepare {

    private static HashMap<Integer, FileWriter> writers;
    private static HashMap<Integer, Long> latestTimecodes;
    private static HashMap<Integer, Double[]> latestCoords;
    private static int counter = 0;
    private static int fileNum = 0;
    private static long fixedDelay = Integer.parseInt(ConfigManager.loadProperty("fixed-time-interval"));
    private static int clusterLimit = 999;

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

    public static int[] generateEvalData(int label1, int label2, File rawData1, File rawData2, boolean deleteExisting) {
        if (ConfigManager.loadProperty("file-limit").equals("1")) {
            try {
                long cs1 = countLines(rawData1, true);
                long cs2 = countLines(rawData1, true);

                clusterLimit = (int) (Math.min(cs1, cs2) / Integer.parseInt(ConfigManager.loadProperty("cluster-size")));
            } catch (IOException e) {
            }
        } else clusterLimit = Integer.MAX_VALUE;

        int[] clusterNums = {
                generateEvalData0(label1, rawData1, deleteExisting),
                generateEvalData0(label2, rawData2, false)};

        ConfigManager.saveProperty("clusters-num", Integer.toString(clusterNums[0] + clusterNums[1]), ConfigType.INT);
        return clusterNums;
    }

    private static int generateEvalData0(int label, File rawData, boolean deleteExisting) {
        String baseDir = ConfigManager.loadProperty("net-in");

        if (deleteExisting)
            deleteAll(baseDir);

        int batch = Integer.parseInt(ConfigManager.loadProperty("cluster-size"));
        float ratio = Integer.parseInt(ConfigManager.loadProperty("eval-ratio")) * 1.0f / 100f;

        int clustersCount = 0;

        File[] csvs = rawData.listFiles((dir, name) -> name.endsWith(".csv"));

        for (int i = 0; i < csvs.length; i++) {
            try {
                File f = csvs[i];
                BufferedReader reader = new BufferedReader(new FileReader(f));

                long lines = countLines(f, false);

                int limit = batch;
                while (limit < lines && clustersCount < clusterLimit) {
                    File ft = new File(baseDir, "t\\features\\" + (fileNum) + ".csv");
                    File fe = new File(baseDir, "e\\features\\" + (fileNum) + ".csv");

                    FileWriter fwt = new FileWriter(ft);
                    FileWriter fwe = new FileWriter(fe);

                    writeLabel(fileNum, label, new File(baseDir, "e" + "\\labels"));
                    writeLabel(fileNum, label, new File(baseDir, "t" + "\\labels"));

                    fileNum++;

                    int l = 0;
                    for (; l < batch * ratio; l++) {
                        fwt.write(reader.readLine() + "\n");
                    }

                    for (; l < batch; l++) {
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

    private static void deleteAll(String baseDir) {
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
        reset();
    }

    private static long countLines(File f, boolean isDir) throws IOException {
        if (isDir) {
            File[] children = f.listFiles();
            long count = 0;
            for (File child : children) {
                BufferedReader reader = new BufferedReader(new FileReader(child));

                while (reader.readLine() != null)
                    count++;
                reader.close();
            }

            return count;
        }

        BufferedReader reader = new BufferedReader(new FileReader(f));
        long count = 0;
        while (reader.readLine() != null)
            count++;
        reader.close();
        return count;
    }

    private static void writeLabel(int filenum, int i, File path) {
        try {
            FileWriter fw = new FileWriter(new File(path, filenum + ".csv"));
            fw.write(Integer.toString(i));
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

    @SuppressWarnings("Duplicates")
    public static INDArray getFeedableData(File f) throws IOException {
        long linesCount = DataPrepare.countLines(f, false);

        INDArray data = Nd4j.zeros(1, 3, linesCount);
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            int i = 0;
            while (i < linesCount) {
                line = br.readLine();
                Scanner sc = new Scanner(line);
                sc.useLocale(Locale.ENGLISH);
                sc.useDelimiter(",");
                for (int k = 0; k < 3; k++)
                    data.putScalar(i , k, i , sc.nextDouble());
                i++;
                sc.close();
            }
        }
        return data;
    }

    @SuppressWarnings("Duplicates")
    public static INDArray getFeedableData(Reader reader, int linesCount) throws IOException {
        INDArray data = Nd4j.zeros(1, 3, linesCount);
        try (BufferedReader br = new BufferedReader(reader)) {
            String line;
            int i = 0;
            while (i < linesCount) {
                line = br.readLine();
                Scanner sc = new Scanner(line);
                sc.useLocale(Locale.ENGLISH);
                sc.useDelimiter(",");
                for (int k = 0; k < 3; k++)
                    data.putScalar(i , k, i , sc.nextDouble());
                i++;
                sc.close();
            }
        }
        return data;
    }

    private static void reset() {
        fileNum = 0;
    }

    public static int getFileNum() {
        return fileNum;
    }
}
