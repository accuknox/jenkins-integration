package io.jenkins.plugins.AccuKnoxCLI;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class KnoxAutoPol extends Builder implements SimpleBuildStep {

    private final String gitBranchName ;
    private final String gitBaseBranchName ;
    private final String gitUserName;
    private final String gitToken;
    private final String gitRepoUrl;
    private boolean useAutoApply;
    private boolean pushToGit;
    private static String OS = System.getProperty("os.name").toLowerCase();
    private static String query;

    @DataBoundConstructor
    public KnoxAutoPol(String gitBranchName, String gitToken, String gitRepoUrl, String gitUserName, String gitBaseBranchName) {
        this.gitBranchName = gitBranchName;
        this.gitUserName = gitUserName;
        this.gitToken = gitToken;
        this.gitRepoUrl = gitRepoUrl;
        this.gitBaseBranchName = gitBaseBranchName;
    }

    public String getGitBranchName() {
        return gitBranchName;
    }
    public String getGitBaseBranchName() {
        return gitBaseBranchName;
    }
    public String getGitToken() {
        return gitToken;
    }
    public String getGitRepoUrl() {
        return gitRepoUrl;
    }
    public String getGitUserName() {
        return gitUserName;
    }
    public boolean isAutoApply() {
        return useAutoApply;
    }
    public boolean isPushToGit() {
        return pushToGit;
    }

    @DataBoundSetter
    public void setUseAutoApply(boolean useAutoApply) {
        this.useAutoApply = useAutoApply;
    }
    @DataBoundSetter
    public void setPushToGit( boolean pushToGit) {
        this.pushToGit = pushToGit;
    }

    public static void downloader(String url,String file_name) {

        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(file_name)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
        // handle exception
        }      
  }

  public static String execCmd(String cmd) {
    String result = null;
    try (InputStream inputStream = Runtime.getRuntime().exec(cmd).getInputStream();
            Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
        result = s.hasNext() ? s.next() : null;
    } catch (IOException e) {
        e.printStackTrace();
    }
    return result;
}

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
       
        if (pushToGit) {
        if (OS.contains("win")) {
            downloader("https://github.com/vishnusomank/policy-cli/raw/main/autopol.exe","knox_autopol.exe");
            System.out.println("Working Directory = " + System.getProperty("user.dir"));
            query = System.getProperty("user.dir") + "\\knox_autopol.exe --git_base_branch="+gitBaseBranchName+" --auto-apply="+useAutoApply+" --git_branch_name="+gitBranchName+"  --git_token="+gitToken+" --git_repo_url="+gitRepoUrl+" --git_username="+gitUserName;
            listener.getLogger().println(execCmd(query));

            
        } else {
            downloader("https://github.com/vishnusomank/policy-cli/raw/main/autopol","knox_autopol"); 
            System.out.println("Working Directory = " + System.getProperty("user.dir")); 
            File file = new File(System.getProperty("user.dir")+"/knox_autopol");
            file.setExecutable(true, false);
		    file.setReadable(true, false);
		    file.setWritable(true, false);
            query = System.getProperty("user.dir") + "/knox_autopol --git_base_branch="+gitBaseBranchName+" --auto-apply="+useAutoApply+" --git_branch_name="+gitBranchName+"  --git_token="+gitToken+" --git_repo_url="+gitRepoUrl+" --git_username="+gitUserName;
            listener.getLogger().println(execCmd(query));
       
        }
        }
    }
    
    @Symbol("KnoxAutoPol")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public FormValidation doCheckGitUserName(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please Enter GitHub Username"); 
            return FormValidation.ok(); 
        }
          
        public FormValidation doCheckGitRepoUrl(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please Enter GitHub Repository URL"); 
            return FormValidation.ok(); 
        }
        public FormValidation doCheckGitToken(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please Enter GitHub Token for authentication"); 
            return FormValidation.ok(); 
        }
        public FormValidation doCheckGitBranchName(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please Enter GitHub Branch to push to"); 
            return FormValidation.ok(); 
        }
        public FormValidation doCheckGitBaseBranchName(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please Enter GitHub Base Branch to create PR"); 
            return FormValidation.ok(); 
        }
        

        @Override
        public String getDisplayName() {
            return "AccuKnox CLI";
        }

    }

}
