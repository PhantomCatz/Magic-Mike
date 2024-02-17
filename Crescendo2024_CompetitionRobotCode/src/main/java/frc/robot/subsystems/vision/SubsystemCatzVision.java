package frc.robot.subsystems.vision;
    
import java.util.ArrayList;
import java.util.List;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.CatzConstants;
import frc.robot.CatzConstants.VisionConstants;
import frc.robot.subsystems.vision.VisionIO.VisionIOInputs;


/*
    Assume the Limelight is the front of the robot
*/
public class SubsystemCatzVision extends SubsystemBase {

    final double limelightPlacementHeight = Units.feetToMeters(1.0);
    final double sourceApriltagHeight = Units.feetToMeters(4.0);
    final double speakerApriltagHeight = Units.feetToMeters(4.33);
    final double trapApriltagHeight = Units.feetToMeters(3.969);
    final double ampApriltagHeight = 1.22;

    static double aprilTagDistanceToWall;
    static double aprilTagDistanceToSource;
    static double aprilTagDistanceToTrap;
    static double aprilTagDistanceToSpeaker;
    static double aprilTagDistanceToAmp;
    static double distanceToAprilTag;
    static String primaryAprilTag;
    static boolean horizontallyAllignedWithAprilTag;

    String name;

    static double horizontalTargetOffset;

    private static SubsystemCatzVision instance = null;

    private final VisionIO camera;
    private final VisionIOInputsAutoLogged inputs;

    private final ArrayList<VisionIOInputsAutoLogged> visionInputArray = new ArrayList<VisionIOInputsAutoLogged>();

    private final List<SubsystemCatzVision.PoseAndTimestamp> results = new ArrayList<>(); //in a list to account for multiple cameras

    private int acceptableTagID;
    private boolean useSingleTag = false;

    //constructor for vision subsystem that creates new vision input objects for each camera set in the singleton implementation
    private SubsystemCatzVision(VisionIO camera) {
        this.camera = camera;
        inputs = new VisionIOInputsAutoLogged();
        visionInputArray.add(inputs);
    }

    //NOTE TO EVERYONE...DON'T GET RID OF UNCOMMETED CODE PLZ (LMAO)

    @Override
    public void periodic() {
        Logger.recordOutput("useSingleTag", useSingleTag); //set by driverstation

        // clear results from last periodic
        results.clear();
        
        // //for every limlight camera process vision with according logic
        // for (int i = 0; i < inputs.length; i++) {
        //     // update and process new inputs for camera
        //     cameras[i].updateInputs(inputs[i]);
        //     Logger.processInputs("Vision/" + cameras[i].getName() + "/Inputs", inputs[i]);
            
        // System.out.println("inputs processed");
        
        //     //checks for when to process vision
        //     if (inputs[i].hasTarget && 
        //         inputs[i].isNewVisionPose && 
        //         !DriverStation.isAutonomous() && 
        //         inputs[i].maxDistance < VisionConstants.LOWEST_DISTANCE) {
        //         if (useSingleTag) {
        //             if (inputs[i].singleIDUsed == acceptableTagID) {
        //                 processVision(i);
        //             }
        //         } 
        //         else {
        //             processVision(i);
        //         }
        //         System.out.println("vision processeed");
        //     }
        // }

        // //Logging
        // Logger.recordOutput("Vision/ResultCount", results.size());

        //for every limlight camera process vision with according logic
            // update and process new inputs for camera
            camera.updateInputs(inputs);
            Logger.processInputs("Vision/" + camera.getName() + "/Inputs", inputs);
                    
            //checks for when to process vision
            if (inputs.hasTarget && 
                inputs.isNewVisionPose && 
                !DriverStation.isAutonomous() && 
                inputs.maxDistance < VisionConstants.LOWEST_DISTANCE) {
                if (useSingleTag) {
                    if (inputs.singleIDUsed == acceptableTagID) {
                        processVision();
                    }
                } 
                else {
                    processVision();
                }
            }
        limelightRangeFinder();
        

        //Logging
        Logger.recordOutput("Vision/ResultCount", results.size());

        //log data
        Logger.recordOutput("AprilTagID", primaryAprilTag);
        Logger.recordOutput("Vertical Degrees to Apriltag", inputs.ty);
        Logger.recordOutput("Distance to Apriltag", distanceToAprilTag);
        Logger.recordOutput("Distance to Wall", aprilTagDistanceToWall);
    }

    // public void processVision(int cameraNum) {
    //     // create a new pose based off the new inputs
    //     Pose2d currentPose = new Pose2d(inputs[cameraNum].x, 
    //                                     inputs[cameraNum].y, 
    //                                     new Rotation2d(inputs[cameraNum].rotation));

    //     //log data
    //     Logger.recordOutput(cameras[cameraNum].getName() + " pose", currentPose);

    //     // add the new pose to a list
    //     results.add(new PoseAndTimestamp(currentPose, inputs[cameraNum].timestamp));
    // }

    public void processVision() {
        // create a new pose based off the new inputs
        Pose2d currentPose = new Pose2d(inputs.x, 
                                        inputs.y, 
                                        new Rotation2d(inputs.rotation));

        // add the new pose to a list
        results.add(new PoseAndTimestamp(currentPose, inputs.timestamp));
    }

    //Returns the last recorded pose in a list
    public List<SubsystemCatzVision.PoseAndTimestamp> getVisionOdometry() {
        return results;
    }

