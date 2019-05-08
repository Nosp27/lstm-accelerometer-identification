package accelTest;

import configWork.ConfigManager;
import gui.trainingFrame.TrainTab;
import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.NumberedFileInputSplit;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.primitives.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class LRNN {
    private static final Logger log = LoggerFactory.getLogger(LRNN.class);

    //'baseDir': Base directory for the data. Change this if you want to save the data somewhere else
    private static File baseDir = new File(ConfigManager.loadProperty("net-in"));
    private static File baseTrainDir = new File(baseDir, "t");
    private static File featuresDirTrain = new File(baseTrainDir, "features");
    private static File labelsDirTrain = new File(baseTrainDir, "labels");
    private static File baseTestDir = new File(baseDir, "e");
    private static File featuresDirTest = new File(baseTestDir, "features");
    private static File labelsDirTest = new File(baseTestDir, "labels");


    int numLabelClasses = 2;
    int midLayers = 30;

    MultiLayerNetwork _network;
    DataSetIterator trainIter;
    DataSetIterator evalIter;

    public LRNN() {
        _network = configureNet();
        Pair<DataSetIterator, DataNormalization> pair = getDataSetIterator(true, null);

        trainIter = pair.getFirst();
        evalIter = getDataSetIterator(false, pair.getSecond()).getFirst();
    }

    public Pair<DataSetIterator, DataNormalization> getDataSetIterator(boolean train, DataNormalization normalizer) {
        try {
            // ----- Load the training data -----
            int miniBatchSize = Integer.parseInt(ConfigManager.loadProperty("cluster-size"));
            int upperBound = Integer.parseInt(ConfigManager.loadProperty("clusters-num")) - 1;

            String featuresDirString = train ? featuresDirTrain.getAbsolutePath() : featuresDirTest.getAbsolutePath();
            String labelsDirString = train ? labelsDirTrain.getAbsolutePath() : labelsDirTest.getAbsolutePath();

            SequenceRecordReader trainFeatures = new CSVSequenceRecordReader();
            trainFeatures.initialize(new NumberedFileInputSplit(featuresDirString + "/%d.csv", 0, upperBound));

            SequenceRecordReader trainLabels = new CSVSequenceRecordReader();
            trainLabels.initialize(new NumberedFileInputSplit(labelsDirString + "/%d.csv", 0, upperBound));

            DataSetIterator dataIter = new SequenceRecordReaderDataSetIterator(trainFeatures, trainLabels, miniBatchSize, numLabelClasses,
                    false, SequenceRecordReaderDataSetIterator.AlignmentMode.ALIGN_END);

            //Normalize the training data
            if (normalizer == null)
                normalizer = new NormalizerStandardize();

            normalizer.fit(dataIter);              //Collect training data statistics
            dataIter.reset();

            //Use previously collected statistics to normalize on-the-fly. Each DataSet returned by 'trainData' iterator will be normalized
            dataIter.setPreProcessor(normalizer);

            return new Pair<>(dataIter, normalizer);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private MultiLayerNetwork configureNet() {
        // ----- Configure the network -----
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)   //Random number generator seed for improved repeatability. Optional.
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam())
                .list()
                .layer(new LSTM.Builder().activation(Activation.RELU).nIn(3).nOut(midLayers).build())
                .layer(new LSTM.Builder().activation(Activation.RELU).nOut(midLayers).build())
                .layer(new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        .activation(Activation.SOFTMAX).nOut(numLabelClasses).build())
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        net.setListeners(new ScoreIterationListener(20));   //Print the score (loss function value) every 20 iterations
        return net;
    }

    public void trainNet(TrainDataListener l, TrainTab.IStopLearning iStopLearning) {
        // ----- Train the network, evaluating the test set performance at each epoch -----
        int nEpochs = 50;
        String str = "Test set evaluation at epoch %d: Accuracy = %.2f";
        for (int i = 0; i < nEpochs && !iStopLearning.stopped(); i++) {
            _network.fit(trainIter);

            //Evaluate on the test set:
            Evaluation evaluation = _network.evaluate(evalIter);
            System.out.println(String.format(str, i, evaluation.accuracy()));
            l.onGetStats(evaluation);

            trainIter.reset();
            evalIter.reset();
        }

        log.info("----- Example Complete -----");

        try {
            saveNet(new File("C:\\Users\\Nosp\\IdeaProjects\\NetworkTest\\standalonedataprocess\\src\\main\\resources\\config\\net-out\\Adam_net.nnd"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveNet(File fileToSave) throws IOException {
        ModelSerializer.writeModel(_network, fileToSave, true);
    }

    public void loadNet(File netLocation) throws IOException {
        _network = ModelSerializer.restoreMultiLayerNetwork(netLocation);
    }

    public double feedNet(INDArray data){
            INDArray out = feedNet0(data);

            double result = 0;
            System.out.println("Size: " + out.size(2));
            for(int i = 0; i < out.size(2); i++){
                result += out.getDouble(0,1, i);
            }
            result /= out.size(2);
            System.out.println("feed result: " + result);
            return result;
    }

    private INDArray feedNet0(INDArray data) {
        if (_network == null)
            return null;

        INDArray output = _network.activate(data, Layer.TrainingMode.TEST);
        return output;
    }

    public interface TrainDataListener {
        void onGetStats(Evaluation evaluation);
    }

    public static void main(String[] args) {
        boolean state = false;
        LRNN net = new LRNN();

        if (state) {
                net.trainNet(
                new TrainDataListener() {
                    @Override
                    public void onGetStats(Evaluation evaluation) {

                    }
                },
                new TrainTab.IStopLearning() {
                    @Override
                    public boolean stopped() {
                        return false;
                    }
                });
        } else {
            try {
                net.loadNet(new File("C:\\Users\\Nosp\\IdeaProjects\\NetworkTest\\standalonedataprocess\\src\\main\\resources\\config\\net-out\\Adam_net.nnd"));
                INDArray in = DataPrepare_ALT.getFeedableData(new File("C:\\Users\\Nosp\\IdeaProjects\\NetworkTest\\standalonedataprocess\\src\\main\\resources\\net_in\\e\\features\\1.csv"));
                INDArray out = net.feedNet0(in);
                double result = 0;
                System.out.println("Size: " + out.size(2));
                for(int i = 0; i < out.size(2); i++){
                    result += out.getDouble(0,1, i);
                    System.out.println("" + out.getDouble(0, 0, i) + "\t " + out.getDouble(0,1, i));
                }

                System.out.println(result * 1.0 / out.size(2));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}