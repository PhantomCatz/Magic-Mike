// // Copyright (c) FIRST and other WPILib contributors.
// // Open Source Software; you can modify and/or share it under the terms of
// // the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.mechanismCmds;

import java.util.function.Supplier;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.CatzConstants;
import frc.robot.CatzConstants.CatzMechanismConstants;
import frc.robot.Robot.manipulatorMode;
import frc.robot.Utils.CatzMechanismPosition;
import frc.robot.subsystems.CatzStateMachine;
import frc.robot.subsystems.CatzStateMachine.NoteDestination;
import frc.robot.subsystems.CatzStateMachine.NoteSource;
import frc.robot.subsystems.elevator.SubsystemCatzElevator;
import frc.robot.subsystems.elevator.SubsystemCatzElevator.ElevatorControlState;
import frc.robot.subsystems.intake.SubsystemCatzIntake;
import frc.robot.subsystems.intake.SubsystemCatzIntake.IntakeRollerState;
import frc.robot.subsystems.intake.SubsystemCatzIntake.IntakeControlState;
import frc.robot.subsystems.shooter.SubsystemCatzShooter;
import frc.robot.subsystems.shooter.SubsystemCatzShooter.ShooterLoadState;
import frc.robot.subsystems.shooter.SubsystemCatzShooter.ShooterNoteState;
import frc.robot.subsystems.shooter.SubsystemCatzShooter.ShooterServoState;
import frc.robot.subsystems.turret.SubsystemCatzTurret;
import frc.robot.subsystems.turret.SubsystemCatzTurret.TurretState;

public class MoveToHandoffPoseCmd extends Command {
  
  //subsystem declaration
  private SubsystemCatzElevator elevator = SubsystemCatzElevator.getInstance();
  private SubsystemCatzIntake intake = SubsystemCatzIntake.getInstance();
  private SubsystemCatzShooter shooter = SubsystemCatzShooter.getInstance();
  private SubsystemCatzTurret turret = SubsystemCatzTurret.getInstance();

  //target Pose declaration
  private CatzMechanismPosition m_targetMechPoseStart;
  private CatzMechanismPosition m_targetMechPoseEnd;


  //destination targets
  private NoteDestination m_noteDestination;
  private NoteSource m_noteSource;

  private boolean m_targetMechPoseStartReached = false;
  private boolean m_targetMechPoseEndReached   = false;

  private Timer transferToShooter  = new Timer();


  public MoveToHandoffPoseCmd(NoteDestination noteDestination, NoteSource noteSource) {
    this.m_noteDestination = noteDestination;
    this.m_noteSource = noteSource;

    addRequirements(intake, elevator, turret, shooter);
  }

