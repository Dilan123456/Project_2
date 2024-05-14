package marathon.marathon;

public class PredictionData {
    private float distance;
    private float duration;
    private float age_group;

    public PredictionData(float dis, float dur, float ag){
        this.distance = dis;
        this.duration = dur;
        this.age_group = ag;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public float getAgeGroup() {
        return age_group;
    }

    public void setAgeGroup(float age_group) {
        this.age_group = age_group;
    }
}
