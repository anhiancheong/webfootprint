package newCore;

public class coreDriver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String first = "amber";
		String last = "renee";
		if(args.length > 1) {
			first = args[0];
			last = args[1];
		}
		
		
		DebugOutput.debugOn = true;
		//Create the experiment
		CoreExperiment experiment = new CoreExperiment();
		
		//Config contains the paramters for the experiment
		experiment.loadConfig("config.txt");

		/**
		 * 
		 * ONE EXPERIMENT CONFIG WILL RUN MULTIPLE PEOPLE
		 * For each person, load their initial attributes, start inference process, post to database
		 * 
		 * SELECT gtperson_id from linkedin where attribute_name = 'location' and attribute_value in (SELECT attribute_value from googleplus where attribute_name = 'location');
		 * 
		 * */
		
		for(int i = 0; i < 1; i++){ /**TO BE CHANGED!!!**/
		//Config will make the initial round of DB calls to correctly population the experiment
			//Should it take an array of the other values???
			DebugOutput.print("Inference Starting");
			experiment.initialize(first, last);
			
			//Will start the process of looking at websites, merging data, etc
			experiment.run();
			
			experiment.outputDebugFile();
			
			//Will post all the results to the database
			//experiment.postToDB();
		}
	}

}
