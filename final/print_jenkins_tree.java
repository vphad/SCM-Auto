import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*

import hudson.scm.*
import hudson.tasks.*
import com.cloudbees.hudson.plugins.folder.*

jen = Jenkins.instance

jen.getItemByFullName('Sonar Upgrade').getItems().each{
	if(it instanceof Folder){
	println	printDirectoryTree(it)
	}else{
		//printFile("" , it)
	}
}

def printDirectoryTree(def folder) {
    if (!(folder instanceof Folder)) {
        throw new IllegalArgumentException("folder is not a Directory");
    }
    int indent = 0;
    StringBuilder sb = new StringBuilder();
    printDirectoryTree(folder, indent, sb);
    return sb.toString();
}

def printDirectoryTree(def folder, int indent,
        StringBuilder sb) {
    if (!(folder instanceof Folder)) {
        throw new IllegalArgumentException("folder is not a Directory");
    }
    sb.append(getIndentString(indent));
    sb.append("");
    sb.append(folder.getName());
    sb.append("");
    sb.append("\n");
	
    folder.getItems().each{
        if (it instanceof Folder) {
            printDirectoryTree(it, indent + 1, sb);
        } else {
            printFile(it, indent + 1, sb);
        }
    }

}

def printFile(def file, int indent, StringBuilder sb) {
    //sb.append(getIndentString(indent));
    //sb.append("   ");
    //sb.append(file.getName());
    //sb.append("\n");
}

def getIndentString(int indent) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < indent; i++) {
        sb.append(",");
    }
    return sb.toString();
}


println ""