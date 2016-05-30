package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
			conn = DriverManager.getConnection("jdbc:sqlite:resources/twitterData_vips_viptweets_plebTweets+posneg.db",properties);
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
			
			//fkt??
			if (rs.next()) {
				//long getid =
			    rs.getInt(idName);
			    if (rs.wasNull()) {
					return false;
			    } else return true;
			}
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return false;
	}
	
	public String executeQuery(String query){
		String r = "";
		try{
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery( query );
			//fkt??
			while (rs.next()) {
				//long getid =
			    r += //"#-#-#-#-#" + rs.getString("text")+ "\n";
			    		"::" + rs.getLong("id")+ "\n";
			    		// + " " + rs.getInt("friend") +"\n";
			}
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		System.out.println(r);
		return r;
	}
	
	public ArrayList<Vip> getAllVIPsfromDB(){
		ArrayList<Vip> vips = new ArrayList<Vip>();
		try{
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT * FROM vip;");
			
			while (rs.next()) {
				Vip vip = new Vip(rs.getString("screenName"), rs.getString("userName"));
				
				//(id, screenName, userName, followerCount,profilePicture)
//				vip.setId(rs.getLong("id"));
//				vip.setScreenName(rs.getString("screenName"));
//				vip.setUserName(rs.getString("userName"));
//				vip.setFollowerCount(rs.getInt("followerCount"));
//				vip.setProfilePicture(rs.getString("profilePicture"));
				
				vips.add(vip);
			}
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return vips;
	}

	public ArrayList<Tweet> getAllTweetsfromDB(String table){
		ArrayList<Tweet> ts = new ArrayList<Tweet>();
		try{
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT * FROM " + table);

			while (rs.next()) {
				Tweet t = new Tweet(rs.getLong("authorId"),
								rs.getString("idStr"),
								rs.getString("text"),
								rs.getInt("id"),
								rs.getInt("sentimentPos"),
								rs.getInt("sentimentNeg"));
				
				ts.add(t);
			}
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return ts;
	}
	

	//todo make usable for pleb and vip
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
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
	}
	
	public void cleanDB(){
		
		PreparedStatement preparedStatement = null;

		String removeUnnecessaryVipTweets = "DELETE FROM vipTweets " +
											"WHERE retweetOrigin NOT IN (SELECT id FROM vip) " + 
											"AND inReplyTo NOT IN (SELECT id FROM vip)" +
											"AND NOT EXISTS (SELECT * FROM vipTweetMentions,vip WHERE vipTweetId=vipTweets.id AND mention=vip.id)";
		
		String removeUnnecessaryVipTweetMentions="DELETE FROM vipTweetMentions " +
												 "WHERE vipTweetid NOT IN (SELECT id FROM vipTweets)" +
												 "OR mention NOT IN (SELECT id FROM vip)";

		String removeUnnecessaryVipFriends = "DELETE FROM vipFriends " +
											 "WHERE friend NOT IN (SELECT id FROM vip)";
		
		String removeUnnecessaryPlebFriends = "DELETE FROM plebFriends WHERE friend NOT IN (SELECT id FROM vip)";
		
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
			
			preparedStatement = conn.prepareStatement(removeUnnecessaryPlebFriends);
			
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
	

	public void cleanPlebTweets(){
		
		PreparedStatement preparedStatement = null;

		String removeUnnecessaryPlebTweets = "DELETE FROM plebTweets " +
											"WHERE sentimentPos = 1 AND sentimentNeg = -1;";
		
		
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
}
