package team492.subsystems;

import frclib.subsystem.FrcShooter;
import team492.RobotParams;
import trclib.motor.TrcMotor;
import trclib.robotcore.TrcPidController;
import trclib.subsystem.TrcShooter;

public class Shooter {
    private final TrcShooter shooter;
    public Shooter()
    {
        FrcShooter.Params shooterParams = new FrcShooter.Params()
            .setShooterMotor1(
                RobotParams.Shooter.MOTOR1_ID, RobotParams.Shooter.MOTOR1_TYPE, RobotParams.Shooter.MOTOR1_BRUSHLESS,
                RobotParams.Shooter.MOTOR1_ENC_ABS, RobotParams.Shooter.MOTOR1_INVERTED);
        if (RobotParams.Shooter.HAS_TWO_SHOOTER_MOTORS)
        {
            shooterParams.setShooterMotor2(
                RobotParams.Shooter.MOTOR2_ID, RobotParams.Shooter.MOTOR2_TYPE, RobotParams.Shooter.MOTOR2_BRUSHLESS,
                RobotParams.Shooter.MOTOR2_ENC_ABS, RobotParams.Shooter.MOTOR2_INVERTED,
                RobotParams.Shooter.MOTOR2_IS_FOLLOWER);
        }

        shooter = new FrcShooter(RobotParams.Shooter.SUBSYSTEM_NAME, shooterParams).getShooter();
        configShooterMotor(shooter.getShooterMotor1(), RobotParams.Shooter.shooter1PidCoeffs);
        TrcMotor shooterMotor2 = shooter.getShooterMotor2();
        if (shooterMotor2 != null)
        {
            configShooterMotor(shooterMotor2, RobotParams.Shooter.shooter2PidCoeffs);
        }
    }

    public TrcShooter getShooter()
    {
        return shooter;
    }

    private void configShooterMotor(TrcMotor motor, TrcPidController.PidCoefficients pidCoeffs)
    {
        motor.setPositionSensorScaleAndOffset(RobotParams.Shooter.GOBILDA1620_RPC, 0.0);
        motor.setSoftwarePidEnabled(RobotParams.Shooter.SOFTWARE_PID_ENABLED);
        motor.setVelocityPidParameters(pidCoeffs, RobotParams.Shooter.SHOOTER_PID_TOLERANCE);
    }
}
