package datacrunch;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import model.Pleb;
import model.Vip;
import model.VipMatrix;
import util.Database;

public class CrunchMain {

    //TODO put to the right place (maybe a config or similar)
    private static float F_WEIGHT = 10;
    private static float M_WEIGHT = 1;
    private static float RT_WEIGHT = 2;
    private static float RESP_WEIGHT = 2;

    public static void main(String[] args) {






        Database database;
        ArrayList<Vip> vips;
        Map<Long, Integer> vipIdMap = new HashMap<>();
        ArrayList<Pleb> plebs;

        try {
            database = new Database("resources/hiphopData.db");

            // TODO make LOAD VIPS and LOAD PLEBS in extra class and maybe a bit nicer
            // ----------- LOAD VIPS -------------
            File vipFile = new File("resources/savedVips.txt");
            //check if savedVips already exists
            if (vipFile.isFile() && vipFile.canRead()) {
                //load from file
                System.out.println("Start reading from file at: " + vipFile.getCanonicalPath());
                ObjectInputStream vipInputStream = new ObjectInputStream(
                        new FileInputStream(vipFile));

                vipIdMap = (Map<Long, Integer>) vipInputStream.readObject();
                vips = (ArrayList<Vip>) vipInputStream.readObject();


                vipInputStream.close();
            } else {
                //load from DB and write file
                int count = 0;
                vips = database.getAllVIPsfromDB();
//				vips = database.getNVipsFromDB(20); //DEBUGGING HELPER

                // initialise Vips and Helpers
                for (Vip vip : vips) {
                    vipIdMap.put(vip.getId(), count);
                    vip.setTweets(database.getVipTweets(vip.getId()));
                    vip.setFriends(database.getVipFriends(vip.getId()));
                    count++;
                    System.out.println("Creating VIP no " + count);
                }


                System.out.println("Start writing to file at: " + vipFile.getCanonicalPath());
                ObjectOutputStream vipOutputStream = new ObjectOutputStream(new FileOutputStream(vipFile));

                vipOutputStream.writeObject(vipIdMap);
                vipOutputStream.writeObject(vips);

                vipOutputStream.flush();
                vipOutputStream.close();

            }


//			// -------- LOAD PLEBS --------
            File plebFile = new File("resources/savedPlebs.txt");
            if (plebFile.isFile() && plebFile.canRead()) {
                //load from file
                System.out.println("Start reading from file at: " + plebFile.getCanonicalPath());

                ObjectInputStream plebInputStream = new ObjectInputStream(
                        new FileInputStream(plebFile));

                plebs = (ArrayList<Pleb>) plebInputStream.readObject();

                plebInputStream.close();
            } else {
                //load from DB and write file
                plebs = database.getAllPlebsfromDB();
//			    plebs = database.getNPlebsfromDB(20); //DEBUGGING HELPER

                System.out.println("Start writing to file at: " + plebFile.getCanonicalPath());

                ObjectOutputStream plebOutputStream = new ObjectOutputStream(new FileOutputStream(plebFile));

                plebOutputStream.writeObject(plebs);

                plebOutputStream.flush();
                plebOutputStream.close();

            }
//
//			//-------- MATRIX CALCULATIONS-------
			VipMatrix vipMatrix = new VipMatrix(plebs, vips, vipIdMap);
			vipMatrix.calculateVipFriendships();
			vipMatrix.calculateVipMentions();
//			vipMatrix.calculatePlebFriendships();
//			vipMatrix.calculatePlebMentions();
            vipMatrix.generateMatrix(F_WEIGHT,M_WEIGHT,RT_WEIGHT,RESP_WEIGHT);
//
            System.out.println(vips.get(6).getScreenName());
            System.out.println(Arrays.toString(vips.get(6).getFriends()));
			vipMatrix.writeToCsv("normMatrix");

            System.out.println("Finished");
        } catch (ClassNotFoundException | SQLException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
