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
			
			for(Long friend : friends){
				preparedStatement = conn.prepareStatement(insertTableSQL);
				preparedStatement.setLong(1, pleb.getId());
				preparedStatement.setLong(2, friend);

				// execute insert SQL statement
				preparedStatement.executeUpdate();
				preparedStatement.close();
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

	public boolean isIDInDB(long id, String idName, String db) {
		try {
			Statement statement = conn.createStatement();

			ResultSet rs = statement
					.executeQuery("SELECT " + idName + " FROM " + db + " WHERE " + idName + " = " + id + ";");

			if (rs.next()) {
				// long getid =
				rs.getInt(idName);
				if (rs.wasNull()) {
					return false;
				} else
					return true;
			}

			statement.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return false;
	}
	
	public boolean isPlebTweetInDB(String id){
		try{
			Statement statement = conn.createStatement();

			ResultSet rs = statement.executeQuery( "SELECT idStr FROM plebTweets WHERE idStr = '" + id + "';" );

			if (rs.next()) {
				//long getid =
			    rs.getString("idStr");
			    if (rs.wasNull()) {
			    	System.out.println("Tweet was not in DB");
					return false;
			    } else {
			    	System.out.println("Tweet" + id  + "was in DB");
			    	return true;
			    }
			}
			
			statement.close();
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
    	System.out.println("Tweet " + id + " was not in DB");
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
	
	//debugging and testing
	public String executeQuery(String query){
		String r = "";
		// String selectPlebMentions = "SELECT authorId, COUNT(pm.mention) AS
		// friendcnt "//"DELETE pm, pt "
		// + "FROM plebTweetMentions pm "
		// + "JOIN plebTweets pt ON pm.plebTweetId = pt.id "
		// + "JOIN plebFriends pf ON pt.authorId = pf.pleb "
		// + "WHERE pf.friend = 0 "
		// + "GROUP BY pt.authorId HAVING COUNT(pm.mention) >= 2";
		try {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(query );
			while (rs.next()) {
				// long getid =
				r += // "#-#-#-#-#" + rs.getString("text")+ "\n";
				"::" + rs.getLong("plebTweetid") + "\n";
				// + " " + rs.getInt("friend") +"\n";
				// ":: " + rs.getLong("authorId") + " # " +
				// rs.getInt("friendcnt") + "\n";
			}

			statement.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("result: " + r);
		return r;
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
		try {
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

			for (VipTweet tweet : vipTweets) {

				rs = statement.executeQuery(
						"SELECT COUNT(*) as count FROM VipTweetMentions WHERE vipTweetId=" + tweet.getGeneratedId());
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
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		return vipTweets;
	}

	public long[] getVipFriends(long vipId) {

		try {

			Statement statement = conn.createStatement();

			ResultSet rs = statement.executeQuery("SELECT COUNT(*) as count FROM vipFriends WHERE vip=" + vipId);
			rs.next();
			int count = rs.getInt("count");

			long[] vipFriends = new long[count];

			rs = statement.executeQuery("SELECT * FROM vipFriends WHERE vip=" + vipId);

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
	 */
	public long[] getPlebsWithoutFriends()
	{
		
		try {

			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT COUNT(authorId) as count FROM plebTweets LEFT JOIN plebFriends ON plebTweets.authorID = plebFriends.pleb WHERE pleb IS NULL");
			rs.next();
			int count = rs.getInt("count");
			long[] plebIds = new long[count];
			
			statement = conn.createStatement();
			rs = statement.executeQuery("SELECT authorId FROM plebTweets LEFT JOIN plebFriends ON plebTweets.authorID = plebFriends.pleb WHERE pleb IS NULL");
			
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

	public void cleanVips() {

		System.out.println("Cleaning Vip Data in Database");
		PreparedStatement preparedStatement = null;

		// Lösche alle viptweets die nicht bezug auf einen anderen vip nehmen
		String removeUnnecessaryVipTweets = "DELETE FROM vipTweets "
				+ "WHERE retweetOrigin NOT IN (SELECT id FROM vip) " + "AND inReplyTo NOT IN (SELECT id FROM vip)"
				+ "AND NOT EXISTS (SELECT * FROM vipTweetMentions,vip WHERE vipTweetId=vipTweets.id AND mention=vip.id)";

		// lösche alle mentions, zu denen der tweet nichtmehr existiert-> siehe
		// removeUnnecessaryVipTweets
		// und alle unnötigen mentions, also mentions die nicht auf einen vip
		// verweisen
		String removeUnnecessaryVipTweetMentions = "DELETE FROM vipTweetMentions "
				+ "WHERE vipTweetid NOT IN (SELECT id FROM vipTweets)" + "OR mention NOT IN (SELECT id FROM vip)";

		// lösche alle freunde von vips die selbst keine vips sind
		String removeUnnecessaryVipFriends = "DELETE FROM vipFriends " + "WHERE friend NOT IN (SELECT id FROM vip)";

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

	public void cleanNeutralSentiScorePlebTweets() {

		PreparedStatement preparedStatement = null;

		String removeUnnecessaryPlebTweets = "DELETE FROM plebTweets " + "WHERE sentimentPos = 1 AND sentimentNeg = -1";

		try {

			preparedStatement = conn.prepareStatement(removeUnnecessaryPlebTweets);

			preparedStatement.executeUpdate();

			preparedStatement.close();
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

	public void cleanProtectedPleb(long tweetId) {

		PreparedStatement preparedStatement = null;

		String removeFromPlebTweets = "DELETE FROM plebTweets " + "WHERE id = " + tweetId;

		String removeFromPlebTweetMentions = "DELETE FROM plebTweetMentions " + "WHERE plebTweetId = " + tweetId;

		try {

			preparedStatement = conn.prepareStatement(removeFromPlebTweets);
			preparedStatement.executeUpdate();
			preparedStatement.close();

			System.out.println("deleted plebTweet");

			preparedStatement = conn.prepareStatement(removeFromPlebTweetMentions);
			preparedStatement.executeUpdate();
			preparedStatement.close();

			System.out.println("deleted plebTweetMention");
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
	
	//todo tweeten uU auch ueber andere Vips (aber anscheinend nicht viele)
	//todo stimmt removePlebTweet?
	public void cleanPlebFriends(){
		PreparedStatement preparedStatement = null;
		String removeFromPlebFriends = "DELETE FROM plebTweets " + "WHERE friend = 0";

		String removePlebTweetOneMention = "DELETE FROM plebTweets " + "WHERE id IN (SELECT id "
				+ "FROM plebTweetMentions pm " + "JOIN plebTweets pt ON pm.plebTweetId = pt.id "
				+ "JOIN plebFriends pf ON pt.authorId = pf.pleb " + "WHERE pf.friend = 0 "
				+ "GROUP BY pt.authorId HAVING COUNT(pm.mention) < 2)";

		// DELETE plebTweets, PlebTweetMentions,plebFriends FROM plebTweets JOIN
		// plebTweetmentions ON plebTweetId = plebTweets.id JOIN plebFriends ON
		// authorId = pleb WHErE authorId IN (SELECT authorId
		// FROM plebTweetMentions pm
		// JOIN plebTweets pt ON pm.plebTweetId = pt.id
		// JOIN plebFriends pf ON pt.authorId = pf.pleb
		// WHERE pf.friend = 0
		// GROUP BY pt.authorId HAVING COUNT(pm.mention) >= 2) AND NOT text LIKE
		// '%Youtube%'ORDER BY authorId
		//
		//
		String removeWithoutYoutube = "DELETE FROM plebTweets " + "WHERE id IN (SELECT id "
				+ "FROM plebTweetMentions pm " + "JOIN plebTweets pt ON pm.plebTweetId = pt.id " + "WHERE authorId IN "
				+ "(SELECT authorId FROM plebTweetMentions pm " + "JOIN plebTweets pt ON pm.plebTweetId = pt.id "
				+ "JOIN plebFriends pf ON pt.authorId = pf.pleb " + "WHERE pf.friend=0 "
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
}
