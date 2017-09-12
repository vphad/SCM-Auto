def FOLDER_NAME = 'vphad_test/Folder1'
def JOB_REGEX = 'RepsAndWarranties_upgradeTest_copy'

import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*

jenkins = Jenkins.instance

def folder = jenkins.getItemByFullName(FOLDER_NAME)
if (folder == null) {
  println "ERROR: Folder '$FOLDER_NAME' not found"
  return
}

jenkins.items.grep { it.name =~ "${JOB_REGEX}" }.each { job ->
  println "Moving '$job.name' to '$folder.name'"
  Items.move(job, folder)
}

println "end"