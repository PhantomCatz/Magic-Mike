package frc.robot.subsystems.SubsystemCatzIntake;

import org.littletonrobotics.junction.AutoLog;

public interface IntakeIO {

    @AutoLog
    public class IntakeIOInputs {
        public double dummyVariable;
    }

    public default void updateInputs(IntakeIOInputs inputs) {}

    public default void exampleAccessMethod(double test) {}

    public default void deployIntake(boolean enable) {}

    public default void rollerEnable(boolean enable) {}
}
