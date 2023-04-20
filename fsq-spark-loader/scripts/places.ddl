CREATE or replace TABLE "foursquare"."places_test"
(
    "h3hash" BIGINT (shard_key),
    "placeIdentifier" VARCHAR (32),
    "name" VARCHAR (128),
    "storeId" VARCHAR (32),
    "description" VARCHAR,
    "localizedNames" VARCHAR (256),
    "realityScore" VARCHAR (16),
    "address" VARCHAR,
    "geocodes" VARCHAR (256),
    "contactInfo" VARCHAR,
    "socialMediaInfo" VARCHAR (256),
    "operationsInfo" VARCHAR,
    "categories" VARCHAR (32),
    "chains" VARCHAR (64),
    "parent" VARCHAR (32)
)
TIER STRATEGY (
( ( VRAM 1, RAM 5, PERSIST 5 ) )
);
