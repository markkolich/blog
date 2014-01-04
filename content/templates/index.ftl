<#ftl attributes={"title":"Software Engineer"}>

<#include "common/header.ftl">

<#list entries as e>

    <div class="entry">

        <h2 class="title"><a href="${e.name}">${e.title}</a></h2>
        <p class="hash">${e.commit}</p>
        <p class="date">${e.dateFormatted}</p>
        <p>${e.content}</p>

        <div class="fader"></div>

    </div>

</#list>

<div class="row">
  <button type="button" class="btn btn-default btn-md btn-block more">Load More</button>
</div>

<#include "common/footer.ftl">
