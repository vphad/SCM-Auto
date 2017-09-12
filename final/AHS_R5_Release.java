import hudson.*
import hudson.model.*

import java.text.SimpleDateFormat

import jenkins.*
import jenkins.model.*


def jen = Jenkins.instance

def allJobs = jen.getAllItems(AbstractProject.class)

SimpleDateFormat sf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss")

from = Date.parse("MM-dd-yyyy HH:mm:ss", "07-01-2017 00:00:00")
to = Date.parse("MM-dd-yyyy HH:mm:ss", "08-01-2017 00:00:00")

long secondInMillis = 1000;
long minuteInMillis = secondInMillis * 60;
long hourInMillis = minuteInMillis * 60;
long dayInMillis = hourInMillis * 24;

long duration = minuteInMillis

def output = ""

for(Job job : allJobs) {
	
	if(!job.getFullName().trim().toLowerCase().startsWith("ahs_mdp")
		&& !job.getFullName().trim().toLowerCase().contains("/edw-da/")
	&& !job.getFullName().trim().toLowerCase().contains("data-intake-utility")
	&& !job.getFullName().trim().toLowerCase().contains("ecm-dm")
	&& !job.getFullName().trim().toLowerCase().contains("/edw/")
	&& !job.getFullName().trim().toLowerCase().contains("mdms")
	){
		continue
	}
	
	builds = job.getBuilds().byTimestamp(from.getTime(), to.getTime())

	for(def build : builds){

		def params  = build.getAction(hudson.model.ParametersAction.class)

		def paramout = ""

		def tag = ""
		def branch = ""
		def label = ""
		if(params != null) {

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
						//paramout += (param instanceof hudson.scm.listtagsparameter.ListSubversionTagsParameterValue) ? ( "," + param.getName()  + "," + param.getTag()) :  ( "," + param.getName()  + "," + param.getValue())
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
			builtOn = builtOnServer.getDisplayName()
		}

		def result  = build.getResult()

		def buildTime = build.getTime().format("MM-dd-yyyy HH:mm:ss")
		
				action = build.getAction(jenkins.metrics.impl.TimeInQueueAction.class)

		def totalDuration = ""
		def totalQDuration = ""
		def totalBuildDuration = ""
		def totalJobDuration = ""

		if (action != null) {
			totalDuration = String.format( "%02.02f", (action.getTotalDurationMillis() / duration))
			totalQDuration = String.format( "%02.02f", (action.getQueuingDurationMillis() / duration))
			totalBuildDuration = String.format( "%02.02f", (action.getBuildingDurationMillis() / duration))
			//totalJobDuration  = totalDuration + totalQDuration
		}
		
		def userCause = build.getCause(hudson.model.Cause$UserIdCause)
		def userName = userCause?.userId ?: 'Parent'

		def parentCause = build.getCause(com.cloudbees.plugins.flow.FlowCause)
		def parentCauseData = parentCause?.shortDescription?:""

		// if(tag.contains('sprint-17') || branch.contains('sprint-17') || label.contains('sprint-17')){
			output += ("\n${job.getFullName()},${build.getAbsoluteUrl()},${userName},${parentCauseData},${result},${builtOn},${buildTime},${totalQDuration},${totalBuildDuration},${totalDuration},${branch},${tag},${label},${paramout}")
		// }
	}

}


println "Job Name,Url,Built By,Parent Cause,Result,Built On,Build Time, Q Duration,Build Duration,Total Build Duration,Branch,Tag,Label,Params"

println output