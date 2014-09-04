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
	private static Map<String,List<String>> conceptHolesQuestionMap;//topic is the key, and the list of holes for that topic are in this map -- if a concept is in holes it means that it is in question outcome but is not addressed in any example
	private static Map<String,List<String>> conceptHolesExampleMap;//topic is the key, and the list of holes for that topic are in this map -- if a concept is in holes it means that it is in example outcome but is not addressed in any question

	private static Map<String,List<String>> topicQuestionOutcomeMap;
	private static Map<String,List<String>> topicExampleOutcomeMap;
	
	private static Map<String,List<String>> exampleConceptContentMap = new HashMap<String,List<String>>();
	private static Map<String,List<String>> questionConceptContentMap= new HashMap<String,List<String>>();
	private static Map<Integer,String> topicOrderMap;


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
                "ActualMethodParameter","PublicClassSpecifier"};
		readTopicOrder();
		readTopicDirection();
		findConceptHoles();
		writeConceptHoles();
		file = new File("./resources/topic_outcomes.txt");
		try {
			if (!file.exists())
				file.createNewFile();
			fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
		} catch (IOException e) {
				e.printStackTrace();
		}
		writeTopicOutcomes();
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
	
	private static void writeTopicOutcomes() {
		int q,e;
		try {
			for (String topic : topicQuestionOutcomeMap.keySet())
			{
				for (String c : topicQuestionOutcomeMap.get(topic))
				{
					q = (questionConceptContentMap.get(c) == null? 0:questionConceptContentMap.get(c).size());
					e = (exampleConceptContentMap.get(c) == null? 0:exampleConceptContentMap.get(c).size());

					bw.write(findOrder(topic)+","+topic+","+c+","+" q:"+q+" e:"+e);
					bw.newLine();
				    bw.flush();
				}
			}	
			
			for (String topic : topicExampleOutcomeMap.keySet())
			{
				for (String c : topicExampleOutcomeMap.get(topic))
				{
					if (topicQuestionOutcomeMap.get(topic) == null)
						System.out.println("####"+topic);
					if (topicQuestionOutcomeMap.get(topic).contains(c) == false)
					{
						q = (questionConceptContentMap.get(c) == null? 0:questionConceptContentMap.get(c).size());
						e = (exampleConceptContentMap.get(c) == null? 0:exampleConceptContentMap.get(c).size());

						bw.write(findOrder(topic)+","+topic+","+c+","+" q:"+q+" e:"+e);
						bw.newLine();
					    bw.flush();
					}					
				}
			}	
		} catch (IOException e2) {
			e2.printStackTrace();
		}		
	
		
	}


	private static int findOrder(String topic) {
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
				return curTopic;
	}

	private static void writeConceptHoles() {
		int q,e;
		try {
			for (String topic : conceptHolesQuestionMap.keySet())
			{
				for (String c : conceptHolesQuestionMap.get(topic))
				{
					q = (questionConceptContentMap.get(c) == null? 0:questionConceptContentMap.get(c).size());
					e = (exampleConceptContentMap.get(c) == null? 0:exampleConceptContentMap.get(c).size());

					bw.write(findOrder(topic)+","+topic+","+c+","+" q:"+q+" e:"+e);
					bw.newLine();
				    bw.flush();
				}
			}	
			
			for (String topic : conceptHolesExampleMap.keySet())
			{
				for (String c : conceptHolesExampleMap.get(topic))
				{
					q = (questionConceptContentMap.get(c) == null? 0:questionConceptContentMap.get(c).size());
					e = (exampleConceptContentMap.get(c) == null? 0:exampleConceptContentMap.get(c).size());

					bw.write(findOrder(topic)+","+topic+","+c+","+" q:"+q+" e:"+e);
					bw.newLine();
				    bw.flush();
				}
			}	
		} catch (IOException e2) {
			e2.printStackTrace();
		}		
	}

	private static void findConceptHoles()
	{
		List<String> holes;
		conceptHolesQuestionMap = new HashMap<String, List<String>>();
		conceptHolesExampleMap = new HashMap<String, List<String>>();

		//finding holes for outcomes in question that has no example describing them 
		for (String topic : topicQuestionOutcomeMap.keySet())
		{
			for (String concept : topicQuestionOutcomeMap.get(topic))
			{
				if (topicExampleOutcomeMap.containsKey(topic))
				{
					if (topicExampleOutcomeMap.get(topic).contains(concept) == false)
					{
						holes = conceptHolesQuestionMap.get(topic);
						if (holes != null)
						{
							if (holes.contains(concept) == false)
								holes.add(concept);
						}
						else
						{
							holes = new ArrayList<String>();
							holes.add(concept);
							conceptHolesQuestionMap.put(topic,holes);
						}						
					}
				}
			}			
		}

		// finding holes for outcomes in example that has no question addressing them
		for (String topic : topicExampleOutcomeMap.keySet()) {
			for (String concept : topicExampleOutcomeMap.get(topic)) {
				if (topicQuestionOutcomeMap.containsKey(topic)) {
					if (topicQuestionOutcomeMap.get(topic).contains(concept) == false) {
						holes = conceptHolesExampleMap.get(topic);
						if (holes != null) {
							if (holes.contains(concept) == false)
								holes.add(concept);
						} else {
							holes = new ArrayList<String>();
							holes.add(concept);
							conceptHolesExampleMap.put(topic, holes);
						}
					}
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
		topicQuestionOutcomeMap = new HashMap<String,List<String>>();
		topicExampleOutcomeMap = new HashMap<String,List<String>>();

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		boolean isHeader = false; //there is no header
		try {
			br = new BufferedReader(new FileReader("./resources/adjusted_direction_automatic_indexing.txt"));
			String[] clmn;
			String content;
			String topic;
			String concept;
			String direction;
			String type;
			while ((line = br.readLine()) != null) {
				if (isHeader)
				{
					isHeader = false;
					continue;
				}
				clmn = line.split(cvsSplitBy);
				content = clmn[0];
				topic = clmn[1];
				concept = clmn[2];
				direction = clmn[4];
				type = clmn[5];
				List<String> list;
				if (type.equals("question"))
				{
					if (direction.equals("outcome"))
					{
						if (topicQuestionOutcomeMap.containsKey(topic) != false)
						{
							list = topicQuestionOutcomeMap.get(topic);	
							if (list.contains(concept) == false)
								list.add(concept);
							topicQuestionOutcomeMap.put(topic, list);
						}
						else
						{
							list = new ArrayList<String>();	
							list.add(concept);
							topicQuestionOutcomeMap.put(topic, list);
						}
						if (questionConceptContentMap.containsKey(concept) == true)
						{
							List<String> l = questionConceptContentMap.get(concept);
							if (l.contains(content) == false)
								l.add(content);
						}
						else
						{
							List<String> l = new ArrayList<String>();
							l.add(content);
							questionConceptContentMap.put(concept, l);
						}
					}		
				}
				else if (type.equals("example"))
				{
					if (direction.equals("outcome"))
					{
						if (topicExampleOutcomeMap.containsKey(topic) != false)
						{
							list = topicExampleOutcomeMap.get(topic);	
							if (list.contains(concept) == false)
								list.add(concept);
							topicExampleOutcomeMap.put(topic, list);
						}
						else
						{
							list = new ArrayList<String>();	
							list.add(concept);
							topicExampleOutcomeMap.put(topic, list);
						}
						if (exampleConceptContentMap.containsKey(concept) == true)
						{
							List<String> l = exampleConceptContentMap.get(concept);
							if (l.contains(content) == false)
								l.add(content);
						}
						else
						{
							List<String> l = new ArrayList<String>();
							l.add(content);
							exampleConceptContentMap.put(concept, l);
						}
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
		for (String t : topicQuestionOutcomeMap.keySet())
		{
			countTopic++;
			count += topicQuestionOutcomeMap.get(t).size();
		}
		System.out.println("topicQuestionOutcomeMap: topic:"+countTopic+" topic_concept:"+count);	
		
		count = 0;countTopic = 0;
		for (String t : topicExampleOutcomeMap.keySet())
		{
			countTopic++;
			count += topicExampleOutcomeMap.get(t).size();
		}
		System.out.println("topicExampleOutcomeMap: topic:"+countTopic+" topic_concept:"+count);	
	}
}
