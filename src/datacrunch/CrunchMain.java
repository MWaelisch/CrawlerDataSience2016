package datacrunch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import java_cup.runtime.virtual_parse_stack;
import model.Vip;
import util.Database;

public class CrunchMain {
	private static int FRIENDSHIPVALUE = 10;
	private static int POSTINGVALUE = 1;
	private static int DEBUGGINGCOUNT = 5;


	public static void main(String [] args){
		
		Database database = new Database();
		ArrayList<Vip> vips = database.getAllVIPsfromDB();


		Map<Long, Integer> vipIdMap = new HashMap<>();
		int count = 0;

		//initialise Vips and Helpers
		for(Vip vip : vips){
			vipIdMap.put(vip.getId(),count);
			vip.setTweets(database.getVipTweets(vip.getId()));
			vip.setFriends(database.getVipFriends(vip.getId()));
			count++;

			if(count == DEBUGGINGCOUNT){
				break;
			}
			System.out.println(count);
		}
		System.out.println(vipIdMap);

		//calculate RelationRow for a VIP
		int[][] vipRelationMatrix = new int[vips.size()][vips.size()];

		for(int i = 0; i < vips.size(); i++){
			if(i >= DEBUGGINGCOUNT){
				break;
			}
			for(long friend : vips.get(i).getFriends()){
				if(vipIdMap.containsKey(friend)){
					vipRelationMatrix[i][vipIdMap.get(friend)] += FRIENDSHIPVALUE;
				}
			}
		}

		System.out.println(Arrays.toString(vipRelationMatrix[2]));
		System.out.println(Arrays.toString(vipRelationMatrix[3]));

		//combine friend-Relations
		for(int row =0; row<vips.size(); row++){
			for(int col=0; col<vips.size(); col++){
				if(col<row){
					vipRelationMatrix[row][col] += vipRelationMatrix[col][row];
					vipRelationMatrix[col][row]  = vipRelationMatrix[row][col];
				}
			}
		}

		System.out.println("Combined");
		System.out.println(Arrays.toString(vipRelationMatrix[2]));
		System.out.println(Arrays.toString(vipRelationMatrix[3]));


		System.out.println("Finished");
	}

}
