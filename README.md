# Scala Hadoop MapReduce: On dataset of various publications at many different venues
## Overview
This is a distributed program for parallel processing of the [publically available DBLP dataset](https://dblp.uni-trier.de) that contains entries for various publications at many different venues (e.g., conferences and journals). Raw [XML-based DBLP dataset](https://dblp.uni-trier.de/xml) is also publically available along with its schema and the documentation.

Each entry in the dataset describes a publication, which contains the list of authors, the title, and the publication venue and a few other attributes. The file is approximately 3.01Gb - not big by today's standards, but large enough. Each entry is independent from the other one in that it can be processed without synchronizing with processing some other entries.

Consider the following entry in the dataset.
```xml
<inproceedings mdate="2017-05-24" key="conf/icst/GrechanikHB13">
<author>Mark Grechanik</author>
<author>B. M. Mainul Hossain</author>
<author>Ugo Buy</author>
<title>Testing Database-Centric Applications for Causes of Database Deadlocks.</title>
<pages>174-183</pages>
<year>2013</year>
<booktitle>ICST</booktitle>
<ee>https://doi.org/10.1109/ICST.2013.19</ee>
<ee>http://doi.ieeecomputersociety.org/10.1109/ICST.2013.19</ee>
<crossref>conf/icst/2013</crossref>
<url>db/conf/icst/icst2013.html#GrechanikHB13</url>
</inproceedings>
```

This entry lists a paper at the IEEE International Conference on Software Testing, Verification and Validation (ICST) published in 2013 whose authors are B.M. Mainul Hussain, Mark Grechanik, and Ugo Buy. 


## Functionality: parallel distributed processing of the publication dataset
Can produce the following statistics about the authors and the venues they published their papers at. 

- Compute top ten published authors at each venue. 
([TopTenPerVenue.scala](./src/main/scala/TopTenPerVenue.scala))
- Compute the list of authors who published without interruption for N years where 10 <= N.
([PublishWithoutInterruption.scala](./src/main/scala/PublishWithoutInterruption.scala))
- Compute for each venue produce the list of publications that contains only one author.
([OneAuthor.scala](./src/main/scala/OneAuthor.scala))
- Compute the list of publications for each venue that contain the highest number of authors for each of these venues.
([HighestAuthorsPublications.scala](./src/main/scala/HighestAuthorsPublications.scala)) 
- Compute the list of top 100 authors in the descending order who publish with most co-authors and the list of 100 authors who publish without any co-authors. 
([ListRanking.scala](./src/main/scala/ListRanking.scala))

Distinct mappers and reducers exist for each functionality.

## Getting the .jar for hadoop
Obtaining the .jar file for a specific task.

First you will need to import this project into intellij as a scala/sbt project from github. Next, depending 
on which functionality you want in the .jar (e.g. OneAuthor, TopTenPerVenue, etc.), you will modify only the [Driver class](./src/main/scala/Driver.scala).
Within the Driver class you must set the appropriate mapper and reducer classes for the job. 
The class type of both mapper and reducer outputs must also be adjusted to match the out pair types of the task file (e.g. ListRanking.scala)
```scala
    // Specifying which functionality
    job.setMapperClass(classOf[ListRankingMapper])   //mapper of desired functionality
    job.setReducerClass(classOf[ListRankingReducer]) //reducer of desired functionality
    // Output pair of mapper. MUST MATCH ListRankingMapper output type
    job.setMapOutputKeyClass(classOf[Text])
    job.setMapOutputValueClass(classOf[MapWritable])
    // Output pair of reducer. MUST MATCH ListRankingReducer output type
    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[Text])
```


## Running Programs
Once you have the .jar file/files and downloaded the 3Gb data from the link provided. Simply run 
the following hadoop command (assuming you have hadoop installed already):

```
hadoop jar <.jar file> <MainClass> <XMLInputFile> <OutputPath>
```

or 

```
hadoop jar <.jar file> <XMLInputFile> <OutputPath>
```

MainClass => Driver


## Dependencies
See [build.sbt](./build.sbt) and [assembly.sbt](./project/assembly.sbt)


##### Updates coming soon