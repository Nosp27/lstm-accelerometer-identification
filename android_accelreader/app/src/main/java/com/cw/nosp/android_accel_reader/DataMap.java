package com.cw.nosp.android_accel_reader;

import java.io.*;
import java.util.*;

public class DataMap {
    public final List<Double[]> data;
    private String _toString;

    public DataMap(List<Double[]> _d) {
        data = new LinkedList<>(_d);
    }

    public DataMap(File f) throws IOException {
        data = new LinkedList<>();
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        while ((line = br.readLine()) != null) {
            Scanner sc = new Scanner(line);
            sc.useLocale(Locale.ENGLISH);
            sc.useDelimiter(",");
            Double[] dd = new Double[3];
            for (int i = 0; i < 3; i++)
                dd[i] = sc.nextDouble();
            data.add(dd);
        }
        br.close();
    }

    public int getSize() {
        return toString().getBytes().length;
    }

    @Override
    public String toString() {
        if (_toString == null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < data.size(); i++) {
                for (int k = 0; k < 3; k++) {
                    sb.append(Double.toString(data.get(i)[k]));
                    if (k != 2)
                        sb.append(",");
                }
                sb.append("\n");
            }
            _toString = sb.toString();
        }
        return _toString;
    }
}
