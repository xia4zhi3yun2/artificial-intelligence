import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class homework {
	private static List<List<List<String>>> kb;
	private static Map<String,List<int[]>> dic;
	private static List<List<String>> queries;
	
	
	
	public static void main(String args[]) {
		try {
			// read file
			String pathname = "input.txt";
			File filename = new File(pathname);
			InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
			BufferedReader br = new BufferedReader(reader);

			
			// read queries
			String num_query = br.readLine();
			queries=new ArrayList<>(); 
			
			for(int i=0;i<Integer.parseInt(num_query);i++){
				//split function name and args
				String[] pred=br.readLine().split("[()]+");
				//format_query: [predicate, arg1, arg2, ...]
				List<String> formatted_pred=new ArrayList<>();
				
				
				formatted_pred.add(pred[0].trim());
				String[] pred_args=pred[1].split(",");
				for(int j=0;j<pred_args.length;j++){
			       formatted_pred.add(pred_args[j].trim());
				}
				
				queries.add(formatted_pred);
				
			}

			
			// read KB
			String num_kb = br.readLine();
			kb=new ArrayList<>();
			dic=new HashMap<>();
			
			for(int i=0;i<Integer.parseInt(num_kb);i++){
				List<List<String>> sentence=new ArrayList<>();
				//split predicates
				String[] preds=br.readLine().split("\\|");
				for(int m=0;m<preds.length;m++){
					//split function name and args
					String[] pred=preds[m].split("[()]+");
					//format_query: [predicate, arg1, arg2, ...]
					List<String> formatted_pred=new ArrayList<>();
					//transform sentence to list
					formatted_pred.add(pred[0].trim());
					String[] pred_args=pred[1].split(",");
					for(int j=0;j<pred_args.length;j++){
				       formatted_pred.add(pred_args[j].trim());
					}				
					sentence.add(formatted_pred);
				}
				addToKB(sentence,kb,dic,i);
			}
			

			
//			List<List<String>> test=new ArrayList<>();
//			//split predicates
//			String[] preds=br.readLine().split("\\|");
//			for(int m=0;m<preds.length;m++){
//				String[] pred=preds[m].split("[()]+");
//				List<String> formatted_pred=new ArrayList<>();
//				formatted_pred.add(pred[0].trim());
//				String[] pred_args=pred[1].split(",");
//				for(int j=0;j<pred_args.length;j++){
//			       formatted_pred.add(pred_args[j].trim());
//				}				
//				test.add(formatted_pred);
//			}
//			System.out.println(checkDuplicate(kb,dic,test));
//			
			br.close();

			
			// inference and write file
			File writename = new File("output.txt");
			writename.createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(writename));
			
			for(int i=0;i<Integer.parseInt(num_query);i++){			
				//copy kb and dic
				List<List<List<String>>> mykb=new ArrayList<>();
				Map<String,List<int[]>> mydic=new HashMap<>();
				copyKB(kb,mykb);
				copyDic(dic,mydic);
				
				//inference
				if(inference(queries.get(i),mykb,mydic))	out.write("TRUE\n");
				else out.write("FALSE\n");
				
				printKB(mykb);
			}


			out.flush();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean inference(List<String> query,List<List<List<String>>> mykb,Map<String,List<int[]>> mydic){
		String pred_name=query.get(0);
		query.set(0, negation(pred_name)) ;
		
		//add ~query to mykb and mydic
		List<List<String>> sentence=new ArrayList<>();
		sentence.add(query);
		addToKB(sentence,mykb,mydic,mykb.size());
		
		//resolution
		boolean state=true;
		while(state){
			state=false;
			int size=mykb.size();
			for(int i=0;i<size;i++){
				for(int j=i+1;j<size;j++){
					String res=resolve(mykb,mydic,i,j);
					if(res.equals("EMPTY")) return true; //empty: find conflict
					else if(res.equals("TRUE")) state=true; //true: find new sentence
					else continue; //false: cannot find new sentence
				}
			}
		}
		
		return state;
	}
	
	public static String resolve(List<List<List<String>>> mykb,Map<String,List<int[]>> mydic,int i,int j){
		List<List<String>> l1=mykb.get(i);
		List<List<String>> l2=mykb.get(j);
		
		
		//create all possible unification
		boolean create_new=false;
		for(int m=0;m<l1.size();m++){
			List<String> pred=l1.get(m);
			String pred_name=pred.get(0);
			String negated_name=negation(pred_name);
			
			//find all predicates need unify
			if(!mydic.containsKey(negated_name)) continue;
			List<int[]> pos=mydic.get(negated_name);
			for(int n=0;n<pos.size();n++){
				if(pos.get(n)[0]!=j) continue;
				
				//find a pair of predicate can unify
				List<String> pred_to_uni=l2.get(pos.get(n)[1]);	
				Map<String,String> unify_map=new HashMap<>();
				if(unify(pred,pred_to_uni,unify_map,(l1.size()==1||l2.size()==1))) {
					if(l1.size()==1&&l2.size()==1) return "EMPTY";
					
					//create new sentence
					List<List<String>> sentence=new ArrayList<>();
					createSentence(sentence,l1,l2,unify_map,m,pos.get(n)[1]);
					
					//check duplicate
					boolean dupli=checkDuplicate(mykb,mydic,sentence);
					if(!dupli){
						addToKB(sentence,mykb,mydic,mykb.size());
						//test print
//						System.out.println(i+"+"+j+"->"+(mykb.size()-1));
//						for(List<String>p1:sentence){
//							for(String s:p1){
//								System.out.println(s);
//							}
//							System.out.println("\n");
//						}
						create_new=true;
					}
				}
			}	
		}
		
		if(create_new) return "TRUE";
		else return "FALSE";
		
		
	}
	
	
	public static void createSentence(List<List<String>> sentence,List<List<String>> l1,List<List<String>> l2,
			Map<String,String> unify_map,int delete_pred1,int delete_pred2){
		
		for(int m=0;m<l1.size();m++){
			if(m!=delete_pred1){
				//unify and copy
				List<String> newp=new ArrayList<>();
				List<String> oldp=l1.get(m);
				newp.add(oldp.get(0));
				for(int n=1;n<oldp.size();n++){
					if(unify_map.containsKey(oldp.get(n))){
						newp.add(unify_map.get(oldp.get(n)));
					}
					else newp.add(oldp.get(n));
				}
				sentence.add(newp);
			}
		}
		for(int m=0;m<l2.size();m++){
			if(m!=delete_pred2){
				//unify and copy
				List<String> newp=new ArrayList<>();
				List<String> oldp=l2.get(m);
				newp.add(oldp.get(0));
				for(int n=1;n<l2.get(m).size();n++){
					if(unify_map.containsKey(oldp.get(n))){
						newp.add(unify_map.get(oldp.get(n)));
					}
					else newp.add(oldp.get(n));
				}
				sentence.add(newp);
			}
		}
	}
	
	
	public static boolean unify(List<String> pred1,List<String> pred2,Map<String,String> unify_map,boolean special_case){
		for(int n=1;n<pred1.size();n++){
			String arg1=pred1.get(n);
			String arg2=pred2.get(n);
			
			if(arg1.charAt(0)<='Z'&& arg1.charAt(0)>='A'&&arg2.charAt(0)<='Z'&& arg2.charAt(0)>='A') {
				if(!arg1.equals(arg2)) return false;
			}
			else if(arg1.charAt(0)<='Z'&& arg1.charAt(0)>='A') unify_map.put(arg2,arg1);
			else if(arg2.charAt(0)<='Z'&& arg2.charAt(0)>='A') unify_map.put(arg1,arg2);
			else return special_case;
		}
		return true;
	}

	public static String negation(String s){
		if(s.charAt(0)=='~') return s.substring(1,s.length()) ;
		else return "~"+s;
	}

	public static void addToKB(List<List<String>> sentence,List<List<List<String>>> mykb,Map<String,List<int[]>> mydic,int rule_num){
		mykb.add(sentence);
		
		for(int i=0;i<sentence.size();i++){
			int[] pos=new int[2];
			pos[0]=rule_num;
			pos[1]=i;
			List<String> pred=sentence.get(i);
			if(mydic.containsKey(pred.get(0))) {
				List<int[]> position= mydic.get(pred.get(0));
				position.add(pos);
				mydic.put(pred.get(0),position);
			}
			else{
				ArrayList<int[]> position=new ArrayList<>();
				position.add(pos);
				mydic.put(pred.get(0),position);
			}
		}
		
//		//test dic
//		for (Entry<String, List<int[]>> entry : mydic.entrySet()) {
//		    String key = entry.getKey();
//		    List<int[]> value = entry.getValue();
//		    System.out.println("key:"+key);
//		    for(int[] p:value){
//		    	System.out.println("position:"+p[0]+" "+p[1]);
//		    }  
//		}
		
	}

	public static void printKB(List<List<List<String>>> original_kb){
		int i=0;
		for(List<List<String>> sen:original_kb){
			System.out.print(i+":");
			for(List<String> p:sen){
				System.out.print(p.get(0)+"(");
				for(int j=1;j<p.size();j++)  System.out.print(p.get(j)+" ");
				System.out.print(") | ");
			}
			System.out.print("\n");
			i++;
		}
	}
	
	public static void copyKB(List<List<List<String>>> original_kb,List<List<List<String>>> mykb){
		for(List<List<String>> s:original_kb){
			List<List<String>> sentence=new ArrayList<>();
			for(List<String> p:s){
				List<String> pred=new ArrayList<>(p);
				sentence.add(pred);
			}
			mykb.add(sentence);
		}
	}

	public static void copyDic(Map<String,List<int[]>> original_dic,Map<String,List<int[]>> mydic){
		for(Entry<String,List<int[]>> entry:original_dic.entrySet()){
			 String key = entry.getKey();
			 List<int[]> value = new ArrayList<>(entry.getValue());
			 mydic.put(key, value);
		}
	}

	public static boolean checkDuplicate(List<List<List<String>>> mykb,Map<String,List<int[]>> mydic,List<List<String>> sentence){
		Map<String,List<Integer>> map=new HashMap<>();
		for(int m=0;m<sentence.size();m++){
			String pred_name=sentence.get(m).get(0);
			if(map.containsKey(pred_name)) {
				List<Integer> position=map.get(pred_name);
				position.add(m);
				map.put(pred_name, position);
			}
			else{
				List<Integer> position=new ArrayList<>();
				position.add(m);
				map.put(pred_name, position);
			}
		}
		
		for(int m=0;m<mykb.size();m++){
			//go through every sentence already in mykb
			List<List<String>> cur=mykb.get(m);
			
			boolean same=true;
			int n;
			for(n=0;n<cur.size();n++){
				same=false;
				String pred_name=cur.get(n).get(0);
				if(map.containsKey(pred_name)){
					List<Integer> pos_list=map.get(pred_name);
					for(int p:pos_list){
						if(sentence.get(p).equals(cur.get(n))) {
							same=true;
							break;
						}
					}
					if(!same) break;
				}
				else break;
			}
			if(n==cur.size()) return true;
		}
		
//		for(int m=0;m<mykb.size();m++){
//			//go through every sentence already in mykb
//			List<List<String>> cur=mykb.get(m);
//			if(cur.size()!=sentence.size()) continue;
//			
//			//check every pred of new sentence to see if equals to cur
//			boolean same=true;
//			int n;
//			for(n=0;n<sentence.size();n++){
//				same=false;
//				String pred_name=sentence.get(n).get(0);
//				List<int[]> pos_list=mydic.get(pred_name);
//				for(int[] p:pos_list){
//					//cur also have this pred, compare this pred
//					if(p[0]==m&&sentence.get(n).equals(cur.get(p[1]))) {
//						same=true;
//						break;
//					}
//				}
//				if(!same) break;
//			}
//			if(n==sentence.size()) return true;
//		}
		return false;
	}
}
