package marathon.marathon;

public class MarathonData {
    private double duration;
    private double distance;
    private String gender;
    private String age_group;

    public MarathonData(double dur, double dis, String gen, String ag) {
        this.duration = dur;
        this.distance = dis;
        this.gender = gen;
        this.age_group = ag;
    }

    public double getDuration(){
        return this.duration;
    }

    public double getDistance(){
        return this.distance;
    }

    public String getGender(){
        return this.gender;
    }

    public String getAgeGroup(){
        return this.age_group;
    }

    public void setDuration(double dur) {
        this.duration = dur;
    }

    public void setDistance(double dis) {
        this.distance = dis;
    }

    public void setGender(String gen) {
        this.gender = gen;
    }

    public void setAgeGroup(String ag) {
        this.age_group = ag;
    }

}
