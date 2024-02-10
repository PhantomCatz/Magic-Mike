// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.turret;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.CatzConstants;
import frc.robot.Utils.CatzMechanismPosition;


public class SubsystemCatzTurret extends SubsystemBase {
  //intake io block
  private final TurretIO io;
  private final TurretIOInputsAutoLogged inputs = new TurretIOInputsAutoLogged();
  //intake instance
  private static SubsystemCatzTurret instance = new SubsystemCatzTurret();

  //turret variables
  //constants
  //private final double ENC_TO_INTAKE_GEAR_RATIO = (46.0 / 18.0)* (32.0 / 10.0);
  //private final double WRIST_CNTS_PER_DEGREE = (2096.0 * ENC_TO_INTAKE_GEAR_RATIO) / 360.0;

  private final double TURRET_POWER     = 0.6;
  private final double TURRET_ENC_TARGE_POS = 0.6;

  private static final double TURRET_kP = 0.0;
  private static final double TURRET_kI = 0.0;
  private static final double TURRET_kD = 0.0;

  public static double turretEncoderPosition = 0.0;

  //variables
  private double m_turretTargetDegree;
  private double pidTurretPower;

  private PIDController pid;


  public SubsystemCatzTurret() {

    switch (CatzConstants.currentMode) {
      case REAL: io = new TurretIOReal();
      break;

      case SIM : io = null;
      break;

      case REPLAY: io = new TurretIOReal() {};
      break;

      default: io = null;
      break;
    }

    pid = new PIDController(TURRET_kP, 
                            TURRET_kI, 
                            TURRET_kD);

  }

  private static TurretState currentTurretState;
  public static enum TurretState {
    AUTO,
    FULL_MANUAL
  }

  // Get the singleton instance of the Turret Subsystem
  public static SubsystemCatzTurret getInstance() {
    turretEncoderPosition = 0.0;
      return instance;
  }


  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("intake/inputs", inputs);   
    Logger.recordOutput("Turret Encoder", inputs.turretEncValue);

    double currentTurretAngle = inputs.turretEncValue; //TBD make conversion
    
    turretEncoderPosition = currentTurretAngle;

    if(currentTurretState == TurretState.AUTO) {
      
      pidTurretPower = pid.calculate(currentTurretAngle, m_turretTargetDegree);
      io.turretSet(pidTurretPower);

    } else {
      
    }



  }

  //------------------------------------Turret Methods----------------
  public Command cmdTurretDegree(double turretDeg) {
    return run(()->setTurretTargetDegree(turretDeg));
  }

  public void setTurretTargetDegree(double turretTargetDegree) {
    m_turretTargetDegree = turretTargetDegree;

  }


  //-------------------------------------Manual methods--------------------------------
  public Command cmdTurretLT() {
    currentTurretState = TurretState.FULL_MANUAL;
    // return run(()-> io.turretRotate(TURRET_POWER));
    return run(()-> io.turretSet(-TURRET_POWER));
  }

  public Command cmdTurretRT() {
    currentTurretState = TurretState.FULL_MANUAL;
    return run(()-> io.turretSet(TURRET_POWER));
  }

  public Command cmdTurretOff() {
    currentTurretState = TurretState.FULL_MANUAL;
    return run(()-> io.turretSet(0.0));
  }

}
