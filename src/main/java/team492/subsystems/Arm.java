package team492.subsystems;

// import frclib.motor.FrcMotor.MotorType;
import frclib.subsystem.FrcMotorActuator;
import team492.RobotParams;
// import team492.RobotParams.Robot;
import trclib.motor.TrcMotor;

public class Arm
{
    private TrcMotor armMotor;

    public Arm()
    {
        FrcMotorActuator.Params motorParams = new FrcMotorActuator.Params()
            .setMotorInverted(RobotParams.Arm.MOTOR_INVERTED)
            .setVoltageCompensationEnabled(RobotParams.Arm.VOLTAGE_COMP_ENABLED)
            .setPositionScaleAndOffset(RobotParams.Arm.DEGREES_PER_COUNT, RobotParams.Arm.POS_OFFSET)
            .setPositionPresets(RobotParams.Arm.POS_PRESET_TOLERANCE, RobotParams.Arm.posPresets);

        armMotor = new FrcMotorActuator(
            RobotParams.Arm.SUBSYSTEM_NAME, RobotParams.Arm.MOTOR_ID, RobotParams.Arm.MOTOR_TYPE,
            RobotParams.Arm.MOTOR_BRUSHLESS, RobotParams.Arm.MOTOR_ENC_ABS, motorParams).getActuator();
        armMotor.setSoftwarePidEnabled(RobotParams.Arm.SOFTWARE_PID_ENABLED);
        armMotor.setPositionPidParameters(RobotParams.Arm.posPidCoeffs, RobotParams.Arm.POS_PID_TOLERANCE);
        armMotor.setPositionPidPowerComp(this::getGravityComp);
        armMotor.setStallProtection(
            RobotParams.Arm.STALL_MIN_POWER, RobotParams.Arm.STALL_TOLERANCE,
            RobotParams.Arm.STALL_TIMEOUT, RobotParams.Arm.STALL_RESET_TIMEOUT);
    }

    public TrcMotor getArmMotor()
    {
        return armMotor;
    }

    private double getGravityComp(double currPower)
    {
        return RobotParams.Arm.GRAVITY_COMP_POWER * Math.sin(Math.toRadians(armMotor.getPosition()));
    }
}
