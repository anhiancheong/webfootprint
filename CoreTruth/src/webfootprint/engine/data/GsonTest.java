package webfootprint.engine.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FileUtils;

import webfootprint.engine.util.Pair;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Pattern;

import webfootprint.engine.apriori.*;

 
public class GsonTest {
 
    public static void main(String[] args) throws IOException {
        // Deserialize JSON to object
    	System.out.println(URLEncoder.encode("a d+f", "UTF-8").replaceAll(Pattern.quote("+"), "%20").
    			replaceAll(Pattern.quote("*"), "%2A"));
    	ArrayList<String> array = new ArrayList<String>();
		array.add("a");
		array.add(null);
		System.out.println(array.size());
		for(int i = 0; i < array.size(); i++) {
			String elemeent = array.get(i);
			System.out.println(elemeent);
		}
        String txt = "{'name': 'James', 'age': '25'}";
        Gson gson = new Gson();
        Person person = gson.fromJson(txt, Person.class);
        System.out.println(person.toString());
        /*
         * name: James
         * age: 25
         *
         * */
 
        String r = readToString("json.txt");
        r = read("json.txt");
        Inference inference = new Inference("first", "last");
        inference.addInference("male", "peter", 1);
        ArrayList<String> list = new ArrayList<String>();
        list.add("peter");
        list.add("john");
        
        String jsonString = gson.toJson(inference);
        RuleConstituent antecedent = new RuleConstituent(0);
        RuleConstituent consequent = new RuleConstituent(1);
        AssociationRule rule = new AssociationRule(antecedent, consequent, null, 0.0);
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("peter", 1);
        map.put("john", 2);
        JsonObject element = (JsonObject)gson.toJsonTree(map);
        JsonElement ele = element.get("peter");
     
   
        System.out.println(element.isJsonObject());
        // Parse JSON directly (into JsonElement)
        JsonParser parser = new JsonParser();
        JsonObject obj = (JsonObject)parser.parse(gson.toJson(rule));
        System.out.println(obj.toString());
        System.out.println(obj);
        JsonElement id = obj.get("id");
        System.out.println(obj.isJsonArray());
        System.out.println(id.isJsonPrimitive());
        System.out.println(id); // Prints "10001"
        boolean judge = obj.get("result").isJsonArray();
        judge = obj.get("result").isJsonObject();
        judge = obj.get("result").isJsonPrimitive();
        JsonArray arr = obj.get("result").getAsJsonArray();
        JsonElement p = arr.get(0);
        System.out.println(p); // Prints {"name": "John","age": "22"}
    }
 
    public static String readToString(String path){
        File f = new File(path);
        try{
            String r = FileUtils.readFileToString(f, "UTF-8");
            return r;
        }
        catch (IOException e){
             e.printStackTrace();
             return null;
        }
    }
    
    public static String read(String filename) throws IOException {
    	BufferedReader in = new BufferedReader(new FileReader(filename));
    	StringBuilder builder = new StringBuilder();
    	String line = "";
    	while((line = in.readLine()) != null) {
    		builder.append(line);
    	}
    	in.close();
    	return builder.toString();
    }
}
 
class Person{
    private String name;
    private String age;
 
    public Person(String name, String age){
        this.name = name;
        this.age = age;
    }
 
    @Override
    public String toString(){
         StringBuilder sb = new StringBuilder();
         sb.append("name: " + name + "\n");
         sb.append("age: " + age + "\n");
         return sb.toString();
    }
 
}
