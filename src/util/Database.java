package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import model.*;

public class Database {
	private Connection conn;
	
	public Database(){
		
    	// register the driver 
        String sDriverName = "org.sqlite.JDBC";
        try {
			Class.forName(sDriverName);
			// create a database connection
			Properties properties = new Properties();
			properties.setProperty("PRAGMA foreign_keys", "ON");
			conn = DriverManager.getConnection("jdbc:sqlite:resources/twitterData.db",properties);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	public void closeConnection(){
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
		
	/**
	 * 
	 * @param vip
	 */
	public void addVip(Vip vip){
		addVipUserdata(vip);
		addVipFriends(vip);
	}
	
	private void addVipUserdata(Vip vip){
		PreparedStatement preparedStatement = null;

		String insertTableSQL = "INSERT INTO vip"
				+ "(id, screenName, userName, followerCount,profilePicture) VALUES"
				+ "(?,?,?,?,?)";

		try {
			preparedStatement = conn.prepareStatement(insertTableSQL);

			preparedStatement.setLong(1, vip.getId());
			preparedStatement.setString(2, vip.getScreenName());
			preparedStatement.setString(3, vip.getUserName());
			preparedStatement.setInt(4, vip.getFollowerCount());
			preparedStatement.setString(5, vip.getProfilePicture());

			// execute insert SQL statement
			preparedStatement.executeUpdate();

			System.out.println("VIP Inserted!");
		}catch (SQLException e) {

			System.out.println(e.getMessage());

		} finally {

			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void addVipFriends(Vip vip){
		PreparedStatement preparedStatement = null;

		String insertTableSQL = "INSERT INTO vipFriends"
				+ "(vip,friend) VALUES"
				+ "(?,?)";

		try {
			preparedStatement = conn.prepareStatement(insertTableSQL);
			
			for(long friend : vip.getFriends()){
				preparedStatement.setLong(1, vip.getId());
				preparedStatement.setLong(2, friend);
				// execute insert SQL statement
				preparedStatement.executeUpdate();
				preparedStatement.clearParameters();
			}

		}catch (SQLException e) {

			System.out.println(e.getMessage());

		} finally {

			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public void addVipTweet(VipTweet vipTweet){
		addVipTweetData(vipTweet);
		if(vipTweet.getGeneratedId() != 0){
			addVipTweetMentions(vipTweet);
		}else{
			try {
				throw new Exception("VIP Tweet database Insert returned 0");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
	private void addVipTweetData(VipTweet vipTweet){
		PreparedStatement preparedStatement = null;

		String insertTableSQL = "INSERT INTO vipTweets"
				+ "(authorId, authorName, idStr, inReplyTo, retweetOrigin,text) VALUES"
				+ "(?,?,?,?,?,?)";

		try {
			preparedStatement = conn.prepareStatement(insertTableSQL,Statement.RETURN_GENERATED_KEYS);

			
			preparedStatement.setLong(1, vipTweet.getAuthorId());
			preparedStatement.setString(2, vipTweet.getScreenName());
			preparedStatement.setString(3, vipTweet.getIdStr());
			preparedStatement.setLong(4, vipTweet.getInReplyTo());
			preparedStatement.setLong(5, vipTweet.getRetweetOrigin());
			preparedStatement.setString(6, vipTweet.getText());

			// execute insert SQL statement
			preparedStatement.executeUpdate();

			ResultSet rs = preparedStatement.getGeneratedKeys();
			if (rs.next()) {
			  int generatedId = rs.getInt(1);
			  vipTweet.setGeneratedId(generatedId);
			}
		}catch (SQLException e) {

			System.out.println(e.getMessage());

		} finally {

			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void addVipTweetMentions(VipTweet vipTweet){
		PreparedStatement preparedStatement = null;

		String insertTableSQL = "INSERT INTO vipTweetMentions"
				+ "(vipTweetId, mention) VALUES"
				+ "(?,?)";

		try {
			preparedStatement = conn.prepareStatement(insertTableSQL);
			for(long mention : vipTweet.getMentions()){
				preparedStatement.setInt(1,vipTweet.getGeneratedId());
				preparedStatement.setLong(2, mention);
				// execute insert SQL statement
				preparedStatement.executeUpdate();
				preparedStatement.clearParameters();
			}
		}catch (SQLException e) {

			System.out.println(e.getMessage());

		} finally {

			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
	}
	
	public long getVipID(String screenName){
		try{
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery( "SELECT * FROM vip WHERE screenName = '" + screenName + "';" );
			//fkt??
			if (rs.next()) {
			    return rs.getLong("ID");
			}
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return 0;
	}
	
	public void addPlebTweet(Tweet plebTweet, long vipId){
		addPlebTweetData(plebTweet);
		if(plebTweet.getGeneratedId() != 0){
			PreparedStatement preparedStatement = null;

			String insertTableSQL = "INSERT INTO plebTweetMentions"
					+ "(plebTweetId, mention) VALUES"
					+ "(?,?)";
			try {
				preparedStatement = conn.prepareStatement(insertTableSQL);
				preparedStatement.setInt(1,plebTweet.getGeneratedId());
				preparedStatement.setLong(2, vipId);
				
				// execute insert SQL statement
				preparedStatement.executeUpdate();
				preparedStatement.clearParameters();
			}catch (SQLException e) {

				System.out.println(e.getMessage());

			} finally {

				if (preparedStatement != null) {
					try {
						preparedStatement.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}else{
			try {
				throw new Exception("VIP Tweet database Insert returned 0");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void addPlebTweetData(Tweet plebTweet){
		PreparedStatement preparedStatement = null;

		String insertTableSQL = "INSERT INTO plebTweets"
				+ "(idStr, text, authorId) VALUES"
				+ "(?,?,?)";

		try {
			preparedStatement = conn.prepareStatement(insertTableSQL);

			preparedStatement.setString(1, plebTweet.getIdStr());
			preparedStatement.setString(2, plebTweet.getText());
			preparedStatement.setLong(3, plebTweet.getAuthorId());

			// execute insert SQL statement
			preparedStatement.executeUpdate();
			
			ResultSet rs = preparedStatement.getGeneratedKeys();
			if (rs.next()) {
			  int generatedId = rs.getInt(1);
			  plebTweet.setGeneratedId(generatedId);
			}
		}catch (SQLException e) {

			System.out.println(e.getMessage());

		} finally {

			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void addPlebFriend(PlebFriend plebFriend){
		PreparedStatement preparedStatement = null;

		String insertTableSQL = "INSERT INTO plebFriends"
				+ "(pleb, friend) VALUES"
				+ "(?,?)";

		try {
			preparedStatement = conn.prepareStatement(insertTableSQL);

			preparedStatement.setLong(1, plebFriend.getId());
			preparedStatement.setLong(2, plebFriend.getFriend());

			// execute insert SQL statement
			preparedStatement.executeUpdate();

			//System.out.println("PlebFriend Inserted!");
		}catch (SQLException e) {

			System.out.println(e.getMessage());

		} finally {

			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean isIDInDB(long id, String idName, String db){
		try{
			Statement statement = conn.createStatement();

			ResultSet rs = statement.executeQuery( "SELECT " + idName + " FROM " + db + " WHERE " + idName + " = " + id + ";" );

			if (rs.next()) {
				//long getid =
			    rs.getInt(idName);
			    if (rs.wasNull()) {
					return false;
			    } else return true;
			}
			
			statement.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return false;
	}

	//debugging and testing
	public String executeQuery(String query){
		String r = "";
//		String selectPlebMentions =  "SELECT authorId, COUNT(pm.mention) AS friendcnt "//"DELETE pm, pt "
//				+ "FROM plebTweetMentions pm "
//				+ "JOIN plebTweets pt ON pm.plebTweetId = pt.id "
//				+ "JOIN plebFriends pf ON pt.authorId = pf.pleb "
//				+ "WHERE pf.friend = 0 "
//				+ "GROUP BY pt.authorId HAVING COUNT(pm.mention) >= 2";
		try{
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(query );

			while (rs.next()) {
				//long getid =
			    r += //"#-#-#-#-#" + rs.getString("text")+ "\n";
			    		"::" + rs.getLong("plebTweetid")+ "\n";
			    		// + " " + rs.getInt("friend") +"\n";
//			    		":: " + rs.getLong("authorId") + " # " + rs.getInt("friendcnt") + "\n";
			}

			statement.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		System.out.println("result: " + r);
		return r;
	}

	public ArrayList<Vip> getNVipsFromDB(int numberOfVips){

		ArrayList<Vip> vips = new ArrayList<Vip>();
		try{
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT * FROM vip ORDER BY id ASC;");
			int count = 0;
			while (rs.next()) {
				Vip vip = new Vip();

				vip.setId(rs.getLong("id"));
				vip.setScreenName(rs.getString("screenName"));
				vip.setUserName(rs.getString("userName"));
				vip.setFollowerCount(rs.getInt("followerCount"));
				vip.setProfilePicture(rs.getString("profilePicture"));

				vips.add(vip);
				count++;
				if(count == numberOfVips){
					break;
				}

			}

			statement.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return vips;
	}

	public ArrayList<Vip> getAllVIPsfromDB(){
		ArrayList<Vip> vips = new ArrayList<Vip>();
		try{
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT * FROM vip ORDER BY id ASC;");
			
			while (rs.next()) {
				Vip vip = new Vip();
				
				vip.setId(rs.getLong("id"));
				vip.setScreenName(rs.getString("screenName"));
				vip.setUserName(rs.getString("userName"));
				vip.setFollowerCount(rs.getInt("followerCount"));
				vip.setProfilePicture(rs.getString("profilePicture"));
				
				vips.add(vip);
			}
			
			statement.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return vips;
	}

    public ArrayList<Pleb> getAllPlebsfromDB(){
        HashMap<Long,Pleb> plebMap = new HashMap<>();
        ArrayList<Long> mentionsList = new ArrayList<Long>();
        ArrayList<Long> friendsList = new ArrayList<Long>();
        int tweetId;
        long plebId;
        try{
            Statement plebStatement = conn.createStatement();
            Statement tweetStatement = conn.createStatement();
			Statement friendStatement = conn.createStatement();

            ResultSet rsPleb = plebStatement.executeQuery("SELECT * FROM plebTweets ORDER BY authorId ASC;");

            while (rsPleb.next()) {
                plebId = rsPleb.getLong("authorId");
				ResultSet rsFriends = friendStatement.executeQuery("SELECT  * FROM plebFriends WHERE pleb = "+ plebId +";");
                //check for all mentions in this tweet
                tweetId = rsPleb.getInt("id");
                ResultSet rsMention = tweetStatement.executeQuery("SELECT * FROM plebTweetMentions WHERE plebTweetId = " + tweetId + ";");

                //construct tweet
                Tweet tweet = new Tweet();
                tweet.setAuthorId(plebId);
                tweet.setSentimentPos(rsPleb.getInt("sentimentPos"));
                tweet.setSentimentNeg(rsPleb.getInt("sentimentNeg"));

                //add mentions to tweet
                mentionsList.clear();
                while(rsMention.next()){
                    mentionsList.add(rsMention.getLong("mention"));
                }
                Long[] mentionsArr = new Long[mentionsList.size()];
                mentionsArr = mentionsList.toArray(mentionsArr);
                tweet.setMentions(mentionsArr);

				//get friendsArray for Pleb
				friendsList.clear();
				while(rsFriends.next()){
					friendsList.add(rsFriends.getLong("friend"));
				}
				Long[] friendsArr = new Long[friendsList.size()];
				friendsArr = friendsList.toArray(friendsArr);

                //check if pleb already existing and add tweet, else create
                if(plebMap.containsKey(plebId)){
                    plebMap.get(plebId).addTweet(tweet);
					plebMap.get(plebId).setFriends(friendsArr);
                }else{
                    Pleb pleb = new Pleb();
                    pleb.setId(plebId);
                    pleb.addTweet(tweet);
					pleb.setFriends(friendsArr);
                    plebMap.put(plebId,pleb);
                }
            }

            plebStatement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return new ArrayList<>(plebMap.values());
    }

    public ArrayList<Pleb> getNPlebsfromDB(int numberOfPlebs){
        HashMap<Long,Pleb> plebMap = new HashMap<>();
        ArrayList<Long> mentionsList = new ArrayList<Long>();
		ArrayList<Long> friendsList = new ArrayList<Long>();

		int tweetId;
        long plebId;
        try{
            Statement plebStatement = conn.createStatement();
            Statement tweetStatement = conn.createStatement();
			Statement friendStatement = conn.createStatement();

            ResultSet rsPleb = plebStatement.executeQuery("SELECT * FROM plebTweets ORDER BY authorId ASC;");


            int count = 0;
            while (rsPleb.next()) {

				if (count >= numberOfPlebs) {
					break;
				} else {
					System.out.println("Processing Pleb no: " + count);
					count++;
				}

				plebId = rsPleb.getLong("authorId");
				ResultSet rsFriends = friendStatement.executeQuery("SELECT  * FROM plebFriends WHERE pleb = " + plebId + ";");
				//check for all mentions in this tweet
				tweetId = rsPleb.getInt("id");
				ResultSet rsMention = tweetStatement.executeQuery("SELECT * FROM plebTweetMentions WHERE plebTweetId = " + tweetId + ";");

				//construct tweet
				Tweet tweet = new Tweet();
				tweet.setAuthorId(plebId);
				tweet.setSentimentPos(rsPleb.getInt("sentimentPos"));
				tweet.setSentimentNeg(rsPleb.getInt("sentimentNeg"));

				//add mentions to tweet
				mentionsList.clear();
				while (rsMention.next()) {
					mentionsList.add(rsMention.getLong("mention"));
				}
				Long[] mentionsArr = new Long[mentionsList.size()];
				mentionsArr = mentionsList.toArray(mentionsArr);
				tweet.setMentions(mentionsArr);

				//get friendsArray for Pleb
				friendsList.clear();
				while (rsFriends.next()) {
					friendsList.add(rsFriends.getLong("friend"));
				}
				Long[] friendsArr = new Long[friendsList.size()];
				friendsArr = friendsList.toArray(friendsArr);

				//check if pleb already existing and add tweet, else create
				if (plebMap.containsKey(plebId)) {
					plebMap.get(plebId).addTweet(tweet);
					plebMap.get(plebId).setFriends(friendsArr);
				} else {
					Pleb pleb = new Pleb();
					pleb.setId(plebId);
					pleb.addTweet(tweet);
					pleb.setFriends(friendsArr);
					plebMap.put(plebId, pleb);
				}
			}
            plebStatement.close();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return new ArrayList<>(plebMap.values());
    }


    public ArrayList<Tweet> getAllTweetsfromDB(String table){
		ArrayList<Tweet> ts = new ArrayList<Tweet>();
		try{
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT * FROM " + table + " ORDER BY id ASC;");
		
			while (rs.next()) {
				Tweet t = new Tweet();
				t.setAuthorId(rs.getLong("authorId"));
				t.setIdStr(rs.getString("idStr"));
				t.setText(rs.getString("text"));
				t.setGeneratedId(rs.getInt("id"));
				t.setSentimentPos(rs.getInt("sentimentPos"));
				t.setSentimentNeg(rs.getInt("sentimentNeg"));
				
				ts.add(t);
			}
			
			statement.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return ts;
	}
	
	public ArrayList<VipTweet> getVipTweets(long authorId){
		ArrayList<VipTweet> vipTweets = new ArrayList<VipTweet>();
		try{
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT * FROM vipTweets WHERE authorId=" + authorId + ";");
		
			while (rs.next()) {
				VipTweet t = new VipTweet();
				t.setAuthorId(rs.getLong("authorId"));
				t.setIdStr(rs.getString("idStr"));
				t.setText(rs.getString("text"));
				t.setGeneratedId(rs.getInt("id"));
				t.setSentimentPos(rs.getInt("sentimentPos"));
				t.setSentimentNeg(rs.getInt("sentimentNeg"));
				t.setInReplyTo(rs.getInt("inReplyTo"));
				t.setRetweetOrigin(rs.getInt("retweetOrigin"));
				vipTweets.add(t);
			}
		
			
			for(VipTweet tweet : vipTweets){
				
				rs = statement.executeQuery("SELECT COUNT(*) as count FROM VipTweetMentions WHERE vipTweetId=" + tweet.getGeneratedId());
				rs.next();
				int count = rs.getInt("count");
				
				
				
				Long[] mentions = new Long[count];
				rs = statement.executeQuery("SELECT * FROM VipTweetMentions WHERE vipTweetId=" + tweet.getGeneratedId());
				int i=0;
				while(rs.next()){
					mentions[i] = rs.getLong("mention");
					i++;
				}
				tweet.setMentions(mentions);	
			}
			statement.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return vipTweets;
	}
	
	public long[] getVipFriends(long vipId){
		
		try{
			
			Statement statement = conn.createStatement();
			
			ResultSet rs = statement.executeQuery("SELECT COUNT(*) as count FROM vipFriends WHERE vip=" + vipId);
			rs.next();
			int count = rs.getInt("count");
			
			long[] vipFriends=  new long[count];
			
			rs = statement.executeQuery("SELECT * FROM vipFriends WHERE vip=" + vipId);
		
			int i = 0;
			while (rs.next()) {
				vipFriends[i] = rs.getLong("friend");
				i++;
			}
			
			statement.close();
			return vipFriends;
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return null;
	}
	

	public void updateTweets(ArrayList<Tweet> tweets, String table){
		String sql;
		Statement stmt = null;

		for(Tweet t : tweets){
			sql = "UPDATE "+table+" " +
					"SET sentimentPos = "+t.getSentimentPos()+", sentimentNeg = "+t.getSentimentNeg()+
					" WHERE id ="+t.getGeneratedId();
			try {
				stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
	}
	
	public void cleanDB(){
		
		PreparedStatement preparedStatement = null;

		//Lösche alle viptweets die nicht bezug auf einen anderen vip nehmen
		String removeUnnecessaryVipTweets = "DELETE FROM vipTweets " +
											"WHERE retweetOrigin NOT IN (SELECT id FROM vip) " + 
											"AND inReplyTo NOT IN (SELECT id FROM vip)" +
											"AND NOT EXISTS (SELECT * FROM vipTweetMentions,vip WHERE vipTweetId=vipTweets.id AND mention=vip.id)";
		
		//lösche alle mentions, zu denen der tweet nichtmehr existiert-> siehe removeUnnecessaryVipTweets
		//und alle unnötigen mentions, also mentions die nicht auf einen vip verweisen
		String removeUnnecessaryVipTweetMentions="DELETE FROM vipTweetMentions " +
												 "WHERE vipTweetid NOT IN (SELECT id FROM vipTweets)" +
												 "OR mention NOT IN (SELECT id FROM vip)";

		//lösche alle freunde von vips die selbst keine vips sind
		String removeUnnecessaryVipFriends = "DELETE FROM vipFriends " +
											 "WHERE friend NOT IN (SELECT id FROM vip)";
		
		try {

			preparedStatement = conn.prepareStatement(removeUnnecessaryVipTweets);

			preparedStatement.executeUpdate();
			
			preparedStatement.close();
				
			preparedStatement = conn.prepareStatement(removeUnnecessaryVipTweetMentions);
			
			preparedStatement.executeUpdate();
			
			preparedStatement.close();
			
			preparedStatement = conn.prepareStatement(removeUnnecessaryVipFriends);
			
			preparedStatement.executeUpdate();
			
			preparedStatement.close();
			
		}catch (SQLException e) {

			System.out.println(e.getMessage());

		} finally {

			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	

	public void cleanNeutralSentiScorePlebTweets(){
		
		PreparedStatement preparedStatement = null;

		String removeUnnecessaryPlebTweets = "DELETE FROM plebTweets " +
											"WHERE sentimentPos = 1 AND sentimentNeg = -1";
		
		try {

			preparedStatement = conn.prepareStatement(removeUnnecessaryPlebTweets);

			preparedStatement.executeUpdate();
			
			preparedStatement.close();
		}catch (SQLException e) {

			System.out.println(e.getMessage());

		} finally {

			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void cleanProtectedPleb(long tweetId){
		
		PreparedStatement preparedStatement = null;

		String removeFromPlebTweets = "DELETE FROM plebTweets " +
									  "WHERE id = " + tweetId;
		
		String removeFromPlebTweetMentions = "DELETE FROM plebTweetMentions " +
											 "WHERE plebTweetId = " + tweetId;
		
		
		try {

			preparedStatement = conn.prepareStatement(removeFromPlebTweets);
			preparedStatement.executeUpdate();
			preparedStatement.close();
			
			System.out.println("deleted plebTweet");
			
			preparedStatement = conn.prepareStatement(removeFromPlebTweetMentions);
			preparedStatement.executeUpdate();
			preparedStatement.close();
			
			System.out.println("deleted plebTweetMention");
		}catch (SQLException e) {

			System.out.println(e.getMessage());

		} finally {

			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	//todo tweeten uU auch ueber andere Vips (aber anscheinend nicht viele)
	//todo stimmt removePlebTweet?
	public void cleanPlebFriends(){
		PreparedStatement preparedStatement = null;
		String removeFromPlebFriends = "DELETE FROM plebTweets " +
									  "WHERE friend = 0";
		
		String removePlebTweetOneMention = "DELETE FROM plebTweets "
				+ "WHERE id IN (SELECT id "
				+ "FROM plebTweetMentions pm "
				+ "JOIN plebTweets pt ON pm.plebTweetId = pt.id "
				+ "JOIN plebFriends pf ON pt.authorId = pf.pleb "
				+ "WHERE pf.friend = 0 "
				+ "GROUP BY pt.authorId HAVING COUNT(pm.mention) < 2)";
		
//		DELETE plebTweets, PlebTweetMentions,plebFriends FROM plebTweets JOIN plebTweetmentions ON plebTweetId = plebTweets.id JOIN plebFriends ON authorId = pleb  WHErE authorId IN (SELECT authorId
//        FROM plebTweetMentions pm 
//        JOIN plebTweets pt ON pm.plebTweetId = pt.id 
//        JOIN plebFriends pf ON pt.authorId = pf.pleb 
//        WHERE pf.friend = 0 
//        GROUP BY pt.authorId HAVING COUNT(pm.mention) >= 2) AND NOT text LIKE '%Youtube%'ORDER BY authorId
//        
//        
		String removeWithoutYoutube = "DELETE FROM plebTweets "
				+ "WHERE id IN (SELECT id "
				+ "FROM plebTweetMentions pm "
				+ "JOIN plebTweets pt ON pm.plebTweetId = pt.id "
				+ "WHERE authorId IN "
				+ "(SELECT authorId FROM plebTweetMentions pm "
				+ "JOIN plebTweets pt ON pm.plebTweetId = pt.id "
				+ "JOIN plebFriends pf ON pt.authorId = pf.pleb "
				+ "WHERE pf.friend=0 "
				+ "GROUP BY pt.authorId HAVING COUNT(pm.mention) >= 2) AND NOT text LIKE '%Youtube%')";
//DELETE FROM plebTweets WHERE authorId IN (SELECT authorId
//    FROM plebTweetMentions pm 
//    JOIN plebTweets pt ON pm.plebTweetId = pt.id 
//    JOIN plebFriends pf ON pt.authorId = pf.pleb 
//    WHERE pf.friend=0 
//    GROUP BY pt.authorId HAVING COUNT(pm.mention) >= 2) AND NOT text LIKE '%Youtube%';
//    
//DELETE FROM plebTweetMentions WHERE plebTweetId NOT IN (SELECT id FROM plebTweets);
		

		String removeSuspendedOrDeleted = "DELETE FROM plebTweets "
				+ "WHERE id IN (SELECT id "
				+ "FROM plebTweetMentions pm "
				+ "JOIN plebTweets pt ON pm.plebTweetId = pt.id "
				+ "WHERE pt.authorId NOT IN (SELECT pleb FROM plebFriends))";

		String removePlebTweetMentions = "DELETE FROM plebTweetMentions "
				+ "WHERE plebTweetId NOT IN "
				+ "(SELECT id FROM plebTweets)";

		try {
			preparedStatement = conn.prepareStatement(removePlebTweetOneMention);
			preparedStatement.executeUpdate();
			preparedStatement.close();

			preparedStatement = conn.prepareStatement(removeWithoutYoutube);
			preparedStatement.executeUpdate();
			preparedStatement.close();

			preparedStatement = conn.prepareStatement(removeSuspendedOrDeleted);
			preparedStatement.executeUpdate();
			preparedStatement.close();

			preparedStatement = conn.prepareStatement(removePlebTweetMentions);
			preparedStatement.executeUpdate();
			preparedStatement.close();

//			preparedStatement = conn.prepareStatement(removeFromPlebFriends);
//			preparedStatement.executeUpdate();
//			preparedStatement.close();

			System.out.println("deleted plebFriends");
		}catch (SQLException e) {

			System.out.println(e.getMessage());

		} finally {

			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
