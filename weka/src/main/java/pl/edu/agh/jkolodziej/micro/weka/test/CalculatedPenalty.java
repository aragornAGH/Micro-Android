package pl.edu.agh.jkolodziej.micro.weka.test;

/**
 * @author - Jakub Kołodziej
 */
public class CalculatedPenalty {
    private static final double PENALTY_FACTOR = 1.2;
    private final long energy;
    private final double percentOfBattery;
    private final long time;

    public CalculatedPenalty(long time, long energy, double percentOfBattery) {
        this.time = time;
        this.energy = energy;
        this.percentOfBattery = percentOfBattery;
    }

    public long getEnergy() {
        return (long) (PENALTY_FACTOR * energy);
    }

    public long getTime() {
        return (long) (PENALTY_FACTOR * time);
    }

    public double getPercentOfBattery() {
        return PENALTY_FACTOR * percentOfBattery;
    }
}
