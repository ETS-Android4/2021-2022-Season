package org.firstinspires.ftc.teamcode;

import android.graphics.Color;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.SwitchableLight;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.List;
import java.util.Locale;

@Autonomous(name = "FinalAuto", group = "comp")
public class FinalAuto extends LinearOpMode {

    // Declaring Motors & Servos
    private     DcMotorEx     leftFront;        //port 0
    private     DcMotorEx     rightFront;       //port 1
    private     DcMotorEx     leftBack;         //port 1
    private     DcMotorEx     rightBack;        //port 3
    private     DcMotor       spinner;
    private     DcMotor       arm;
    private     Servo       wrist;        //port 0
    private     Servo       fingers;         //port 0

    //Declaring Distance Sensor Variables
    private DistanceSensor leftDistance;
    private DistanceSensor rightDistance;

    //Declaring Color Sensor Variables
    NormalizedColorSensor lfColorSensor;
    NormalizedColorSensor rfColorSensor;
    NormalizedRGBA colors;
    boolean foundRed = false;
    boolean foundWhite = false;

    //Declaring IMU Variables
    BNO055IMU imu;
    Orientation angles;
    Acceleration gravity;
    double heading;
    boolean turned = false;

    //Declaring Camera Variables
    OpenCvCamera webcam;
    OpenCVTest.SamplePipeline pipeline;

    @Override

    public void runOpMode() {
        initialize();
        if (opModeIsActive()) {
            encoders("n");

            if (pipeline.getType1().toString().equals("BLUESQUARE") || pipeline.getType2().toString().equals("BLUESQUARE") || pipeline.getType3().toString().equals("BLUESQUARE")) {
                //On Blue Side

                if (rightDistance.getDistance(DistanceUnit.CM) < 80.00) {
                    //Distance Sensor on Right < x
                    //On Carousel Side

                    if (pipeline.getType1().toString().equals("DUCK")) {
                        //Duck in Field 1
                    } else if (pipeline.getType2().toString().equals("DUCK")) {
                        //Duck in Field 2
                    } else if (pipeline.getType3().toString().equals("DUCK")) {
                        //Duck in Field 3
                    }

                } else if (rightDistance.getDistance(DistanceUnit.CM) > 80.00) {
                    //Distance Sensor on Right > x
                    //On Storage Side

                    if (pipeline.getType1().toString().equals("DUCK")) {
                        //Duck in Field 1
                    } else if (pipeline.getType2().toString().equals("DUCK")) {
                        //Duck in Field 2
                    } else if (pipeline.getType3().toString().equals("DUCK")) {
                        //Duck in Field 3
                    }
                }

            } else if (pipeline.getType1().toString().equals("REDSQUARE") || pipeline.getType2().toString().equals("REDSQUARE") || pipeline.getType3().toString().equals("REDSQUARE")) {
                //On Red Side
                if (leftDistance.getDistance(DistanceUnit.CM) < 80.00) {
                    //Distance Sensor on Left < x
                    //On Carousel Side

                    if (pipeline.getType1().toString().equals("DUCK")) {
                        //Duck in Field 1
                    } else if (pipeline.getType2().toString().equals("DUCK")) {
                        //Duck in Field 2
                    } else if (pipeline.getType3().toString().equals("DUCK")) {
                        //Duck in Field 3
                    }

                } else if (leftDistance.getDistance(DistanceUnit.CM) > 80.00) {
                    //Distance Sensor on Left > x
                    //On Storage Side

                    if (pipeline.getType1().toString().equals("DUCK")) {
                        //Duck in Field 1
                    } else if (pipeline.getType2().toString().equals("DUCK")) {
                        //Duck in Field 2
                    } else if (pipeline.getType3().toString().equals("DUCK")) {
                        //Duck in Field 3
                    }
                }
            }
        }
    }

    String formatAngle(AngleUnit angleUnit, double angle) {
        return formatDegrees(AngleUnit.DEGREES.fromUnit(angleUnit, angle));
    }

    String formatDegrees(double degrees){
        return String.format(Locale.getDefault(), "%.1f", AngleUnit.DEGREES.normalize(degrees));
    }

    //Method to Completely Stop ALL Robot Movement Excluding Servos
    private void forceStop() {
        leftFront.setPower(0);
        rightFront.setPower(0);
        leftBack.setPower(0);
        rightBack.setPower(0);
        spinner.setPower(0);
        arm.setPower(0);
    }

    //Method to Move Robot @ Designated Speed & Duration
    public void move(double speed, long dur) {
        leftFront.setPower(speed);
        rightFront.setPower(speed);
        leftBack.setPower(speed);
        rightBack.setPower(speed);
        sleep(dur);
    }

