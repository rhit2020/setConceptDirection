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
	private static String[] commonConcepts;
	private static File file; //output file where similarity results are stored
	private static FileWriter fw;
	private static BufferedWriter bw;	
	private static Map<String,List<String>> topicContentMap;
	private static Map<Integer,String> topicOrderMap;
	
	public static void main (String[] args)
	{
		commonConcepts = new String[]{"FormalMethodParameter","ClassDefinition","VoidDataType",
				                      "MethodDefinition","StaticMethodSpecifier","PublicMethodSpecifier",
				                      "ActualMethodParameter","PublicClassSpecifier"};
		//currently topic order and content for course-id = 1 is used. So, the direction of only contents in topic_content would be adjusted
		readTopicContent();
		readTopicOrder();
		file = new File("./resources/adjusted_direction_automatic_indexing.txt");
		try {
			if (!file.exists())
				file.createNewFile();
			fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
		} catch (IOException e) {
				e.printStackTrace();
		}
		topicOutcomeMap = new HashMap<String, List<String>>();
		updateConceptDirection();
		try {
			if (fw != null) {
				fw.close();
			}
			if (bw != null) {
				bw.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void readTopicContent() {
		topicContentMap = new HashMap<String,List<String>>(); 
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		boolean isHeader = true;
		try {
			br = new BufferedReader(new FileReader("./resources/topic_content.csv"));
			String[] clmn;
			String topic;
			String content;
			List<String> list;
			while ((line = br.readLine()) != null) {
				if (isHeader)
				{
					isHeader = false;
					continue;
				}
				clmn = line.split(cvsSplitBy);
				topic = clmn[0];				
				content = clmn[1];
				if (topicContentMap.containsKey(topic) == true)
				{
					list = topicContentMap.get(topic);
					if (list.contains(content) == false)
						list.add(content);									
				}
				else
				{
					list = new ArrayList<String>();
					list.add(content);
					topicContentMap.put(topic,list);
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
		int count = 0;
		for (List<String> l : topicContentMap.values())
			count += l.size();
		System.out.println("topicContentMap: "+count);
	}

	private static void readTopicOrder() {
		topicOrderMap = new HashMap<Integer,String>(); 
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		boolean isHeader = true;
		try {
			br = new BufferedReader(new FileReader("./resources/topic_order.csv"));
			String[] clmn;
			String topic;
			int order;
			while ((line = br.readLine()) != null) {
				if (isHeader)
				{
					isHeader = false;
					continue;
				}
				clmn = line.split(cvsSplitBy);
				order = Integer.parseInt(clmn[0]);
				topic = clmn[1];				
				topicOrderMap.put(order, topic);
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
		System.out.println("topicOrderMap: "+topicOrderMap.size());	
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
			String firstTopic = "Variables";
			while ((line = br.readLine()) != null) {
				if (isHeader)
				{
					isHeader = false;
					continue;
				}
				clmn = line.split(cvsSplitBy);
				title = clmn[0];
				concept = clmn[1];
				tfidf = clmn[2];
				direction = clmn[3];
				type = clmn[4];				
				topic = clmn[5];				
				if (topic != null)
				{					
					if (Arrays.asList(commonConcepts).contains(concept) == true)
					{
						direction = "-";
					}
					else
					{
						if (topic.equals(firstTopic) == true)
						{
							direction = "outcome";
						}
						else
						{
							boolean isOutcomePreviousTopics;
							isOutcomePreviousTopics = isOutcomePreviousTopics(topic,concept);
							if (isOutcomePreviousTopics)
								direction = "prerequisite";
							else						
								direction = "outcome";						
						}								

					}
					//write it to the adjusted direction file
					writeAdjustedDirection(title,topic,concept,tfidf,direction,type);
					if (direction.equals("outcome"))
					{
						if ( topicOutcomeMap.containsKey(topic) == false)
						{
							List<String> list = new ArrayList<String>();
							list.add(concept);
							topicOutcomeMap.put(topic,list);
						}
						else
						{
							List<String> list = topicOutcomeMap.get(topic);
							if (list.contains(concept) == false)
								list.add(concept);
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

	private static boolean isOutcomePreviousTopics(String topic, String concept) {
		//find the order of the current topic
		int curTopic = 0;
		for (int i : topicOrderMap.keySet())
		{
			if (topicOrderMap.get(i).equals(topic))
			{
				curTopic = i;
				break;
			}
		}
		//find list of topics before this current topic
		List<String> preTopicList = new ArrayList<String>();
		for (int i : topicOrderMap.keySet())
		{
			if ( i < curTopic )
				preTopicList.add(topicOrderMap.get(i));			
		}
		//list all outcomes of the previous topics
		for (String t : preTopicList)
		{
			if(topicOutcomeMap.get(t) == null)
				System.out.println(t+" has no outcome ");
			else 
			{
				if (topicOutcomeMap.get(t).contains(concept))
					return true;
			}			
		}
		return false;		
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
}
