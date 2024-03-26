// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.mechanismCmds;

import edu.wpi.first.wpilibj.Timer;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.CatzConstants.CatzMechanismConstants;
import frc.robot.Utils.CatzMechanismPosition;
import frc.robot.CatzConstants.NoteDestination;
import frc.robot.CatzConstants.NoteSource;
import frc.robot.subsystems.elevator.SubsystemCatzElevator;
import frc.robot.subsystems.elevator.SubsystemCatzElevator.ElevatorControlState;
import frc.robot.subsystems.intake.SubsystemCatzIntake;
import frc.robot.subsystems.intake.SubsystemCatzIntake.IntakeRollerState;
import frc.robot.subsystems.intake.SubsystemCatzIntake.IntakeControlState;
import frc.robot.subsystems.shooter.SubsystemCatzShooter;
import frc.robot.subsystems.turret.SubsystemCatzTurret;
import frc.robot.subsystems.turret.SubsystemCatzTurret.TurretState;

public class ScoreAmpCmd extends Command {
  private SubsystemCatzElevator elevator = SubsystemCatzElevator.getInstance();
  private SubsystemCatzIntake intake = SubsystemCatzIntake.getInstance();
  private SubsystemCatzShooter shooter = SubsystemCatzShooter.getInstance();
  private SubsystemCatzTurret turret = SubsystemCatzTurret.getInstance();

  private static Timer intakeNoteTimer = new Timer();

  private boolean m_targetMechPoseStartReached = false;
  private boolean m_targetMechPoseEndReached   = false;

  public ScoreAmpCmd() {
    addRequirements(intake, elevator, shooter, turret);
  }

  @Override
  public void initialize() {
    intake.setSquishyMode(true);
    if(intake.getWristAngle() < SubsystemCatzIntake.INTAKE_TRANSITION_CHECK_DEG) {
      runMechanismSetpoints(CatzMechanismConstants.SCORING_AMP_PRESET);
    }
    intakeNoteTimer.reset();
    intake.setWasIntakeInAmpScoring(false);
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    if(intake.getWristAngle() > -30) {
      intake.pivotFullManual(-0.1);
    } else {
      intake.pivotFullManual(-0.3);
    }

  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    intake.setSquishyMode(false);
      runMechanismSetpoints(CatzMechanismConstants.AMP_TRANSITION_PRESET);
    intake.setWasIntakeInAmpScoring(true);

  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }

  //factory for updating all mechanisms with the packaged target info associated with the new postion
  private void runMechanismSetpoints(CatzMechanismPosition pose) {
    elevator.updateTargetPositionElevator(pose.getElevatorTargetRev());
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
