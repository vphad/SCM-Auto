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



allowPerms = ['hudson.model.Item.Cancel','hudson.model.Item.Read','hudson.model.Item.Build','hudson.model.Item.Discover']
jenkins = Jenkins.instance

try{
	// checkGlobalAuthorization()

	checkFolderAuthorizations()
}catch(Exception e){
	println "Error: Failed to verify access - " + e.getMessage()
	printStackTrace(e)
	return 1
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
	
	def name = job.getFullName()
	
	def isFolder = false
	
	if(job instanceof Folder){
		isFolder = true
	}
	
	def amp = null
	
	try{
		amp = job.getProperties().get(AuthorizationMatrixProperty.class)
	}catch(MissingPropertyException e){
		println "ERROR - AuthorizationMatrixProperty not found for job: " + job.getFullName()
		printStackTrace(e)
		
		failed = true		
		return
	}

	if(amp==null){
		return
	}

	def op = amp.grantedPermissions

	def jobFailed= false
	
	op.keySet().each{ perm ->

		for(Iterator<String> st = op.get(perm).iterator(); st.hasNext();){
				def user = st.next()
				
				// check application level permissions
				if(isFolder && (2 == name.split('/').length) && (!allowPerms.contains(perm.name)){
					output += "\n${job.getFullName()},${job.getAbsoluteUrl()},${job.getClass().getSimpleName()},${user},${perm.name}"
					jobFailed = true
				}else{
					// no permissions should be at job level
					output += "\n${job.getFullName()},${job.getAbsoluteUrl()},${job.getClass().getSimpleName()},${user},${perm.name}"
					jobFailed = true
				}
			}
			
		}

		failed = (jobFailed || failed)	
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
/* For now ignored for checks
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
					userFoundList << sid.toLowerCase()
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
							userFoundList << sid.toLowerCase()
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
*/
String printStackTrace(Exception e){
	StringWriter sw = new StringWriter();
	e.printStackTrace(new PrintWriter(sw));
	println sw.toString();

}