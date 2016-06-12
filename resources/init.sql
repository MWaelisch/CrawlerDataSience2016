------------- SQLite3 Dump File -------------

-- ------------------------------------------
-- Dump of "plebFriends"
-- ------------------------------------------

DROP TABLE IF EXISTS "plebFriends";

CREATE TABLE "plebFriends"(
	"pleb" Integer NOT NULL,
	"friend" Integer NOT NULL,
	CONSTRAINT "lnk_plebFriends_plebTweets" FOREIGN KEY ( "pleb" ) REFERENCES "plebTweets"( "authorId" )
,
PRIMARY KEY ( "pleb", "friend" ) );


-- ------------------------------------------
-- Dump of "plebTweetMentions"
-- ------------------------------------------

DROP TABLE IF EXISTS "plebTweetMentions";

CREATE TABLE "plebTweetMentions"(
	"mention" Integer NOT NULL,
	"plebTweetId" Integer NOT NULL,
	CONSTRAINT "plebTweetMentions_`plebTweets`_NO ACTION_NO ACTION_plebTweetId_id_0" FOREIGN KEY ( "plebTweetId" ) REFERENCES "`plebTweets`"( "id" )
,
PRIMARY KEY ( "mention", "plebTweetId" ) );


-- ------------------------------------------
-- Dump of "plebTweets"
-- ------------------------------------------

DROP TABLE IF EXISTS "plebTweets";

CREATE TABLE "plebTweets"(
	"id" Integer NOT NULL PRIMARY KEY AUTOINCREMENT,
	"idStr" Text NOT NULL,
	"text" Text NOT NULL,
	"sentimentPos" Integer,
	"sentimentNeg" Integer,
	"authorId" Integer NOT NULL, 
	CONSTRAINT "unique_idStr" UNIQUE ( "idStr" ));


-- ------------------------------------------
-- Dump of "vip"
-- ------------------------------------------

DROP TABLE IF EXISTS "vip";

CREATE TABLE "vip"(
	"id" Integer NOT NULL PRIMARY KEY,
	"screenName" Text NOT NULL,
	"userName" Text NOT NULL,
	"followerCount" Integer NOT NULL,
	"profilePicture" Text NOT NULL,
CONSTRAINT "unique_id" UNIQUE ( "id" ),
CONSTRAINT "unique_screenName" UNIQUE ( "screenName" ) );

CREATE INDEX "index1" ON "vip"( "id" );

-- ------------------------------------------
-- Dump of "vipFriends"
-- ------------------------------------------

DROP TABLE IF EXISTS "vipFriends";

CREATE TABLE "vipFriends"(
	"vip" Integer NOT NULL,
	"friend" Integer NOT NULL,
PRIMARY KEY ( "vip", "friend" ) );

CREATE INDEX "index2" ON "vipFriends"( "vip" );

-- ------------------------------------------
-- Dump of "vipTweetMentions"
-- ------------------------------------------

DROP TABLE IF EXISTS "vipTweetMentions";

CREATE TABLE "vipTweetMentions"(
	"vipTweetId" Integer NOT NULL,
	"mention" Integer NOT NULL,
PRIMARY KEY ( "vipTweetId", "mention" ) );

CREATE INDEX "index3" ON "vipTweetMentions"( "vipTweetId" );

-- ------------------------------------------
-- Dump of "vipTweets"
-- ------------------------------------------

DROP TABLE IF EXISTS "vipTweets";

CREATE TABLE "vipTweets"(
	"id" Integer NOT NULL PRIMARY KEY AUTOINCREMENT,
	"idStr" Text NOT NULL,
	"authorId" Integer NOT NULL,
	"authorName" Text NOT NULL,
	"text" Text NOT NULL,
	"retweetOrigin" Integer,
	"inReplyTo" Integer,
	"sentimentPos" Integer,
	"sentimentNeg" Integer,
CONSTRAINT "unique_ID" UNIQUE ( "id" ),
CONSTRAINT "unique_idStr" UNIQUE ( "idStr" )
);

CREATE INDEX "index4" ON "vipTweets"( "authorId" );

