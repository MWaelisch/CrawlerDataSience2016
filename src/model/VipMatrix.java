package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by clem on 04/06/16.
 */
public class VipMatrix {

    private static int FRIENDSHIPVALUE = 10;

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




}
