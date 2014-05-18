package newCore;

public class DebugOutput {

	public static boolean debugOn = false;
	
	public static void print(String message){
		if(debugOn){
			System.out.println(message);
		}
	}
	
}
