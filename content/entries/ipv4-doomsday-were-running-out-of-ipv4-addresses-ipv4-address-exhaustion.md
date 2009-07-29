Unless you've been living under a rock for the last few years, you've probably heard of [the great doomsday scenario surrounding IPv4](http://en.wikipedia.org/wiki/IPv4_address_exhaustion#Problem_statement).  Someone even wrote a cute iPhone app to count down to IPv4 doomsday: the day the world runs out of IPv4 addresses.  Fact is, we're running out of IPv4 addresses; those super convenient and useful ID's used to uniquely identify each attached device/computer/whatever on the Internet.  For grins, I decided to use [ARIN's public WHOIS database](http://ws.arin.net/whois) to determine who owns the major Class A (CIDR /8) IP address blocks.

he results are shocking; no wonder the world is running out of IPv4 addresses.  The world has a total of 4,294,967,296 possible IPv4 addresses (0.0.0.0 through 255.255.255.255 so 256^4), most of which are owned entirely by a select few [Class A holders](http://en.wikipedia.org/wiki/IPv4_subnetting_reference).  Each Class A IPv4 block consists of 16,777,216 total addresses.  As of 7/29/09, here is a *somewhat complete* list of Class A /8 IPv4 holders:

* 1.0.0.0/8: Reserved by IANA
* 2.0.0.0/8: Reserved by IANA
* 3.0.0.0/8: General Electric Company
* 4.0.0.0/8: Level 3 Communications
* 5.0.0.0/8: Reserved by IANA
* 6.0.0.0/8: U.S. Army Information Systems Engineering Command
* 7.0.0.0/8: U.S. Department of Defense
* 8.0.0.0/8: Level 3 Communications
* 9.0.0.0/8: IBM Corporation
* 10.0.0.0/8: Reserved by IANA
* 11.0.0.0/8: U.S. Department of Defense
* 12.0.0.0/8: AT&T
* 13.0.0.0/8: Xerox Corporation
* 14.0.0.0/8: Reserved by IANA
* 15.0.0.0/8: Hewlett-Packard Company
* 16.0.0.0/8: Hewlett-Packard Company
* 17.0.0.0/8: Apple Computer
* 18.0.0.0/8: Massachusetts Institute of Technology
* 19.0.0.0/8: Ford Motor Company
* 20.0.0.0/8: Computer Sciences Corporation
* 21.0.0.0/8: U.S. Department of Defense
* 22.0.0.0/8: U.S. Department of Defense
* 23.0.0.0/8: Reserved by IANA
* 24.0.0.0/8: Comcast Cable Communications
* 25.0.0.0/8: Royal Signals and Radar Establishment (UK)
* 26.0.0.0/8: U.S. Department of Defense
* 27.0.0.0/8: Reserved by IANA
* 28.0.0.0/8: U.S. Department of Defense
* 29.0.0.0/8: U.S. Department of Defense
* 30.0.0.0/8: U.S. Department of Defense
* 31.0.0.0/8: Reserved by IANA
* 32.0.0.0/8: AT&T
* 33.0.0.0/8: U.S. Department of Defense
* 34.0.0.0/8: Haliburton Company
* 35.0.0.0/8: Merit Network Inc.
* 36.0.0.0/8: Reserved by IANA
* 37.0.0.0/8: Reserved by IANA
* 38.0.0.0/8: PSINet, Inc.
* 39.0.0.0/8: Reserved by IANA
* 40.0.0.0/8: Eli Lilly and Company
* 41.0.0.0/8: Assigned to Africa, allocated by African Network Information Center
* 42.0.0.0/8: Reserved by IANA
* 43.0.0.0/8: Assigned to Japan; allocated by Asia Pacific Network Information Center
* 44.0.0.0/8: Amateur Radio Digital Communications
* 45.0.0.0/8: Interop Show Network
* 46.0.0.0/8: Reserved by IANA
* 47.0.0.0/8: Bell-Northern Research
* 48.0.0.0/8: The Prudential Insurance Company of America
* 49.0.0.0/8: Reserved by IANA
* 50.0.0.0/8: Reserved by IANA
* 51.0.0.0/8: UK Government Department for Work and Pensions
* 52.0.0.0/8: E.I. du Pont de Nemours and Co., Inc.
* 53.0.0.0/8: cap debis ccs (Germany)
* 54.0.0.0/8: Merck and Co., Inc.
* 55.0.0.0/8: U.S. Army Information Systems Engineering Command
* 56.0.0.0/8: United States Postal Service
* 57.0.0.0/8: Societe Internationale de Telecommunications Aeronautiques (France)
* 58.0.0.0/8: Assigned to Asia; allocated by Asia Pacific Network Information Center
* 59.0.0.0/8: Assigned to Asia; allocated by Asia Pacific Network Information Center
* 60.0.0.0/8: Assigned to Asia; allocated by Asia Pacific Network Information Center
* 61.0.0.0/8: Assigned to Asia; allocated by Asia Pacific Network Information Center
* 62.0.0.0/8: Assigned to Europe; allocated by European Registry

Yes, this is **not a complete list** &mdash; I just wanted to cover the major blocks.

Here are a few interesting takeaways:

1. Hewlett-Packard owns 33,554,432 Class A IPv4 addresses, or 1/128 of the IPv4 space &mdash; apparently more than the countries of India and China combined (unverified).
2. The U.S. Department of Defense owns 150,994,944 IPv4 addresses.  What the DoD is doing with almost 151 million IPv4 addresses is beyond me.  I don't think anyone, or any country, in their right mind needs 150 million IPv4 addresses.
3. Massachusetts Institute of Technology owns 16,777,216 IPv4 addresses.  No college needs almost 17 million IPv4 addresses for itself.  According to Wikipedia, by comparison, some organizations such as Stanford University (formerly owner of the 36.0.0.0 - 36.255.255.255 range), have returned their designated ranges due to IP address shortages in recent years.  Perhaps MIT should follow Stanford's lead?
4. The IANA has reserved approximately 251,658,240 IPv4 /8 addresses.  I know some blocks are reserved for special use, but perhaps the IANA could release some of those blocks back to the world?  We have a serious IPv4 shortage on our hands.  Why is the IANA holding onto more than 250 million IPv4 addresses?
5. I don't see why companies like Apple, Ford Motor Company, HP, IBM, Haliburton, etc. need their own blocks.  Using NAT for most internal IT operations should be sufficient; there's no need to hold onto the glory days of excessively hogging IPv4 blocks.

Bottom line, we're running out of IPv4 addresses.  However, I don't think the problem is directly due to the number of new countries or devices coming online.  Rather I think the core of the IPv4 exhaustion issue is directly related to IPv4 address hogs: colleges, businesses, or organizations that hold onto large blocks of Class A IP addresses simply because they can.  Instead of pushing some cumbersome IPv4 replacement (*ahem* [IPv6 this means you](http://en.wikipedia.org/wiki/IPv6)), why don't we work together to encourage the current IPv4 holders to reevaluate, and possibly release, unused blocks of addresses?