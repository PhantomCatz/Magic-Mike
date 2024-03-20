package frc.robot.commands.mechanismCmds;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;

import org.littletonrobotics.junction.Logger;

import java.awt.geom.Point2D;
import java.util.function.Supplier;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.interpolation.InterpolatingDoubleTreeMap;
import edu.wpi.first.math.util.Units;
import frc.robot.CatzAutonomous;
import frc.robot.CatzConstants;
import frc.robot.CatzConstants.CatzMechanismConstants;
import frc.robot.Utils.CatzMechanismPosition;
import frc.robot.Utils.FieldRelativeAccel;
import frc.robot.Utils.FieldRelativeSpeed;
import frc.robot.subsystems.drivetrain.SubsystemCatzDrivetrain;
import frc.robot.subsystems.elevator.SubsystemCatzElevator;
import frc.robot.subsystems.elevator.SubsystemCatzElevator.ElevatorControlState;
import frc.robot.subsystems.intake.SubsystemCatzIntake;
import frc.robot.subsystems.intake.SubsystemCatzIntake.IntakeControlState;
import frc.robot.subsystems.shooter.SubsystemCatzShooter;
import frc.robot.subsystems.turret.SubsystemCatzTurret;
import frc.robot.subsystems.vision.SubsystemCatzVision;


public class AimAndOrFireAtSpeakerCmd extends Command {
  //subsystem declaration
  private SubsystemCatzElevator elevator = SubsystemCatzElevator.getInstance();
  private SubsystemCatzIntake intake = SubsystemCatzIntake.getInstance();
  private SubsystemCatzShooter shooter = SubsystemCatzShooter.getInstance();
  private SubsystemCatzTurret turret = SubsystemCatzTurret.getInstance();
  private SubsystemCatzDrivetrain drivetrain = SubsystemCatzDrivetrain.getInstance();

  //--------------------------------------------------------------
  // Interpolation tables
  //--------------------------------------------------------------
  /** Shooter angle look up table key: meters, values: pivot position */
  private static final InterpolatingDoubleTreeMap shooterPivotTable = new InterpolatingDoubleTreeMap();

  static { //TBD add values in through testing
    shooterPivotTable.put(1.37, 0.6);
    shooterPivotTable.put(1.37, 0.7);
    shooterPivotTable.put(1.87, 0.65);
    shooterPivotTable.put(1.87, 0.5);
    shooterPivotTable.put(2.37, 0.35);
    shooterPivotTable.put(2.37, 0.3);
    shooterPivotTable.put(2.87, 0.3);
    shooterPivotTable.put(2.87, 0.28); //bOUNCEDD OFF INSIDE ON 0.25
    shooterPivotTable.put(3.37, 0.25);
    shooterPivotTable.put(3.37, 0.20);
    shooterPivotTable.put(3.87, 0.125); //bounced off on 0.15
    shooterPivotTable.put(3.87, 0.1);
    shooterPivotTable.put(4.87, 0.1);
    shooterPivotTable.put(4.87, 0.05);
    shooterPivotTable.put(4.87, 0.1);
    shooterPivotTable.put(5.87, 0.0);


  }

  //time table look up for calculating how long it takes to get note into speaker
  /** angle to time look up table key: ty angle, values: time */
  private static final InterpolatingDoubleTreeMap timeTable = new InterpolatingDoubleTreeMap();
      // (distance, time seconds)
  static { 
        // (ty-angle,time)
        timeTable.put(1.37, 0.78);
        timeTable.put(2.37, 0.80);
        timeTable.put(2.87, 0.81);
        timeTable.put(3.37, 0.82);
        timeTable.put(4.87, 0.825);
        timeTable.put(5.87, 0.83);
  
  }

  public static final double k_ACCEL_COMP_FACTOR = 0.100; // in units of seconds

  //aiming variables
  private FieldRelativeSpeed m_robotVel;
  private FieldRelativeAccel m_robotAccel;

  private Translation2d m_targetXY;
  private Translation2d robotToGoalXY;
  private Translation2d movingGoalLocation;
  private Translation2d testGoalLocation;
  private Translation2d toTestGoal;

  //number variables
  private double distanceToSpeakerMeters;
  private double shotTime;
  private double newShotTime;

  private double m_virtualGoalX;
  private double m_virtualGoalY;

  private Supplier<Boolean> m_bSupplier;

  public AimAndOrFireAtSpeakerCmd(Supplier<Boolean> bSupplier) {
    m_bSupplier = bSupplier;
    addRequirements(turret, shooter, intake, elevator);
  }

  public AimAndOrFireAtSpeakerCmd() {
    addRequirements(turret, shooter, intake, elevator);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    //start the flywheel
    shooter.startShooterFlywheel();
    intake.updateAutoTargetPositionIntake(CatzMechanismConstants.INTAKE_SOURCE.getIntakePivotTargetAngle());
    elevator.updateTargetPositionElevator(CatzMechanismConstants.STOW);

    if(CatzAutonomous.chosenAllianceColor.get() == CatzConstants.AllianceColor.Blue) {
      //translation of the blue alliance speaker
      m_targetXY = new Translation2d(0.0, 5.55);
    } else {
      //translation of the Red alliance speaker
      m_targetXY = new Translation2d(0.0 + CatzConstants.FieldConstants.FIELD_LENGTH_MTRS , 5.55);
    }

  }

  @Override 
  public void execute() {
    if(m_bSupplier.get() == true) {
        shooter.cmdShoot();
    }

    double newDist = m_targetXY.getDistance(drivetrain.getPose().getTranslation());
    
    if(intake.getIntakeInPos() &&
       elevator.getElevatorInPos()) {
      //send the new target to the turret
    }
    if(intake.getIntakeInPos()) {
      turret.aimAtGoal(m_targetXY, false, false);
    }

    double servoPos = shooterPivotTable.get(newDist);
    //send new target to the shooter
    shooter.updateShooterServo(servoPos);

    Logger.recordOutput("servoCmdPos", servoPos);
    Logger.recordOutput("ShooterCalcs/Fixed Time", shotTime);
    Logger.recordOutput("ShooterCalcs/NewDist", newDist);
    Logger.recordOutput("ShooterCalcs/Calculated (mtrs)", distanceToSpeakerMeters);
    Logger.recordOutput("ShooterCalcs/NewShotTime", newShotTime);

  }

}
