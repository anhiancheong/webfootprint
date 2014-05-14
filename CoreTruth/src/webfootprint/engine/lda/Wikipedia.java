package webfootprint.engine.lda;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.io.IOException;

public class Wikipedia {	
	
	private int queryTopNDoc;
	private int delay;
	private boolean parentCate; 
	private boolean interCate;
	private int parentCateLevel;
	
	public Wikipedia(WikipediaSetting setting) {
		this.queryTopNDoc = setting.getQueryTopNDoc();
		this.interCate = setting.getInterCate();
		this.delay = setting.getDelay();
		this.parentCate = setting.getParentCate();
		this.parentCateLevel = setting.getParentCateLevel();
	}
	
	public ArrayList<String> query(String query) throws InterruptedException, IOException {
		
		ArrayList<String> categories = new ArrayList<String>();
		
		String url = "http://cs-sys-1.uis.georgetown.edu/~cosc688/wiki/lemur.cgi?x=false&q=";
		String[] tokens = query.split("[ \t]+");
		if (tokens.length == 0) {
			return categories;
		}
		String parsedQuery = "";
		for(int i = 0; i < tokens.length - 1; i++) {
			parsedQuery = parsedQuery + tokens[i] + "+" ;
		}
		parsedQuery = parsedQuery + tokens[tokens.length - 1];
		//System.out.println(parsedQuery);
		String request = url;
		request = request + parsedQuery;
	
		URL serviceURL = new URL(request);
		InputStream is = serviceURL.openStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);	
		String nextLineFromService = "";
		boolean valid = false;
		int numberDocument = queryTopNDoc;
					
		while ((nextLineFromService = br.readLine()) != null) {

			if(nextLineFromService.indexOf("<ol type=1 start=\"1\">") >= 0) {
				valid = true;
			}
			
			if((nextLineFromService.indexOf("<li><a href=") >= 0) && valid && (numberDocument > 0)) {
				if(queryCategory(nextLineFromService, categories)) {
					numberDocument--;					
				}
			}
		}
		
		return categories;
	}	
	
	private boolean queryCategory(String page, ArrayList<String> categories) throws InterruptedException, IOException {
		int beginIndex = page.indexOf("<a href=\"") + 9;
		int endIndex = page.indexOf("\">");
		String pageUrl = page.substring(beginIndex, endIndex);
		
		Thread.sleep(delay);
		URL url = new URL(pageUrl);
		InputStream is;
		try {
			is = url.openStream();
		}catch(Exception e) {
			return false;
		}
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String nextLineFromService = "";		
		String categoryLine = "";
		while ((nextLineFromService = br.readLine()) != null) {				
			try{				
				if(nextLineFromService.indexOf("<a href=\"/wiki/Help:Category\" title=") >= 0) {						
					categoryLine = nextLineFromService;
				}
			}catch(Exception e) {
				System.out.println();
			}
		}
		if (categoryLine.indexOf("<a href=\"/wiki/Help:Category\" title=") >= 0) {
			int begin = categoryLine.indexOf("<ul>");
			int end = categoryLine.indexOf("</ul>");
			if(begin <  0 || end < 0 || begin >= end) {
				return false;
			}
			retrieveCategory(categoryLine, categories);
			//System.out.println(pageUrl + "\n");
			//System.out.println();
			return true;
		}else {
			return false;
		}		
	}	
	
	private void retrieveCategory(String categoryLine, ArrayList<String> categories) throws InterruptedException, IOException {
		int beginIndex = categoryLine.indexOf("<ul>");
		int endIndex = categoryLine.indexOf("</ul>");
		
		categoryLine = categoryLine.substring(beginIndex, endIndex);
		
		String[] subLine = categoryLine.split("<li>");
		for(int i = 1; i < subLine.length; i++) {
			String[] subSection = subLine[i].split("<a");
			beginIndex = subSection[1].indexOf("\">") + 2;
			endIndex = subSection[1].indexOf("</a>");
			String category = subSection[1].substring(beginIndex, endIndex);
			if(!category.matches("^[\u0000-\u0080]+$")) {
				continue;
			}
			if(!parentCate) {
				categories.add(category);
			} else {
				if(interCate) {
					categories.add(category);
				}
				beginIndex = subSection[1].indexOf("\"/wiki/Category:") + 16;
				endIndex = subSection[1].indexOf("\" title=");				
				retrieveParentCategories(subSection[1].substring(beginIndex, endIndex), this.parentCateLevel, categories);
			}
		}
	}
	
	public void retrieveParentCategories(String categoryAddress, int parentCategoryLevel, ArrayList<String> categories) throws InterruptedException, IOException {
		String categoryUrl = "http://en.wikipedia.org/wiki/Category:" + categoryAddress;
		if(this.parentCateLevel - parentCategoryLevel > 0) {
			//System.out.println("parent category " + (this.parentCategoryLevel - parentCategoryLevel) + " level above: " + categoryUrl);
		}
		Thread.sleep(delay);
		URL url = new URL(categoryUrl);
		InputStream is;
		try {
			is = url.openStream();
		
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String nextLineFromService = "";		
		
			while ((nextLineFromService = br.readLine()) != null) {
				//try{
					if(nextLineFromService.indexOf("<a href=\"/wiki/Help:Category\" title=") >= 0) {
						int beginIndex = nextLineFromService.indexOf("<ul>");
						int endIndex = nextLineFromService.indexOf("</ul>");
						nextLineFromService = nextLineFromService.substring(beginIndex, endIndex);					
						String[] subLine = nextLineFromService.split("<li>");
						for(int i = 1; i < subLine.length; i++) {
							String[] subSection = subLine[i].split("<a");
							beginIndex = subSection[1].indexOf("\">") + 2;
							endIndex = subSection[1].indexOf("</a>");
							String category = subSection[1].substring(beginIndex, endIndex);
							if(!category.matches("^[\u0000-\u0080]+$")) {
								continue;
							}
							if (parentCategoryLevel == 1) {
								categories.add(category);
							}else {
								if(interCate) {
									categories.add(category);
								}							
								beginIndex = subSection[1].indexOf("\"/wiki/Category:") + 16;
								endIndex = subSection[1].indexOf("\" title=");
								retrieveParentCategories(subSection[1].substring(beginIndex, endIndex), parentCategoryLevel - 1, categories);
							}							
						}								
					}	
				//}catch() {
					//;
				//}
			}
		}catch(Exception e) {
			;
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		WikipediaSetting setting = new WikipediaSetting();
		setting.setInterCate(true);
		setting.setParentCate(true);
		setting.setParentCateLevel(2);
		setting.setQueryTopNDoc(1);
		Wikipedia wiki = new Wikipedia(setting);
		ArrayList<String> query = new ArrayList<String>();
		ArrayList<String> categories = wiki.query("white+house");
		System.out.println();
		
	}
}
