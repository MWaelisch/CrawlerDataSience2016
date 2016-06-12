package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import twitter4j.IDs;

public class Database {
	private Connection conn;

	public Database(String databaseFile)
			throws SQLException, FileNotFoundException, IOException, ClassNotFoundException {

		// register the driver
		String sDriverName = "org.sqlite.JDBC";
		Class.forName(sDriverName);
		// create a database connection
		// for usage in eclipse
		// conn = DriverManager.getConnection("jdbc:sqlite:resources/twitterData.db",properties);

		// for usage with jar file
		File file = new File(databaseFile);
		if (!file.exists()) {
			conn = DriverManager.getConnection("jdbc:sqlite:./" + databaseFile);
			// so on
			ScriptRunner runner = new ScriptRunner(conn, false, false);
			InputStream in = getClass().getResourceAsStream("/init.sql"); 
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			runner.runScript(reader);
		} else {
			conn = DriverManager.getConnection("jdbc:sqlite:./" + databaseFile);
		}
	}

	public void closeConnection() {
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
	public void addVip(Vip vip) {
		addVipUserdata(vip);
		addVipFriends(vip);
	}

	private void addVipUserdata(Vip vip) {
		PreparedStatement preparedStatement = null;

		String insertTableSQL = "INSERT INTO vip" + "(id, screenName, userName, followerCount,profilePicture) VALUES"
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
		} catch (SQLException e) {

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

	private void addVipFriends(Vip vip) {
		PreparedStatement preparedStatement = null;

		String insertTableSQL = "INSERT INTO vipFriends" + "(vip,friend) VALUES" + "(?,?)";

		try {
			preparedStatement = conn.prepareStatement(insertTableSQL);

			for (long friend : vip.getFriends()) {
				preparedStatement.setLong(1, vip.getId());
				preparedStatement.setLong(2, friend);
				// execute insert SQL statement
				preparedStatement.executeUpdate();
				preparedStatement.clearParameters();
			}

		} catch (SQLException e) {

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

	public void addVipTweet(VipTweet vipTweet) {
		if (!isTweetInDb(vipTweet.getIdStr(),"vipTweets")) {
			addVipTweetData(vipTweet);
			if (vipTweet.getGeneratedId() != 0) {
				addVipTweetMentions(vipTweet);
			} else {
				try {
					throw new Exception("VIP Tweet database Insert returned 0");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else{
			System.out.println("VipTweet " + vipTweet.getIdStr() + " is already in DB");
		}
	}

	private void addVipTweetData(VipTweet vipTweet) {
		PreparedStatement preparedStatement = null;

		String insertTableSQL = "INSERT INTO vipTweets"
				+ "(authorId, authorName, idStr, inReplyTo, retweetOrigin,text) VALUES" + "(?,?,?,?,?,?)";

		try {
			preparedStatement = conn.prepareStatement(insertTableSQL, Statement.RETURN_GENERATED_KEYS);

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
		} catch (SQLException e) {

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

	private void addVipTweetMentions(VipTweet vipTweet) {
		PreparedStatement preparedStatement = null;

		String insertTableSQL = "INSERT INTO vipTweetMentions" + "(vipTweetId, mention) VALUES" + "(?,?)";

		try {
			preparedStatement = conn.prepareStatement(insertTableSQL);
			for (long mention : vipTweet.getMentions()) {
				preparedStatement.setInt(1, vipTweet.getGeneratedId());
				preparedStatement.setLong(2, mention);
				// execute insert SQL statement
				preparedStatement.executeUpdate();
				preparedStatement.clearParameters();
			}
		} catch (SQLException e) {

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

	public long getVipID(String screenName) {
		try {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT * FROM vip WHERE screenName = '" + screenName + "';");
			// fkt??
			if (rs.next()) {
				return rs.getLong("ID");
			}
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return 0;
	}

	public void addPlebTweet(Tweet plebTweet, long vipId){
		addPlebTweetData(plebTweet);
		//falls tweet erfolgreich eingefügt wurde -> füge mentions in db ein
		if(plebTweet.getGeneratedId() != 0){
			for(long mention : plebTweet.getMentions()){
				addPlebTweetMentions(plebTweet.getGeneratedId(), mention);
			}
			if(!isPlebMentionInDB(plebTweet.getGeneratedId(), vipId))
				addPlebTweetMentions(plebTweet.getGeneratedId(), vipId);
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
	

	public void addPlebTweetMentions(long plebTweetId, long mention){
		PreparedStatement preparedStatement = null;

		String insertTableSQL = "INSERT INTO plebTweetMentions"
				+ "(plebTweetId, mention) VALUES"
				+ "(?,?)";

		try {
			preparedStatement = conn.prepareStatement(insertTableSQL);
			if(!isPlebMentionInDB(plebTweetId, mention)){
				preparedStatement.setLong(1, plebTweetId);
				preparedStatement.setLong(2, mention);
				// execute insert SQL statement
				preparedStatement.executeUpdate();
				preparedStatement.clearParameters();
			}
		} catch (SQLException e) {

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

	public void addPlebFriends(Pleb pleb) {
		Long[] friends = pleb.getFriends();
		PreparedStatement preparedStatement = null;

		String insertTableSQL = "INSERT INTO plebFriends" + "(pleb, friend) VALUES" + "(?,?)";

		try {
			
			for (Long friend : friends) {
				if (!isPlebFriendInDB(pleb.getId(), friend)) {
					preparedStatement = conn.prepareStatement(insertTableSQL);
					preparedStatement.setLong(1, pleb.getId());
					preparedStatement.setLong(2, friend);

					// execute insert SQL statement
					preparedStatement.executeUpdate();
					preparedStatement.close();
				}
			}

			// System.out.println("PlebFriend Inserted!");
		} catch (SQLException e) {

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
	
	
	public boolean isVipInDb(String screenName) {
		try {
			Statement statement = conn.createStatement();

			ResultSet rs = statement
					.executeQuery("SELECT COUNT(*) as count FROM vip WHERE screenName = '" + screenName + "';");

			rs.next();
			int count = rs.getInt("count");
			if(count > 0){
				return true;
			}

			statement.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return false;
	}

	public boolean isIdInDb(long id, String idName, String table) {
		try {
			Statement statement = conn.createStatement();

			ResultSet rs = statement
					.executeQuery("SELECT COUNT(*) as count FROM " + table + " WHERE " + idName + " = " + id + ";");

			rs.next();
			int count = rs.getInt("count");
			if(count > 0){
				return true;
			}
			statement.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return false;
	}
	
	
	public boolean isTweetInDb(String idStr, String table){
		try{
			Statement statement = conn.createStatement();

			ResultSet rs = statement.executeQuery( "SELECT COUNT(idStr) as count FROM "+ table +" WHERE idStr = '" + idStr + "';" );

			rs.next();
			int count = rs.getInt("count");
			if(count > 0){
				System.out.println("Tweet" + idStr + " was in DB");
				return true;
			}
			
			statement.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
    	System.out.println("Tweet " + idStr + " was not in DB");
		return false;
	}
	
	
	public boolean isPlebMentionInDB(long plebTweetId, long mention){
		try{
			Statement statement = conn.createStatement();

			ResultSet rs = statement.executeQuery( "SELECT COUNT(*) as count FROM plebTweetMentions "
					+ "WHERE plebTweetId = " + plebTweetId + " "
					+ "AND mention = " + mention + ";" );

			rs.next();
			int count = rs.getInt("count");
			if(count > 0){
				return true;
			}
			statement.close();
			return false;
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return false;
	}
	
	public boolean isPlebFriendInDB(long pleb, long friend){
		try{
			Statement statement = conn.createStatement();

			ResultSet rs = statement.executeQuery( "SELECT COUNT(*) as count FROM plebFriends "
					+ "WHERE pleb = " + pleb + " "
					+ "AND friend = " + friend + ";" );

			rs.next();
			int count = rs.getInt("count");
			if(count > 0){
				System.out.println("Pleb Friendship was already in DB");
				return true;
			}
			
			statement.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return false;
	}
	
	public long getTweetId(String tweetId){
		try{
			Statement statement = conn.createStatement();

			ResultSet rs = statement.executeQuery( "SELECT id FROM plebTweets "
					+ "WHERE idStr = '" + tweetId + "';" );

			if (rs.next()) {
				long id = rs.getLong("id");
			    if (rs.wasNull()) {
					return -1;
			    } else {
			    	return id;
			    }
			}
			
			statement.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return -1;
	}
	
	public ArrayList<Vip> getNVipsFromDB(int numberOfVips){
		ArrayList<Vip> vips = new ArrayList<Vip>();
		try {
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
				if (count == numberOfVips) {
					break;
				}

			}

			statement.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return vips;
	}

	public ArrayList<Vip> getAllVIPsfromDB() {
		ArrayList<Vip> vips = new ArrayList<Vip>();
		try {
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
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
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

            ResultSet rsPleb = plebStatement.executeQuery("SELECT * FROM plebTweets LEFT JOIN plebFriends ON authorId = pleb WHERE friend IN (SELECT id FROM vip)" +
            " ORDER BY authorId ASC;");

            while (rsPleb.next()) {
                plebId = rsPleb.getLong("authorId");
				ResultSet rsFriends = friendStatement.executeQuery("SELECT  * FROM plebFriends WHERE pleb = "+ plebId +" AND friend IN (SELECT id FROM vip);");
                //check for all mentions in this tweet
                tweetId = rsPleb.getInt("id");
                ResultSet rsMention = tweetStatement.executeQuery("SELECT * FROM plebTweetMentions WHERE plebTweetId = " + tweetId + " AND mention IN (SELECT id FROM vip);");

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


    public ArrayList<Tweet> getAllUnscoredTweetsfromDB(String table){
		ArrayList<Tweet> ts = new ArrayList<Tweet>();
		try {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT * FROM " + table + " WHERE sentimentPos IS NULL OR sentimentNeg IS NULL  ORDER BY id ASC;");
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
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return ts;
	}

	public ArrayList<VipTweet> getVipTweets(long authorId) {
		ArrayList<VipTweet> vipTweets = new ArrayList<VipTweet>();
		try {
			Statement statement = conn.createStatement();
			//Hole alle tweets des authors, bei denen entweder 
			//auf den Tweet eines anderen Vip geantwortet wird, 
			//ein anderer Vip retweetet wird
			//oder ein anderer vip erwähnt wird
			ResultSet rs = statement.executeQuery("SELECT * FROM vipTweets LEFT JOIN vipTweetMentions WHERE"
					+ " authorId=" + authorId + " AND " + " (inReplyTo IN (SELECT id FROM vip)  OR mention IN (SELECT id FROM vip) OR retweetOrigin IN (SELECT id FROM vip));");
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

			for (VipTweet tweet : vipTweets) {

				//hole alle mentions zu einem tweet, die einen anderen vip referenzieren
				rs = statement.executeQuery(
						"SELECT COUNT(*) as count FROM VipTweetMentions WHERE vipTweetId=" + tweet.getGeneratedId() 
						+ " AND mention IN (SELECT id FROM vip)");
				rs.next();
				int count = rs.getInt("count");
				Long[] mentions = new Long[count];
				rs = statement.executeQuery(
						"SELECT * FROM VipTweetMentions WHERE vipTweetId=" + tweet.getGeneratedId() 
						+ " AND mention IN (SELECT id FROM vip)");
				int i=0;
				while(rs.next()){
					mentions[i] = rs.getLong("mention");
					i++;
				}
				tweet.setMentions(mentions);
			}
			statement.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return vipTweets;
	}

	public long[] getVipFriends(long vipId) {

		try {

			Statement statement = conn.createStatement();

			//Hole alle Freunde eines Vips, die ebenfalls vip sind
			ResultSet rs = statement.executeQuery("SELECT COUNT(*) as count FROM vipFriends WHERE vip=" + vipId +
												 " AND friend IN (SELECT id FROM vip)");
			rs.next();
			int count = rs.getInt("count");

			long[] vipFriends = new long[count];

			rs = statement.executeQuery("SELECT * as count FROM vipFriends WHERE vip=" + vipId +
					 " AND friend IN (SELECT id FROM vip)");

			int i = 0;
			while (rs.next()) {
				vipFriends[i] = rs.getLong("friend");
				i++;
			}

			statement.close();
			return vipFriends;
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return null;
	}
	
	
	/**
	 * Get all tweet authors(plebs) of which 
	 * no friends have been crawled yet
	 * and which are relevant -> they are author of a Tweet with sentiment other than -1/1
	 * DICTINCT -> avoid duplicates
	 */
	public long[] getRelevantPlebsWithoutFriends()
	{
		
		try {

			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT COUNT(DISTINCT authorId) as count FROM plebTweets LEFT JOIN plebFriends ON plebTweets.authorID = plebFriends.pleb WHERE pleb IS NULL AND (sentimentPos > 1 OR sentimentNeg < -1)");
			rs.next();
			int count = rs.getInt("count");
			long[] plebIds = new long[count];
			
			statement = conn.createStatement();
			rs = statement.executeQuery("SELECT DISTINCT authorId FROM plebTweets LEFT JOIN plebFriends ON plebTweets.authorID = plebFriends.pleb WHERE pleb IS NULL AND (sentimentPos > 1 OR sentimentNeg < -1)");
			
			int i = 0;
			while (rs.next()) {
				long id = rs.getLong("authorId");
				plebIds[i] = id;
				i++;
			}
			statement.close();
			return plebIds;
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return null;
	}
		
	public void updateTweets(ArrayList<Tweet> tweets, String table) {
		String sql;
		Statement stmt = null;

		for (Tweet t : tweets) {
			sql = "UPDATE " + table + " " + "SET sentimentPos = " + t.getSentimentPos() + ", sentimentNeg = "
					+ t.getSentimentNeg() + " WHERE id =" + t.getGeneratedId();
			try {
				stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
	}
}
