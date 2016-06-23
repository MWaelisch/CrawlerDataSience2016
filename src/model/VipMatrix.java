package model;

import com.sun.tools.doclets.formats.html.SourceToHTMLConverter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by clem on 04/06/16.
 */
public class VipMatrix {

    private static float FRIENDSHIPVALUE = 150; //TODO make averages from all DB!
    private static float MENTIONVALUE = 1000;
    private static float REPLYVALUE = 100;
    private static float RETWEETVALUE = 200;

    private static float PLEBMENTIONVALUE = 1;
    private static float PLEBFRIENDVALUE = 1;
    private static float RETWEETORIGIN = -1;
    private static float RETWEETMENTION = 1;


    private Map<Long, Integer> vipIdMap;
    private ArrayList<Vip> vips;
    private ArrayList<Pleb> plebs;


    private float[][] vipFriendsMatrix;
    private float[][] vipMentionsMatrix;
    private float[][] vipRetweetMatrix;
    private float[][] vipReplyMatrix;


    private int[][] vipRelationMatrix;


    public VipMatrix(ArrayList<Pleb> plebs,ArrayList<Vip> vips, Map<Long, Integer> vipIdMap){
        this.plebs = plebs;
        this.vips = vips;
        this.vipIdMap = vipIdMap;
        vipRelationMatrix = new int[vips.size()][vips.size()];
        vipFriendsMatrix  = new float[vips.size()][vips.size()];
        vipMentionsMatrix = new float[vips.size()][vips.size()];
        vipRetweetMatrix = new float[vips.size()][vips.size()];
        vipReplyMatrix = new float[vips.size()][vips.size()];
    }

    //calculate RelationRow for a VIP and norm
    public void calculateVipFriendships(){
        for(int i = 0; i < vips.size(); i++){
            long[] vipFriends = vips.get(i).getFriends();

            for(long friend : vipFriends){
                if(vipIdMap.containsKey(friend)){
                    vipFriendsMatrix[i][vipIdMap.get(friend)] += FRIENDSHIPVALUE/(float) vipFriends.length;
                }
            }
        }

        //TODO check if needed/if better results without
        //combine friend-Relations
        for(int row = 0; row < vips.size(); row++){
            for(int col = 0; col < vips.size(); col++){
                if(col < row){
                    vipFriendsMatrix[row][col] += vipFriendsMatrix[col][row];
                    vipFriendsMatrix[col][row]  = vipFriendsMatrix[row][col];
                }else if(col == row) { //TODO check if selfFRIENDSHIP makes sense in Graph
                    vipFriendsMatrix[row][col] = FRIENDSHIPVALUE;
                }

            }
        }

    }
    //check for each vipTweet if somebody is mentioned/replied/retweeted and apply Value multiplied with sentiment to Relationship
    public void calculateVipMentions(){
        float mCount, rtCount, repCount;
        float tempVal;

        for(int row = 0; row < vips.size(); row++){

            //initialise counter to sum up given mentions/retweets/replies of each VIP
            mCount   = 0;
            rtCount  = 0;
            repCount = 0;

            for(VipTweet tweet : vips.get(row).getTweets()){

                //add mentions
                for(long mention : tweet.getMentions()){
                    if(vipIdMap.containsKey(mention)){
                        tempVal = MENTIONVALUE*tweet.getSentimentPos();//TODO make generic for Pos/NEG
                        vipMentionsMatrix[row][vipIdMap.get(mention)] += tempVal;
                        mCount += tempVal;
                    }
                }
                //add RetweetValue
                if(vipIdMap.containsKey(tweet.getRetweetOrigin())){
                    tempVal = RETWEETVALUE*tweet.getSentimentPos();//TODO make generic for Pos/NEG
                    vipRetweetMatrix[row][vipIdMap.get(tweet.getRetweetOrigin())] += tempVal;
                    rtCount += tempVal;
                }
                //add ReplyValue
                if(vipIdMap.containsKey(tweet.getInReplyTo())){
                    tempVal = REPLYVALUE*(float)tweet.getSentimentPos();//TODO make generic for Pos/NEG
                    vipReplyMatrix[row][vipIdMap.get(tweet.getInReplyTo())] += tempVal;
                    repCount += tempVal;
                }
            }

            //norm the row
            for (int col = 0; col < vips.size(); col++) {
                vipMentionsMatrix[row][col] /= mCount;
                vipRetweetMatrix[row][col] /= rtCount;
                vipReplyMatrix[row][col] /= repCount;

            }


        }



    }
    
    /**
     * Pleb erwähnt vip und hat vip freund -> erhöhe Freundschaftswert für beide Vips
     */
    public void calculatePlebMentions(){
        float tweetOrigin;
        for(Pleb pleb : plebs){
            for(Tweet tweet : pleb.getTweets()){
                for(long mention : tweet.getMentions()){

                    //check for retweetOrigin
                    if(mention < 0) {
                        tweetOrigin = RETWEETORIGIN;
                        mention = Math.abs(mention);
                    }else{
                        tweetOrigin = RETWEETMENTION;
                    }

                    for(long friend : pleb.getFriends()){
                        if(vipIdMap.containsKey(mention) && vipIdMap.containsKey(friend) && mention != friend){
                            vipRelationMatrix[vipIdMap.get(friend)][vipIdMap.get(mention)] += tweet.getSentiment()*PLEBMENTIONVALUE*tweetOrigin;
                            vipRelationMatrix[vipIdMap.get(mention)][vipIdMap.get(friend)] += tweet.getSentiment()*PLEBMENTIONVALUE*tweetOrigin;
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



    public void generateMatrix(float fWeight, float mWeight, float rtWeight, float respWeight){
        for(int row = 0; row < vips.size(); row++) {
            for (int col = 0; col < vips.size(); col++) {
                vipRelationMatrix[row][col] = (int)
                        (
                                (vipFriendsMatrix[row][col] * fWeight  +
                                vipMentionsMatrix[row][col] * mWeight +
                                vipRetweetMatrix[row][col] * rtWeight +
                                vipReplyMatrix[row][col] * respWeight) //TODO check if this is working with 100
                                //TODO eliminate magic number 100
                        );

            }
        }
    }


    public int[][] getVipRelationMatrix() {
        return vipRelationMatrix;
    }

    public void writeToCsv(String fileName){
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
            System.out.println("Finished writing to file at: "+file.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }






}
