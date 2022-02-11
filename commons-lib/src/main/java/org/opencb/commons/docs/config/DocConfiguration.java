package org.opencb.commons.docs.config;

import org.opencb.commons.docs.doc.DocFactory;

import java.util.List;

public class DocConfiguration {


    private List<Class> docClasses;
    private String outputDir;
    private DocFactory.DocFactoryType type;
    private String githubServerURL;
    private String jsondir;
    private String gitbookServerURL;

    public DocConfiguration() {
    }

    public DocConfiguration(List<Class> docClasses, String outputDir, DocFactory.DocFactoryType type,
                            String githubServerURL, String jsondir, String gitbookServerURL) {
        this.docClasses = docClasses;
        this.outputDir = outputDir;
        this.type = type;
        this.githubServerURL = githubServerURL;
        this.jsondir = jsondir;
        this.gitbookServerURL = gitbookServerURL;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DocConfiguration{");
        sb.append("docClasses=").append(docClasses);
        sb.append(", outputDir='").append(outputDir).append('\'');
        sb.append(", type=").append(type);
        sb.append(", githubServerURL='").append(githubServerURL).append('\'');
        sb.append(", jsondir='").append(jsondir).append('\'');
        sb.append(", gitbookServerURL='").append(gitbookServerURL).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public List<Class> getDocClasses() {
        return docClasses;
    }

    public DocConfiguration setDocClasses(List<Class> docClasses) {
        this.docClasses = docClasses;
        return this;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public DocConfiguration setOutputDir(String outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    public DocFactory.DocFactoryType getType() {
        return type;
    }

    public DocConfiguration setType(DocFactory.DocFactoryType type) {
        this.type = type;
        return this;
    }

    public String getGithubServerURL() {
        return githubServerURL;
    }

    public DocConfiguration setGithubServerURL(String githubServerURL) {
        this.githubServerURL = githubServerURL;
        return this;
    }

    public String getJsondir() {
        return jsondir;
    }

    public DocConfiguration setJsondir(String jsondir) {
        this.jsondir = jsondir;
        return this;
    }

    public String getGitbookServerURL() {
        return gitbookServerURL;
    }

    public DocConfiguration setGitbookServerURL(String gitbookServerURL) {
        this.gitbookServerURL = gitbookServerURL;
        return this;
    }
}
