package com.kolich.blog.components;

import com.gitblit.models.PathModel;
import com.gitblit.utils.JGitUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.entities.Entry;
import com.kolich.blog.mappers.MarkdownDrivenContentResponseMapper;
import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.handlers.components.CuracaoComponent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public final class GitRepository implements CuracaoComponent {

    private static final Logger logger__ = getLogger(GitRepository.class);

    private static final Boolean isDevMode__ =
        ApplicationConfig.isDevMode();
    private static final String markdownRootDir__ =
        ApplicationConfig.getMarkdownRootDir();
    private static final String blogRepoCloneUrl__ =
        ApplicationConfig.getBlogRepoCloneUrl();
    private static final String clonePath__ =
        ApplicationConfig.getClonePath();
    private static final Boolean shouldCloneOnStartup__ =
        ApplicationConfig.shouldCloneOnStartup();

    private static final String userDir__ = System.getProperty("user.dir");

    private static final String entryDirName__ = markdownRootDir__ +
        File.separator + Entry.ENTRIES_CONTENT_DIR_NAME;

    private Map<String,Entry> entryCache_;

    private Repository repo_;
    private Git git_;

    @Override
    public void initialize(final ServletContext context) throws Exception {
        final File repoDir = getRepoDir(context);
        logger__.info("Active repository path: " + repoDir.getCanonicalFile());
        // If were not in dev mode, and the clone path doesn't exist or we need
        // to force clone from scratch, do that now.
        final boolean clone = (!repoDir.exists() || shouldCloneOnStartup__);
        if(!isDevMode__ && clone) {
            logger__.info("Clone path does not exist, or we were asked " +
                "to re-clone. Cloning from: " + blogRepoCloneUrl__);
            // Recursively delete the existing clone of the repo, if it exists,
            // and clone the repository.
            FileUtils.deleteDirectory(repoDir);
            Git.cloneRepository()
                .setURI(blogRepoCloneUrl__)
                .setDirectory(repoDir)
                .setBranch("master")
                .call();
        }
        // Construct a new pointer to the repository on disk.
        repo_ = new FileRepositoryBuilder()
            .setWorkTree(repoDir)
            .readEnvironment()
            .build();
        git_ = new Git(repo_);
        if(!isDevMode__) {
            // Pull a fresh copy of the repo from the remote.  Yes, we may have
            // just cloned it fresh, but this is what we do, bandwidth is cheap.
            git_.pull().call();
        }
        logger__.info("Successfully initialized Git repository: " + repo_);
        entryCache_ = buildEntryCache(git_, repo_);
    }

    @Override
    public void destroy(final ServletContext context) throws Exception {
        repo_.close();
    }

    private static final File getRepoDir(final ServletContext context) {
        final File repoDir;
        if(isDevMode__) {
            // If in "development mode", the clone path is the local copy of
            // the repo on disk.
            repoDir = new File(userDir__);
        } else {
            // Otherwise, it could be somewhere else either in an absolute
            // location (/...) or somewhere under the Servlet container when
            // relative.
            repoDir = (clonePath__.startsWith(File.separator)) ?
                // If the specified clone path in the application configuration
                // is "absolute", then use that path raw.
                new File(clonePath__) :
                // Otherwise, the clone path is relative to the container Servlet
                // context of this application.
                new File(context.getRealPath(File.separator + clonePath__));
        }
        return repoDir;
    }

    private static final Map<String,Entry> buildEntryCache(final Git git,
        final Repository repo) throws Exception {
        final Map<String,Entry> cache = Maps.newLinkedHashMap();
        for(final RevCommit commit : git.log().call()) {
            for(final PathModel.PathChangeModel change : JGitUtils.getFilesInCommit(repo, commit)) {
                final String hash = change.objectId, name = change.name;
                final DiffEntry.ChangeType type = change.changeType;
                if(name.startsWith(entryDirName__) && type.equals(DiffEntry.ChangeType.ADD)) {
                    final File markdown = new File(repo.getWorkTree(), name);
                    cache.put(FilenameUtils.removeExtension(markdown.getName()),
                        new Entry(markdown, hash, type));
                }
            }
        }
        return cache;
    }

    public final Git getGit() {
        return git_;
    }

    public final Repository getRepo() {
        return repo_;
    }

    public final Entry getEntry(final String name) {
        return entryCache_.get(name);
    }

}
