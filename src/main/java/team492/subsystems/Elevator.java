package team492.subsystems;

// import frclib.motor.FrcMotor.MotorType;
import frclib.subsystem.FrcMotorActuator;
import team492.RobotParams;
// import team492.RobotParams.Robot;
import trclib.motor.TrcMotor;

public class Elevator
{
    private TrcMotor elevatorMotor;

    public Elevator()
    {
        FrcMotorActuator.Params motorParams = new FrcMotorActuator.Params()
            .setMotorInverted(RobotParams.Elevator.MOTOR_INVERTED)
            .setVoltageCompensationEnabled(RobotParams.Elevator.VOLTAGE_COMP_ENABLED)
            .setPositionScaleAndOffset(RobotParams.Elevator.INCHES_PER_COUNT, RobotParams.Elevator.POS_OFFSET)
            .setPositionPresets(RobotParams.Elevator.POS_PRESET_TOLERANCE, RobotParams.Elevator.posPresets);

        elevatorMotor = new FrcMotorActuator(
            RobotParams.Elevator.SUBSYSTEM_NAME, RobotParams.Elevator.MOTOR_ID, RobotParams.Elevator.MOTOR_TYPE,
            RobotParams.Elevator.MOTOR_BRUSHLESS, RobotParams.Elevator.MOTOR_ENC_ABS, motorParams).getActuator();
        elevatorMotor.setSoftwarePidEnabled(RobotParams.Elevator.SOFTWARE_PID_ENABLED);
        elevatorMotor.setPositionPidParameters(RobotParams.Elevator.posPidCoeffs, RobotParams.Elevator.POS_PID_TOLERANCE);
        // elevatorMotor.setPositionPidPowerComp(this::getGravityComp);
        elevatorMotor.setStallProtection(
            RobotParams.Elevator.STALL_MIN_POWER, RobotParams.Elevator.STALL_TOLERANCE,
            RobotParams.Elevator.STALL_TIMEOUT, RobotParams.Elevator.STALL_RESET_TIMEOUT);
    }

    public TrcMotor getElevatorMotor()
    {
        return elevatorMotor;
    }

    // private double getGravityComp(double currPower)
    // {
    //     return RobotParams.Elevator.GRAVITY_COMP_POWER;
    // }
}
