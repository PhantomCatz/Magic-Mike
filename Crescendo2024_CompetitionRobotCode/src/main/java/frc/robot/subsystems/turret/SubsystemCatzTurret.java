// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.turret;

import java.sql.Driver;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.CatzConstants;
import frc.robot.Utils.CatzMechanismPosition;
import frc.robot.subsystems.vision.SubsystemCatzVision;


public class SubsystemCatzTurret extends SubsystemBase {
  //intake io block
  private final TurretIO io;
  private final TurretIOInputsAutoLogged inputs = new TurretIOInputsAutoLogged();

  //intake instance
  private static SubsystemCatzTurret instance = new SubsystemCatzTurret();

  //turret constants
  private final double TURRET_POWER     = 0.6;
  private final double TURRET_DECEL_PWR = 0.3;
 
  private static final double TURRET_kP = 0.02;
  private static final double TURRET_kI = 0.0;
  private static final double TURRET_kD = 0.0;
  
  private static final double LIMELIGHT_kP = 0.013;
  private static final double LIMELIGHT_kI = 0.0;
  private static final double LIMELIGHT_kD = 0.0001;

  private final double TURRET_POSITIVE_MAX_RANGE = 120.0; //120
  private final double TURRET_NEGATIVE_MAX_RANGE = -120.0; //-120

  private final double NEGATIVE_DECEL_THRESHOLD  =  -15.0;
  private final double POS_DECEL_THRESHOLD       =   15.0;

  private static final double TURRET_GEARBOX_PINION      = 9.0/1.0;
  private static final double TURRET_GEARBOX_TURRET_GEAR = 140.0/10.0;
 
  private static final double GEAR_REDUCTION     =  TURRET_GEARBOX_PINION * TURRET_GEARBOX_TURRET_GEAR;
  private static final double TURRET_REV_PER_DEG = GEAR_REDUCTION / 360;
  
  private final double HOME_POSITION       = 0.0;

  public static double currentTurretDegree = 0.0; //0.0

  //turret variables
  private double m_turretTargetDegree;
  private double apriltagTrackingPower;
  private double setPositionPower;
  private double offsetAprilTagX;

  private PIDController pid;
  private PIDController limelightPID;
  private double manualTurretPwr;
  

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

    limelightPID = new PIDController(LIMELIGHT_kP,
                                     LIMELIGHT_kI,
                                     LIMELIGHT_kD);
  }
  
  // Get the singleton instance of the Turret Subsystem
  public static SubsystemCatzTurret getInstance() {
      return instance;
  }
  
  private static TurretState currentTurretState;

  public static enum TurretState {
    AUTO,
    TRACKING_APRILTAG,
    FULL_MANUAL
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);

    currentTurretDegree = inputs.turretEncValue / TURRET_REV_PER_DEG; //TBD make conversion
    
    //obtain calculation values
    apriltagTrackingPower = -limelightPID.calculate(offsetAprilTagX, 0);
    setPositionPower      = pid.calculate(currentTurretDegree, m_turretTargetDegree);
    offsetAprilTagX       = SubsystemCatzVision.getInstance().getOffsetX(1);
    

    if(DriverStation.isDisabled()) {
      io.turretSetPwr(0.0);
    } else { 
      if (currentTurretState == TurretState.AUTO) {
        io.turretSetPwr(setPositionPower);

      } else if (currentTurretState == TurretState.TRACKING_APRILTAG) {
        //only track the shooterlimelight to the speaker apriltag
        if(SubsystemCatzVision.getInstance().getAprilTagID(1) == 7) {
          io.turretSetPwr(apriltagTrackingPower);
        }
      }
      else {
        io.turretSetPwr(manualTurretPwr);
      }

    }

    Logger.recordOutput("turret/offsetXTurret", offsetAprilTagX);
    Logger.recordOutput("turret/PwrPID", apriltagTrackingPower);
    Logger.recordOutput("turret/curretnTurretState", currentTurretState);
    Logger.recordOutput("turret/currentTurretDeg", currentTurretDegree);
    Logger.recordOutput("turret/m_TurretTargetDegree", m_turretTargetDegree);
  }

  //------------------------------------Turret Methods----------------
  
  public void setTurretTargetDegree(double turretTargetDegree) {
    currentTurretState = TurretState.AUTO;
    m_turretTargetDegree = turretTargetDegree;
  }
  
  // *********** code for turning turret towards static target (Apriltag tX is static + an offset) ************* //
  public void autoRotate() {
    currentTurretState = TurretState.TRACKING_APRILTAG;
  }
  
  public void rotateLeft(){
    currentTurretState = TurretState.FULL_MANUAL;
    
    if (currentTurretDegree > (TURRET_NEGATIVE_MAX_RANGE - NEGATIVE_DECEL_THRESHOLD)) {
      manualTurretPwr = -TURRET_POWER;
    }
    else if ((currentTurretDegree < (TURRET_NEGATIVE_MAX_RANGE - NEGATIVE_DECEL_THRESHOLD)) && (currentTurretDegree >= TURRET_NEGATIVE_MAX_RANGE) && (manualTurretPwr < 0)){
      manualTurretPwr = -TURRET_DECEL_PWR;
    }
    else if ((currentTurretDegree < TURRET_NEGATIVE_MAX_RANGE))
    {
       io.turretSetPwr(pid.calculate(currentTurretDegree, TURRET_NEGATIVE_MAX_RANGE ));
    }  
    else {
      manualTurretPwr = 0.0;
    }          
  }
  
  
  public void rotateRight(){
    currentTurretState = TurretState.FULL_MANUAL;
    
    if (currentTurretDegree < (TURRET_POSITIVE_MAX_RANGE - POS_DECEL_THRESHOLD)){
      manualTurretPwr = TURRET_POWER;
    }
    else if ((currentTurretDegree > (TURRET_POSITIVE_MAX_RANGE - POS_DECEL_THRESHOLD)) && (currentTurretDegree < TURRET_POSITIVE_MAX_RANGE) && (manualTurretPwr > 0)){
      manualTurretPwr = TURRET_DECEL_PWR;
    }
    else if ((currentTurretDegree > TURRET_POSITIVE_MAX_RANGE))
    {
      io.turretSetPwr(pid.calculate(currentTurretDegree, TURRET_POSITIVE_MAX_RANGE));
    }  
    else {
      manualTurretPwr = 0.0;
    }          
  }

  public double getTurretAngle() {
    return currentTurretDegree;
  }

  
  
  //-------------------------------------Manual methods--------------------------------
  public Command cmdTurretLT() {
    return run(() -> rotateLeft());
  }
  
  public Command cmdTurretRT() {
    return run(() -> rotateRight());
  }
  
  public Command cmdTurretOff() {
    currentTurretState = TurretState.FULL_MANUAL;
    return run(() -> io.turretSetPwr(0.0));
  }
  
  public Command cmdResetTurretPosition(){
    return run(() -> io.turretSetEncoderPos(HOME_POSITION));
  }
  
  public Command cmdTurretDegree(double turretDeg) {
    return run(() -> setTurretTargetDegree(turretDeg));
  }

  public Command cmdAutoRotate() {
    return run(() -> autoRotate());
  }
  
}
