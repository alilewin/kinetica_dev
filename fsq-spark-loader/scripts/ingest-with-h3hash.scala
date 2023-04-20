// import the uber h3 lib
import com.uber.h3core.H3CoreV3

// define a udf to be used to create H3hash column in dataframe
def getH3hash(lat:Double, lon:Double, resolution:Integer):Long = {
  H3CoreV3.newInstance.geoToH3(lat, lon, resolution)
}
val udfGetH3Hash = udf(getH3hash _)

// read in parquest file from s3://
val df=spark.read.parquet("s3a://kinetica-4sq-partner-grapheval/4sqplaces.parquet")
df.printSchema

// add the h3hash column in with resolution level 6
val df1=df.withColumn("h3hash", udfGetH3Hash($"geocodes_mainEntry_latitude", $"geocodes_mainEntry_longitude", lit(6)))
df1.printSchema

// define connection string to kinetica
val host = "172.31.31.29"
val port = 9191
var url = s"jdbc:kinetica://${host}:9191;"
val username = "ingest"
val password = "Password@1"
val options = Map(
   "url" -> url,
   "driver" -> "com.kinetica.jdbc.Driver",
   "UID" -> username,
   "PWD" -> password,
   "dbtable" -> s"JYIN.places",
   "table.create" -> "false",
   "table.truncate" -> "false",
   "table.ErrorIfExists" -> "false",
   "UpdateOnExistingPk" -> "true"
)

// save the dataframe to kinetica
df1.write.format("jdbc").options(options).mode("append").save()