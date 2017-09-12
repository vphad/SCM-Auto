import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*

import hudson.scm.*
import hudson.tasks.*
import com.cloudbees.hudson.plugins.folder.*

jen = Jenkins.instance

def resolver = build.buildVariableResolver
def folderPath =  resolver.resolve('Folder')?.trim()

def items = (null==folderPath || "" == folderPath ) ?  jen.getItems() : jen.getItemByFullName(folderPath)?.getItems()

output = "Job Name,Url,Schedulers"

for(def item : items){
	if(item instanceof Folder){
		processFolder(item)
	}else{
		processJob(item)
	}
}

void processJob(Item job){
	  if(job.hasProperty('triggers') && null!=job.triggers && 0 < job.triggers.size()){
       
		job.triggers.each{
			output += ("\n" + job.getFullName() + "," + job.getAbsoluteUrl()  + "," +  it.value.spec)
		}
	}
}

void processFolder(Item folder){
	folder.getItems().each{
		if(it instanceof Folder){
			processFolder(it)
		}else{
			processJob(it)
		}
	}
}


println output

def parentPath = build.workspace.toString()

def outputFile = parentPath + "/Jenkins-Job-Schedules.csv"



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

