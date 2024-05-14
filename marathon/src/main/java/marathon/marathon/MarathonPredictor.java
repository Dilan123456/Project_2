package marathon.marathon;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.translate.Batchifier;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

public class MarathonPredictor {
    //private static final String MODEL_NAME = "dataclassifier-0002.params";
    private static final Path MODEL_DIR = Paths.get("marathon/src/main/java/marathon/marathon/models");

    private Predictor<NDArray, NDArray> predictor;

    public MarathonPredictor() throws IOException, TranslateException, MalformedModelException {
        Model model = MarathonModel.getModel();
        model.load(MODEL_DIR);

        Translator<NDArray, NDArray> translator = new MarathonTranslator();

        this.predictor = model.newPredictor(translator);
    }

    public float predict(NDArray input) throws TranslateException{
        NDArray output = predictor.predict(input);
        return output.argMax().getLong();
    }

    private static class MarathonTranslator implements Translator<NDArray, NDArray> {
        @Override
        public NDList processInput(TranslatorContext ctx, NDArray input) throws Exception {
            return new NDList(input);
        }
    
        @Override
        public NDArray processOutput(TranslatorContext ctx, NDList list) throws Exception {
            return (NDArray) list.singletonOrThrow();
        }
    
        @Override
        public Batchifier getBatchifier() {
            return null;
        }
    }
}
