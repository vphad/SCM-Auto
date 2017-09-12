/*
 * Script to get Sonar urls from Jenkins jobs execution
*/

import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*


def jen = Jenkins.instance

allJobs = jen.getAllItems().findAll{ job ->
	 (!job.hasProperty("dsl")) && (job.hasProperty('scm') && (job.getScm() instanceof hudson.scm.SubversionSCM)) && (job.hasProperty("disabled") && !job.isDisabled()) && (!job.getFullName().toLowerCase().contains('ahs')) && (job instanceof Job) 
}

def output = "Job,Build Url,Built On,Build Time,SonarUrl"

for(Job job : allJobs) {
	// Get the last build
	jobBuild = job.getLastSuccessfulBuild()
	
	if(null==jobBuild){
		continue
	}

	displayName = job.getFullName()
	jobUrl = job.getAbsoluteUrl()

	// get built on node info
	def builtOnServer = jobBuild.builtOn
	def builtOn = (builtOnServer instanceof Slave) ? builtOnServer.name : builtOnServer.getDisplayName()

	lastBuildTime = (jobBuild.getTime() !=null ? jobBuild.getTime().format("MM-dd-yyyy HH:mm:ss") : "")

	// handle failed job analysis
	if(jobBuild.result == Result.SUCCESS){
		logFile = jobBuild.getLogFile()

		category = ""
		comment = ""

		// Categories & comments
		
		def url = ""

		logFile.withReader { reader ->
			while ((logText = reader.readLine()) !=null ) {
				  sonarregex = logText =~ /ANALYSIS SUCCESSFUL\, you can browse (.*)?/
  
				  if(sonarregex.getCount() > 0){
				   sonarregex.each{
					  url = it[1]
					}
				  }
			}
		}

		output+= ("\n" +displayName + "," + jobBuild.getAbsoluteUrl()+ "console" + "," + builtOn + "," + lastBuildTime + ","+ url)

	}
}

def exit = 0

def parentPath = build.workspace.toString()
def outputFile = parentPath + "/Jenkins-SonarUrls.csv"

def fp = null

if(build.workspace.isRemote())
{
    channel = build.workspace.channel;
    fp = new FilePath(channel, outputFile)
} else {
    fp = new FilePath(new File(outputFile))
}

if(fp != null)
{
  	println "\n\n\n---------\nOutputFile - " + outputFile
    fp.write(output, null); //writing to file
} else{
  println "Error - Failed to update file data"
  exit = 1
}

println "\n\n\n"

println output

println ""

return exit