package team492.subsystems;

import frclib.subsystem.FrcIntake;
import frclib.subsystem.FrcIntake.SensorType;
import team492.RobotParams;
import trclib.subsystem.TrcIntake;

public class Intake {
    private TrcIntake intakeMotor;

    public Intake()
    {
        FrcIntake.Params motorParams = new FrcIntake.Params()
            .setMotorInverted(RobotParams.Intake.MOTOR_INVERTED)
            .setVoltageCompensationEnabled(RobotParams.Intake.VOLTAGE_COMP_ENABLED)
            // .setEntrySensor(SensorType.DigitalSensor, RobotParams.Intake.SENSOR_DIGITAL_CHANNEL, 
            //                 RobotParams.Intake.SENSOR_INVERTED, 0.0, null);
        
        intakeMotor = new FrcIntake(
            RobotParams.Intake.SUBSYSTEM_NAME, RobotParams.Intake.MOTOR_TYPE, RobotParams.Intake.MOTOR_ID,
            RobotParams.Intake.MOTOR_BRUSHLESS, RobotParams.Intake.MOTOR_ENC_ABS, motorParams).getIntake();         
    }

    public TrcIntake getIntake()
    {
        return intakeMotor;
    }
}
