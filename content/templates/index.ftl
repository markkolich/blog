<#ftl attributes={"title":"Software Engineer"}>

<#include "common/html-header.ftl">
<#include "common/header.ftl">

<!-- content row -->
<div class="row">

    <!-- entry list column -->
    <div class="col-md-9 col-lg-9">

        <#list entries as e>
            <div class="entry" id="${e.commit}">

                <h2 class="title"><a href="${context}${e.name}">${e.title}</a></h2>
                <p class="hash">${e.commit}</p>
                <p class="date">${e.dateFormatted}</p>
                <div class="content">${e.content}</div>

                <div class="fader"></div>

            </div>
        </#list>

        <#if remaining &gt; 0>
            <div class="row">
                <button type="button" class="btn btn-default btn-md btn-block more">Load More</button>
            </div>
        </#if>

    </div>
    <!-- /entry list column -->

    <#include "common/right.ftl">

</div>
<!-- /content row -->

<#include "common/footer.ftl">
<#include "common/html-footer.ftl">
