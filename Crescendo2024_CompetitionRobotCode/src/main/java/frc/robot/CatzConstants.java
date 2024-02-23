package frc.robot;

import edu.wpi.first.math.controller.HolonomicDriveController;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.util.Units;
import frc.robot.Utils.CatzMechanismPosition;

  import static edu.wpi.first.apriltag.AprilTagFields.k2024Crescendo;

  import edu.wpi.first.apriltag.AprilTagFieldLayout;
  import edu.wpi.first.math.geometry.*;
  import edu.wpi.first.math.util.Units;
  import java.io.IOException;

/***
 * CatzConstants
 * @version 1.0
 * @author Kynam Lenghiem
 * 
 * This class is where reusable constants are defined
 ***/
public final class CatzConstants {
  public static final boolean tuningMode = true;
  public static final Mode currentMode = Mode.REAL;

  public static enum Mode {
    /** Running on a real robot. */
    REAL,

    /** Running a physics simulator. */
    SIM,

    /** Replaying from a log file. */
    REPLAY
  }

  public static enum AllianceColor {
    Blue, Red
  }

 public static final class OIConstants {

  public static final int XBOX_DRV_PORT = 0;
  public static final int XBOX_AUX_PORT = 1;

  public static final int kDriverYAxis = 1;
  public static final int kDriverXAxis = 0;
  public static final int kDriverRotAxis = 4;
  public static final int kDriverFieldOrientedButtonIdx = 1;

  public static final double kDeadband = 0.1;
  }

  public static final class CatzMechanismConstants {
    public static final CatzMechanismPosition POS_STOW = new CatzMechanismPosition(0, 0, 0);
    //public static final CatzMechanismPosition NOTE_POS_HANDOFF = new CatzMechanismPosition();
    //public static final CatzMechanismPosition NOTE_POS_SCORING_SPEAKER = new CatzMechanismPosition();
    public static final CatzMechanismPosition NOTE_POS_SCORING_AMP = new CatzMechanismPosition(100000, 0, 0);
    public static final CatzMechanismPosition NOTE_POS_INTAKE_GROUND = new CatzMechanismPosition(0, 1, 0);
    //public static final CatzMechanismPosition NOTE_POS_INTAKE_SOURCE = new CatzMechanismPosition();
    //public static final CatzMechanismPosition POS_CLIMB_PREP = new CatzMechanismPosition();
    //public static final CatzMechanismPosition POS_CLIMB = new CatzMechanismPosition();
    //public static final CatzMechanismPosition POS_CLIMB_SCORE_TRAP = new CatzMechanismPosition();

  }

  public static final class VisionConstants {
    public static final double LOWEST_DISTANCE = Units.feetToMeters(10.0);
    public static final Transform3d LIMELIGHT_OFFSET = new Transform3d(-Units.inchesToMeters(8), -Units.inchesToMeters(11.5), Units.inchesToMeters(19), new Rotation3d(0.0,0.0,180.0)); //tbd need to understand how these classese work transform3d vs translation3d
    public static final Transform3d LIMELIGHT_TURRET_OFFSET = new Transform3d(0.0, 0.0, 0.0, null); //tbd need to understand how these classese work transform3d vs translation3d
  }

  public static final class TrajectoryConstants {
    public static final double ALLOWABLE_POSE_ERROR = 0.05;
    public static final double ALLOWABLE_ROTATION_ERROR = 5;
  }

  /**
   * Contains various field dimensions and useful reference points. Dimensions are in meters, and sets
   * of corners start in the lower left moving clockwise. <b>All units in Meters</b> <br>
   * <br>
   *
   * <p>All translations and poses are stored with the origin at the rightmost point on the BLUE
   * ALLIANCE wall.<br>
   * <br>
   * Length refers to the <i>x</i> direction (as described by wpilib) <br>
   * Width refers to the <i>y</i> direction (as described by wpilib)
   */
  public class FieldConstants {
    public static final double SPEAKER_COORD_MTRS_Y = Units.inchesToMeters(219.277);
    public static double FIELD_LENGTH_MTRS = Units.inchesToMeters(651.223);
    public static double fieldWidth = Units.inchesToMeters(323.277);
    public static double wingX = Units.inchesToMeters(229.201);
    public static double podiumX = Units.inchesToMeters(126.75);
    public static double startingLineX = Units.inchesToMeters(74.111);

    public static Translation2d ampCenter =
        new Translation2d(Units.inchesToMeters(72.455), Units.inchesToMeters(322.996));

