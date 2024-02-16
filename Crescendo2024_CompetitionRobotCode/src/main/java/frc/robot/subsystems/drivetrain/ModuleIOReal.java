package frc.robot.subsystems.drivetrain;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.DutyCycleOut;
import com.ctre.phoenix6.controls.VelocityTorqueCurrentFOC;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.revrobotics.CANSparkMax;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import frc.robot.CatzConstants.MtrConfigConstants;

import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkLowLevel.MotorType;


public class ModuleIOReal implements ModuleIO {

    private final CANSparkMax STEER_MOTOR;
    private final TalonFX DRIVE_MOTOR;

    private DutyCycleEncoder magEnc;
    private DigitalInput MagEncPWMInput;

    private StatusCode initializationStatus = StatusCode.StatusCodeNotInitialized;

            //create new config objects
    private TalonFXConfiguration talonConfigs = new TalonFXConfiguration();
    private Slot0Configs driveConfigs = new Slot0Configs();

    public ModuleIOReal(int driveMotorIDIO, int steerMotorIDIO, int magDIOPort) {

        //mag encoder setup
        MagEncPWMInput = new DigitalInput(magDIOPort);
        magEnc = new DutyCycleEncoder(MagEncPWMInput);

        //steer motor setup
        STEER_MOTOR = new CANSparkMax(steerMotorIDIO, MotorType.kBrushless);
        STEER_MOTOR.restoreFactoryDefaults();
        STEER_MOTOR.setSmartCurrentLimit(MtrConfigConstants.NEO_CURRENT_LIMIT_AMPS);
        STEER_MOTOR.setIdleMode(IdleMode.kCoast);
        STEER_MOTOR.enableVoltageCompensation(12.0);

        //Drive Motor setup
        DRIVE_MOTOR = new TalonFX(driveMotorIDIO);
            //reset to factory defaults
        DRIVE_MOTOR.getConfigurator().apply(new TalonFXConfiguration());
        talonConfigs.Slot0 = driveConfigs;
            //current limit
        talonConfigs.CurrentLimits = new CurrentLimitsConfigs();
        talonConfigs.CurrentLimits.SupplyCurrentLimitEnable = MtrConfigConstants.FALCON_ENABLE_CURRENT_LIMIT;
        talonConfigs.CurrentLimits.SupplyCurrentLimit       = MtrConfigConstants.FALCON_CURRENT_LIMIT_AMPS;
        talonConfigs.CurrentLimits.SupplyCurrentThreshold   = MtrConfigConstants.FALCON_CURRENT_LIMIT_TRIGGER_AMPS;
        talonConfigs.CurrentLimits.SupplyTimeThreshold      = MtrConfigConstants.FALCON_CURRENT_LIMIT_TIMEOUT_SECONDS;
            //neutral mode
        talonConfigs.MotorOutput.NeutralMode = NeutralModeValue.Brake;
            //pid
        driveConfigs.kP = 2.4;
        driveConfigs.kI = 0.0;
        driveConfigs.kD = 0.00;
        driveConfigs.kV = 0.1189; //TBD need tick eq for this

        //check if drive motor is initialized correctly
        for(int i=0;i<5;i++){
            initializationStatus = DRIVE_MOTOR.getConfigurator().apply(talonConfigs);
            if(!initializationStatus.isOK())
                System.out.println("Failed to Configure CAN ID" + driveMotorIDIO);
        }

    }

    @Override
    public void updateInputs(ModuleIOInputs inputs) {
        inputs.driveMtrVelocity       = DRIVE_MOTOR.getRotorVelocity().getValue();
        inputs.driveMtrSensorPosition = DRIVE_MOTOR.getRotorPosition().getValue();
        inputs.driveAppliedVolts      = DRIVE_MOTOR.getMotorVoltage().getValueAsDouble();
        inputs.magEncoderValue        = magEnc.get();
        inputs.driveVelocityError     = DRIVE_MOTOR.getClosedLoopError().getValueAsDouble();
        inputs.steerAppliedVolts      = STEER_MOTOR.getAppliedOutput();
    }

    @Override
    public void setDriveVelocityIO(double velocity) {
        DRIVE_MOTOR.setControl(new VelocityTorqueCurrentFOC(velocity));
    }

    @Override
    public void setDrivePwrPercentIO(double drivePwrPercent) {
        DRIVE_MOTOR.setControl(new DutyCycleOut(drivePwrPercent,
                        true,
                        false,
                        false,
                        false));

    }

    @Override
    public void setSteerPwrIO(double SteerPwr) {
        STEER_MOTOR.set(SteerPwr);
    }

    @Override
    public void setSteerVoltageIO(double steerVoltage) {
        STEER_MOTOR.setVoltage(steerVoltage);
    }

    @Override
    public void setSteerCoastModeIO() {
        STEER_MOTOR.setIdleMode(IdleMode.kCoast);
    }

    @Override
    public void setSteerBrakeModeIO() {
        STEER_MOTOR.setIdleMode(IdleMode.kBrake);
    }

    @Override
    public void setDrvSensorPositionIO(double sensorPos) {
        DRIVE_MOTOR.setPosition(sensorPos);
    }
    @Override
    public void reverseDriveIO(boolean enable) {
        DRIVE_MOTOR.setInverted(enable);
    }
    
    @Override
    public void resetMagEncoderIO() {
        magEnc.reset();
    }

}
