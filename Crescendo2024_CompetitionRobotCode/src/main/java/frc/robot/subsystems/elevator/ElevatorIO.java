package frc.robot.subsystems.elevator;

import org.littletonrobotics.junction.AutoLog;

public interface ElevatorIO {
    @AutoLog
    public class ElevatorIOInputs {
        public double elevatorVoltage;
        public double elevatorVelocity;
        public double elevatorDutyCycle;
        public double elevatorTorqueCurrent;
        public double elevatorPosRev;
        public double elevatorPositionError;
        public boolean bottomSwitchTripped;
        public boolean reverseSwitchTripped;
    }

    public default void updateInputs(ElevatorIOInputs inputs) {}

    public default void setElevatorPosition(double newPositionElevator) {}

    public default void setElevatorPercentOutput(double speed) {}

    public default void setSelectedSensorPosition(double pos) {}

    public default void setElevatorVoltage(double volts) {}

}