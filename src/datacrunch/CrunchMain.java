package datacrunch;

import java.util.ArrayList;

import java_cup.runtime.virtual_parse_stack;
import model.Vip;
import util.Database;

public class CrunchMain {
	
	
	public static void main(String [] args){
		
		Database database = new Database();
		ArrayList<Vip> vips = database.getAllVIPsfromDB();
		int count = 0;
		for(Vip vip : vips){
			vip.setTweets(database.getVipTweets(vip.getId()));
			vip.setFriends(database.getVipFriends(vip.getId()));
			count++;
			System.out.println(count);
		}
		
		System.out.println("Ready");
	}

}
