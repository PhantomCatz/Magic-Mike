package frc.robot;

import java.util.function.Supplier;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.CatzConstants.CatzColorConstants;
import frc.robot.CatzConstants.CatzMechanismConstants;
import frc.robot.CatzConstants.OIConstants;
import frc.robot.Utils.CatzMechanismPosition;
import frc.robot.commands.DriveCmds.TeleopDriveCmd;
import frc.robot.commands.mechanismCmds.MoveToPresetHandoffCmd;
import frc.robot.commands.mechanismCmds.MoveToPreset;
import frc.robot.commands.mechanismCmds.ScoreAmpCmd;
import frc.robot.commands.mechanismCmds.ScoreTrapCmd;
import frc.robot.commands.mechanismCmds.ClimbCmd;
import frc.robot.commands.mechanismCmds.StowPoseCmd;
import frc.robot.commands.mechanismCmds.ManualElevatorCmd;
import frc.robot.commands.mechanismCmds.MoveToAmpTransition;
import frc.robot.commands.mechanismCmds.AimAndOrFireAtSpeakerCmd;
import frc.robot.subsystems.CatzStateMachine;
import frc.robot.subsystems.CatzStateMachine.NoteDestination;
import frc.robot.subsystems.CatzStateMachine.NoteSource;
import frc.robot.subsystems.LEDs.SubsystemCatzLED;
import frc.robot.subsystems.climb.SubsystemCatzClimb;
import frc.robot.subsystems.drivetrain.SubsystemCatzDrivetrain;
import frc.robot.subsystems.elevator.SubsystemCatzElevator;
import frc.robot.subsystems.intake.SubsystemCatzIntake;
import frc.robot.subsystems.shooter.SubsystemCatzShooter;
import frc.robot.subsystems.turret.SubsystemCatzTurret;
import frc.robot.subsystems.vision.SubsystemCatzVision;

 public class RobotContainer {
    
  //subsystems
  private SubsystemCatzDrivetrain driveTrain; 
  private SubsystemCatzVision     vision;
  private SubsystemCatzIntake     intake;
  private SubsystemCatzShooter    shooter;
  private SubsystemCatzClimb      climb;
  private SubsystemCatzElevator   elevator;
  private SubsystemCatzTurret     turret;
  private SubsystemCatzLED        led;

  private CatzStateMachine stateMachine;

  private CatzAutonomous auton = new CatzAutonomous();

  //xbox controller
  private CommandXboxController xboxDrv;
  private CommandXboxController xboxAux;

  public RobotContainer() {
    //instantiate subsystems
    elevator   = SubsystemCatzElevator.getInstance();
    driveTrain = SubsystemCatzDrivetrain.getInstance(); 
    vision     = SubsystemCatzVision.getInstance();
    intake     = SubsystemCatzIntake.getInstance();
    turret     = SubsystemCatzTurret.getInstance();
    shooter    = SubsystemCatzShooter.getInstance();
    climb      = SubsystemCatzClimb.getInstance();

    stateMachine = CatzStateMachine.getInstance();
    

    xboxDrv = new CommandXboxController(OIConstants.XBOX_DRV_PORT); 
    xboxAux = new CommandXboxController(OIConstants.XBOX_AUX_PORT);

    // Configure the trigger bindings and default cmds
    defaultCommands();
    configureBindings();
  }
  
  private boolean isRampedUp = false;
  private boolean dpadUP = false;
  private boolean dpadDN = false;
  private boolean dpadRT = false;
  private boolean dpadLT = false;

  private void configureBindings() {    
    
    //------------------------------------------------------------------------------------
    //  Drive commands
    //------------------------------------------------------------------------------------
    //RESET GYRO
      xboxDrv.start().onTrue(driveTrain.resetGyro());

      //LED
      xboxDrv.x().and(xboxDrv.back()).onTrue(Commands.runOnce(()->led.signalHumanPlayerAMP()));

      //INTAKE
      xboxDrv.leftBumper().onTrue(intake.cmdRollerIn()); // intake in
      xboxDrv.rightBumper().onTrue(intake.cmdRollerOut()); // intake out
      xboxDrv.b().onTrue(intake.cmdRollerOff());
   

      xboxDrv.rightStick().onTrue(new StowPoseCmd());
     
      xboxDrv.leftStick().and(xboxDrv.povRight()).onTrue(new MoveToPresetHandoffCmd(NoteDestination.SPEAKER, NoteSource.INTAKE_GROUND));
      xboxDrv.leftStick().and(xboxDrv.povLeft()).onTrue(new MoveToPresetHandoffCmd(NoteDestination.AMP, NoteSource.INTAKE_GROUND));

    //----------------------------------------------------------------------------------------
    //  Aux Commands
    //---------------------------------------------------------------------------------------- 

    //SPEAKER MODE
    xboxAux.y().and(xboxAux.povRight()).onTrue(new MoveToPresetHandoffCmd(NoteDestination.SPEAKER, NoteSource.FROM_INTAKE));

    xboxAux.b().and(xboxAux.povRight()).onTrue(shooter.shootPreNote());
    xboxAux.a().and(xboxAux.povRight()).onTrue(shooter.cmdShoot());

    xboxAux.x().and(xboxAux.povRight()).onTrue(new AimAndOrFireAtSpeakerCmd());

    xboxAux.povRight().onTrue(shooter.cmdServoPosition(xboxAux.getRightY()));

    xboxAux.povRight().onTrue(turret.rotate(xboxAux.getRightX()));



    //AMP MODE
    xboxAux.y().and(xboxAux.povLeft()).onTrue(new MoveToPresetHandoffCmd(NoteDestination.AMP, NoteSource.FROM_SHOOTER));

    xboxAux.x().and(xboxAux.povLeft()).onTrue(new MoveToAmpTransition());

    xboxAux.b().and(xboxAux.povLeft()).onTrue(new ScoreAmpCmd());

    xboxAux.povLeft().onTrue(new ManualElevatorCmd(()->xboxAux.getRightY()));

    // xboxAux.x().and(()->stateMachine.getNoteDestination() == NoteDestination.AMP).onTrue(Commands.print("Amp state"));
    // xboxAux.x().and(()->stateMachine.getNoteDestination() == NoteDestination.SPEAKER).onTrue(Commands.print("speaker state"));

    xboxAux.rightStick().onTrue(shooter.setPositionCmd(()->xboxAux.getRightY()));

    xboxAux.start().onTrue(new MoveToPreset(CatzMechanismConstants.HOARD_PRESET));


    //CLIMB MODE

    xboxAux.povUp().onTrue(new ClimbCmd(()-> xboxDrv.getLeftY(), ()-> xboxDrv.getRightY()));
    xboxAux.povUp().and(xboxAux.b()).onTrue(new ScoreTrapCmd());


  }


  //mechanisms with default commands revert back to these cmds if no other cmd requiring the subsystem is active
  private void defaultCommands() {  
    driveTrain.setDefaultCommand(new TeleopDriveCmd(()-> xboxDrv.getLeftX(),
                                                    ()-> xboxDrv.getLeftY(),
                                                    ()-> -xboxDrv.getRightX(),
                                                    ()-> xboxDrv.b().getAsBoolean()));

  }

  public Command getAutonomousCommand() {
    return auton.getCommand();
  }

}
