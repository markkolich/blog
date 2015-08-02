<#ftl attributes={"title":"Tagged content"}>

<#include "common/html-header.ftl">
<#include "common/header.ftl">

<!-- content row -->
<div class="row">

    <!-- entry list column -->
    <div class="col-md-9 col-lg-9">

        <h3 class="tags"><i class="fa fa-tags"></i> Content tagged: ${tag}</h3>

        <#list entries as e>
            <div class="entry" id="${e.commit}">

                <h2 class="title"><a href="${context}${e.name}">${e.title}</a></h2>
                <p class="hash">${e.commit}</p>
                <p class="date">${e.dateFormatted}</p>
                <p>${e.content}</p>

                <div class="fader"></div>

            </div>
        </#list>

    </div>
    <!-- /entry list column -->

    <#include "common/right.ftl">

</div>
<!-- /content row -->

<#include "common/footer.ftl">
<#include "common/html-footer.ftl">
