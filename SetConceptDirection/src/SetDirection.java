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
import java.util.Map.Entry;


public class SetDirection {
	private static Map<String,List<String>> topicOutcomeMap;
	private static Map<String,List<String>> topicPreMap;
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
		readTopicDirection();
		file = new File("./resources/adjusted_direction_automatic_indexing.txt");
		try {
			if (!file.exists())
				file.createNewFile();
			fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
		} catch (IOException e) {
				e.printStackTrace();
		}
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
				topic = getTopic(title);
				if (topic != null)
				{
					boolean isInOutcomeManualIndexing;
					boolean isOutcomePreviousTopics;
					if (Arrays.asList(commonConcepts).contains(concept) == false)
					{
						isInOutcomeManualIndexing = isInOutcomeManualIndexing(topic,concept);
						isOutcomePreviousTopics = isOutcomePreviousTopics(topic,concept);
						if (isOutcomePreviousTopics & isInOutcomeManualIndexing)
							System.out.println("Overlapping outcomes: "+topic+" "+concept);
						if (isInOutcomeManualIndexing == true)
						{
							direction = "outcome";
						}
//						else if (isInPreManualIndexing(topic,concept) == true)
						else if (isOutcomePreviousTopics)
							direction = "prerequisite";
						else
							direction = "unknown";	
					}
					else
						direction = "-";					
					//write it to the adjusted direction file
					writeAdjustedDirection(title,topic,concept,tfidf,direction,type);
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
	//we assume that each content is assigned to one topic
	private static String getTopic(String title) {
		for (Entry<String, List<String>> entry : topicContentMap.entrySet())
		{
			if (entry.getValue().contains(title))
				return entry.getKey();
		}
		return null;
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
			if (topicOutcomeMap.get(t).contains(concept))
				return true;
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
			if (topicOutcomeMap.get(topic).contains(concept))
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
