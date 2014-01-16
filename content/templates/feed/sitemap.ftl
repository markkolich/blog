<?xml version="1.0" encoding="utf-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">

    <url>
        <loc>${fullUri}</loc>
        <lastmod>${lastUpdated}</lastmod>
        <priority>1.0</priority>
    </url>

    <#list entries as e>

        <url>
            <loc>${fullUri}${e.name}</loc>
            <lastmod>${e.sitemapDateFormatted}</lastmod>
        </url>

    </#list>

</urlset>