    /** Staging locations for each note */
    public static final class StagingLocations {
      public static double centerlineX = FIELD_LENGTH_MTRS / 2.0;

      // need to update
      public static double centerlineFirstY = Units.inchesToMeters(29.638);
      public static double centerlineSeparationY = Units.inchesToMeters(66);
      public static double spikeX = Units.inchesToMeters(114);
      // need
      public static double spikeFirstY = Units.inchesToMeters(161.638);
      public static double spikeSeparationY = Units.inchesToMeters(57);

      public static Translation2d[] centerlineTranslations = new Translation2d[5];
      public static Translation2d[] spikeTranslations = new Translation2d[3];

      static {
        for (int i = 0; i < centerlineTranslations.length; i++) {
          centerlineTranslations[i] =
              new Translation2d(centerlineX, centerlineFirstY + (i * centerlineSeparationY));
        }
      }

      static {
        for (int i = 0; i < spikeTranslations.length; i++) {
          spikeTranslations[i] = new Translation2d(spikeX, spikeFirstY + (i * spikeSeparationY));
        }
      }
    }

    /** Each corner of the speaker * */
    public static final class Speaker {

      // corners (blue alliance origin)
      public static Translation3d topRightSpeaker =
          new Translation3d(
              Units.inchesToMeters(18.055),
              Units.inchesToMeters(238.815),
              Units.inchesToMeters(13.091));

      public static Translation3d topLeftSpeaker =
          new Translation3d(
              Units.inchesToMeters(18.055),
              Units.inchesToMeters(197.765),
              Units.inchesToMeters(83.091));

      public static Translation3d bottomRightSpeaker =
          new Translation3d(0.0, Units.inchesToMeters(238.815), Units.inchesToMeters(78.324));
      public static Translation3d bottomLeftSpeaker =
          new Translation3d(0.0, Units.inchesToMeters(197.765), Units.inchesToMeters(78.324));

      /** Center of the speaker opening (blue alliance) */
      public static Translation3d centerSpeakerOpening =
          new Translation3d(
              topLeftSpeaker.getX() / 2.0,
              fieldWidth - Units.inchesToMeters(104.0),
              (bottomLeftSpeaker.getZ() + bottomRightSpeaker.getZ()) / 2.0);
    }

    public static double aprilTagWidth = Units.inchesToMeters(6.50);
    public static AprilTagFieldLayout aprilTags;

