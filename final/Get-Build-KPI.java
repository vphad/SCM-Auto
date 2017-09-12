/*
 * This job retrieves build KPI details for Jenkins applications
 * 
 * Author : Vijayanand Vitthal Phad
 * 
 */

import jenkins.*
import jenkins.model.* 
import hudson.* 
import hudson.model.* 
import hudson.maven.*
import hudson.tasks.*
import java.text.SimpleDateFormat
import java.util.concurrent.*

def resolver = build.buildVariableResolver
def foldersText =  resolver.resolve('Folders').trim().toLowerCase()
def ignoredFoldersText =  resolver.resolve('IgnoredFolders').trim().toLowerCase()
def startDateText =  resolver.resolve('StartDate').trim()
def endDateText =  resolver.resolve('EndDate').trim()

if(null==startDateText || ""==startDateText.trim() || null==endDateText || ""==endDateText.trim()){
	println "\n\nError - Start and End date can't be empty"
	return 1
}

if(foldersText==null || foldersText==""){
	foldersText= "all"
}

ignored = ignoredFoldersText.split(',')

def jen = Jenkins.instance
def allJobs = jen.getAllItems(AbstractProject.class)

from = Date.parse("MM-dd-yyyy HH:mm:ss", startDateText)
to = Date.parse("MM-dd-yyyy HH:mm:ss", endDateText)

println "Start Date - " + from
println "End Date - " + to

if(from.after(to)){
	println "\n\nError: End date can't be before start date"
	return 1
}

appsL = Arrays.asList(foldersText.split(","))

appsL.each{
	it ->
	it = it.trim().toLowerCase()
}

long secondInMillis = 1000;
long minuteInMillis = secondInMillis * 60;
long hourInMillis = minuteInMillis * 60;
long dayInMillis = hourInMillis * 24;

long duration = minuteInMillis

println "List of applications to include - " + appsL

def output = "Job Name,Url,Built By,Parent Cause,Result,Built On,Build Time, Q Duration,Build Duration,Total Build Duration,Branch,Tag,Label,Params"

for(Job job : allJobs) {	
	/*
		Applications in scope,
		AHS
		EDW-DA
		Data-Intake-Utility
		ECM-DM
		EDW
		MDMS
	*/
	
	if(isIgnored(job.getFullName())){
		continue
	}

	
	def found = false
	
	if(!appsL.contains("all")){
		for(def app : appsL){
			if(job.getFullName().trim().toLowerCase().contains(app)){
				found = true
				break
			}
		}
		
		if(!found){
			continue
		}
	}
	
	println "Getting build details for - " + job.getFullName()
	builds = job.getBuilds().byTimestamp(from.getTime(), to.getTime())
	println "Total builds found - " + builds.size()

	for(def build : builds){

		def params  = build.getAction(hudson.model.ParametersAction.class)

		def paramout = ""

		def tag = ""
		def branch = ""
		def label = ""
		if(params != null) {

			// println("--- Parameters for " + item.name + " ---")
			for(param in params.getParameters()) {
				try {

					if(param.getName().trim().toLowerCase().contains('tag') || param.getName().toLowerCase().trim()=="svnbranch"){
						tag = (param instanceof hudson.scm.listtagsparameter.ListSubversionTagsParameterValue) ? param.getTag() : param.getValue()
					}
					else if(param.getName().toLowerCase().trim()=="branch"){
						branch = (param instanceof hudson.scm.listtagsparameter.ListSubversionTagsParameterValue) ? param.getTag() : param.getValue()
					}
					else if(param.getName().toLowerCase().trim()=="label"){
						label = (param instanceof hudson.scm.listtagsparameter.ListSubversionTagsParameterValue) ? param.getTag() : param.getValue()
					}
					else{
						// TODO - not required for KPI
						// paramout += (param instanceof hudson.scm.listtagsparameter.ListSubversionTagsParameterValue) ? ( "," + param.getName()  + "," + param.getTag()) :  ( "," + param.getName()  + "," + param.getValue())
					}

				}
				catch(Exception e) {
					// println(param.name)
				}
			}
		}

		def builtOnServer = build.builtOn
		def builtOn = ""

		if(builtOnServer instanceof Slave){
			builtOn = builtOnServer.name
		}else{
			builtOn = builtOnServer?.getDisplayName()
		}

		def result  = build.getResult()

		def buildTime = build.getTime().format("MM-dd-yyyy HH:mm:ss")
		
		def userCause = build.getCause(hudson.model.Cause$UserIdCause)
		def userName = userCause?.userId ?: 'Parent'

		def parentCause = build.getCause(com.cloudbees.plugins.flow.FlowCause)
		def parentCauseData = parentCause?.shortDescription?:""
		
		
		def totalDuration = ""
		def totalQDuration = ""
		def totalBuildDuration = ""
		def totalJobDuration = ""

		def action = build.getAction(jenkins.metrics.impl.TimeInQueueAction.class)
		if (action != null) {
			totalDuration = String.format( "%02.02f", (action.getTotalDurationMillis() / duration))
			totalQDuration = String.format( "%02.02f", (action.getQueuingDurationMillis() / duration))
			totalBuildDuration = String.format( "%02.02f", (action.getBuildingDurationMillis() / duration))
			//totalJobDuration  = totalDuration + totalQDuration
		}
		
		
		output += ("\n${job.getFullName()},${build.getAbsoluteUrl()},${userName},${parentCauseData},${result},${builtOn},${buildTime},${totalQDuration},${totalBuildDuration},${totalDuration},${branch},${tag},${label},${paramout}")
	}

}


println "-" * 80

println output
println "\n\nEnd" + ("-"*80)


def parentPath = build.workspace.toString()

def dateTime = (new Date()).format("MMddyy-HHmmss")
def outputFile = parentPath + "/Jenkins-Build-KPI_${dateTime}.csv"


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
  return 1
}


def isIgnored(def name){
	for(def app: ignored){
		if(name.toLowerCase().startsWith(app.toLowerCase())){
			println 'Ignoring - ' + name
			return true
		}
	}
	
	return false	
}

