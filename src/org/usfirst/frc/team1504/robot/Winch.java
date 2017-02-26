package org.usfirst.frc.team1504.robot;

import org.usfirst.frc.team1504.robot.Update_Semaphore.Updatable;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Timer;

public class Winch implements Updatable
{
	private static Winch _instance = new Winch();
	private Servo _servo1;
	private boolean _deployed = false;
	
	private boolean _override = false;
	
	private DriverStation _ds = DriverStation.getInstance();
	
	private CANTalon _nancy;
	private CANTalon _mead;
	
	//1 servo pwm = 0, 0 deg and 180 = extended
	
	protected Winch()
	{
		_servo1 = new Servo(Map.WINCH_SERVO1);

		_nancy = new CANTalon(Map.NANCY_TALON_PORT);
		_nancy.EnableCurrentLimit(true);
		_nancy.setCurrentLimit(Map.WINCH_CURRENT_LIMIT);
		
		_mead = new CANTalon(Map.MEAD_TALON_PORT);
		_mead.EnableCurrentLimit(true);
		_mead.setCurrentLimit(Map.WINCH_CURRENT_LIMIT);

		
		Update_Semaphore.getInstance().register(this);
		System.out.println("Winch is ready to end the game. And end your life.");
	}
	
	public static Winch getInstance()
	{
		return _instance;
	}
	public static void initialize()
	{
		getInstance();
	}
	public boolean get_deployed()
	{
		return _deployed;
	}
	
	public void set_deployed(boolean deployed)
	{
		_deployed = deployed;
		set_servo();
	}
	
	private void set_servo()
	{
		if(get_deployed())
		{
			_servo1.set(Map.WINCH_SERVO_DEPLOYED);
			
			//_servo2.set(Map.WINCH_SERVO_DEPLOYED);
		}
		else //if(!get_deployed() || !_ds.isEnabled())
		{
			_servo1.set(Map.WINCH_SERVO_STORED);
			//_servo2.set(Map.WINCH_SERVO_STORED);
			_deployed = false;
		}
	}
	private void set_current_limit(boolean override)
	{
		if (_override != override)
		{
			_nancy.EnableCurrentLimit(!_override);
			_mead.EnableCurrentLimit(!_override);
		}
		_override = override;
	}
	
	public void semaphore_update()
	{
		set_current_limit(IO.winch_override());
		
		if(_ds.getMatchTime() > 30.0 && !IO.winch_override())
			return;
		
		// Deploy winch out the side of the robot
		if(IO.winch_deploy())
		{
			if(!get_deployed())
			{
				new Thread(new Runnable() {public void run() 
					{
						Timer.delay(3); 
						set_deployed(false);
					}
				}).start();
			}
			_deployed = true;
		}
		set_servo();
		
		
		// Run that thang!
		_nancy.set(IO.winch_input());
		_mead.set(-IO.winch_input());
	}
}