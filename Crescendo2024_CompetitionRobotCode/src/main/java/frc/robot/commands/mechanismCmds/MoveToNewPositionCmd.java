// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.mechanismCmds;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.CatzConstants;
import frc.robot.CatzConstants.CatzMechanismConstants;
import frc.robot.CatzConstants.ManipulatorMode;
import frc.robot.Robot.manipulatorMode;
import frc.robot.Utils.CatzMechanismPosition;
import frc.robot.subsystems.elevator.SubsystemCatzElevator;
import frc.robot.subsystems.intake.SubsystemCatzIntake;
import frc.robot.subsystems.shooter.SubsystemCatzShooter;

public class MoveToNewPositionCmd extends Command {
  
  //subsystem declaration
  private SubsystemCatzElevator elevator = SubsystemCatzElevator.getInstance();
  private SubsystemCatzIntake intake = SubsystemCatzIntake.getInstance();
  private SubsystemCatzShooter shooter = SubsystemCatzShooter.getInstance();


  private CatzMechanismPosition m_newPosition;
  private ManipulatorMode       m_manipulatorMode;

  private WaitState currentWaitState;
  private enum WaitState {
    WAIT_FOR_INTAKE,
    WAIT_FOR_ELEVATOR,
    NOT_WAITING
  }

  public MoveToNewPositionCmd(CatzMechanismPosition newPosition, ManipulatorMode newManipulatorMode) {
    m_manipulatorMode = newManipulatorMode;
    m_newPosition = newPosition;
    addRequirements(intake, elevator);
  }

  @Override
  public void initialize() {
    intake.updateTargetPosition(m_newPosition);
    elevator.updateTargetPosition(m_newPosition);
  }

  
  @Override
  public void execute() {}

  @Override
  public void end(boolean interrupted) {}

  @Override
  public boolean isFinished() {
    return intake.inPosition() && elevator.inPosition();
  }
}
