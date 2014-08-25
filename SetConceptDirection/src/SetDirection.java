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


public class SetDirection {
	
	private static Map<String,List<String>> topicOutcomeMap;
	private static Map<String,List<String>> topicPreMap;
	private static String[] commonConcepts;
	private static File file; //output file where similarity results are stored
	private static FileWriter fw;
	private static BufferedWriter bw;	
	public static void main (String[] args)
	{
		commonConcepts = new String[]{"FormalMethodParameter","ClassDefinition","VoidDataType",
				                      "MethodDefinition","StaticMethodSpecifier","PublicMethodSpecifier",
				                      "ActualMethodParameter"};
		readTopicDirection();
		file = new File("./resources/adjusted_direction_automatic_indexing.csv");
		try {
			if (!file.exists())
				file.createNewFile();
			fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
		} catch (IOException e) {
				e.printStackTrace();
		}
		updateConceptDirection();
	}

	private static void updateConceptDirection() {
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
				tfidf = clmn[3];
				direction = clmn[4];
				type = clmn[5];
//				if (Arrays.asList(commonConcepts).contains(concept) == false)
//				{}
				if (isInOutcomeManualIndexing(topic,concept) == true)
				{
					direction = "outcome";
				}
				else if (isInPreManualIndexing(topic,concept) == true)
					direction = "prerequisite";
				else
					direction = "unknown";		
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
	
	public static void writeAdjustedDirection(String title,String topic,String concept, String tfidf, String direction, String type) {
		try {
			bw.write(title+","+topic+","+concept+","+tfidf+","+direction+","+type);
			bw.newLine();
		    bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static boolean isInPreManualIndexing(String topic, String concept) {
		if (topicPreMap.get(topic) != null)
		{
			for (String pre : topicPreMap.get(topic))
				if (pre.equals(concept))
					return true;		
		}
		else
			System.out.println(topic+" "+" has no prerequisite");
		return false;
	}

	private static boolean isInOutcomeManualIndexing(String topic,String concept) {
		if (topicOutcomeMap.get(topic) != null)
		{
			for (String outcome : topicOutcomeMap.get(topic))
				if (outcome.equals(concept))
					return true;
		}	
		else
			System.out.println(topic+" "+" has no outcome");
		return false;
	}

	private static void readTopicDirection() {
		topicOutcomeMap = new HashMap<String,List<String>>();
		topicPreMap = new HashMap<String,List<String>>();
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
				else if (direction.equals("0"))
				{
					if (topicPreMap.containsKey(topic) != false)
					{
						list = topicPreMap.get(topic);	
						if (list.contains(concept) == false)
							list.add(concept);
						topicPreMap.put(topic, list);
					}
					else
					{
						list = new ArrayList<String>();	
						list.add(concept);
						topicPreMap.put(topic, list);
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
		count = 0;
		countTopic = 0;
		for (String t : topicPreMap.keySet())
		{
			countTopic++;
			count += topicPreMap.get(t).size();
		}
		System.out.println("topicPreMap: topic:"+countTopic+" topic_concept:"+count);
	}
}
