// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.elevator;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.CatzConstants;
import frc.robot.CatzConstants.CatzMechanismConstants;
import frc.robot.CatzConstants.ElevatorConstants;
import frc.robot.Utils.CatzMechanismPosition;
import frc.robot.subsystems.elevator.ElevatorIOInputsAutoLogged;


public class SubsystemCatzElevator extends SubsystemBase {
  
  private final ElevatorIO io;
  private static SubsystemCatzElevator instance;
  private final ElevatorIOInputsAutoLogged inputs = new ElevatorIOInputsAutoLogged();

  private CatzMechanismPosition m_newPosition;

  private double m_elevatorPercentOutput;

  public SubsystemCatzElevator() {
            switch (CatzConstants.currentMode) {
            case REAL: io = 
                    new ElevatorIOReal();
                break;
            case SIM : io = null;
                break;
            default : io = 
                    new ElevatorIOReal() {};
                break;
        }
  }

  //Run every 20 ms
  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Elevator/inputs", inputs);

    double targetEncPos;

    if(DriverStation.isDisabled()) {
      io.setElevatorPercentOutput(0);
    }
    else if(m_newPosition != null) {
      targetEncPos = m_newPosition.getElevatorTargetEncPos();
      io.setElevatorPosition(targetEncPos);
      Logger.recordOutput("targetEncElevator", targetEncPos);
    } else {
      io.setElevatorPercentOutput(m_elevatorPercentOutput);
    }

    if(inputs.forwardSwitchTripped){
      io.setSelectedSensorPosition(ElevatorConstants.FWD_SWITCH_POS);
    }
    if(inputs.reverseSwitchTripped){
      io.setSelectedSensorPosition(ElevatorConstants.REV_SWITCH_POS);
    }
  }

  public void setNewPos(CatzMechanismPosition targetPos) {
    m_newPosition = targetPos;
  }

  public void setElevatorPercentOutput(double percentOutput) {
    this.m_elevatorPercentOutput = percentOutput;
  }

  // Get the singleton instance of the ClimbSubsystem
  public static SubsystemCatzElevator getInstance() {
      return instance;
  }
}