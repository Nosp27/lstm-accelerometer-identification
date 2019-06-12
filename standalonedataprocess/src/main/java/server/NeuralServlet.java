package server;

import accelTest.DataPrepare;
import accelTest.LRNN;
import com.github.jaiimageio.impl.plugins.clib.InputStreamAdapter;
import org.nd4j.linalg.api.ndarray.INDArray;

import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

public class NeuralServlet extends HttpServlet {
    LRNN feedNet;
    boolean error = false;

    @Override
    public void init() throws ServletException {
//        feedNet = new LRNN();
//        try {
//            feedNet.loadNet(NeuralServlet.class.getResourceAsStream("net.nnd"));
//        } catch (IOException e) {
//            error = true;
//        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        resp.setContentType("text/plain");
//        if (error) {
//            resp.getWriter().write("Error occured!");
//            return;
//        }
//        ServletInputStream dataIn = req.getInputStream();
//        StringBuilder sb = new StringBuilder();
//        int lines = 0;
//        int b = dataIn.read();
//        while (b != 0) {
//            sb.append((char) b);
//            if (b == '\n')
//                lines++;
//            b = dataIn.read();
//        }
//        if(!checkFormat(sb))
//            resp.getWriter().write("Error occured!");
//        INDArray input = DataPrepare.getFeedableData(new StringReader(sb.toString()), lines);
//        double result = feedNet.feedNet(input);
//        resp.getWriter().write("R " + result);
        resp.setContentType("text/plain");
        resp.getWriter().write("HW");
    }

    private boolean checkFormat(StringBuilder sb) throws IOException{
//        StringReader r = new StringReader(sb.toString());
//        BufferedReader br = new BufferedReader(r);
//        String line;
//        while ((line = br.readLine()) != null)
//            if(!line.matches("[0-9]+.[0-9]+, [0-9]+.[0-9]+, [0-9]+.[0-9]+"))
//                return false;
        return true;
    }
}
