package postProcessing;

import java.util.ArrayList;

import model.Tweet;
import uk.ac.wlv.sentistrength.*;
import util.Database;

public class SentiStrengthWrapper {

    private SentiStrength sentiStrength;
    private Database database;

    public SentiStrengthWrapper() {
        this.sentiStrength = new SentiStrength();
        //Create an array of command line parameters to send (not text or file to process)
        String ssthInitialisation[] = {"sentidata", "SentiData/sentistrength_de/"};
        sentiStrength.initialise(ssthInitialisation);
    }

    public Integer[] getSentiScores(String text) {
        String[] stringResult = sentiStrength.computeSentimentScores(text).split(" ");
        //todo make failsafe
        return new Integer[]{Integer.parseInt(stringResult[0]), Integer.parseInt(stringResult[1])};
    }


    public void calculateSentiScore(String table) {

        ArrayList<Tweet> tweets = database.getAllUnscoredTweetsfromDB(table);
        //calculate sentiScore for each tweet
        Integer[] sentiScore;
        for (Tweet t : tweets) {
            sentiScore = getSentiScores(t.getText());
            t.setSentiScore(sentiScore);
        }

        //write sentiScore in vipTweets table in db
        database.updateTweets(tweets, table);
    }


    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }
}
