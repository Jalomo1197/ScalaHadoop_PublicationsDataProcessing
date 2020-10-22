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
You have two options to obtain the .jar file for a specific task
### Option One: Import To Intellij
First you will need to import this project into intellij as a scala/sbt project from github. Next, depending 
on which functionality you want in the .jar (e.g. OneAuthor, TopTenPerVenue, etc.), you will modify only the [Driver class](./src/main/scala/Driver.scala).
Within the Driver class you must set the appropriate mapper and reducer classes for the job. 
The class type of both mapper and reducer outputs must also be adjusted
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

### Option Two: Download .jar File

## Helpful References 


## Dependencies
See [build.sbt](./build.sbt) and [assembly.sbt](./project/assembly.sbt)


 explain how they work,
 The output of your map/reduce is a spreadsheet or an CSV file with the required statistics.

This project was create and run your software application using [Apache Hadoop](http://hadoop.apache.org/), a framework for distributed processing of large data sets across multiple computers (or even on a single node) using the map/reduce model. Even though you can install and configure Hadoop on your computers, I recommend that you use a virtual machine (VM) of [Hortonworks Sandbox](http://hortonworks.com/products/sandbox/), a preconfigured Apache Hadoop installation with a comprehensive software stack. To run the VM, you can install vmWare or VirtualBox. As UIC students, you have access to free vmWare licenses, go to http://go.uic.edu/csvmware to obtain your free license. In some cases, I may have to provide your email addresses to a department administrator to enable your free VM academic licenses. Please notify me if you cannot register and gain access to the webstore.


You can complete this homework using Scala and __you will immensely enjoy__ the embedded XML processing facilities that come with Scala. You will use Simple Build Tools (SBT) for building the project and running automated tests. I recommend that you run the downloaded VM locally in vmWare or VirtualBox to develop and test your program before you move it to AWS.

Next, after creating and testing your map/reduce program locally, you will deploy it and run it on the Amazon Elastic MapReduce (EMR) - you can find plenty of [documentation online](http://docs.aws.amazon.com/emr/latest/ManagementGuide/emr-work-with-steps.html). You will produce a short movie that documents all steps of the deployment and execution of your program with your narration and you will upload this movie to [youtube](www.youtube.com) and you will submit a link to your movie as part of your submission in the README.md file. To produce a movie, you may use an academic version of [Camtasia](https://shop.techsmith.com/store/techsm/en_US/cat/categoryID.67158100) or some other cheap/free screen capture technology from the UIC webstore or an application for a movie capture of your choice. The captured web browser content should show your login name in the upper right corner of the AWS application and you should introduce yourself in the beginning of the movie speaking into the camera.



## Baseline Submission
Your baseline project submission should include your implementation, a conceptual explanation in the document or in the comments in the source code of how your mapper and reducer work to solve the problem, and the documentation that describe the build and runtime process, to be considered for grading. Your project submission should include all your source code written in Scala as well as non-code artifacts (e.g., configuration files), your project should be buildable using the SBT, and your documentation must specify how you paritioned the data and what input/outputs are. Simply copying Java programs from examples at the DBLP website and modifying them a bit will result in rejecting your submission.


## Git logistics
**This is an individual homework.** Separate repositories will be created for each of your homeworks and for the course project. You will find a corresponding entry for this homework at git@bitbucket.org:cs441_fall2020/homework2.git. You will fork this repository and your fork will be private, no one else besides you, the TA and your course instructor will have access to your fork. Please remember to grant a read access to your repository to your TA and your instructor. In future, for the team homeworks and the course project, you should grant the write access to your forkmates, but NOT for this homework. You can commit and push your code as many times as you want. Your code will not be visible and it should not be visible to other students (except for your forkmates for a team project, but not for this homework). When you push the code into the remote repo, your instructor and the TA will see your code in your separate private fork. Making your fork public, pushing your code into the main repo, or inviting other students to join your fork for an individual homework will result in losing your grade. For grading, only the latest push timed before the deadline will be considered. **If you push after the deadline, your grade for the homework will be zero**. For more information about using the Git and Bitbucket specifically, please use this [link as the starting point](https://confluence.atlassian.com/bitbucket/bitbucket-cloud-documentation-home-221448814.html). For those of you who struggle with the Git, I recommend a book by Ryan Hodson on Ry's Git Tutorial. The other book called Pro Git is written by Scott Chacon and Ben Straub and published by Apress and it is [freely available](https://git-scm.com/book/en/v2/). There are multiple videos on youtube that go into details of the Git organization and use.

Please follow this naming convention while submitting your work : "Firstname_Lastname_hw2" without quotes, where you specify your first and last names **exactly as you are registered with the University system**, so that we can easily recognize your submission. I repeat, make sure that you will give both your TA and the course instructor the read access to your *private forked repository*.



## Submission deadline and logistics
Saturday, October 17 at 11PM CST via the bitbucket repository. Your submission will include the code for your program, your documentation with instructions and detailed explanations on how to assemble and deploy your program along with the results of its run and what the limitations of your implementation are. Again, do not forget, please make sure that you will give both your TA and your instructor the read access to your private forked repository. Your name should be shown in your README.md file and other documents. Your code should compile and run from the command line using the commands **sbt clean compile test** and **sbt clean compile run** or some other build/run system like cmake. Also, you project should be IntelliJ or PyCharm or CLion friendly, i.e., your graders should be able to import your code into IntelliJ and run from there. Use .gitignore to exlude files that should not be pushed into the repo.


## Evaluation criteria
- the maximum grade for this homework is 10% with the bonus up to 3% for doing the AWS EMR part. Points are subtracted from this maximum grade: for example, saying that 2% is lost if some requirement is not completed means that the resulting grade will be 10%-2% => 8%; if the core homework functionality does not work, no bonus points will be given;
- the code does not work in that it does not produce a correct output or crashes: up to 5% lost;
- having less than five unit and/or integration tests: up to 4% lost;
- missing comments and explanations from the program: up to 5% lost;
- logging is not used in the program: up to 3% lost;
- hardcoding the input values in the source code instead of using the suggested configuration libraries: up to 4% lost;
- no instructions in README.md on how to install and run your program: up to 10% lost;
- the documentation exists but it is insufficient to understand how you assembled and deployed all components of the cloud: up to 4% lost;
- the minimum grade for this homework cannot be less than zero.

That's it, folks!
