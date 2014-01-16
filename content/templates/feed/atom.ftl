<?xml version="1.0" encoding="utf-8"?>
<feed xmlns="http://www.w3.org/2005/Atom">

    <title>${blogTitle}</title>
    <link rel="alternate" type="text/html" href="${fullUri}"/>
    <link rel="self" type="application/atom+xml" href="${fullUri}atom.xml"/>
    <id>http://mark.koli.ch</id>
    <updated>${lastUpdated}</updated>
    <subtitle>${blogSubTitle}</subtitle>

    <#list entries as e>

        <entry>

            <title>${e.title}</title>
            <id>${e.commit}</id>

            <published>${e.atomFeedDateFormatted}</published>
            <updated>${e.atomFeedDateFormatted}</updated>

            <author>
                <name>Mark S. Kolich</name>
                <uri>${fullUri}</uri>
            </author>

            <content type="html" xml:lang="en-US" xml:base="${fullUri}">
                <![CDATA[${e.content}]]>
            </content>

        </entry>

    </#list>

</feed>
