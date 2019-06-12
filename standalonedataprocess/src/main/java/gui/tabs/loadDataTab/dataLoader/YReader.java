package gui.tabs.loadDataTab.dataLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Scanner;

public class YReader {
    private File file;

    public YReader(File file) throws FileNotFoundException {
        if (!file.exists())
            throw new FileNotFoundException();

        this.file = file;
    }

    private BufferedReader getReader() throws FileNotFoundException {
        return new BufferedReader(new FileReader(file));
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
            BufferedReader r = getReader();
            while (true) {
                Double[] curr = new Double[3];
                for (int i = 0; i < 3; i++)
                    curr[i] = readNext(r, i + 1);

                ds.add(curr);
            }
        } catch (IOException e) {

        }
        System.out.println(ds.size());
        return ds;
    }
}
