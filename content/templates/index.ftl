<#ftl attributes={"title":"software engineer"}>

<#include "common/header.ftl">

<#list entries as e>
  ${e_index + 1}. <a href="${e.name}">${e.title}</a><#if e_has_next><br /></#if>
</#list>

<#include "common/footer.ftl">
