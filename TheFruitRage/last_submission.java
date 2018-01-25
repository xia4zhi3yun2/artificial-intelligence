import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

public class homework {
	private static int size;
	private static int type;
	private static float time;
	private static long startTime;
	private static float step=1;
	private static int end_depth=0;
	public static void main(String args[]) {
		try {
			
			startTime=System.currentTimeMillis();
			
			// read file
			String pathname = "input.txt";
			File filename = new File(pathname);
			InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
			BufferedReader br = new BufferedReader(reader);

			// read game variables (first 3 lines)
			String size_string = br.readLine();
			String type_string = br.readLine();
			String time_string = br.readLine();
			size = Integer.parseInt(size_string);
			type = Integer.parseInt(type_string);
			time = Float.parseFloat(time_string);
			
			
			// read game board
			char[][] board = new char[size][size];
			String row = "";
			int i = 0;
			row = br.readLine();
			while (row != null) {
				for (int j = 0; j < row.length(); j++) {
					board[i][j] = row.charAt(j);
				}
				i++;
				row = br.readLine();
			}
			br.close();

			// call function to find solution
			//estimate(board);
			int[] move=IterativeDFS(board);
			remove(board,move[1],move[2]);
			fall(board);
			

			// write file
			File writename = new File("output.txt");
			writename.createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(writename));
			
			out.write(move[2]+'A');
			out.write(Integer.toString(move[1]+1));
			out.write("\n");
			for (i = 0; i < size; i++) {
				StringBuilder s = new StringBuilder();
				for (int j = 0; j < size; j++){
					s.append(board[i][j]);
				}
				s.append("\n");
				out.write(s.toString());
			}
			
			out.flush();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

	public static int[] IterativeDFS(char[][] board){
		int i=1;
		int[] move=new int[3];
		long endTime=startTime;
		while((endTime-startTime)/1000*20 <=time/step){
			move=MaxValue2(board,0,Integer.MIN_VALUE,Integer.MAX_VALUE,0,i);
			endTime=System.currentTimeMillis();
			
			//test
			//System.out.println(endTime);
//			System.out.println(endTime-startTime);
//			System.out.println("step"+step);
//			System.out.println("time"+time/step);
//			System.out.println("layer"+i);
//			System.out.println("end_depth"+end_depth);
			
			if(step>=150&&i==2) break;
			if(end_depth<i) break;
			i++;
		}
		return move;
		
	}
	
	public static int[] MaxValue(char[][] board,int points,int alpha,int beta,int layer,int max_depth){
		int[] result=new int[3];
		if(GameOver(board,layer,max_depth)) {
			result[0]=points;
			result[1]=-1;
			result[2]=-1;
			return result;
		}
		
		//check for all possible move
		boolean[][] check_list=new boolean[size][size];//record if this region already checked
		for(int i=0;i<size;i++){
			for(int j=0;j<size;j++){
				if(board[i][j]!='*'&&!check_list[i][j]) {
					//create a new_board for this move
					char[][] temp=new char[size][size];
					for(int p=0;p<size;p++){
						for(int q=0;q<size;q++) temp[p][q]=board[p][q];
					}
					
					//evaluate this move
					int point=evaluate(temp,check_list,i,j);
					remove(temp,i,j);
					fall(temp);
					
					int[] t=MinValue(temp,points+point,alpha,beta,layer+1,max_depth);
					if(t[0]>alpha){
						alpha=t[0];
						result[1]=i;result[2]=j;
					}
					if(alpha>=beta) {
						result[0]=beta;
						return result;
					}
					
					//test
					//System.out.println(point);
					//System.out.println(alpha);
				}
			}
		}
		result[0]=alpha;
		return result;
	
	}
	
	public static int[] MinValue(char[][] board,int points,int alpha,int beta,int layer,int max_depth){
		int[] result=new int[3];
		if(GameOver(board,layer,max_depth)) {
			result[0]=points;
			result[1]=-1;
			result[2]=-1;
			return result;
		}
		
		
		//check for all possible move
		boolean[][] check_list=new boolean[size][size];//record if this region already checked
		for(int i=0;i<size;i++){
			for(int j=0;j<size;j++){
				if(board[i][j]!='*'&&!check_list[i][j]) {
					//create a new_board for this move
					char[][] temp=new char[size][size];
					for(int p=0;p<size;p++){
						for(int q=0;q<size;q++) temp[p][q]=board[p][q];
					}
					
					//evaluate this move
					int point=evaluate(temp,check_list,i,j);
					remove(temp,i,j);
					fall(temp);
					
					int[] t=MaxValue(temp,points-point,alpha,beta,layer+1,max_depth);
					if(t[0]<beta){
						beta=t[0];
						result[1]=i;result[2]=j;
					}
					if(beta<=alpha) {
						result[0]=alpha;
						return result;
					}
					
					//test
					//System.out.println(point);
					//System.out.println(beta);
				}
			}
		}
		result[0]=beta;
		return result;
	
	}
	
	public static int[] MaxValue2(char[][] board,int points,int alpha,int beta,int layer,int max_depth){
		int[] result=new int[3];
		
		//check for all possible move
		Queue<int[]> heap = new PriorityQueue<> (1, new Comparator<int[]>(){
			public int compare(int[] l1, int[] l2) {
			             return l2[0] - l1[0];}});
		
		boolean[][] check_list=new boolean[size][size];//record if this region already checked
		step=0;
		for(int i=0;i<size;i++){
			for(int j=0;j<size;j++){
				if(board[i][j]!='*'&&!check_list[i][j]) {
					step++;
					//evaluate this move
					int point=evaluate(board,check_list,i,j);
					heap.offer(new int[]{point,i,j});
				}
			}
		}
		int[] best=heap.peek();
		result[0]=best[0];
		result[1]=best[1];
		result[2]=best[2];
		
		
		
		
		while (!heap.isEmpty()) {
	        int[] move = heap.poll();
	        
	        //test
	        //System.out.println(move[0]);
	
			char[][] temp=new char[size][size];
			for(int p=0;p<size;p++){
				for(int q=0;q<size;q++) temp[p][q]=board[p][q];
			}
			
			//evaluate this move
			remove(temp,move[1],move[2]);
			fall(temp);
			
			int[] t=MinValue(temp,points+move[0],alpha,beta,layer+1,max_depth);
			if(t[0]>alpha){
				alpha=t[0];
				result[1]=move[1];result[2]=move[2];
			}
			if(alpha>=beta) {
				result[0]=beta;
				return result;
			}
	    }
		result[0]=alpha;
		return result;
	
	}


	public static int[] MinValue2(char[][] board,int points,int alpha,int beta,int layer,int max_depth){
		int[] result=new int[3];
		if(GameOver(board,layer,max_depth)) {
			result[0]=points;
			result[1]=-1;
			result[2]=-1;
			return result;
		}
		
		
		// check for all possible move
		Queue<int[]> heap = new PriorityQueue<>(1, new Comparator<int[]>() {
			public int compare(int[] l1, int[] l2) {
				return l1[0] - l2[0];
			}
		});
	
		boolean[][] check_list = new boolean[size][size];
		step = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (board[i][j] != '*' && !check_list[i][j]) {
					step++;
					// evaluate this move
					int point = evaluate(board, check_list, i, j);
					heap.offer(new int[] { point, i, j });
				}
			}
		}
		int[] best = heap.peek();
		result[0] = best[0];
		result[1] = best[1];
		result[2] = best[2];
	
		
	
