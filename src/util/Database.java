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
			conn = DriverManager.getConnection("jdbc:sqlite:resources/twitterData_test.db",properties);
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
			preparedStatement.setString(2, vipTweet.getAuthorName());
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
	
	public void addPlebTweet(PlebTweet plebTweet, long vipId){
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

	
	private void addPlebTweetData(PlebTweet plebTweet){
		PreparedStatement preparedStatement = null;

		String insertTableSQL = "INSERT INTO plebTweets"
				+ "(idStr, text, authorId) VALUES"
				+ "(?,?,?)";

		try {
			preparedStatement = conn.prepareStatement(insertTableSQL);

			preparedStatement.setString(1, plebTweet.getIdStr());
			preparedStatement.setString(2, plebTweet.getTweet());
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
			ResultSet rs = statement.executeQuery( "SELECT " + idName + " FROM " + db + " WHERE ID = " + id + ";" );
			statement.close();
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
			statement.close();
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
			statement.close();
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

	public ArrayList<VipTweet> getAllVIPTweetsfromDB(){
		ArrayList<VipTweet> vipTweets = new ArrayList<VipTweet>();
		try{
			Statement statement = conn.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT * FROM vipTweets;");
			statement.close();
			while (resultSet.next()) {
				VipTweet vipTweet = new VipTweet(resultSet.getLong("id"), resultSet.getString("text"),resultSet.getInt("sentimentPos"));

				vipTweets.add(vipTweet);
			}
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return vipTweets;
	}

	//todo make usable for pleb and vip
	public void updateTweets(ArrayList<VipTweet> vipTweets, String table){
		String sql;
		Statement stmt = null;

		for(VipTweet vt : vipTweets){
			sql = "UPDATE "+table+" " +
					"SET sentimentPos = "+vt.getPosSentiment()+", sentimentNeg = "+vt.getNegSentiment()+
					" WHERE id ="+vt.getAuthorId();
			try {
				stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
	}

    //todo maybe merge with getAllVipTweets
	public ArrayList<PlebTweet> getAllPlebTweetsfromDB(){
		ArrayList<PlebTweet> pts = new ArrayList<PlebTweet>();
		try{
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT * FROM plebTweets;");
			statement.close();
			while (rs.next()) {
				PlebTweet pt = new PlebTweet(rs.getLong("authorId"), rs.getString("text"));
				
				pts.add(pt);
			}
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return pts;
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
}
