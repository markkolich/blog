package com.kolich.blog.mappers;

import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.GitRepository;
import com.kolich.blog.entities.MarkdownDrivenContent;
import com.kolich.blog.entities.Page;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;

import javax.annotation.Nonnull;
import java.nio.file.FileSystems;
import java.nio.file.Path;

@ControllerReturnTypeMapper(Page.class)
public final class PageResponseMapper
    extends MarkdownDrivenContentResponseMapper {

    private static final String markdownDir__ =
        ApplicationConfig.getMarkdownRootDir();

    @Injectable
    public PageResponseMapper(GitRepository git) {
        super(git);
    }

    @Override
    public final Path getPathToMarkdown(@Nonnull final MarkdownDrivenContent md) {
        return FileSystems.getDefault().getPath(markdownDir__, "pages",
            md.getName() + ".md");
    }

}
