/*
 * Copyright (c) 2020 Titan Robotics Club (http://www.titanrobotics.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package team492;

import common.CmdPidDrive;
import common.CmdTimedDrive;
import frclib.FrcChoiceMenu;
import frclib.FrcRemoteVisionProcessor;
import hallib.HalDashboard;
import trclib.TrcPose2D;
import trclib.TrcRobot;
import trclib.TrcRobot.RunMode;
import trclib.TrcTaskMgr;

public class FrcAuto implements TrcRobot.RobotMode
{
    private static final String moduleName = "FrcAuto";
    public static final String CUSTOM_XPOS_KEY = "Auto/CustomXPos";

    public enum AutoStrategy
    {
        SHOOTER_AUTO, FLYWHEEL_CHARACTERIZATION, ARM_CHARACTERIZATION, X_TIMED_DRIVE, Y_TIMED_DRIVE, X_DISTANCE_DRIVE, Y_DISTANCE_DRIVE, TURN_DEGREES, DO_NOTHING
    }   // enum AutoStrategy

    public enum StartPosition
    {
        LEFT_BUMPER_FEEDER(RobotInfo.FEEDER_STATION_RIGHT_X_POS + RobotInfo.ROBOT_WIDTH / 2), IN_VISION(
        RobotInfo.TARGET_X_POS), RIGHT_WALL(RobotInfo.FIELD_WIDTH - RobotInfo.ROBOT_WIDTH / 2), CUSTOM(0);

        private double xPos;

        StartPosition(double xPos)
        {
            this.xPos = xPos;
        }

        public double getXPos()
        {
            return xPos;
        }
    }

    private final Robot robot;
    //
    // Menus.
    //
    private FrcChoiceMenu<AutoStrategy> autoStrategyMenu;
    private FrcChoiceMenu<StartPosition> startPosMenu;
    private FrcChoiceMenu<CmdShooterAuto.AfterAction> shooterAutoAfterMenu;
    private AutoStrategy autoStrategy;
    private double delay;
    private TrcTaskMgr.TaskObject populateTask;
    private StartPosition lastPos = null;

    private TrcRobot.RobotCommand autoCommand;

    public FrcAuto(Robot robot)
    {
        this.robot = robot;
        //
        // Create Autonomous Mode specific menus.
        //
        autoStrategyMenu = new FrcChoiceMenu<>("Auto/AutoStrategies");
        //
        // Populate Autonomous Mode menus.
        //
        autoStrategyMenu.addChoice("High Goal Auto", AutoStrategy.SHOOTER_AUTO, true, false);
        autoStrategyMenu.addChoice("Flywheel Characterization", AutoStrategy.FLYWHEEL_CHARACTERIZATION);
        autoStrategyMenu.addChoice("Arm Characterization", AutoStrategy.ARM_CHARACTERIZATION);
        autoStrategyMenu.addChoice("X Timed Drive", AutoStrategy.X_TIMED_DRIVE);
        autoStrategyMenu.addChoice("Y Timed Drive", AutoStrategy.Y_TIMED_DRIVE);
        autoStrategyMenu.addChoice("X Distance Drive", AutoStrategy.X_DISTANCE_DRIVE);
        autoStrategyMenu.addChoice("Y Distance Drive", AutoStrategy.Y_DISTANCE_DRIVE);
        autoStrategyMenu.addChoice("Turn Degrees", AutoStrategy.TURN_DEGREES);
        autoStrategyMenu.addChoice("Do Nothing", AutoStrategy.DO_NOTHING, false, true);

        startPosMenu = new FrcChoiceMenu<>("Auto/StartPositions");
        startPosMenu.addChoice("Left Bumper Feeder", StartPosition.LEFT_BUMPER_FEEDER, true, false);
        startPosMenu.addChoice("In Vision", StartPosition.IN_VISION);
        startPosMenu.addChoice("Custom", StartPosition.CUSTOM, false, true);

        shooterAutoAfterMenu = new FrcChoiceMenu<>("Auto/ShooterAutoAfterAction");
        shooterAutoAfterMenu.addChoice("Intake and Shoot", CmdShooterAuto.AfterAction.INTAKE_AND_SHOOT, true, false);
        shooterAutoAfterMenu.addChoice("Intake Only", CmdShooterAuto.AfterAction.INTAKE);
        shooterAutoAfterMenu.addChoice("Nothing", CmdShooterAuto.AfterAction.NOTHING, false, true);

        HalDashboard.refreshKey(CUSTOM_XPOS_KEY, 0.0);
        HalDashboard.refreshKey("Auto/Delay", 0.0);
        populateTask = TrcTaskMgr.getInstance().createTask("PopulateTask", this::populateTask);
        populateTask.registerTask(TrcTaskMgr.TaskType.PREPERIODIC_TASK);
    }   // FrcAuto

    private void populateTask(TrcTaskMgr.TaskType taskType, RunMode runMode)
    {
        StartPosition currChoice = startPosMenu.getCurrentChoiceObject();
        if (currChoice != lastPos)
        {
            HalDashboard.putNumber(CUSTOM_XPOS_KEY, currChoice.getXPos());
            lastPos = currChoice;
        }
    }

    public boolean isAutoActive()
    {
        return autoCommand != null && autoCommand.isActive();
    }

    public void cancel()
    {
        if (autoCommand != null)
        {
            autoCommand.cancel();
            autoCommand = null;
            autoStrategy = AutoStrategy.DO_NOTHING;
        }
    }

    //
    // Implements TrcRobot.RunMode.
    //

    @Override
    public void startMode(RunMode prevMode, RunMode nextMode)
    {
        final String funcName = moduleName + ".startMode";

        populateTask.unregisterTask();

        robot.driveBase.resetOdometry(true, true);

        robot.ledIndicator.reset();

        if (robot.preferences.useVision)
        {
            robot.vision.setEnabled(true);
        }

        robot.setNumBalls(3);

        robot.getGameInfo();
        robot.globalTracer
            .traceInfo(funcName, "%s_%s%03d (%s%d) [FMSConnected=%b] msg=%s", robot.eventName, robot.matchType,
                robot.matchNumber, robot.alliance.toString(), robot.location, robot.ds.isFMSAttached(),
                robot.gameSpecificMessage);
        //
        // Retrieve menu choice values.
        //
        autoStrategy = autoStrategyMenu.getCurrentChoiceObject();
        delay = HalDashboard.getNumber("Auto/Delay", 0.0);

        switch (autoStrategy)
        {
            case SHOOTER_AUTO:
                CmdShooterAuto shooterAuto = new CmdShooterAuto(robot);
                shooterAuto
                    .start(delay, startPosMenu.getCurrentChoiceObject(), shooterAutoAfterMenu.getCurrentChoiceObject());
                this.autoCommand = shooterAuto;
                break;

            case FLYWHEEL_CHARACTERIZATION:
                autoCommand = new CmdTalonCharacterization(robot.shooter.flywheel);
                break;

            case ARM_CHARACTERIZATION:
                autoCommand = new CmdTalonCharacterization(robot.shooter.pitchMotor);
                robot.shooter.setEnabled(false);
                break;

            case X_TIMED_DRIVE:
                this.autoCommand = new CmdTimedDrive(robot, delay, robot.driveTime, robot.drivePower, 0.0, 0.0);
                break;

            case Y_TIMED_DRIVE:
                this.autoCommand = new CmdTimedDrive(robot, delay, robot.driveTime, 0.0, robot.drivePower, 0.0);
                break;

            case X_DISTANCE_DRIVE:
                this.autoCommand = new CmdPidDrive(robot, robot.pidDrive, delay, robot.driveDistance, 0.0, 0.0,
                    robot.drivePowerLimit, false, false);
                break;

            case Y_DISTANCE_DRIVE:
                this.autoCommand = new CmdPidDrive(robot, robot.pidDrive, delay, 0.0, robot.driveDistance, 0.0,
                    robot.drivePowerLimit, false, false);
                break;

            case TURN_DEGREES:
                this.autoCommand = new CmdPidDrive(robot, robot.pidDrive, delay, 0.0, 0.0, robot.turnDegrees,
                    robot.drivePowerLimit, false, false);
                break;

            default:
            case DO_NOTHING:
                this.autoCommand = null;
                break;
        }
    }   // startMode

    @Override
    public void stopMode(RunMode prevMode, RunMode nextMode)
    {
        if (autoCommand != null)
        {
            autoCommand.cancel();
        }
        populateTask.registerTask(TrcTaskMgr.TaskType.PREPERIODIC_TASK);
        TrcTaskMgr.getInstance().printTaskPerformanceMetrics(robot.globalTracer);
    }   // stopMode

    @Override
    public void runPeriodic(double elapsedTime)
    {
        if (robot.preferences.doAutoUpdates)
        {
            robot.updateDashboard(RunMode.AUTO_MODE);
        }
    } // runPeriodic

    @Override
    public void runContinuous(double elapsedTime)
    {
        if (autoCommand != null)
        {
            autoCommand.cmdPeriodic(elapsedTime);

            if (robot.pidDrive.isActive())
            {
                robot.encoderXPidCtrl.printPidInfo(robot.globalTracer, false, robot.battery);
                robot.encoderYPidCtrl.printPidInfo(robot.globalTracer, false, robot.battery);
                robot.gyroTurnPidCtrl.printPidInfo(robot.globalTracer, false, robot.battery);
            }
        }
    } // runContinuous

} // class FrcAuto
