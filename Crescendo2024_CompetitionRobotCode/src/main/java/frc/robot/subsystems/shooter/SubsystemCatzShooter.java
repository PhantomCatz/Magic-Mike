// // Copyright (c) FIRST and other WPILib contributors.
// // Open Source Software; you can modify and/or share it under the terms of
// // the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.shooter;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.CatzConstants;
import frc.robot.CatzConstants.CatzMechanismConstants;
import frc.robot.CatzConstants.OIConstants;
import frc.robot.Utils.CatzMechanismPosition;
import frc.robot.Utils.LoggedTunableNumber;
import frc.robot.subsystems.turret.SubsystemCatzTurret;


public class SubsystemCatzShooter extends SubsystemBase {
  
  private final ShooterIO io;
  private final ShooterIOInputsAutoLogged inputs = new ShooterIOInputsAutoLogged();

  private static SubsystemCatzShooter instance = new SubsystemCatzShooter();
  
  
  //shooter constants and variables
  private static int currentLoaderMode;
      
  /*-----------------------------------------------------------------------------------------
   * Linear Servo Values
   *-----------------------------------------------------------------------------------------*/
  LoggedTunableNumber servoPosTunning = new LoggedTunableNumber("ServoPos", 0);
    
  /*-----------------------------------------------------------------------------------------
   * States and Variables for Periodic
   *-----------------------------------------------------------------------------------------*/
  private static final int LOAD_IN = 1;
  private static final int FINE_TUNE = 2;
  private static final int LOAD_IN_DONE = 3;
  private static final int WAIT_FOR_NOTE_TO_SETTLE = 4;
  private static final int WAIT_FOR_MOTORS_TO_REV_UP = 5;
  private static final int START_SHOOTER_FLYWHEEL = 6;
  private static final int SHOOTING = 7;
  private static final int LOAD_OFF = 8;
  private static final int LOAD_OUT = 9;



/*-----------------------------------------------------------------------------------------
  * Constants for Shooter
  *-----------------------------------------------------------------------------------------*/
  public static double SERVO_OPTIMAL_HANDOFF_POS = 0.0;

  private double m_newServoPosition;
  private double m_servoPosError;

  //for determining state machine for shooter
  private ShooterServoState currentShooterServoState;
  public enum ShooterServoState {
    FULL_MANUAL,
    AUTO,
    TUNNING,
    IN_POSITION
  }
 
  //shooter note state for determining when other mechanism should turn off
  private ShooterNoteState currentNoteState;
  public enum ShooterNoteState {
    NOTE_IN_POSTION,
    NOTE_IN_ADJUST,
    NOTE_HAS_BEEN_SHOOT,
    NULL
  }

  private final double LOOP_CYCLE_MS = 0.02;

  private static final boolean BEAM_IS_BROKEN     = true;
  private static final boolean BEAM_IS_NOT_BROKEN = false;

  private boolean m_desiredBeamBreakState;
  private int     m_iterationCounter;


  
  //XboxController for rumbling
  private XboxController xboxDrvRumble;

