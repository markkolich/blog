<#include "common/html-header.ftl">
<#include "common/header.ftl">

<!-- content row -->
<div class="row">

    <!-- article column -->
    <div class="col-md-9 col-lg-9">

        <h2 class="title">${title}</h2>
        <p class="hash"><a href="https://github.com/markkolich/blog/commit/${commit}">${commit}</a></p>
        <p class="date">${date}</p>

        <article id="${commit}">${content}</article>

        <#if tags?has_content>
            <p class="tags">
                <i class="fa fa-tags"></i>
                <#list tags as t>
                    <span class="label label-default"><a href="${context}tagged/${t.urlEncodedText}">${t.displayText}</a></span>
                </#list>
            </p>
        </#if>

    </div>
    <!-- /article column -->

    <#include "common/right.ftl">

</div>
<!-- /content row -->

<#include "common/footer.ftl">
<#include "common/html-footer.ftl">
