import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ConceptHoleFinder {
	private static File file; 
	private static FileWriter fw;
	private static BufferedWriter bw;	
	private static Map<String,List<String>> conceptHolesMap;//topic is the key, and the list of holes for that topic are in this map
	private static Map<String,List<String>> topicOutcomeMap;
	private static Map<String,List<String>> autoTopicExampleConcept;
	private static String[] commonConcepts;

	public static void main (String[] args)
	{
		file = new File("./resources/concept_holes.txt");
		try {
			if (!file.exists())
				file.createNewFile();
			fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
		} catch (IOException e) {
				e.printStackTrace();
		}
		commonConcepts = new String[]{"FormalMethodParameter","ClassDefinition","VoidDataType",
                "MethodDefinition","StaticMethodSpecifier","PublicMethodSpecifier",
                "ActualMethodParameter"};
		readTopicDirection();
		createAutomaticIndexingExampleConcepts();
		findConceptHoles();
	}
	
	private static void createAutomaticIndexingExampleConcepts() {
		autoTopicExampleConcept = new HashMap<String,List<String>>(); 
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		boolean isHeader = true;
		try {
			br = new BufferedReader(new FileReader("./resources/automatic_indexing.csv"));
			String[] clmn;
			String title,topic, concept, tfidf, direction, type;
			List<String> list;
			while ((line = br.readLine()) != null) {
				if (isHeader)
				{
					isHeader = false;
					continue;
				}
				clmn = line.split(cvsSplitBy);
				title = clmn[0];
				topic = clmn[1];
				concept = clmn[2];
				if (concept.toLowerCase().equals("true"))
					concept = "TRUE";
				else if (concept.toLowerCase().equals("false"))
					concept = "FALSE";
				else if (concept.equals("System.out.println"))
					...
				tfidf = clmn[3];
				direction = clmn[4];
				type = clmn[5];
				if (Arrays.asList(commonConcepts).contains(concept) == false)
				{
					if (type.equals("example"))
					{
						if (autoTopicExampleConcept.containsKey(title) == false)
						{
							if (autoTopicExampleConcept.get(title) != null)
							{
								list = autoTopicExampleConcept.get(title);
								if (list.contains(concept) == false)
									list.add(concept);
							}	
							else
							{
								list = new ArrayList<String>();
								list.add(concept);
								autoTopicExampleConcept.put(topic, list);
							}
						}
					}					
				}			
			}
		}catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}		
	}

	private static void findConceptHoles()
	{
		conceptHolesMap = new HashMap<String,List<String>>(); 
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		boolean isHeader = true;
		try {
			br = new BufferedReader(new FileReader("./resources/automatic_indexing.csv"));
			String[] clmn;
			String title,topic, concept, tfidf, direction, type;
			while ((line = br.readLine()) != null) {
				if (isHeader)
				{
					isHeader = false;
					continue;
				}
				clmn = line.split(cvsSplitBy);
				title = clmn[0];
				topic = clmn[1];
				concept = clmn[2];
				if (concept.toLowerCase().equals("true"))
					concept = "TRUE";
				else if (concept.toLowerCase().equals("false"))
					concept = "FALSE";
				else if (concept.equals("System.out.println"))
					...
				tfidf = clmn[3];
				direction = clmn[4];
				type = clmn[5];
				if (Arrays.asList(commonConcepts).contains(concept) == false)
				{
					if (isInOutcomeManualIndexing(topic,concept) == true)
					{
						direction = "outcome";
					}
					else if (isInPreManualIndexing(topic,concept) == true)
						direction = "prerequisite";
					else
						direction = "unknown";	
				}
				else
					direction = "-";					
				//write it to the adjusted direction file
				writeAdjustedDirection(title,topic,concept,tfidf,direction,type);
			}
		}catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}			
	
	}
	
	public static void writeAdjustedDirection(String title,String topic,String concept, String tfidf, String direction, String type)
	{
		try {
			bw.write(title+","+topic+","+concept+","+tfidf+","+direction+","+type);
			bw.newLine();
		    bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void readTopicDirection() {
		topicOutcomeMap = new HashMap<String,List<String>>();
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		boolean isHeader = true;
		try {
			br = new BufferedReader(new FileReader("./resources/Manual_indexing.csv"));
			String[] clmn;
			String topic;
			String concept;
			String direction;
			while ((line = br.readLine()) != null) {
				if (isHeader)
				{
					isHeader = false;
					continue;
				}
				clmn = line.split(cvsSplitBy);
				topic = clmn[1];
				concept = clmn[2];
				direction = clmn[3];
				List<String> list;
				if (direction.equals("1"))
				{
					if (topicOutcomeMap.containsKey(topic) != false)
					{
						list = topicOutcomeMap.get(topic);	
						if (list.contains(concept) == false)
							list.add(concept);
						topicOutcomeMap.put(topic, list);
					}
					else
					{
						list = new ArrayList<String>();	
						list.add(concept);
						topicOutcomeMap.put(topic, list);
					}
				}				
			}	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}	
		int count = 0,countTopic = 0;
		for (String t : topicOutcomeMap.keySet())
		{
			countTopic++;
			count += topicOutcomeMap.get(t).size();
		}
		System.out.println("topicOutcomeMap: topic:"+countTopic+" topic_concept:"+count);	
	}
}
