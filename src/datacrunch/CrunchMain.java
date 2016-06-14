package datacrunch;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import model.Pleb;
import model.Vip;
import model.VipMatrix;
import util.Database;

public class CrunchMain {

	public static void main(String [] args){

		Database database;
		try {
			database = new Database("dbname");

			// -------- VIPS --------
			ArrayList<Vip> vips = database.getAllVIPsfromDB();
			// ArrayList<Vip> vips = database.getNVipsFromDB(80);

			Map<Long, Integer> vipIdMap = new HashMap<>();
			int count = 0;

			// initialise Vips and Helpers
			for (Vip vip : vips) {
				vipIdMap.put(vip.getId(), count);
				vip.setTweets(database.getVipTweets(vip.getId()));
				vip.setFriends(database.getVipFriends(vip.getId()));
				count++;
				System.out.println("Creating VIP no " + count);
			}

			// -------- PLEBS --------
			ArrayList<Pleb> plebs = database.getAllPlebsfromDB();
			// ArrayList<Pleb> plebs = database.getNPlebsfromDB(100);


			//-------- MATRIX -------
			VipMatrix vipMatrix = new VipMatrix(plebs, vips, vipIdMap);
			vipMatrix.calculateVipFriendships();
			vipMatrix.calculateVipMentions();
			vipMatrix.calculatePlebFriendships();
			vipMatrix.calculatePlebMentions();

			vipMatrix.writeToCsv("vipLio");

			System.out.println("Finished");
		} catch (ClassNotFoundException | SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
