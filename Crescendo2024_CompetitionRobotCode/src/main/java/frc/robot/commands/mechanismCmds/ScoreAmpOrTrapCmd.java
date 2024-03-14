// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.mechanismCmds;

import edu.wpi.first.wpilibj.Timer;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.CatzConstants.CatzMechanismConstants;
import frc.robot.Utils.CatzMechanismPosition;
import frc.robot.subsystems.CatzStateMachine;
import frc.robot.subsystems.CatzStateMachine.NoteDestination;
import frc.robot.subsystems.elevator.SubsystemCatzElevator;
import frc.robot.subsystems.elevator.SubsystemCatzElevator.ElevatorControlState;
import frc.robot.subsystems.intake.SubsystemCatzIntake;
import frc.robot.subsystems.intake.SubsystemCatzIntake.IntakeRollerState;
import frc.robot.subsystems.intake.SubsystemCatzIntake.IntakeControlState;
import frc.robot.subsystems.shooter.SubsystemCatzShooter;
import frc.robot.subsystems.shooter.SubsystemCatzShooter.ShooterServoState;
import frc.robot.subsystems.turret.SubsystemCatzTurret;
import frc.robot.subsystems.turret.SubsystemCatzTurret.TurretState;

public class ScoreAmpOrTrapCmd extends Command {
  private SubsystemCatzElevator elevator = SubsystemCatzElevator.getInstance();
  private SubsystemCatzIntake intake = SubsystemCatzIntake.getInstance();
  private SubsystemCatzShooter shooter = SubsystemCatzShooter.getInstance();
  private SubsystemCatzTurret turret = SubsystemCatzTurret.getInstance();

  private static Timer intakeNoteTimer = new Timer();

  private boolean m_targetMechPoseStartReached = false;
  private boolean m_targetMechPoseEndReached   = false;

  public ScoreAmpOrTrapCmd() {
    addRequirements(intake, elevator, shooter, turret);
  }

  @Override
  public void initialize() {
    intake.setSquishyMode(true);
    if(CatzStateMachine.getInstance().getNoteDestination() == NoteDestination.TRAP) {
      runMechanismSetpoints(CatzMechanismConstants.SCORING_TRAP);
    } else {
      runMechanismSetpoints(CatzMechanismConstants.SCORING_AMP);
    }
    intakeNoteTimer.reset();

  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {


  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    intake.setSquishyMode(false);
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }

  //factory for updating all mechanisms with the packaged target info associated with the new postion
  private void runMechanismSetpoints(CatzMechanismPosition pose) {
    intake.updateAutoTargetPositionIntake(pose.getIntakePivotTargetAngle());
    elevator.updateTargetPositionElevator(pose);
    shooter.updateTargetPositionShooter(pose);
    turret.updateTargetPositionTurret(pose);
  }

  private boolean areMechanismsInPosition() {
    return (intake.getIntakeInPos() && 
            turret.getTurretInPos() &&
            shooter.getShooterServoInPos() &&
            elevator.getElevatorInPos());
  }
}