    //Method to Find & Move to a White, Red, or Blue Line
    void senseLine(String color, double speed) {
        final float[] hsvValues = new float[3];
        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        foundRed = false;
        foundWhite = false;
        int countRed = 0;
        int countWhite = 0;

        //If the "foundRed" Boolean is False, Run Loop
        while ((!foundRed && !foundWhite) && opModeIsActive()) {
            //Needed (updating) Variables
            NormalizedRGBA colors = lfColorSensor.getNormalizedColors();
            double heading = Double.parseDouble(formatAngle(angles.angleUnit, angles.thirdAngle));
            double P = Math.abs(0.025 * heading);
            //If-Else-If Statement to Drive Forward in a Straight Line
            if (speed > 0) {
                if (heading < -0.1  && heading > -90){
                    leftFront.setPower(speed + P);
                    leftBack.setPower(speed + P);
                    rightFront.setPower(speed - P);
                    rightBack.setPower(speed - P);
                }else if (heading > 0.1 && heading < 90){
                    leftFront.setPower(speed - P);
                    leftBack.setPower(speed - P);
                    rightFront.setPower(speed + P);
                    rightBack.setPower(speed + P);
                } else {
                    leftFront.setPower(speed);
                    leftBack.setPower(speed);
                    rightFront.setPower(speed);
                    rightBack.setPower(speed);
                }
            } else if (speed < 0) {
                //May need to switch the positives and negatives if gyro seems not too work
                if (heading < -0.1  && heading > -90){
                    leftFront.setPower(speed + P);
                    leftBack.setPower(speed + P);
                    rightFront.setPower(speed - P);
                    rightBack.setPower(speed - P);
                }else if (heading > 0.1 && heading < 90){
                    leftFront.setPower(speed - P);
                    leftBack.setPower(speed - P);
                    rightFront.setPower(speed + P);
                    rightBack.setPower(speed + P);
                } else {
                    leftFront.setPower(speed);
                    leftBack.setPower(speed);
                    rightFront.setPower(speed);
                    rightBack.setPower(speed);
                }
            }

            //Telemetry Info for Diagnostics
            telemetry.addLine()
                    .addData("Alpha Output", "%.3f", colors.alpha)
                    .addData("Heading Output", "%.3f", heading)
                    .addData("Loop Count", countRed);
            telemetry.update();

            if (color.equals("red")) {
                //If Statement to Detect the Red Line and Break the Loop
                if (colors.alpha < 0.2) {
                    forceStop();
                    foundRed = true;
                }
                countRed++;
            } else if (color.equals("white")) {
                if (colors.alpha > 0.5) {
                    forceStop();
                    foundWhite = true;
                }
                countWhite++;
            }
        }
    }

    //Method to Turn Robot Using IMU
    void turn(double speed, int angleMeasure) {
        turned = false;
        while (opModeIsActive() && !turned) {
            angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            double heading = Math.abs(Double.parseDouble(formatAngle(angles.angleUnit, angles.thirdAngle)));

            telemetry.addData("heading", heading);
            telemetry.update();

            if (heading <= angleMeasure + 1 && heading >= angleMeasure - 1) {
                forceStop();
                turned = true;
            } else if (heading >= (0.9 * angleMeasure) && heading < (angleMeasure - 1)) {
                rightFront.setPower(0.5 * speed);
                leftFront.setPower(0.5 * -speed);
                rightBack.setPower(0.5 * speed);
                leftBack.setPower(0.5 * -speed);
            } else {
                rightFront.setPower(speed);
                leftFront.setPower(-speed);
                rightBack.setPower(speed);
                leftBack.setPower(-speed);
            }
        }
    }