    static {
      try {
        aprilTags = AprilTagFieldLayout.loadFromResource(k2024Crescendo.m_resourceFile);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static final class MtrConfigConstants {
    //Falcon configuration constants
    public static final int     FALCON_CURRENT_LIMIT_AMPS            = 55;
    public static final int     FALCON_CURRENT_LIMIT_TRIGGER_AMPS    = 55;
    public static final double  FALCON_CURRENT_LIMIT_TIMEOUT_SECONDS = 0.5;
    public static final boolean FALCON_ENABLE_CURRENT_LIMIT          = true;

    //Neo config constants
    public static final int     NEO_CURRENT_LIMIT_AMPS      = 30;

    //Neo 550 config constants
  }

  //--------------------------------------Drivetrain-------------------------------
  public static final class DriveConstants {

     public static final double LT_FRNT_OFFSET =  -0.7194285554857165; //-0.0013; //MC ID 2
     public static final double LT_BACK_OFFSET =  0.3238585205964611; //0.0498; //MC ID 4
     public static final double RT_FRNT_OFFSET =  -0.30105623252640606; //0.0222; //MC ID 8//overtime
     public static final double RT_BACK_OFFSET =  -0.0855185771379645;

     public static final int LT_FRNT_DRIVE_ID = 1; //overtime
     public static final int LT_BACK_DRIVE_ID = 3;
     public static final int RT_BACK_DRIVE_ID = 5;
     public static final int RT_FRNT_DRIVE_ID = 7;

    // public static final double LT_FRNT_OFFSET = 0.510253350256333775; //0.5112305378; atlas
    // public static final double LT_BACK_OFFSET = 0.54683660117091502; //0.5446386386;
    // public static final double RT_BACK_OFFSET = 0.756404343910107;//0.7591109064;
    // public static final double RT_FRNT_OFFSET = 0.5354068633851713;//0.5363121009;
    // 0.00406; for SN1
// -0.03950;
// -0.75084;
//  0.55098;

    // public static final int LT_FRNT_DRIVE_ID = 1;
    // public static final int LT_BACK_DRIVE_ID = 3;
    // public static final int RT_BACK_DRIVE_ID = 22;
    // public static final int RT_FRNT_DRIVE_ID = 7; //SN1


    
    public static final int LT_FRNT_STEER_ID = 2;
    public static final int LT_BACK_STEER_ID = 4;
    public static final int RT_BACK_STEER_ID = 6;
    public static final int RT_FRNT_STEER_ID = 8;

    public static final int LT_FRNT_ENC_PORT = 9;
    public static final int LT_BACK_ENC_PORT = 6; //SN1 8 //atlas 6
    public static final int RT_BACK_ENC_PORT = 7;
    public static final int RT_FRNT_ENC_PORT = 8; //SN1 6 //atlas 8

    //--------------------------------------MTR CONFIGS------------------------------------

    public static final Rotation2d defaultRot = new Rotation2d(0.0);
    private static final double ROBOT_WIDTH = Units.inchesToMeters(23.5); //29 atlas
    private static final double ROBOT_LENGTH = Units.inchesToMeters(24); //29 atlas

    public static final double ESTIMATION_COEFFICIENT = 0.025;

    private static final Translation2d SWERVE_LEFT_FRONT_LOCATION  = new Translation2d(ROBOT_LENGTH, ROBOT_WIDTH).div(2.0);
    private static final Translation2d SWERVE_LEFT_BACK_LOCATION   = new Translation2d(-ROBOT_LENGTH, ROBOT_WIDTH).div(2.0);
    private static final Translation2d SWERVE_RIGHT_BACK_LOCATION  = new Translation2d(-ROBOT_LENGTH, -ROBOT_WIDTH).div(2.0);
    private static final Translation2d SWERVE_RIGHT_FRONT_LOCATION = new Translation2d(ROBOT_LENGTH, -ROBOT_WIDTH).div(2.0);
    
    // calculates the orientation and speed of individual swerve modules when given the motion of the whole robot
    public static final SwerveDriveKinematics swerveDriveKinematics = new SwerveDriveKinematics(
        SWERVE_LEFT_FRONT_LOCATION,
        SWERVE_LEFT_BACK_LOCATION,
        SWERVE_RIGHT_BACK_LOCATION,
        SWERVE_RIGHT_FRONT_LOCATION
    );
    
    //data has been referenced using recalc calculator https://www.reca.lc/drive
    public static final double MAX_SPEED = Units.feetToMeters(14.34); // meters per second 4.81

    public static final double MAX_ANGSPEED_RAD_PER_SEC = 12.0; // radians per second
    public static final double MAX_SPEED_DESATURATION = MAX_SPEED; 

    public static final double SDS_L1_GEAR_RATIO = 8.14;       //SDS mk4i L1 ratio reduction
    public static final double SDS_L2_GEAR_RATIO = 6.75;       //SDS mk4i L2 ratio reduction
    public static final double SDS_L2_PLUS_GEAR_RATIO = 6.75 * (14/16);       //SDS mk4i L2 ratio reduction plud random numbers from eddy

                                                                //overtime
    public static final double DRVTRAIN_WHEEL_DIAMETER_METERS = Units.inchesToMeters(3.3);//0.095;// mUnits.inchesToMeters(4);
    public static final double DRVTRAIN_WHEEL_CIRCUMFERENCE   = (Math.PI * DRVTRAIN_WHEEL_DIAMETER_METERS);

    public static final boolean START_FLIPPED = false;

    public static final double FEEDFOWARD_Kv_VELOCITY_METERS = 2.68;
    public static final double FEEDFOWARD_Kv_VELOCITY_ACCELERATION_METERS = 0.24;

    private static ProfiledPIDController autoTurnPIDController = new ProfiledPIDController(5, 0, 0, new TrapezoidProfile.Constraints(3,3));//6

    public static final HolonomicDriveController holonomicDriveController = new HolonomicDriveController(
      new PIDController(2, 0, 0),
      new PIDController(2, 0, 0),
      autoTurnPIDController
    );
  }

  //any type of Elevator Mtr Config Constnats/Logic Constants should go here 
  public static final class ElevatorConstants {
    public static int ELEVATOR_MTR_ID = 50;
  }
  
  //any type of Intake Mtr Config Constnats/Logic Constants should go here 
  public static final class IntakeConstants {
    public static int PIVOT_MTR_ID = 51;
    public static int ROLLER_MTR_ID = 52;
  }

  //any type of Shooter Mtr Config Constnats/Logic Constants should go here 
  public static final class ShooterConstants {
    public static int SHOOTER_MTR_ID = 53;
    public static int TURRET_MTR_ID = 54;
  }
}