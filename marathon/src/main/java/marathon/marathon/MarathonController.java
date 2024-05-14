package marathon.marathon;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ai.djl.MalformedModelException;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.translate.TranslateException;


@RestController
public class MarathonController {
    
    @Autowired
    private ModelTraining trainService;


    @PostMapping("/predict")
    public ResponseEntity<String> predictGender(@RequestBody PredictionData data) throws MalformedModelException, IOException {
        try {
            NDManager manager = NDManager.newBaseManager(null, "MXNet");
            float[] input = new float[]{(float)data.getDistance(), (float)data.getDuration(), (float)data.getAgeGroup()};
            NDArray inputArray = manager.create(input);
            String output = trainService.predict(inputArray);
            return ResponseEntity.ok(output);
        } catch (TranslateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("-2: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("-4: " + e.getMessage());
        }
    }
}