    //Determine Whether To Run With Encoder
    public void encoders(String status) {
        if (status.equals("on")) {
            leftFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            rightFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            leftBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            rightBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

            leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            leftBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rightBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        } else if (status.equals("off")) {
            leftFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            rightFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            leftBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            rightBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

            leftFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            rightFront.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            leftBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            rightBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
    }

    //Method to Move Robot Using Encoders
    void moveInches(double distance, double velocity) {

        encoders("on");

        double calcPosition = distance * (100* 280/(16.9646003294*4 *8.8 * 1.0555555556));
        int setPosition = (int) Math.round(calcPosition);

        int setVelocity = (int) Math.round(velocity);

        leftFront.setTargetPosition(setPosition);
        rightFront.setTargetPosition(setPosition);
        leftBack.setTargetPosition(setPosition);
        rightBack.setTargetPosition(setPosition);

        leftFront.setVelocity(setVelocity);
        rightFront.setVelocity(setVelocity);
        leftBack.setVelocity(setVelocity);
        rightBack.setVelocity(setVelocity);

        leftFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        leftBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        while (opModeIsActive() && leftFront.isBusy()) {
            telemetry.addData("position", leftFront.getCurrentPosition());
            telemetry.addData("is at target", !leftFront.isBusy());
            telemetry.update();
        }

        leftFront.setVelocity(0);
        rightFront.setVelocity(0);
        leftBack.setVelocity(0);
        rightBack.setVelocity(0);

        encoders("off");
    }

    public static class SamplePipeline extends OpenCvPipeline {
        private static final Scalar BLUE = new Scalar(0, 0, 255);

        Point topLeft = new Point(0, 160);
        Point bottomRight = new Point(50, 210);
        Point topLeft2 = new Point(130, 150);
        Point bottomRight2 = new Point(180, 200);
        Point topLeft3 = new Point(265, 140);
        Point bottomRight3 = new Point(315, 190);

        Mat region1_Y;
        Mat region1_Cr;
        Mat region1_Cb;
        Mat region2_Y;
        Mat region2_Cr;
        Mat region2_Cb;
        Mat region3_Y;
        Mat region3_Cr;
        Mat region3_Cb;
        Mat YCrCb = new Mat();
        Mat Y = new Mat();
        Mat Cr = new Mat();
        Mat Cb = new Mat();

        private volatile int averageY;
        private volatile int averageCr;
        private volatile int averageCb;
        private volatile int averageY2;
        private volatile int averageCr2;
        private volatile int averageCb2;
        private volatile int averageY3;
        private volatile int averageCr3;
        private volatile int averageCb3;
        private volatile OpenCVTest.SamplePipeline.TYPE type1 = OpenCVTest.SamplePipeline.TYPE.NULL;
        private volatile OpenCVTest.SamplePipeline.TYPE type2 = OpenCVTest.SamplePipeline.TYPE.NULL;
        private volatile OpenCVTest.SamplePipeline.TYPE type3 = OpenCVTest.SamplePipeline.TYPE.NULL;

        private void inputToY(Mat input) {
            Imgproc.cvtColor(input, YCrCb, Imgproc.COLOR_RGB2YCrCb);
            Core.extractChannel(YCrCb, Y, 0);
        }
        private void inputToCr(Mat input) {
            Imgproc.cvtColor(input, YCrCb, Imgproc.COLOR_RGB2YCrCb);
            Core.extractChannel(YCrCb, Cr, 1);
        }
        private void inputToCb(Mat input) {
            Imgproc.cvtColor(input, YCrCb, Imgproc.COLOR_RGB2YCrCb);
            Core.extractChannel(YCrCb, Cb, 2);
        }


        @Override
        public void init(Mat input) {
            inputToY(input);
            inputToCr(input);
            inputToCb(input);

            region1_Y = Y.submat(new Rect(topLeft, bottomRight));
            region1_Cr = Cr.submat(new Rect(topLeft, bottomRight));
            region1_Cb = Cb.submat(new Rect(topLeft, bottomRight));

            region2_Y = Y.submat(new Rect(topLeft2, bottomRight2));
            region2_Cr = Cr.submat(new Rect(topLeft2, bottomRight2));
            region2_Cb = Cb.submat(new Rect(topLeft2, bottomRight2));

            region3_Y = Y.submat(new Rect(topLeft3, bottomRight3));
            region3_Cr = Cr.submat(new Rect(topLeft3, bottomRight3));
            region3_Cb = Cb.submat(new Rect(topLeft3, bottomRight3));
        }

        @Override
        public Mat processFrame(Mat input) {

            inputToY(input);
            inputToCr(input);
            inputToCb(input);

            averageY = (int) Core.mean(region1_Y).val[0];
            averageCr = (int) Core.mean(region1_Cr).val[0];
            averageCb = (int) Core.mean(region1_Cb).val[0];

            averageY2 = (int) Core.mean(region2_Y).val[0];
            averageCr2 = (int) Core.mean(region2_Cr).val[0];
            averageCb2 = (int) Core.mean(region2_Cb).val[0];

            averageY3 = (int) Core.mean(region3_Y).val[0];
            averageCr3 = (int) Core.mean(region3_Cr).val[0];
            averageCb3 = (int) Core.mean(region3_Cb).val[0];

            Imgproc.rectangle(input, topLeft, bottomRight, BLUE, 2);
            Imgproc.rectangle(input, topLeft2, bottomRight2, BLUE, 2);
            Imgproc.rectangle(input, topLeft3, bottomRight3, BLUE, 2);

            if (averageCb < 110) {
                type1 = OpenCVTest.SamplePipeline.TYPE.DUCK;
            } else if (averageCb >130) {
                type1 = OpenCVTest.SamplePipeline.TYPE.BLUESQUARE;
            } else if (averageCr >135) {
                type1 = OpenCVTest.SamplePipeline.TYPE.REDSQUARE;
            } else {
                type1 = OpenCVTest.SamplePipeline.TYPE.NULL;
            }

            if (averageCb2 < 110) {
                type2 = OpenCVTest.SamplePipeline.TYPE.DUCK;
            } else if (averageCb2 >130) {
                type2 = OpenCVTest.SamplePipeline.TYPE.BLUESQUARE;
            } else if (averageCr2 >135) {
                type2 = OpenCVTest.SamplePipeline.TYPE.REDSQUARE;
            } else {
                type2 = OpenCVTest.SamplePipeline.TYPE.NULL;
            }


            if (averageCb3 < 110) {
                type3 = OpenCVTest.SamplePipeline.TYPE.DUCK;
            } else if (averageCb3 >130) {
                type3 = OpenCVTest.SamplePipeline.TYPE.BLUESQUARE;
            } else if (averageCr3 >135) {
                type3 = OpenCVTest.SamplePipeline.TYPE.REDSQUARE;
            } else {
                type3 = OpenCVTest.SamplePipeline.TYPE.NULL;
            }

            return input;
        }


        public OpenCVTest.SamplePipeline.TYPE getType1() {
            return type1;
        }
        public OpenCVTest.SamplePipeline.TYPE getType2() {
            return type2;
        }
        public OpenCVTest.SamplePipeline.TYPE getType3() {
            return type3;
        }

        public int getAverageY() {
            return averageY;
        }
        public int getAverageCr() {
            return averageCr;
        }
        public int getAverageCb() {
            return averageCb;
        }
        public int getAverageY2() {
            return averageY2;
        }
        public int getAverageCr2() {
            return averageCr2;
        }
        public int getAverageCb2() {
            return averageCb2;
        }
        public int getAverageY3() {
            return averageY3;
        }
        public int getAverageCr3() {
            return averageCr3;
        }
        public int getAverageCb3() {
            return averageCb3;
        }


        public enum TYPE {
            BALL, BLUESQUARE, CUBE, DUCK, NULL, REDSQUARE
        }
    }

    public void initialize() {
        telemetry.addData("Stat", "Initializing...");
        telemetry.update();

        //Mapping Motors
        rightFront = hardwareMap.get(DcMotorEx.class, "rightFront");
        leftFront = hardwareMap.get(DcMotorEx.class, "leftFront");
        rightBack = hardwareMap.get(DcMotorEx.class, "rightBack");
        leftBack = hardwareMap.get(DcMotorEx.class, "leftBack");
        spinner = hardwareMap.dcMotor.get("spinner");
        arm = hardwareMap.dcMotor.get("arm");

        //Mapping Servos
        wrist = hardwareMap.servo.get("wrist");
        fingers = hardwareMap.servo.get("fingers");

        // Extra Motor Steps
        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);    //Reverse
        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);     //Reverse

        //Mapping Distance Sensors
        leftDistance = hardwareMap.get(DistanceSensor.class, "leftDistance");
        rightDistance = hardwareMap.get(DistanceSensor.class, "rightDistance");

        //Mapping Color Sensors
        lfColorSensor = hardwareMap.get(NormalizedColorSensor.class, "lfColorSensor");
        if (lfColorSensor instanceof SwitchableLight) {
            ((SwitchableLight) lfColorSensor).enableLight(true);
        }
        rfColorSensor = hardwareMap.get(NormalizedColorSensor.class, "rfColorSensor");
        if (rfColorSensor instanceof SwitchableLight) {
            ((SwitchableLight) rfColorSensor).enableLight(true);
        }

        //IMU Mapping and Set-Up
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json";
        parameters.loggingEnabled = true;
        parameters.loggingTag = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);

        //Initialize Camera
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        webcam = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);

        //OpenCv Set-Up
        pipeline = new OpenCVTest.SamplePipeline();
        webcam.setPipeline(pipeline);
        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                webcam.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {

            }
        });

        //Reset Encoders
        leftFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //Initialization Complete
        telemetry.addData("Stat", "Start Program");
        telemetry.update();

        //Waiting for start via Player
        waitForStart();
    }

}