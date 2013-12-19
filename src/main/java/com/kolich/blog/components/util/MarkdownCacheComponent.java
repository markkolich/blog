package com.kolich.blog.components.util;

import com.gitblit.models.PathModel;
import com.gitblit.utils.JGitUtils;
import com.google.common.collect.Maps;
import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.GitRepository;
import com.kolich.blog.entities.MarkdownDrivenContent;
import com.kolich.curacao.handlers.components.CuracaoComponent;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.servlet.ServletContext;
import java.io.File;
import java.nio.file.FileSystems;
import java.util.Map;

import static org.apache.commons.io.FilenameUtils.removeExtension;

public abstract class MarkdownCacheComponent<T extends MarkdownDrivenContent>
    implements CuracaoComponent {

    private static final String markdownRootDir__ =
        ApplicationConfig.getMarkdownRootDir();

    private final GitRepository git_;
    private final Map<String, T> cache_;

    public MarkdownCacheComponent(final GitRepository git) {
        git_ = git;
        cache_ = Maps.newLinkedHashMap();
    }

    @Override
    public final void initialize(final ServletContext context) throws Exception {
        buildCache(getBuilder());
    }

    @Override
    public final void destroy(final ServletContext context) throws Exception {
        // Nothing, intentional.
    }

    protected final T get(final String key) {
        return cache_.get(key);
    }

    private void buildCache(final MarkdownEntityBuilder<T> builder) throws Exception {
        final Git git = git_.getGit();
        final Repository repo = git_.getRepo();
        final String pathToContent = FileSystems.getDefault().getPath(
            markdownRootDir__, getContentDirectory()).toString();
        for(final RevCommit commit : git.log().call()) {
            for(final PathModel.PathChangeModel change :
                JGitUtils.getFilesInCommit(repo, commit)) {
                final String hash = change.objectId, name = change.name;
                final DiffEntry.ChangeType type = change.changeType;
                final Long timestamp = commit.getCommitTime() * 1000L;
                if(name.startsWith(pathToContent) &&
                    type.equals(DiffEntry.ChangeType.ADD)) {
                    final File markdown = new File(repo.getWorkTree(), name);
                    final String entityName = removeExtension(markdown.getName());
                    cache_.put(entityName, builder.build(entityName,
                            markdown, hash, timestamp, type));
                }
            }
        }
    }

    public abstract MarkdownEntityBuilder<T> getBuilder();

    public abstract String getContentDirectory();

    protected interface MarkdownEntityBuilder<T> {
        public T build(final String name,
                       final File markdown,
                       final String hash,
                       final Long timestamp,
                       final DiffEntry.ChangeType changeType);
    }

}
