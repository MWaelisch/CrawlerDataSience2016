package datacrunch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import java_cup.runtime.virtual_parse_stack;
import model.Vip;
import model.VipMatrix;
import util.Database;

public class CrunchMain {

	public static void main(String [] args){


		Database database = new Database();
		//For testing usage of getNVips
// 		ArrayList<Vip> vips = database.getAllVIPsfromDB();
		ArrayList<Vip> vips = database.getNVipsFromDB(4);


		Map<Long, Integer> vipIdMap = new HashMap<>();
		int count = 0;

		//initialise Vips and Helpers
		for(Vip vip : vips){
			vipIdMap.put(vip.getId(),count);
			vip.setTweets(database.getVipTweets(vip.getId()));
			vip.setFriends(database.getVipFriends(vip.getId()));
			count++;

			System.out.println(count);
		}
		System.out.println(vipIdMap);


		VipMatrix vipMatrix = new VipMatrix(vips,vipIdMap);
		vipMatrix.calculateFriendships();
		vipMatrix.calculateMentions();

		vipMatrix.writeToCsv();


		System.out.println("Finished");
	}

}
