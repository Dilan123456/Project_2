package marathon.marathon;

import ai.djl.Model;
import ai.djl.nn.Block;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;

public class MarathonModel {

    public static final int features = 3;

    // the number of output classes
    public static final int classes = 2;
    
    public static final String model_name = "genderPredictor";

    public static Model getModel() {

        Model model = Model.newInstance(model_name);
        Block mlpBlock = createMlpBlock();
        model.setBlock(mlpBlock);
        return model;
    }

    private static Block createMlpBlock() {
        SequentialBlock block = new SequentialBlock();
        block.add(Blocks.batchFlattenBlock(features));
        block.add(Linear.builder().setUnits(128).build());
        block.add(ai.djl.nn.Activation::relu);
        block.add(Linear.builder().setUnits(64).build());
        block.add(ai.djl.nn.Activation::relu);
        block.add(Linear.builder().setUnits(5).build());
        return block;
    }

}
