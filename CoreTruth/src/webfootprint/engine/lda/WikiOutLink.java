package webfootprint.engine.lda;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

@Deprecated
public class WikiOutLink {	
	
	private final static int queryTopNPage = 3;
	private final static int delay = 100;
	private final static boolean parentCategory = false; 
	private final static int parentCategoryLevel = 1;
	
	public HashSet<String> query(String query) {
		
		HashSet<String> outLinks = new HashSet<String>();
		try {
			String url = "http://cs-sys-1.uis.georgetown.edu/~cosc688/wiki/lemur.cgi?x=false&q=";
			String[] tokens = query.split("[ \t]+");
			if (tokens.length == 0) {
				return outLinks;
			}
			String parsedQuery = "";
			for(int i = 0; i < tokens.length - 1; i++) {
				parsedQuery = parsedQuery + tokens[i] + "+" ;
			}
			parsedQuery = parsedQuery + tokens[tokens.length - 1];
			String request = url;
			request = request + parsedQuery;
		
			URL serviceURL = new URL(request);
			InputStream is = serviceURL.openStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);	
			String nextLineFromService = "";
			boolean valid = false;
			int numberPage = queryTopNPage;
						
			while ((nextLineFromService = br.readLine()) != null) {

				if(nextLineFromService.indexOf("<ol type=1 start=\"1\">") >= 0) {
					valid = true;
				}
				
				if((nextLineFromService.indexOf("clueweb09") >= 0) && valid && (numberPage > 0)) {
					System.out.println(nextLineFromService);
					if (queryOutLink(nextLineFromService, outLinks)) {
						numberPage--;
					}
				}
			}			
			//System.out.println(response);
		} catch (Exception e) {
			;
		}
		return outLinks;
	}	
	
	private boolean queryOutLink(String page, HashSet<String> outLinks) throws Exception {
		int beginIndex = page.indexOf("size=\"-1\">(") + 11;
		int endIndex = page.indexOf(")</font></br>");
		String pageUrl = "http://cs-sys-1.uis.georgetown.edu/~cosc688/wiki/lemur.cgi?e=" + page.substring(beginIndex, endIndex);
		
		Thread.sleep(delay);
		URL url = new URL(pageUrl);		
		InputStream is;
		try {
			is = url.openStream();
		}catch(Exception e) {
			return false;
		}
		System.out.println(pageUrl);
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String nextLineFromService = "";
		FileWriter out = new FileWriter("gplus_given_name_gender1.txt", true);
		boolean read = false;
		while ((nextLineFromService = br.readLine()) != null) {				
			try{
				if(nextLineFromService.indexOf("<body class=") >= 0) {
					read = true;										
				}
				if(nextLineFromService.indexOf("<div class=\"printfooter\">") >= 0) {
					read = false;										
				}
				if(read) {						
					if((nextLineFromService.indexOf("<a href=\"") >= 0) && (nextLineFromService.indexOf("title=") >= 0)) {
						//System.out.println(nextLineFromService);
						String[] tokens = nextLineFromService.split("<a ");
						for(int i = 1; i < tokens.length; i++) {
							beginIndex = tokens[i].indexOf("href=\"") + 6;
							endIndex = tokens[i].indexOf("\" title");
							int imageIndex = tokens[i].indexOf("class=\"image\"");
							int externalTextIndex = tokens[i].indexOf("class=\"external text\"");
							int phpIndex = tokens[i].indexOf("php");
							int colonIndex = tokens[i].indexOf(":");
							
							if((beginIndex < 0) || (endIndex < 0) || (imageIndex >= 0) || 
									(externalTextIndex >= 0) || (phpIndex >=0 ) || (colonIndex >= 0)) {
								continue;
							}
							String outLink = tokens[i].substring(beginIndex, endIndex);
							String[] directories = outLink.split("/");
							boolean trivialIndicator = (directories[directories.length - 1].length() <= 2);
							boolean hashIndicator = (outLink.indexOf("#") >= 0);
							String[] words = directories[directories.length - 1].split("_");
							boolean numberIndicator = true;
							for(int j = 0; j < words.length; j++) {
								if(!words[j].matches("^[0-9\\-]+$")) {
									numberIndicator = false;
								}								
							}
							boolean monthIndicator = false;
							for(int j = 0; j < words.length; j++) {
								if(words[j].matches("(J|j)anuary|(J|j)an|(F|f)ebruary|(F|f)eb|(M|m)arch|(M|m)ar|(A|a)pril|(A|a)pr" +
										"|(M|m)ay|(J|j)une|(J|j)un|(J|j)uly|(J|j)ul|(A|a)ugust|(A|a)ug|(S|s)eptember|(S|s)ept|(S|s)ep|" +
										"(O|o)ctober|(O|o)ct|(N|n)ovember|(N|n)ov|(D|d)ecember|(D|d)ec")) {
									monthIndicator = true;
								}
							}
							if(trivialIndicator || hashIndicator || monthIndicator || numberIndicator) {
								continue;
							}
							outLinks.add(outLink);
							out.write(outLink + "\n");
							out.flush();
						}
					}
				}
			}catch(Exception e) {
				System.out.println();
			}
		}
		System.out.println();
		out.close();
		return true;
	}
	
	private void retrieveCategory(String categoryLine, ArrayList<String> categories) throws Exception{
		int beginIndex = categoryLine.indexOf("<ul>");
		int endIndex = categoryLine.indexOf("</ul>");
		categoryLine = categoryLine.substring(beginIndex, endIndex);
		
		String[] subLine = categoryLine.split("<li>");
		for(int i = 1; i < subLine.length; i++) {
			String[] subSection = subLine[i].split("<a");
			beginIndex = subSection[1].indexOf(">") + 1;
			endIndex = subSection[1].indexOf("</a>");
			String category = subSection[1].substring(beginIndex, endIndex);
			if(!category.matches("^[\u0000-\u0080]+$")) {
				continue;
			}
			if(!parentCategory) {
				categories.add(category);
			}else {
				beginIndex = subSection[1].indexOf("\"/wiki/Category:") + 16;
				endIndex = subSection[1].indexOf("\" title=");				
				retrieveParentCategories(subSection[1].substring(beginIndex, endIndex), this.parentCategoryLevel, categories);
			}
		}
	}
	
	private void retrieveParentCategories(String category, int parentCategoryLevel, ArrayList<String> categories) throws Exception {
		String categoryUrl = "http://en.wikipedia.org/wiki/Category:" + category;
		if(this.parentCategoryLevel - parentCategoryLevel > 0) {
			System.out.println("parent category " + (this.parentCategoryLevel - parentCategoryLevel) + " level above: " + categoryUrl);
		}
		Thread.sleep(delay);
		URL url = new URL(categoryUrl);		
		InputStream is = url.openStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String nextLineFromService = "";		
		
		while ((nextLineFromService = br.readLine()) != null) {
			try{
				if(nextLineFromService.indexOf("<a href=\"/wiki/Help:Categories\" title=\"Help:Categories\">Categories</a>") >= 0) {
					int beginIndex = nextLineFromService.indexOf("<ul>");
					int endIndex = nextLineFromService.indexOf("</ul>");
					nextLineFromService = nextLineFromService.substring(beginIndex, endIndex);					
					String[] subLine = nextLineFromService.split("<li>");
					for(int i = 1; i < subLine.length; i++) {
						String[] subSection = subLine[i].split("<a");
						beginIndex = subSection[1].indexOf(">") + 1;
						endIndex = subSection[1].indexOf("</a>");
						String parentCategory = subSection[1].substring(beginIndex, endIndex);
						if(!parentCategory.matches("^[\u0000-\u0080]+$")) {
							continue;
						}
						if (parentCategoryLevel == 0) {
							categories.add(category);
						}else {
							beginIndex = subSection[1].indexOf("\"/wiki/Category:") + 16;
							endIndex = subSection[1].indexOf("\" title=");
							retrieveParentCategories(subSection[1].substring(beginIndex, endIndex), parentCategoryLevel - 1, categories);
						}							
					}								
				}	
			}catch(Exception e) {
				System.out.println();
			}
		}			
	}
	
}
