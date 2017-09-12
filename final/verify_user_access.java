/****************************************************************************************
 *
 * Script to user access on Jenkins
 *
 * Author : Vijayanand Vitthal Phad
 * Version : 1.0
 *
 *
 *
 ***************************************************************************************/

import hudson.*
import hudson.model.*
import hudson.scm.*
import hudson.tasks.*
import jenkins.*
import jenkins.model.*

import com.cloudbees.hudson.plugins.folder.*
import com.cloudbees.hudson.plugins.folder.properties.*

import hudson.security.*
import com.cloudbees.hudson.plugins.folder.properties.AuthorizationMatrixProperty


def resolver = build.buildVariableResolver
def input = resolver.resolve('Users')

// For Test - 
// def input = "abcd,xyz"

input = input?.trim()

if(null==input || ""==input){
	println "Empty input, exiting script"
	return 1
}


flag = false

usersList = input.split(",")*.trim() as Set<String>
usersList.remove("")
usersList.remove(null)

println "User input list - " + usersList

if(usersList.size()==0){
	println "ERROR - Empty user list for processing, exiting script"
	return 1
}

userFoundList = [] as Set<String>

jenkins = Jenkins.instance

try{
	checkGlobalAuthorization()

	checkFolderAuthorizations()
}catch(Exception e){
	println "Error: Failed to verify access - " + e.getMessage()
	printStackTrace(e)
	return 1
}


def usersNotFound = usersList.minus(userFoundList)

if(usersNotFound.size() > 0){
	println "Not found - " + usersNotFound
	return false
}

/****************************************************************************************
 * 
 * 
 * Starts Method definitions
 * 
 * 
 * 
 ***************************************************************************************/

boolean checkFolderAuthorizations(){
	def ats = jenkins.authorizationStrategy

	def success = true

	switch(ats){
		case ProjectMatrixAuthorizationStrategy:
			jenkins.getItems().each{
				if(it instanceof Folder){
					if(!processFolder(it)){
						success = false
					}
				}else{
					if(!processJob(it)){
						success = false
					}
				}
			}

			break

		default:
			println 'ERROR - ' + ats + ' Strategy defined; Ignoring verification on jobs/folders'
			break
	}

	return success
}

def processJob(def job){
	def amp = null
	try{
		amp = job.getProperties().get(AuthorizationMatrixProperty.class)
	}catch(MissingPropertyException e){
		println "ERROR - AuthorizationMatrixProperty not found for job: " + job.getFullName()
		printStackTrace(e)
		success = false
		return
	}

	if(amp==null){
		return
	}

	def op = amp.grantedPermissions

	op.keySet().each{ perm ->

		for(Iterator<String> st = op.get(perm).iterator(); st.hasNext();){
			def user = st.next()
			if(usersList*.toLowerCase().contains(user.toLowerCase())){
				println "User "+ user + " found in job " + job.getFullName() + " with Permission: " + perm.name
				userFoundList << user
				flag = true
				}
			}
		}
}


def processFolder(Item job){
	processJob(job)

	job.getItems().each{
		if(it instanceof Folder){
			processFolder(it)
		}else{
			processJob(it)
		}
	}
}

boolean checkGlobalAuthorization(){
	def ats = jenkins.authorizationStrategy
	def perms = Permission.getAll()

	if(ats==null){
		println "ERROR - No authorization strategy found"
		return false;
	}

	switch (ats){
		case GlobalMatrixAuthorizationStrategy:
			def sids = jenkins.authorizationStrategy.getAllSIDs().plus('anonymous')

			for (sid in sids){
				if(usersList*.toLowerCase().contains(sid.toLowerCase())){
					println "User " + sid + " found in Global Security"
					userFoundList << sid
					flag = true
				}
			}
		case ProjectMatrixAuthorizationStrategy:
			def sids = jenkins.authorizationStrategy.getAllSIDs().plus('anonymous')
			for (sid in sids){
				if(usersList*.toLowerCase().contains(sid.toLowerCase())){
					for(per in perms){
						// Check all the permissions explicitely given
						if (jenkins.authorizationStrategy.hasExplicitPermission(sid,per)){
							println sid + " has " + per + " permission"
							userFoundList << sid
							flag = true
						}
					}
				}
			}

			break

		default:
			println "ERROR - Authorization strategy not found : " + ats
			break
	}
}

String printStackTrace(Exception e){
	StringWriter sw = new StringWriter();
	e.printStackTrace(new PrintWriter(sw));
	println sw.toString();

}