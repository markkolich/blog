<#include "common/header.ftl">

<!-- article column -->
<div class="col-md-9 col-lg-9">

    <h2 class="title">${title}</h2>
    <p class="hash"><a href="https://github.com/markkolich/blog/commit/${commit}">${commit}</a></p>
    <p class="date">${date}</p>

    <article>${content}</article>

</div>
<!-- /article column -->

<#include "common/right.ftl">

<#include "common/footer.ftl">
