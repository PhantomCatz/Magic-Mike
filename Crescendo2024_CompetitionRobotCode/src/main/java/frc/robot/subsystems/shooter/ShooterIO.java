package frc.robot.subsystems.shooter;

import org.littletonrobotics.junction.AutoLog;

public interface ShooterIO {
    @AutoLog
    public class ShooterIOInputs {
        public double shooterVelocityLT;
        public double shooterVelocityRT;
        public double shooterPercentOutputLT;
        public double shooterPercentOutputRT;
        public double shooterMotorVoltageLT;
        public double shooterMotorVoltageRT;
        public double shooterTorqueCurrentLT;
        public double shooterTorqueCurrentRT;
        public double shooterDutyCycleLT;
        public double shooterDutyCycleRT;

        public double LoadMotorPercentOutput; 
        public double LoadMotorVelocity;
        public double FeedPercentOutput;
        public double FeedVelocity;

        public double shooterVelocityErrorLT;
        public double shooterVelocityErrorRT;

        public boolean BBShooterUnbroken;
    }

    public default void updateInputs(ShooterIOInputs inputs) {}

    public default void setShooterEnabled() {}

    public default void setShooterDisabled() {}

    //public default void setShooterEnabledCmd(boolean rdyToShoot) {}

    public default void loadReverse() {}

    public default void loadDisabled() {}

    public default void loadForward() {}

    //public default void loadForwardCmd(boolean bbUnBroken) {}

    public default void feedForward() {}

    public default void feedDisabled() {}

    public default void setTurretPosition(double targetEncPos) {}

    public default double getTurretDeg() {
        return 0;
    }

    public default void setTurretCurrentPosition(double currentEncPos) {}
}
