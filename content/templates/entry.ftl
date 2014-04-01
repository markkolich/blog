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

    </div>
    <!-- /article column -->

    <#include "common/right.ftl">

</div>
<!-- /content row -->

<#include "common/footer.ftl">
<#include "common/html-footer.ftl">
