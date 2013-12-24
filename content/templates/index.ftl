<#ftl attributes={"title":"software engineer"}>

<#include "common/header.ftl">

<#list entries as e>
  ${e_index + 1}. ${e.title}<#if e_has_next>,</#if>
</#list>

<#include "common/footer.ftl">
