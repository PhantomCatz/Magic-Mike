package frc.robot;

import com.pathplanner.lib.auto.NamedCommands;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.event.EventLoop;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.PrintCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.CatzConstants.OIConstants;
import frc.robot.Utils.CatzMechanismPosition;
import frc.robot.commands.DriveCmds.TeleopDriveCmd;
import frc.robot.commands.mechanismCmds.MoveToNewPositionCmd;
import frc.robot.commands.mechanismCmds.ManualElevatorCmd;
import frc.robot.commands.mechanismCmds.ManualIntakecmd;
import frc.robot.subsystems.drivetrain.SubsystemCatzDrivetrain;
import frc.robot.subsystems.elevator.SubsystemCatzElevator;
import frc.robot.subsystems.intake.SubsystemCatzIntake;
import frc.robot.subsystems.vision.SubsystemCatzVision;

/**
 * RobotContainer
 * @version 1.0
 * @author Kynam Lenghiem
 * 
 * This class is how the command scheduler(robot.java replacment) is configured
 * Configures:
 * -xbox controller triggers
 * -default commands
 * -instanciated mechanisms using singleton implementation
 * -sets up autonomous from CatzAtutonomouschooser
 */
 public class RobotContainer {
    
    //subsystems
    private SubsystemCatzDrivetrain driveTrain; 
    //private SubsystemCatzVision vision;
    private SubsystemCatzIntake intake;
    //private SubsystemCatzShooter shooter;
    //private SubsystemCatzClimb climb;
    private SubsystemCatzElevator elevator;

    private CatzAutonomous auton = new CatzAutonomous();

    //xbox controller
    public static CommandXboxController xboxDrv;
    private CommandXboxController xboxAux;
 
       
   /** The container for the robot. Contains subsystems, OI devices, and commands. 
    *    -since multiple classes are referencing these mechansims, 
    *     mechanisms are instantiated inside mechanism class(singleton)
    */
   public RobotContainer() {
    //instantiate subsystems
    elevator = SubsystemCatzElevator.getInstance();
    driveTrain = SubsystemCatzDrivetrain.getInstance(); 
    //vision     = SubsystemCatzVision.getInstance();
    intake     = SubsystemCatzIntake.getInstance();

   // shooter    = SubsystemCatzShooter.getInstance();
    //  climb      = SubsystemCatzClimb.getInstance();
    //  arm        = SubsystemCatzElevator.getInstance();
    

     xboxDrv = new CommandXboxController(OIConstants.XBOX_DRV_PORT); 
     xboxAux = new CommandXboxController(OIConstants.XBOX_AUX_PORT);
 
     // Configure the trigger bindings and default cmds
     defaultCommands();
     configureBindings();
   }
 
  
   
   private void configureBindings() {

    
    xboxAux.rightBumper().onTrue(intake.cmdRollerIn());
    xboxAux.leftBumper().onTrue(intake.cmdRollerOut()); 
    //trigger object to store both buttons. If both buttons aren't pressed, stop rollers
    Trigger rollersOffBinding = xboxAux.leftBumper().and (xboxAux.rightBumper());
    rollersOffBinding.onTrue(intake.cmdRollerOff());


    //xboxAux.leftBumper().onTrue(intake.setRollerOut()).onFalse(intake.setRollerDisabled());
    //xboxAux.a().onTrue(new MoveToNewPositionCmd(CatzConstants.CatzMechanismConstants.NOTE_POS_SCORING_AMP));


    //xboxAux.leftStick().onTrue(new ManualIntakecmd(()->xboxAux.getLeftY()/10));
    xboxAux.rightStick().onTrue(new ManualElevatorCmd(()->xboxAux.getRightY()));


    xboxAux.a().onTrue(new MoveToNewPositionCmd(CatzConstants.CatzMechanismConstants.POS_STOW));
    xboxAux.y().onTrue(new MoveToNewPositionCmd(CatzConstants.CatzMechanismConstants.NOTE_POS_SCORING_AMP));
    xboxAux.x().onTrue(new MoveToNewPositionCmd(CatzConstants.CatzMechanismConstants.NOTE_POS_HANDOFF));
    xboxAux.b().onTrue(new MoveToNewPositionCmd(CatzConstants.CatzMechanismConstants.NOTE_POS_INTAKE_GROUND));


 
   }

   //mechanisms with default commands revert back to these cmds if no other cmd requiring the subsystem is active
   private void defaultCommands() {  
      driveTrain.setDefaultCommand(new TeleopDriveCmd(()-> xboxDrv.getLeftX(),
                                                      ()-> xboxDrv.getLeftY(),
                                                      ()-> xboxDrv.getRightX(),
                                                      ()-> xboxDrv.getRightTriggerAxis(), 
                                                      ()-> xboxDrv.b().getAsBoolean()));
    
   }

   /**
    * Use this to pass the autonomous command to the main {@link Robot} class.
    *
    * @return the command to run in autonomous
    */
    public Command getAutonomousCommand() {
      return auton.getCommand();
    }
}
