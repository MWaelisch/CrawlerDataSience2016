package model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by clem on 04/06/16.
 */
public class VipMatrix {

    private static int FRIENDSHIPVALUE = 100;
    private static int MENTIONVALUE = 2;
    private static int REPLYVALUE = 10;
    private static int RETWEETVALUE = 20;

    private Map<Long, Integer> vipIdMap;
    private ArrayList<Vip> vips;


    private int[][] vipRelationMatrix;


    public VipMatrix(ArrayList<Vip> vips, Map<Long, Integer> vipIdMap){
        this.vips = vips;
        this.vipIdMap = vipIdMap;
        vipRelationMatrix = new int[vips.size()][vips.size()];
    }

    //calculate RelationRow for a VIP

    public void calculateFriendships(){
        for(int i = 0; i < vips.size(); i++){
            for(long friend : vips.get(i).getFriends()){
                if(vipIdMap.containsKey(friend)){
                    vipRelationMatrix[i][vipIdMap.get(friend)] += FRIENDSHIPVALUE;
                }
            }
        }

        //debugging
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

        //debugging
        System.out.println("Combined");
        System.out.println(Arrays.toString(vipRelationMatrix[2]));
        System.out.println(Arrays.toString(vipRelationMatrix[3]));

    }

    //check for each vipTweet if somebody is mentioned/replied/retweeted and apply Value multiplied with sentiment to Relationship
    public void calculateMentions(){
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
        System.out.println("VIP "+i+": ,"+Arrays.toString(vipRelationMatrix[i]));

        }
    }

    public int[][] getVipRelationMatrix() {
        return vipRelationMatrix;
    }

    public void writeToCsv(){

        try {
            File file = new File("./resources/relationshipMatrix.csv");
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
                line.setLength(0);
                line.append(Arrays.toString(vipRelationMatrix[count]));
                line.replace(0,1,"");
                line.setLength(line.length()-1);
                line.insert(0,vip.getUserName()+",");
                line.append("\n");
                writer.write(line.toString());
                count++;

            }

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





}
