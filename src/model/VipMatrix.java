package model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by clem on 04/06/16.
 */
public class VipMatrix {

    private static int FRIENDSHIPVALUE = 100;
    private static int MENTIONVALUE = 2;
    private static int REPLYVALUE = 10;
    private static int RETWEETVALUE = 20;
    private static int PLEBMENTIONVALUE = 2;
    private static int PLEBFRIENDVALUE = 1;

    private Map<Long, Integer> vipIdMap;
    private ArrayList<Vip> vips;
    private ArrayList<Pleb> plebs;


    private int[][] vipRelationMatrix;


    public VipMatrix(ArrayList<Pleb> plebs,ArrayList<Vip> vips, Map<Long, Integer> vipIdMap){
        this.plebs = plebs;
        this.vips = vips;
        this.vipIdMap = vipIdMap;
        vipRelationMatrix = new int[vips.size()][vips.size()];
    }

    //calculate RelationRow for a VIP

    public void calculateVipFriendships(){
        for(int i = 0; i < vips.size(); i++){
            for(long friend : vips.get(i).getFriends()){
                if(vipIdMap.containsKey(friend)){
                    vipRelationMatrix[i][vipIdMap.get(friend)] += FRIENDSHIPVALUE;
                }
            }
        }

        //combine friend-Relations
        for(int row =0; row<vips.size(); row++){
            for(int col=0; col<vips.size(); col++){
                if(col<row){
                    vipRelationMatrix[row][col] += vipRelationMatrix[col][row];
                    vipRelationMatrix[col][row]  = vipRelationMatrix[row][col];
                }
            }
        }

    }
    //check for each vipTweet if somebody is mentioned/replied/retweeted and apply Value multiplied with sentiment to Relationship
    public void calculateVipMentions(){
        for(int i = 0; i < vips.size(); i++){
            for(VipTweet tweet : vips.get(i).getTweets()){
                //add mentions
                for(long mention : tweet.getMentions()){
                    if(vipIdMap.containsKey(mention)){
                        vipRelationMatrix[i][vipIdMap.get(mention)] += MENTIONVALUE*tweet.getSentiment();
                    }
                }
                //add ReplyValue
                if(vipIdMap.containsKey(tweet.getInReplyTo())){
                    vipRelationMatrix[i][vipIdMap.get(tweet.getInReplyTo())] += REPLYVALUE*tweet.getSentiment();
                }
                //add RetweetValue
                if(vipIdMap.containsKey(tweet.getRetweetOrigin())){
                    vipRelationMatrix[i][vipIdMap.get(tweet.getRetweetOrigin())] += RETWEETVALUE*tweet.getSentiment();
                }


            }
            //System.out.println("VIP "+i+": ,"+Arrays.toString(vipRelationMatrix[i]));
        }
    }
    
    /**
     * Pleb erwähnt vip und hat vip freund -> erhöhe Freundschaftswert für beide Vips
     */
    public void calculatePlebMentions(){
        for(Pleb pleb : plebs){
            for(Tweet tweet : pleb.getTweets()){
                for(long mention : tweet.getMentions()){
                    for(long friend : pleb.getFriends()){
                        if(vipIdMap.containsKey(mention) && vipIdMap.containsKey(friend) && mention != friend){
                            vipRelationMatrix[vipIdMap.get(friend)][vipIdMap.get(mention)] += tweet.getSentiment()*PLEBMENTIONVALUE;
                            vipRelationMatrix[vipIdMap.get(mention)][vipIdMap.get(friend)] += tweet.getSentiment()*PLEBMENTIONVALUE;
                        }
                    }
                }
            }
        }
    }

    /**
     * Pleb hat zwei verschiedene vip freunde 
     */
    public void calculatePlebFriendships() {
        for (Pleb pleb : plebs) {
            for (long friend1 : pleb.getFriends()) {
                for(long friend2: pleb.getFriends()){
                    if(friend1 != friend2){
                        vipRelationMatrix[vipIdMap.get(friend1)][vipIdMap.get(friend2)] += PLEBFRIENDVALUE;
//                        vipRelationMatrix[vipIdMap.get(friend2)][vipIdMap.get(friend1)] += PLEBFRIENDVALUE;
                    }
                }

            }
        }
    }



    public int[][] getVipRelationMatrix() {
        return vipRelationMatrix;
    }

    public void writeToCsv(String fileName){

    	//testing
    	HashSet specialVips = new HashSet();
    	Set<String> vipSet = new HashSet<String>(Arrays.asList(
    			"Selfmade Records","Kollegah","257ers","GENETIKK","SHINDY","Bushido",
    			"sido","BASS SULTAN HENGZT","3Plusss","CRO","PSAIKODINO","DANJU DANJU",
    			"KAAS","Tua Tolstoi","Bartek","Maeckes"
    			));
        try {
            File file = new File("./resources/"+fileName+".csv");
            file.createNewFile();
            FileWriter writer = new FileWriter(file);

            // Writes the content to the file
            StringBuilder line = new StringBuilder("label");
            for(int i = 0; i < vips.size(); i++){
                line.append(",var"+i);
            }
            writer.write(line.toString()+"\n");

            int count = 0;
            for(Vip vip : vips){
//            	if(vipSet.contains(vip.getUserName())){
                    line.setLength(0);
                    line.append(Arrays.toString(vipRelationMatrix[count]));
                    line.replace(0,1,"");
                    line.setLength(line.length()-1);
                    line.insert(0,vip.getUserName()+",");
                    line.append("\n");
                    writer.write(line.toString());
                    count++;
//            	}
            }

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }






}