  private SubsystemCatzShooter() {
    
    //XboxController
    xboxDrvRumble = new XboxController(OIConstants.XBOX_DRV_PORT);
    //XboxController
    xboxDrvRumble = new XboxController(OIConstants.XBOX_DRV_PORT);

    switch (CatzConstants.currentMode) {
      case REAL: io = new ShooterIOReal();
                 System.out.println("Shooter Configured for Real");
      break;

      case REPLAY: io = new ShooterIOReal() {};
                   System.out.println("Shooter Configured for Replayed Simulation");
      break;

      case SIM:
      default: io = null;
               System.out.println("Current Mode Unconfigured");
      break;
    }
  }
  
  
  // Get the singleton instance of the ShooterSubsystem
  public static SubsystemCatzShooter getInstance() {
      return instance;
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Shooter/shooterinputs", inputs);
        
    //load motor logic
    switch(currentLoaderMode) {
        case LOAD_IN:
          io.loadNote();
          currentLoaderMode = LOAD_IN_DONE;
          currentNoteState = ShooterNoteState.NOTE_IN_ADJUST;
        break;

        case LOAD_IN_DONE:
          if(inputs.shooterLoadBeamBreakState == BEAM_IS_BROKEN) { 
            io.loadDisabled();
            m_iterationCounter = 0;
            currentLoaderMode = WAIT_FOR_NOTE_TO_SETTLE;


          }
        break;

        case WAIT_FOR_NOTE_TO_SETTLE:
          if(inputs.shooterAdjustBeamBreakState == BEAM_IS_BROKEN) { //set off flag that determines adjust direction
            m_desiredBeamBreakState = BEAM_IS_NOT_BROKEN;
            io.fineAdjustBck();
          } else {
            m_desiredBeamBreakState = BEAM_IS_BROKEN;
            io.fineAdjustFwd();
          }
              currentLoaderMode = FINE_TUNE;
        break;

        case FINE_TUNE:
          if(inputs.shooterAdjustBeamBreakState == m_desiredBeamBreakState) { //if front is still conncected adjust foward until it breaks
            io.loadDisabled();
            currentLoaderMode = LOAD_OFF;
            currentNoteState = ShooterNoteState.NOTE_IN_POSTION;
          }
        break;
        
        case START_SHOOTER_FLYWHEEL:
          io.setShooterEnabled();
          currentLoaderMode = WAIT_FOR_MOTORS_TO_REV_UP;
        break;
        
        
        case WAIT_FOR_MOTORS_TO_REV_UP:
        //System.out.println(-inputs.shooterVelocityLT + " Lt sHOOTER " + inputs.velocityThresholdLT);
          if(-inputs.shooterVelocityLT >= inputs.velocityThresholdLT &&
              inputs.shooterVelocityRT >= inputs.velocityThresholdRT) {

            if(DriverStation.isAutonomous()) {
              currentLoaderMode = SHOOTING;
            } else {
              xboxDrvRumble.setRumble(RumbleType.kBothRumble, 0.7);
              
              m_iterationCounter = 0;
            }
          }
        break;

        case SHOOTING:
          io.feedShooter();
          if(DriverStation.isAutonomous() == false) {
            xboxDrvRumble.setRumble(RumbleType.kBothRumble, 0);
          }
          m_iterationCounter++;
          if(m_iterationCounter >= timer(1)) {
            io.setShooterDisabled();
            currentLoaderMode = LOAD_OFF;
            currentNoteState = ShooterNoteState.NOTE_HAS_BEEN_SHOOT;
            SubsystemCatzTurret.getInstance().setTurretTargetDegree(0.0);
          }
        break;

        case LOAD_OFF:
          io.loadDisabled();
        break;

        case LOAD_OUT:
          io.loadBackward();
          m_iterationCounter++;
          if(m_iterationCounter >= timer(0.5)) {
            io.loadDisabled();
            currentLoaderMode = LOAD_OFF;
            m_iterationCounter = 0;
          }
        break;
    }
    Logger.recordOutput("current load state", currentLoaderMode);
    

    //servo Logic
    m_servoPosError = inputs.servoLeftPosition - m_newServoPosition;

    //if(currentShooterServoState == ShooterServoState.TUNNING) {
      double servoPosition = servoPosTunning.get();
      io.setServoPosition(servoPosition);
    //}

    if(currentShooterServoState == ShooterServoState.AUTO) {
      io.setServoPosition(m_newServoPosition);
      if(Math.abs(m_servoPosError) < 0.1) {
        currentShooterServoState = ShooterServoState.IN_POSITION;
      }
    }
  }

  //-------------------------------------------------------------------------------------
  // Intake Calculation Methods
  //-------------------------------------------------------------------------------------
  public void updateShooterTargetPosition(CatzMechanismPosition newPosition) {
    currentShooterServoState = ShooterServoState.AUTO;
    m_newServoPosition = newPosition.getShooterVerticalTargetAngle();
    if(newPosition == CatzMechanismConstants.NOTE_POS_HANDOFF_SPEAKER_PREP) {
      currentLoaderMode = LOAD_IN;
    } else if(newPosition == CatzMechanismConstants.NOTE_POS_HANDOFF_AMP_PREP) {
      currentLoaderMode = LOAD_OUT;
    }
  }

  public void updateShooterServo(double position) {
    currentShooterServoState = ShooterServoState.AUTO;
    m_newServoPosition = position;
  }

  public Command setPositionCmd(double position) {
    currentShooterServoState = ShooterServoState.FULL_MANUAL;
    return run(()->m_newServoPosition = position);
  }

  //-------------------------------------------------------------------------------------
  // Calculation Methods 
  //------------------------------------------------------------------------------------- 
  private double timer(double seconds){ // in seconds; converts time to iteration counter units
    System.out.println(Math.round(seconds/LOOP_CYCLE_MS) + 1);
    return Math.round(seconds/LOOP_CYCLE_MS) + 1;
  }

  //-------------------------------------------------------------------------------------
  // Getter Methods 
  //------------------------------------------------------------------------------------- 
  public ShooterServoState getShooterServoState() {
    return currentShooterServoState;
  }
  public ShooterNoteState getShooterNoteState() {
    return currentNoteState;
  }

  public boolean shooterLoadBeamBrkBroken() {
    return inputs.shooterLoadBeamBreakState;
  }
  
  //-------------------------------------------------------------------------------------
  // Flywheel Commands
  //-------------------------------------------------------------------------------------
  public Command cmdShooterEnabled() {
    return runOnce(()->startShooterFlywheel());
  }

  public void startShooterFlywheel() {
    currentLoaderMode = START_SHOOTER_FLYWHEEL;
  }

  public Command cmdShooterDisabled() {
    return runOnce(()->io.setShooterDisabled());
  }

  //-------------------------------------------------------------------------------------
  // Shooter Loading Methods 
  //-------------------------------------------------------------------------------------
  public Command cmdShoot() {
      return runOnce(()->currentLoaderMode = SHOOTING);
  }

  public Command cmdLoad(){
    return runOnce(()->currentLoaderMode = LOAD_IN);
  }
  
  public Command loadBackward() {
    m_iterationCounter = 0;
    return runOnce(()->currentLoaderMode = LOAD_OUT);
  }
  
  public Command loadDisabled() {
    return runOnce(()->currentLoaderMode = LOAD_OFF);
  }

}
