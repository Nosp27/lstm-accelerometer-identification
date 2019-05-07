package gui.tabs.loadDataTab.dataLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Scanner;

public class YReader {
    File dir;
    private double[] mem = new double[3];
    int fileIndex = 0;

    public YReader(File file) throws FileNotFoundException {
        if (!file.isDirectory())
            throw new IllegalArgumentException();

        if (!file.exists())
            throw new FileNotFoundException();

        dir = file;
    }

    private BufferedReader getReader(int i) throws FileNotFoundException {
        return new BufferedReader(new FileReader(new File(dir, i + ".csv")));
    }

    private double readNext(BufferedReader r, int coordNum) throws IOException {
        String line = r.readLine();
        if (line == null)
            throw new IOException();

        Scanner sc = new Scanner(line);
        sc.useLocale(Locale.US);
        sc.useDelimiter("[,]");


        for (int i = 0; i < coordNum - 1; ++i) {
            if (!sc.hasNextDouble())
                throw new IOException();
            sc.nextDouble();
        }

        if (!sc.hasNextDouble())
            throw new IOException();

        return sc.nextDouble();
    }

    public Collection<Double[]> readAll() {
        ArrayList<Double[]> ds = new ArrayList<>();
        try {
            BufferedReader r = getReader(fileIndex);
            while (true) {
                Double[] curr = new Double[3];
                for (int i = 0; i < 3; i++)
                    curr[i] = readNext(r, i + 1) + mem[i];

                ds.add(curr);

                for (int i = 0; i < 3; i++)
                    mem[i] = curr[i];
            }
        } catch (IOException e) {

        }
        fileIndex++;
        System.out.println(ds.size());
        return ds;
    }
}
