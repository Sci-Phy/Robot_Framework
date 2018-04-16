package org.usfirst.frc.team1504.robot;

import org.usfirst.frc.team1504.robot.Update_Semaphore.Updatable;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Timer;

public class Winch implements Updatable
{
	public static final int INTAKE_POWER_AXIS = 1;
	public static final int LIFT_AXIS = 5;
	public static final int LEFT_TALON_PORT = 40;
	public static final int RIGHT_TALON_PORT = 41;
	public static final int WINCH_CURRENT_LIMIT = 70;
	public static final int WINCH_POWER_AXIS = 1;
	public static final double WINCH_DIRECTION = -1.0;
	public static final double WINCH_SERVO_DEPLOYED = 180.0;
	public static final double WINCH_SERVO_STORED = 0.0;
	public static final double WINCH_BRAKE_TIMEOUT = 15.0;
	
	private static Winch _instance = new Winch();

	private boolean _deployed = true;
	private DriverStation _ds = DriverStation.getInstance();
	
	private WPI_TalonSRX _left;
	private WPI_TalonSRX _right;
	private Thread _winch;
	
	protected Winch()
	{
		_left = new WPI_TalonSRX(RIGHT_TALON_PORT);
		_left.enableCurrentLimit(true);
		_left.configContinuousCurrentLimit(WINCH_CURRENT_LIMIT, 20); //20 is timeout in ms
		
		_right = new WPI_TalonSRX(LEFT_TALON_PORT);
		_right.enableCurrentLimit(true);
		_right.configContinuousCurrentLimit(WINCH_CURRENT_LIMIT, 20); //20 is timeout in ms
		
		new Thread( new Runnable() {
			public void run() {
				double timeout = WINCH_BRAKE_TIMEOUT;

				while(true)
				{
					if(_ds.isEnabled())
					{
						_right.setNeutralMode(NeutralMode.Brake);
						_left.setNeutralMode(NeutralMode.Brake);
					}
					
					else if(!_ds.isEnabled())
					{
//						System.out.println("Winch brakes OFF in "+ timeout +" seconds.");
						new Thread( new Runnable()
						{
							public void run() 
							{
								Timer.delay(timeout);
								if(_ds.isEnabled())
									return;
								_right.setNeutralMode(NeutralMode.Coast);
								_left.setNeutralMode(NeutralMode.Coast);
//								System.out.println("Winch brakes OFF");
							}
						}).start();
					}
					Timer.delay(.2);
				}
			}
		}).start();
		
		_right.setNeutralMode(NeutralMode.Brake);
		_left.setNeutralMode(NeutralMode.Brake);
		
		_winch = new Thread(new Runnable() {
			public void run()
			{
				while(!_deployed) //while winch not deployed, periodically backdrive winch to keep tension
				{
					_left.set(-.25);
					_right.set(.25);
					Timer.delay(.01);
					_left.set(0.0);
					_right.set(0.0);
					
					try {
						Thread.sleep(500); //ms
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				_deployed = false;
				
				_left.set(1.0); //deploy winch
				_right.set(-1.0);
				Timer.delay(2); 
				_left.set(0.0);
				_right.set(0.0);
				
				_deployed = true;
				
			}
		}); 
		_winch.start();

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
	}

	public void semaphore_update()
	{
		//set_current_limit(IO.winch_override());
		//uncomment this once we have inputs figured out
		/*if(_ds.getMatchTime() > 30.0 && !IO.winch_override())
			return;*/
		// Run that thang!
		_left.set(IO.winch_input());
		_right.set(-IO.winch_input());
	}
}