		while (!heap.isEmpty()) {
			int[] move = heap.poll();
	
			char[][] temp = new char[size][size];
			for (int p = 0; p < size; p++) {
				for (int q = 0; q < size; q++)
					temp[p][q] = board[p][q];
			}
	
			// evaluate this move
			remove(temp, move[1], move[2]);
			fall(temp);
			
			int[] t = MaxValue2(temp, points + move[0], alpha, beta, layer + 1,max_depth);
			if(t[0]<beta){
				beta=t[0];
				result[1]=move[1];result[2]=move[2];
			}
			if(beta<=alpha) {
				result[0]=alpha;
				return result;
			}
		}
		result[0] = alpha;
		return result;
	
	}


	public static int evaluate(char[][] board,boolean[][] check_list,int row,int col){
		int point=0;
		char type=board[row][col];
		
		Queue<Point> queue = new LinkedList<>();
		queue.add(new Point(row,col));
		check_list[row][col]=true;
		while (!queue.isEmpty()) {
			Point p = queue.poll();
			int i = p.x, j = p.y;
			point++;

			if (i + 1 < board.length && board[i + 1][j] == type&&!check_list[i+1][j]){
				queue.add(new Point(i + 1, j));
				check_list[i+1][j]=true;
			}
			if (i - 1 >= 0 && board[i - 1][j] == type&&!check_list[i-1][j]){
				queue.add(new Point(i - 1, j));
				check_list[i-1][j]=true;
			}
			if (j + 1 < board[0].length && board[i][j + 1] == type&&!check_list[i][j+1]){
				queue.add(new Point(i, j + 1));
				check_list[i][j+1]=true;
			}
			if (j - 1 >= 0 && board[i][j - 1] == type&&!check_list[i][j-1]){
				queue.add(new Point(i, j - 1));
				check_list[i][j-1]=true;
			}

		}
		
		//test
		//System.out.println(point);

		return point*point;
	}
	
	public static boolean GameOver(char[][] board,int layer,int max_depth){
		//test
		//System.out.println("layer"+layer);
		end_depth=Math.max(end_depth, layer);
		
		//if timeout
		long endTime=System.currentTimeMillis();
		if(endTime-startTime>time*1000-1000) return true;
		
		//if time enough for next layer
		if(layer==max_depth) return true;

		
		//if board is cleared
		for(int j=0;j<size;j++){
			if(board[size-1][j]!='*') return false;
		}
		
		
		
		return true;
	}
	
	
	public static void remove(char[][] board, int row,int col){
		Queue<Point> queue = new LinkedList<>();
		char type=board[row][col];
		
		queue.add(new Point(row,col));
		board[row][col]='*';
		while (!queue.isEmpty()) {
			Point p = queue.poll();
			int i = p.x, j = p.y;

			if (i + 1 < board.length && board[i + 1][j] == type){
				queue.add(new Point(i + 1, j));
				board[i+1][j]='*';
			}
			if (i - 1 >= 0 && board[i - 1][j] == type){
				queue.add(new Point(i - 1, j));
				board[i-1][j]='*';
			}
			if (j + 1 < board[0].length && board[i][j + 1] == type){
				queue.add(new Point(i, j + 1));
				board[i][j+1]='*';
			}
			if (j - 1 >= 0 && board[i][j - 1] == type){
				queue.add(new Point(i, j - 1));
				board[i][j-1]='*';
			}
		}
	}
	
	public static void fall(char[][] board){
		for(int j=0;j<board.length;j++){
			int empty=0,i=board.length-1;
			while(i>=0){
				if(board[i][j]!='*') i--;
				else{
					while(i>=0 && board[i][j]=='*') {
						empty++;
						i--;
					}
					while(i>=0 && board[i][j]!='*') {
						board[i+empty][j]=board[i][j];
						board[i][j]='*';
						i--;
					}
				}
			}
		}
	}

}