    //Inner class to record a pose and its timestamp
    public static class PoseAndTimestamp {
        Pose2d pose;
        double timestamp;

        public PoseAndTimestamp(Pose2d pose, double timestamp) {
            this.pose = pose;
            this.timestamp = timestamp;
        }

        public Pose2d getPose() {
            return pose;
        }

        public double getTimestamp() {
            return timestamp;
        }
    }

    //access method for determining whether to use multiple tags for pose estimation
    public void setUseSingleTag(boolean useSingleTag) {
        setUseSingleTag(useSingleTag, 0);
    }

    public void setUseSingleTag(boolean useSingleTag, int acceptableTagID) {
        this.useSingleTag = useSingleTag;
        this.acceptableTagID = acceptableTagID;
    }

    public double getMinDistance() {
         return inputs.minDistance;
    }

    public double getOffsetX() {
        return inputs.tx;
    }

    //----------------------------------Calculation methods---------------------------------------------
    
    public void limelightRangeFinder() {
        if(inputs.primaryApriltagID == 1 || 
           inputs.primaryApriltagID == 2 || 
           inputs.primaryApriltagID == 9 || 
           inputs.primaryApriltagID == 10) 
        {
            //Source
            primaryAprilTag = "Source";

            //vertical distance to target
            distanceToAprilTag = (sourceApriltagHeight - limelightPlacementHeight) / Math.sin(inputs.ty);
            aprilTagDistanceToWall = (sourceApriltagHeight - limelightPlacementHeight) / Math.tan(inputs.ty);

            //horizontal distance to target
            horizontalTargetOffset = (aprilTagDistanceToWall) * Math.tan(inputs.tx);
            if(Math.abs(horizontalTargetOffset) > 5) // 5 what?? I don't know
            {
                System.out.println("Alligned with Target");
                horizontallyAllignedWithAprilTag = true;

                NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(2);

            } else {
                NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(1);
            }
        } 
        else if (inputs.primaryApriltagID == 3 || 
                 inputs.primaryApriltagID == 4 || 
                 inputs.primaryApriltagID == 7 || 
                 inputs.primaryApriltagID == 8)  
        {
            //Speaker
            primaryAprilTag = "Speaker";

            //vertical distance to target
            distanceToAprilTag = (speakerApriltagHeight - limelightPlacementHeight) / Math.sin(inputs.ty);
            aprilTagDistanceToWall = (speakerApriltagHeight - limelightPlacementHeight) / Math.tan(inputs.ty);
        
            //horizontal distance to target
            horizontalTargetOffset = (aprilTagDistanceToWall) * Math.tan(inputs.tx);

            if(horizontalTargetOffset > 5 && horizontalTargetOffset < 5) // 5 what?? I don't know
            {
                System.out.println("Alligned with Target");
                horizontallyAllignedWithAprilTag = true;

                NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(2);

            } else {
                NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(1);
            }
        } 
        else if (inputs.primaryApriltagID == 11 || 
                 inputs.primaryApriltagID == 12 || 
                 inputs.primaryApriltagID == 13 || 
                 inputs.primaryApriltagID == 14 || 
                 inputs.primaryApriltagID == 15 || 
                 inputs.primaryApriltagID == 16) 
        {
            //Trap
            primaryAprilTag = "Trap";

            //vertical distance to target
            distanceToAprilTag = (trapApriltagHeight - limelightPlacementHeight) / Math.sin(inputs.ty);
            aprilTagDistanceToWall = (trapApriltagHeight - limelightPlacementHeight) / Math.tan(inputs.ty);
            
            //horizontal distance to target
            horizontalTargetOffset = (aprilTagDistanceToWall) * Math.tan(inputs.tx);  
            if(horizontalTargetOffset > 5 && horizontalTargetOffset < 5) // 5 what?? I don't know
            {
                System.out.println("Alligned with Target");
                horizontallyAllignedWithAprilTag = true;

                NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(2);

            } else {
                NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(1);
            }       
        } 
        else if (inputs.primaryApriltagID == 5 || 
                 inputs.primaryApriltagID == 6) 
        {
            //Amp
            primaryAprilTag = "Amp";

            //vertical distance to target
            distanceToAprilTag = (ampApriltagHeight - limelightPlacementHeight) / Math.sin(inputs.ty);
            aprilTagDistanceToWall = (ampApriltagHeight - limelightPlacementHeight) / Math.tan(inputs.ty);

            //horizontal distance to target
            horizontalTargetOffset = (aprilTagDistanceToWall) * Math.tan(inputs.tx);
            if(horizontalTargetOffset > 5 && horizontalTargetOffset < 5) // 5 what?? I don't know
            {
                System.out.println("Alligned with Target");
                horizontallyAllignedWithAprilTag = true;

                NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(2);

            } else {
                NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setNumber(1);
            }
        }   
    } 

    public double getHorizontalAngle() {
        return Units.radiansToDegrees(inputs.tx) - 152;
    }

    /**
    * singleton implenentation of vision
    * Any new cameras should be declared here
    */
    public static SubsystemCatzVision getInstance() {
        if(instance == null) {
            instance = new SubsystemCatzVision(
                new VisionIOLimeLight("limelight", VisionConstants.LIMELIGHT_OFFSET)
                );
            // instance = new SubsystemCatzVision(new VisionIO[] {
            //     new VisionIOLimeLight("limelight", VisionConstants.LIMELIGHT_OFFSET)
            // });
        }
        return instance;
    }

}
