package frc.robot.subsystems.shooter;

import org.littletonrobotics.junction.AutoLog;

public interface ShooterIO {
    @AutoLog
    public class ShooterIOInputs {
        public double shooterVelocityLT;
        public double shooterVelocityRT;
        public double velocityThresholdLT;
        public double velocityThresholdRT;
        public double shooterMotorVoltageLT;
        public double shooterMotorVoltageRT;
        public double shooterTorqueCurrentLT;
        public double shooterTorqueCurrentRT;
        public double shooterDutyCycleLT;
        public double shooterDutyCycleRT;
        public double shooterVelocityErrorLT;
        public double shooterVelocityErrorRT;

        public boolean shooterAdjustBeamBreakState;
        public boolean shooterLoadBeamBreakState;

        public double loadMotorPercentOutput; 
        public double loadMotorVelocity;
        public double loadMotorOutputCurrent;

        public double servoLeft;
        public double servoRight;
    }

    public default void updateInputs(ShooterIOInputs inputs) {}

    public default void setShooterEnabled() {}

    public default void setShooterDisabled() {}

    public default void loadBackward() {}

    public default void fineAdjustBck() {}

    public default void loadDisabled() {}

    public default void loadNote() {}

    public default void feedShooter() {}
    
    public default void fineAdjustFwd() {}

    public default void setServoPosition(double power) {}
}
