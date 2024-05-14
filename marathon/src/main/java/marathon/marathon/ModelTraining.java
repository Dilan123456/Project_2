package marathon.marathon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.metric.Metrics;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.TrainingConfig;
import ai.djl.training.TrainingResult;
import ai.djl.training.dataset.ArrayDataset;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.translate.TranslateException;

@Component
public class ModelTraining implements ApplicationRunner {
    
    private final int EPOCHS = 2;

    @Value("classpath:trimmed_run_data.csv")
    private InputStream csvFile;

    private final List<MarathonData> marathonDatas = new ArrayList<>();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        createMarathonNetwork();
    }

    private void createMarathonNetwork() throws IOException, TranslateException{
        Path modelDir = Paths.get("marathon/src/main/java/marathon/marathon/models");
        readCsv();

        RandomAccessDataset[] datasets = createTest(marathonDatas);
        Loss loss = Loss.softmaxCrossEntropyLoss();
        TrainingConfig config = setupTrainingConfig(loss);
        Model model = MarathonModel.getModel();
        Trainer trainer = model.newTrainer(config);
        trainer.setMetrics(new Metrics());

        Shape inputShape = new Shape(1, 3);
        trainer.initialize(inputShape);

        EasyTrain.fit(trainer, EPOCHS, datasets[0], datasets[1]);

        TrainingResult result = trainer.getTrainingResult();
        model.setProperty("Epoch", String.valueOf(EPOCHS));
        model.setProperty(
                "Accuracy", String.format("%.5f", result.getValidateEvaluation("Accuracy")));
        model.setProperty("Loss", String.format("%.5f", result.getValidateLoss()));
        model.save(modelDir, MarathonModel.model_name);
        model.close();
    }

    private void readCsv() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvFile))) {

            reader.readLine();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");

                MarathonData data = fillMarathonModel(values);
                marathonDatas.add(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MarathonData fillMarathonModel(String[] values) {
        double duration = Double.parseDouble(values[0]);
        double distance = Double.parseDouble(values[1]);
        String gender = values[2];
        String age_group = values[3];

        return new MarathonData(
                duration, distance, gender, age_group);
    }

    private RandomAccessDataset[] createTest(List<MarathonData> mDatas) {
        NDManager manager = NDManager.newBaseManager(null, "MXNet");

        int numSamples = mDatas.size();
        NDArray features = manager.create(new Shape(numSamples, 3));
        NDArray labels = manager.create(new Shape(numSamples));
    
        for (int i = 0; i < numSamples; i++) {
            MarathonData data = mDatas.get(i);
            features.set(new NDIndex(i, 0), manager.create((float) data.getDuration()));
            features.set(new NDIndex(i, 1), manager.create((float) data.getDistance()));
            features.set(new NDIndex(i, 2), manager.create((float) getAgeGroupId(data.getAgeGroup())));
            labels.set(new NDIndex(i), manager.create(getBinaryClassification(data.getGender())));
        }

        // Split the data into training and validation sets
        RandomAccessDataset trainDataset = new ArrayDataset.Builder()
                .setData(features)
                .optLabels(labels)
                .setSampling(64, true)
                .build();
        RandomAccessDataset validationDataset = new ArrayDataset.Builder()
                .setData(features)
                .optLabels(labels)
                .setSampling(64, false)
                .build();
    
        return new RandomAccessDataset[] { trainDataset, validationDataset };
    }
    private float getAgeGroupId(String ag) {
        switch (ag) {
            case "18 - 34":
                return 0;
            case "35 - 54":
                return 1;
            case "55 +":
                return 2;
            default:
                throw new IllegalArgumentException("Invalid site ID: " + ag);
        }
    }

    private float getBinaryClassification(String gender) {
        if(gender.equals("M")){
            return 0;
        }else{
            return 1;
        }
    }

    private String getClassification(float val){
        if(val == 0){
            return "M";
        } else{
            return "F";
        }
    }

    private TrainingConfig setupTrainingConfig(Loss loss) {
        return new DefaultTrainingConfig(loss)
                .addEvaluator(new Accuracy())
                .addTrainingListeners(TrainingListener.Defaults.logging());
    }

    public String predict(NDArray input) throws TranslateException, MalformedModelException, IOException {
        MarathonPredictor predictor = new MarathonPredictor();
        return getClassification(predictor.predict(input));
    }
}