  @Override
  public void initialize() {
    if(m_noteDestination == NoteDestination.AMP &&
       m_noteSource == NoteSource.FROM_SHOOTER) {
        CatzStateMachine.getInstance().cmdNewNoteDestintation(NoteDestination.AMP);
    }

    if(m_noteDestination == NoteDestination.SPEAKER &&
       m_noteSource == NoteSource.FROM_INTAKE) {
        CatzStateMachine.getInstance().cmdNewNoteDestintation(NoteDestination.SPEAKER);
    }

    System.out.println("Handoff " + m_noteDestination.toString());
    System.out.println(m_noteSource.toString());
    m_targetMechPoseStartReached = false;
    m_targetMechPoseEndReached   = false;

    switch(m_noteSource) {
      case INTAKE_GROUND:
        m_targetMechPoseStart = CatzMechanismConstants.INTAKE_GROUND;
        intake.setRollersGround();

        if(m_noteDestination == NoteDestination.HOARD ||
           m_noteDestination == NoteDestination.SPEAKER) {

            m_targetMechPoseEnd = CatzMechanismConstants.STOW;
            System.out.println("Ground speaker");
        } else if(m_noteDestination == NoteDestination.AMP)  {

            m_targetMechPoseEnd = CatzMechanismConstants.AMP_TRANSITION;
            System.out.println("Ground AMP");
        }
      break;

      case INTAKE_SOURCE:
        m_targetMechPoseStart = CatzMechanismConstants.INTAKE_SOURCE;

        if(m_noteDestination == NoteDestination.HOARD ||
           m_noteDestination == NoteDestination.SPEAKER) {

            m_targetMechPoseEnd = CatzMechanismConstants.STOW;
            intake.setRollersIntakeSource();
            System.out.println(" Source Speaker");
        } else if(m_noteDestination == NoteDestination.AMP) {
          m_targetMechPoseEnd = m_targetMechPoseStart;
                        System.out.println("Source Amp");
        }      
      break;

      case FROM_INTAKE:
        m_targetMechPoseStart = CatzMechanismConstants.STOW;

        if(m_noteDestination == NoteDestination.HOARD ||
           m_noteDestination == NoteDestination.SPEAKER) {
            System.out.println("Intake Speaker");

          m_targetMechPoseEnd = CatzMechanismConstants.STOW;
        } else if(m_noteDestination == NoteDestination.AMP) {
            System.out.println("Intake Amp");
          m_targetMechPoseEnd = CatzMechanismConstants.AMP_TRANSITION;
        }
      
      break;

      case FROM_SHOOTER:
        m_targetMechPoseStart = CatzMechanismConstants.STOW;

        if(m_noteDestination == NoteDestination.AMP) {
            m_targetMechPoseEnd = CatzMechanismConstants.AMP_TRANSITION;
            System.out.println("Shooter Amp");

        } 
     
      break;
        
      default: 
        //invalid command...should have used switch handoff positions cmd
        m_targetMechPoseStart = CatzMechanismConstants.HOME;
      break;
    }

    runMechanismSetpoints(m_targetMechPoseStart);    //run initial sepoint
  }

  
  @Override
  public void execute() {
  boolean mechInPos = false;

    if(m_noteSource == NoteSource.INTAKE_GROUND ||
       m_noteSource == NoteSource.INTAKE_SOURCE) {

      //when the the rollers stop intaking due to beambreak
      if(intake.getIntakeBeamBreakBroken()) {
        if(m_targetMechPoseStartReached == false) { 
          
          runMechanismSetpoints(m_targetMechPoseEnd);

          if(m_noteDestination == NoteDestination.SPEAKER) {
            shooter.setShooterLoadState(ShooterLoadState.LOAD_IN);
          }

          m_targetMechPoseStartReached = true; //reached start postion and start for end position
        }
      }

      //when the mechanisms have all reached their end position after collecting 
      if(m_targetMechPoseStartReached == true &&
         m_targetMechPoseEndReached   == false) {

        mechInPos = areMechanismsInPosition();
        if(mechInPos) {

          if(m_noteDestination == NoteDestination.SPEAKER) {
             intake.setRollersOutakeHandoff();

            if(shooter.getShooterNoteState() == ShooterNoteState.NOTE_IN_POSTION) {
              intake.setRollersOff();
              m_targetMechPoseEndReached = true;
            } 
          } else {
            //keep note in intake
          }
        }
      }
    } else if(m_noteSource == NoteSource.FROM_SHOOTER) {
      //when the the rollers stop intaking due to beambreak
      if(m_targetMechPoseStartReached == false) {
        if(areMechanismsInPosition()) {
          intake.setRollersIntakeSource();
          shooter.setShooterLoadState(ShooterLoadState.LOAD_OUT);
          m_targetMechPoseStartReached = true;
        }
      }

      //when the mechanisms have all reached their end position after collecting 
      if(m_targetMechPoseStartReached && 
         m_targetMechPoseEndReached == false) {

        if(intake.getIntakeBeamBreakBroken()) { 

          intake.setRollersOff(); 

          if(m_noteDestination == NoteDestination.AMP) {
            runMechanismSetpoints(m_targetMechPoseEnd);
            m_targetMechPoseEndReached = true;
          } else {
            //keep note in intake
          }
        }
      } 
    } else if(m_noteSource == NoteSource.FROM_INTAKE) {
      //when the the rollers stop intaking due to beambreak
      if(m_targetMechPoseStartReached == false) {
        if(areMechanismsInPosition()) {
            intake.setRollersOutakeHandoff();
          shooter.setShooterLoadState(ShooterLoadState.LOAD_IN);
          m_targetMechPoseStartReached = true;
        }
      }

      //when the mechanisms have all reached their end position after collecting 
      if(m_targetMechPoseStartReached && 
         m_targetMechPoseEndReached == false) {

        if(shooter.shooterLoadBeamBrkBroken()) { 
          intake.setRollersOff();  
        }
      } 
    }


  }

  //factory for updating all mechanisms with the packaged target info associated with the new postion
  private void runMechanismSetpoints(CatzMechanismPosition pose) {

    intake  .updateAutoTargetPositionIntake(pose.getIntakePivotTargetAngle());
    elevator.updateTargetPositionElevator(pose);
    shooter .updateTargetPositionShooter (pose);
    turret  .updateTargetPositionTurret  (pose);
  }

  private boolean areMechanismsInPosition() {
    boolean intakeState   = intake.getIntakeInPos(); 
    boolean turretState   = turret.getTurretInPos();
    boolean shooterState  = shooter.getShooterServoInPos();
    boolean elevatorState = elevator.getElevatorInPos();
    System.out.println("i " + intakeState + "t " + turretState + "s " + shooterState + "e " +elevatorState);
    return(intakeState && turretState && shooterState && elevatorState);
  }

  @Override
  public void end(boolean interrupted) {}

  @Override
  public boolean isFinished() {
    return false;
  }
}
